package com.robothy.s3.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.robothy.s3.core.exception.BucketNotExistException;
import com.robothy.s3.core.exception.BucketTaggingNotExistException;
import com.robothy.s3.core.model.Bucket;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class BucketTaggingServiceTest extends LocalS3ServiceTestBase {

  @MethodSource("bucketServices")
  @ParameterizedTest
  void putTagging(BucketService bucketService) {
    // Returns null if the bucket not exist.
    assertThrows(BucketNotExistException.class, () -> bucketService.getTagging("not-exists-bucket"));

    assertThrows(BucketNotExistException.class, () -> bucketService.putTagging("not-exists-bucket", List.of(Map.of("A", "a"))));
    Bucket bucket = bucketService.createBucket("my-bucket");
    List<Map<String, String>> tagging = List.of(Map.of("A", "a", "B", "b"));
    bucketService.putTagging(bucket.getName(), tagging);
    Collection<Map<String, String>> taggingOpt = bucketService.getTagging(bucket.getName());
    assertEquals(tagging, taggingOpt);

    assertThrows(BucketNotExistException.class, () -> bucketService.deleteTagging("not-exist-bucket"));
    bucketService.deleteTagging(bucket.getName());
    assertThrows(BucketTaggingNotExistException.class, () -> bucketService.getTagging(bucket.getName()));
  }

}