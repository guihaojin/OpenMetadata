package org.openmetadata.service.dataInsight;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.openmetadata.schema.dataInsight.DataInsightChartResult;
import org.openmetadata.schema.datatInsight.type.PercentageOfEntitiesWithOwnerByType;

public class EntitiesOwnerAggregator extends DataInsightAggregatorInterface<PercentageOfEntitiesWithOwnerByType> {

  public EntitiesOwnerAggregator(
      Aggregations aggregations, DataInsightChartResult.DataInsightChartType dataInsightChartType) {
    super(aggregations, dataInsightChartType);
  }

  @Override
  public DataInsightChartResult process() throws ParseException {
    List data = this.aggregate();
    DataInsightChartResult dataInsightChartResult = new DataInsightChartResult();
    return dataInsightChartResult.withData(data).withChartType(this.dataInsightChartType);
  }

  @Override
  public List<PercentageOfEntitiesWithOwnerByType> aggregate() throws ParseException {
    Histogram timestampBuckets = this.aggregations.get(TIMESTAMP);
    List<PercentageOfEntitiesWithOwnerByType> data = new ArrayList();
    for (Histogram.Bucket timestampBucket : timestampBuckets.getBuckets()) {
      String dateTimeString = timestampBucket.getKeyAsString();
      Long timestamp = this.convertDatTimeStringToTimestamp(dateTimeString);
      MultiBucketsAggregation entityTypeBuckets = timestampBucket.getAggregations().get(ENTITY_TYPE);
      for (MultiBucketsAggregation.Bucket entityTypeBucket : entityTypeBuckets.getBuckets()) {
        String entityType = entityTypeBucket.getKeyAsString();
        Sum sumHasOwner = entityTypeBucket.getAggregations().get(HAS_OWNER_FRACTION);
        Sum sumEntityCount = entityTypeBucket.getAggregations().get(ENTITY_COUNT);
        data.add(
            new PercentageOfEntitiesWithOwnerByType()
                .withTimestamp(timestamp)
                .withEntityType(entityType)
                .withEntityCount(sumEntityCount.getValue())
                .withHasOwner(sumHasOwner.getValue())
                .withHasOwnerFraction(sumHasOwner.getValue() / sumEntityCount.getValue()));
      }
    }

    return data;
  }
}
