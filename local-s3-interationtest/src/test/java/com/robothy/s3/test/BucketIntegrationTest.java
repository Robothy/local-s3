package com.robothy.s3.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.BucketPolicy;
import com.amazonaws.services.s3.model.BucketReplicationConfiguration;
import com.amazonaws.services.s3.model.BucketTaggingConfiguration;
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.amazonaws.services.s3.model.CanonicalGrantee;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.DeleteMarkerReplication;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.HeadBucketRequest;
import com.amazonaws.services.s3.model.HeadBucketResult;
import com.amazonaws.services.s3.model.Owner;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.ReplicationDestinationConfig;
import com.amazonaws.services.s3.model.ReplicationRule;
import com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest;
import com.amazonaws.services.s3.model.TagSet;
import com.amazonaws.services.s3.model.replication.ReplicationFilter;
import com.robothy.s3.core.exception.S3ErrorCode;
import com.robothy.s3.jupiter.LocalS3;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class BucketIntegrationTest {

  @Test
  @LocalS3
  void testCreateBucket(AmazonS3 s3) {
    CreateBucketRequest bucketRequest = new CreateBucketRequest("my-bucket", "local");
    Bucket bucket = s3.createBucket(bucketRequest);
    assertEquals("my-bucket", bucket.getName());

    Bucket bucket2 = s3.createBucket("bucket2");
    assertEquals("bucket2", bucket2.getName());

    HeadBucketResult headBucketResult = s3.headBucket(new HeadBucketRequest("my-bucket"));
    assertEquals("local", headBucketResult.getBucketRegion());
    s3.deleteBucket("my-bucket");
    assertThrows(AmazonS3Exception.class, () -> s3.headBucket(new HeadBucketRequest("my-bucket")));
  }

  @Test
  @LocalS3
  void testVersioningEnabled(AmazonS3 s3) {
    Bucket bucket = s3.createBucket("my-bucket");
    BucketVersioningConfiguration bucketVersioningConfiguration = s3.getBucketVersioningConfiguration(bucket.getName());
    assertEquals(BucketVersioningConfiguration.OFF, bucketVersioningConfiguration.getStatus());

    s3.setBucketVersioningConfiguration(new SetBucketVersioningConfigurationRequest(bucket.getName(),
        new BucketVersioningConfiguration(BucketVersioningConfiguration.ENABLED)));
    bucketVersioningConfiguration = s3.getBucketVersioningConfiguration(bucket.getName());
    assertEquals(BucketVersioningConfiguration.ENABLED, bucketVersioningConfiguration.getStatus());

    s3.setBucketVersioningConfiguration(new SetBucketVersioningConfigurationRequest(bucket.getName(),
        new BucketVersioningConfiguration(BucketVersioningConfiguration.SUSPENDED)));
    bucketVersioningConfiguration = s3.getBucketVersioningConfiguration(bucket.getName());
    assertEquals(BucketVersioningConfiguration.SUSPENDED, bucketVersioningConfiguration.getStatus());
  }

  @Test
  @LocalS3
  void testBucketTagging(AmazonS3 s3) {
    Bucket bucket = s3.createBucket("my-bucket");
    assertNull(s3.getBucketTaggingConfiguration(bucket.getName()));

    TagSet tagSet = new TagSet(Map.of("Name", "Bob", "Profession", "Doctor"));
    TagSet tagSet1 = new TagSet(Map.of("Name1", "Bob1", "Profession1", "Doctor1"));

    BucketTaggingConfiguration tagging =
        new BucketTaggingConfiguration(Set.of(tagSet, tagSet1));
    s3.setBucketTaggingConfiguration(bucket.getName(), tagging);
    BucketTaggingConfiguration bucketTaggingConfiguration = s3.getBucketTaggingConfiguration(bucket.getName());
    assertEquals(2, bucketTaggingConfiguration.getAllTagSets().size());
    Set<Map<String, String>> tagSets = bucketTaggingConfiguration.getAllTagSets().stream().map(TagSet::getAllTags)
        .collect(Collectors.toSet());
    assertTrue(tagSets.contains(tagSet.getAllTags()));
    assertTrue(tagSets.contains(tagSet1.getAllTags()));
  }

  @Test
  @LocalS3
  void bucketAcl(AmazonS3 s3) {
    String bucketName = "my-bucket";
    Bucket bucket = s3.createBucket(bucketName);
    AccessControlList bucketAcl = s3.getBucketAcl(bucketName);
    Owner owner = bucketAcl.getOwner();
    assertNotNull(owner);
    assertEquals("LocalS3", owner.getDisplayName()); // The default owner is 'LocalS3'
    assertNotNull(owner.getId());
    assertTrue(bucketAcl.getGrantsAsList().isEmpty());

    AccessControlList accessControlList = new AccessControlList();
    CanonicalGrantee bob = new CanonicalGrantee("123");
    CanonicalGrantee alice = new CanonicalGrantee(owner.getId());
    accessControlList.grantPermission(bob, Permission.FullControl);
    accessControlList.grantPermission(alice, Permission.FullControl);
    accessControlList.grantPermission(GroupGrantee.AllUsers, Permission.Read);
    accessControlList.setOwner(owner);
    s3.setBucketAcl(bucket.getName(), accessControlList);

    AccessControlList fetchedBucketAcl = s3.getBucketAcl(bucket.getName());
    assertEquals(accessControlList, fetchedBucketAcl);
  }

  @LocalS3
  @Test
  void bucketPolicy(AmazonS3 s3) {
    String bucketName = "my-bucket";
    AmazonServiceException amazonServiceException =
        assertThrows(AmazonServiceException.class, () -> s3.getBucketPolicy(bucketName));
    assertEquals(S3ErrorCode.NoSuchBucket.code(), amazonServiceException.getErrorCode());

    s3.createBucket(bucketName);
    BucketPolicy retrievedBucketPolicy = s3.getBucketPolicy(bucketName);
    assertNull(retrievedBucketPolicy.getPolicyText());
    String policyText = "Policy JSON";
    s3.setBucketPolicy(bucketName, policyText);
    BucketPolicy bucketPolicy = s3.getBucketPolicy(bucketName);
    assertEquals(policyText, bucketPolicy.getPolicyText());

    s3.deleteBucketPolicy(bucketName);
    retrievedBucketPolicy = s3.getBucketPolicy(bucketName);
    assertNull(retrievedBucketPolicy.getPolicyText());
  }

//  @Test
//  void testGetBucket() {
//    s3Client.createBucket("test-bucket2");
//    GetBucketResult getBucketResult = s3ControlClient.getBucket(new GetBucketRequest()
//        .withBucket("test-bucket2"));
//
//    assertNotNull(getBucketResult);
//    assertEquals("test-bucket2", getBucketResult.getBucket());
//    assertFalse(getBucketResult.getPublicAccessBlockEnabled()); // always false.
//    assertNotNull(getBucketResult.getCreationDate());
//  }

  @Test
  @LocalS3
  void testBucketReplication(AmazonS3 s3) {
    String bucketName = "my-bucket";
    assertDoesNotThrow(() -> s3.createBucket(bucketName));

    ReplicationDestinationConfig destinationConfig =
        new ReplicationDestinationConfig().withBucketARN("arn:aws:s3:::exampletargetbucket");
    assertDoesNotThrow(() -> s3.setBucketReplicationConfiguration(bucketName, new BucketReplicationConfiguration()
        .addRule("1", new ReplicationRule().withDestinationConfig(destinationConfig)
            .withPriority(1).withDeleteMarkerReplication(new DeleteMarkerReplication().withStatus("Disabled")))));

    BucketReplicationConfiguration replication = s3.getBucketReplicationConfiguration(bucketName);
    ReplicationRule rule = replication.getRule("1");
    assertEquals("Disabled", rule.getDeleteMarkerReplication().getStatus());
    assertEquals("arn:aws:s3:::exampletargetbucket", rule.getDestinationConfig().getBucketARN());
    assertEquals(1, rule.getPriority());

    assertDoesNotThrow(() -> s3.deleteBucketReplicationConfiguration(bucketName));
  }

}
