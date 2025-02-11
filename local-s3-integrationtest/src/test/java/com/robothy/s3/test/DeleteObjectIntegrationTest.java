package com.robothy.s3.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import com.robothy.s3.jupiter.LocalS3;
import org.junit.jupiter.api.Test;

public class DeleteObjectIntegrationTest {

  @LocalS3
  @Test
  void testDeleteObject(S3Client s3) {
    String versioningSuspendedBucket = "my-bucket";
    s3.createBucket(CreateBucketRequest.builder().bucket(versioningSuspendedBucket).build());
    s3.putBucketVersioning(PutBucketVersioningRequest.builder()
        .bucket(versioningSuspendedBucket)
        .versioningConfiguration(VersioningConfiguration.builder()
            .status(BucketVersioningStatus.SUSPENDED)
            .build())
        .build());

    assertDoesNotThrow(() -> s3.deleteObject(DeleteObjectRequest.builder()
        .bucket(versioningSuspendedBucket)
        .key("a.txt")
        .build()));

    ListObjectVersionsResponse versionListing = s3.listObjectVersions(ListObjectVersionsRequest.builder()
        .bucket(versioningSuspendedBucket)
        .prefix("a.txt")
        .build());
    assertEquals(1, versionListing.deleteMarkers().size());

    assertDoesNotThrow(() -> s3.deleteObject(DeleteObjectRequest.builder()
        .bucket(versioningSuspendedBucket)
        .key("a.txt")
        .build()));

    ListObjectVersionsResponse versionListing1 = s3.listObjectVersions(ListObjectVersionsRequest.builder()
        .bucket(versioningSuspendedBucket)
        .prefix("a.txt")
        .build());
    assertEquals(1, versionListing1.deleteMarkers().size());

    assertDoesNotThrow(() -> s3.deleteObject(DeleteObjectRequest.builder()
        .bucket(versioningSuspendedBucket)
        .key("a.txt")
        .versionId("null")
        .build()));

    ListObjectVersionsResponse versionListing2 = s3.listObjectVersions(ListObjectVersionsRequest.builder()
        .bucket(versioningSuspendedBucket)
        .prefix("a.txt")
        .build());
    assertEquals(0, versionListing2.versions().size());

    String unVersionedBucket = "un-versioned-bucket";
    s3.createBucket(CreateBucketRequest.builder().bucket(unVersionedBucket).build());
    assertDoesNotThrow(() -> s3.deleteObject(DeleteObjectRequest.builder()
        .bucket(unVersionedBucket)
        .key("not-exists.txt")
        .build()));

    s3.putObject(PutObjectRequest.builder()
        .bucket(unVersionedBucket)
        .key("a.txt")
        .build(), 
        RequestBody.fromString("Hello"));

    ListObjectVersionsResponse versionListing3 = s3.listObjectVersions(ListObjectVersionsRequest.builder()
        .bucket(unVersionedBucket)
        .prefix("a.txt")
        .build());
    assertEquals(1, versionListing3.versions().size());

    s3.deleteObject(DeleteObjectRequest.builder()
        .bucket(unVersionedBucket)
        .key("a.txt")
        .build());

    ListObjectVersionsResponse versionListing4 = s3.listObjectVersions(ListObjectVersionsRequest.builder()
        .bucket(unVersionedBucket)
        .prefix("a.txt")
        .build());
    assertEquals(0, versionListing4.versions().size());

    assertDoesNotThrow(() -> s3.deleteBucket(DeleteBucketRequest.builder()
        .bucket(unVersionedBucket)
        .build()));
  }
}
