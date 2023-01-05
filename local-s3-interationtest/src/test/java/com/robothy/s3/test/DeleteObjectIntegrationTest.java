package com.robothy.s3.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest;
import com.amazonaws.services.s3.model.VersionListing;
import com.robothy.s3.jupiter.LocalS3;
import org.junit.jupiter.api.Test;

public class DeleteObjectIntegrationTest {


  @LocalS3
  @Test
  void testDeleteObject(AmazonS3 s3) {
    String versioningSuspendedBucket = "my-bucket";
    s3.createBucket(versioningSuspendedBucket);
    s3.setBucketVersioningConfiguration(new SetBucketVersioningConfigurationRequest(versioningSuspendedBucket,
        new BucketVersioningConfiguration(BucketVersioningConfiguration.SUSPENDED)));
    assertDoesNotThrow(() -> s3.deleteObject(versioningSuspendedBucket, "a.txt"));
    VersionListing versionListing = s3.listVersions(versioningSuspendedBucket, "a.txt");
    assertEquals(1, versionListing.getVersionSummaries().size());
    assertTrue(versionListing.getVersionSummaries().get(0).isDeleteMarker());

    assertDoesNotThrow(() -> s3.deleteObject(versioningSuspendedBucket, "a.txt"));
    VersionListing versionListing1 = s3.listVersions(versioningSuspendedBucket, "a.txt");
    assertEquals(1, versionListing1.getVersionSummaries().size());
    assertTrue(versionListing1.getVersionSummaries().get(0).isDeleteMarker());

    assertDoesNotThrow(() -> s3.deleteVersion(versioningSuspendedBucket, "a.txt", "null"));
    VersionListing versionListing2 = s3.listVersions(versioningSuspendedBucket, "a.txt");
    assertEquals(0, versionListing2.getVersionSummaries().size());


    String unVersionedBucket = "un-versioned-bucket";
    s3.createBucket(unVersionedBucket);
    assertDoesNotThrow(() -> s3.deleteObject(unVersionedBucket, "not-exists.txt"));
    s3.putObject(unVersionedBucket, "a.txt", "Hello");
    VersionListing versionListing3 = s3.listVersions(unVersionedBucket, "a.txt");
    assertEquals(1, versionListing3.getVersionSummaries().size());
    s3.deleteObject(unVersionedBucket, "a.txt");
    VersionListing versionListing4 = s3.listVersions(unVersionedBucket, "a.txt");
    assertEquals(0, versionListing4.getVersionSummaries().size());
    assertDoesNotThrow(() -> s3.deleteBucket(unVersionedBucket));
  }



}
