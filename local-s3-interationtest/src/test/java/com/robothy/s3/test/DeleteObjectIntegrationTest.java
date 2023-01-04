package com.robothy.s3.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.VersionListing;
import com.robothy.s3.jupiter.LocalS3;
import org.junit.jupiter.api.Test;

public class DeleteObjectIntegrationTest {


  @LocalS3
  @Test
  void testDeleteObject(AmazonS3 s3) {
    String bucketName = "my-bucket";
    s3.createBucket(bucketName);
    assertDoesNotThrow(() -> s3.deleteObject(bucketName, "a.txt"));
    VersionListing versionListing = s3.listVersions(bucketName, "a.txt");
    assertEquals(1, versionListing.getVersionSummaries().size());
    assertTrue(versionListing.getVersionSummaries().get(0).isDeleteMarker());

    assertDoesNotThrow(() -> s3.deleteObject(bucketName, "a.txt"));
    VersionListing versionListing1 = s3.listVersions(bucketName, "a.txt");
    assertEquals(1, versionListing1.getVersionSummaries().size());
    assertTrue(versionListing1.getVersionSummaries().get(0).isDeleteMarker());

    assertDoesNotThrow(() -> s3.deleteVersion(bucketName, "a.txt", "null"));
    VersionListing versionListing2 = s3.listVersions(bucketName, "a.txt");
    assertEquals(0, versionListing2.getVersionSummaries().size());
  }



}
