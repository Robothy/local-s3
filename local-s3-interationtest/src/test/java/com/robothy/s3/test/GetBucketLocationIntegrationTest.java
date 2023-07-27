package com.robothy.s3.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.Region;
import com.robothy.s3.jupiter.LocalS3;
import org.junit.jupiter.api.Test;

public class GetBucketLocationIntegrationTest {

  @Test
  @LocalS3
  void getBucketLocation(AmazonS3 s3) {
    assertThrows(SdkClientException.class, () -> s3.getBucketLocation("non-exist-bucket"));
    Bucket bucket = s3.createBucket(new CreateBucketRequest("bucket", Region.AP_HongKong));
    assertNotNull(bucket);
    assertEquals(Region.AP_HongKong.toString(), s3.getBucketLocation("bucket"));
  }

}
