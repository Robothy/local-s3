package io.github.robothy.s3.vectors.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.robothy.s3.jupiter.LocalS3;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3vectors.S3VectorsClient;
import software.amazon.awssdk.services.s3vectors.model.CreateVectorBucketResponse;
import software.amazon.awssdk.services.s3vectors.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.s3vectors.model.NotFoundException;
import software.amazon.awssdk.services.s3vectors.model.TagResourceResponse;
import software.amazon.awssdk.services.s3vectors.model.UntagResourceResponse;

public class VectorsTaggingIntegrationTest {

  @LocalS3
  @Test
  void testTagListUntagRoundTrip(S3VectorsClient vectorsClient) {
    String vectorBucketName = "tag-bucket-" + UUID.randomUUID();
    CreateVectorBucketResponse createResponse =
        vectorsClient.createVectorBucket(b -> b.vectorBucketName(vectorBucketName));
    String arn = createResponse.vectorBucketArn();

    TagResourceResponse tagResponse = vectorsClient.tagResource(b -> b
        .resourceArn(arn)
        .tags(Map.of("env", "prod", "team", "core")));
    assertEquals(200, tagResponse.sdkHttpResponse().statusCode());

    ListTagsForResourceResponse listResponse = vectorsClient.listTagsForResource(b -> b.resourceArn(arn));
    assertEquals(Map.of("env", "prod", "team", "core"), listResponse.tags());

    // TagResource merges: provided keys are set, other existing keys are kept.
    vectorsClient.tagResource(b -> b.resourceArn(arn).tags(Map.of("env", "dev")));
    listResponse = vectorsClient.listTagsForResource(b -> b.resourceArn(arn));
    assertEquals(Map.of("env", "dev", "team", "core"), listResponse.tags());

    UntagResourceResponse untagResponse =
        vectorsClient.untagResource(b -> b.resourceArn(arn).tagKeys("env", "missing"));
    assertEquals(200, untagResponse.sdkHttpResponse().statusCode());

    listResponse = vectorsClient.listTagsForResource(b -> b.resourceArn(arn));
    assertEquals(Map.of("team", "core"), listResponse.tags());

    // Untagging the last key results in an empty tag set.
    vectorsClient.untagResource(b -> b.resourceArn(arn).tagKeys("team"));
    listResponse = vectorsClient.listTagsForResource(b -> b.resourceArn(arn));
    assertTrue(listResponse.tags().isEmpty());
  }

  @LocalS3
  @Test
  void testListTagsOnFreshBucketReturnsEmptyTags(S3VectorsClient vectorsClient) {
    String vectorBucketName = "fresh-tag-bucket-" + UUID.randomUUID();
    CreateVectorBucketResponse createResponse =
        vectorsClient.createVectorBucket(b -> b.vectorBucketName(vectorBucketName));
    String arn = createResponse.vectorBucketArn();

    ListTagsForResourceResponse listResponse = vectorsClient.listTagsForResource(b -> b.resourceArn(arn));
    assertTrue(listResponse.tags().isEmpty());
  }

  @LocalS3
  @Test
  void testTaggingOperationsOnMissingBucketThrowNotFound(S3VectorsClient vectorsClient) {
    String missingArn = "arn:aws:s3vectors:::vector-bucket/no-such-bucket-" + UUID.randomUUID();

    assertThrows(NotFoundException.class,
        () -> vectorsClient.tagResource(b -> b.resourceArn(missingArn).tags(Map.of("k", "v"))));
    assertThrows(NotFoundException.class,
        () -> vectorsClient.untagResource(b -> b.resourceArn(missingArn).tagKeys("k")));
    assertThrows(NotFoundException.class,
        () -> vectorsClient.listTagsForResource(b -> b.resourceArn(missingArn)));
  }

}
