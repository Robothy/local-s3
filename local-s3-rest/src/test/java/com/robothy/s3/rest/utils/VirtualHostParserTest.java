package com.robothy.s3.rest.utils;

import com.robothy.s3.rest.model.request.BucketRegion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VirtualHostParserTest {

  @Test
  void getBucketRegionFromHostUnderAwsDomain() {
    assertFalse(VirtualHostParser.getBucketRegionFromHost(null).isPresent());
    assertFalse(VirtualHostParser.getBucketRegionFromHost("").isPresent());

    assertEquals(new BucketRegion("region1", null),
        VirtualHostParser.getBucketRegionFromHost("s3.region1.amazonaws.com").get());
    assertFalse(VirtualHostParser.getBucketRegionFromHost("s3.region1.unsupported.domain").isPresent());

    assertFalse(VirtualHostParser.getBucketRegionFromHost(".s3.region1.amazonaws.com").isPresent());
    assertEquals(new BucketRegion("eu-west-1", null), VirtualHostParser.getBucketRegionFromHost("s3.eu-west-1.amazonaws.com").get());

    assertEquals(new BucketRegion("region1", "bucket1"),
        VirtualHostParser.getBucketRegionFromHost("bucket1.s3.region1.amazonaws.com").get());
    assertEquals(new BucketRegion("region1", "bucket1"),
        VirtualHostParser.getBucketRegionFromHost("bucket1.s3.region1.amazonaws.com").get());

    assertEquals(new BucketRegion("region2", "www.example.com"),
        VirtualHostParser.getBucketRegionFromHost("www.example.com.s3.region2.amazonaws.com").get());
    assertEquals(new BucketRegion("ap-east-1", "www.example.com"),
        VirtualHostParser.getBucketRegionFromHost("www.example.com.s3.ap-east-1.amazonaws.com").get());

    // if using the legacy endpoint, then set the default region "local".
    assertEquals(new BucketRegion("local", "bucket1"),
        VirtualHostParser.getBucketRegionFromHost("bucket1.s3.amazonaws.com").get());
    assertEquals(new BucketRegion("local", "www.example.com"),
        VirtualHostParser.getBucketRegionFromHost("www.example.com.s3.amazonaws.com").get());
    assertFalse(VirtualHostParser.getBucketRegionFromHost(".s3.amazonaws.com").isPresent());
  }


  @Test
  public void getBucketRegionFromLocalDomain() {
    assertEquals(new BucketRegion("local", "bucket1"),
        VirtualHostParser.getBucketRegionFromHost("bucket1.localhost").get());
    assertEquals(new BucketRegion("local", "www.example.com"),
        VirtualHostParser.getBucketRegionFromHost("www.example.com.localhost").get());
    assertEquals(new BucketRegion("local", "bucket.s3"),
        VirtualHostParser.getBucketRegionFromHost("bucket.s3.localhost").get());
    assertEquals(new BucketRegion("local", "bucket.s3."),
        VirtualHostParser.getBucketRegionFromHost("bucket.s3..localhost").get());

    assertFalse(VirtualHostParser.getBucketRegionFromHost("localhost").isPresent());
    assertFalse(VirtualHostParser.getBucketRegionFromHost(".localhost").isPresent());
    assertFalse(VirtualHostParser.getBucketRegionFromHost("127.0.0.1").isPresent());
  }

}