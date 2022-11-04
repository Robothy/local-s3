package com.robothy.s3.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.robothy.s3.core.model.Bucket;
import com.robothy.s3.datatypes.AccessControlPolicy;
import com.robothy.s3.datatypes.Grant;
import com.robothy.s3.datatypes.Owner;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class BucketAclServiceTest extends LocalS3ServiceTestBase {

  @ParameterizedTest
  @MethodSource("bucketServices")
  void testAcl(BucketService bucketService) {
    Bucket bucket = bucketService.createBucket("my-bucket");
    AccessControlPolicy defaultAcl = bucketService.getBucketAcl(bucket.getName());
    assertNotNull(defaultAcl);
    assertNotNull(defaultAcl.getOwner());
    assertNotNull(defaultAcl.getGrants());

    AccessControlPolicy acl = AccessControlPolicy.builder()
        .owner(new Owner("Bob", "002"))
        .grants(List.of(new Grant()))
        .build();
    bucketService.putBucketAcl(bucket.getName(), acl);
    assertEquals(acl, bucketService.getBucketAcl(bucket.getName()));
  }

}