package com.robothy.s3.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.services.s3.AmazonS3;
import com.robothy.s3.jupiter.LocalS3;
import org.junit.jupiter.api.Test;

public class HeadObjectIntegrationTest {

  @Test
  @LocalS3
  void testHeadObject(AmazonS3 s3) {
    String bucketName = "my-bucket";
    s3.createBucket(bucketName);
    s3.putObject(bucketName, "a.txt",  "Hello");
    assertTrue(s3.doesObjectExist(bucketName, "a.txt"));

    s3.deleteObject(bucketName, "a.txt");
    assertFalse(s3.doesObjectExist(bucketName, "a.txt"));
  }

}
