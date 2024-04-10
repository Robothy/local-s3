package com.robothy.s3.rest.utils;

import com.robothy.s3.rest.model.request.BucketRegion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VirtualHostParserTest {

  @Test
  void getBucketRegionFromHost() {
    assertFalse(VirtualHostParser.getBucketRegionFromHost(null).isPresent());
    assertFalse(VirtualHostParser.getBucketRegionFromHost("").isPresent());

    assertEquals(new BucketRegion("region1", null),
        VirtualHostParser.getBucketRegionFromHost("s3.region1.local").get());
    assertFalse(VirtualHostParser.getBucketRegionFromHost("s3.region1.unsupported.domain").isPresent());

    assertFalse(VirtualHostParser.getBucketRegionFromHost(".s3.region1.local").isPresent());
    assertEquals(new BucketRegion("eu-west-1", null), VirtualHostParser.getBucketRegionFromHost("s3.eu-west-1.amazonaws.com").get());

    assertEquals(new BucketRegion("region1", "bucket1"),
        VirtualHostParser.getBucketRegionFromHost("bucket1.s3.region1.local").get());
    assertEquals(new BucketRegion("region1", "bucket1"),
        VirtualHostParser.getBucketRegionFromHost("bucket1.s3.region1.amazonaws.com").get());

    assertEquals(new BucketRegion("region2", "www.example.com"),
        VirtualHostParser.getBucketRegionFromHost("www.example.com.s3.region2.local").get());
    assertEquals(new BucketRegion("ap-east-1", "www.example.com"),
        VirtualHostParser.getBucketRegionFromHost("www.example.com.s3.ap-east-1.amazonaws.com").get());

    // if using the legacy endpoint, then set the default region "local".
    assertEquals(new BucketRegion("local", "bucket1"),
        VirtualHostParser.getBucketRegionFromHost("bucket1.s3.localhost").get());
    assertEquals(new BucketRegion("local", "www.example.com"),
        VirtualHostParser.getBucketRegionFromHost("www.example.com.s3.localhost").get());
    assertFalse(VirtualHostParser.getBucketRegionFromHost(".s3.localhost").isPresent());
  }

}