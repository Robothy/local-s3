package com.robothy.s3.test;

import static org.junit.jupiter.api.Assertions.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.*;
import com.robothy.s3.jupiter.LocalS3;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.model.S3Exception;

public class BucketIntegrationTest {

  @Test
  @LocalS3
  void testCreateBucket(S3Client s3) {
    assertThrows(NoSuchBucketException.class, () -> s3.headBucket(HeadBucketRequest.builder().bucket("my-bucket").build()));
    CreateBucketRequest bucketRequest = CreateBucketRequest.builder()
        .bucket("my-bucket")
        .build();
    CreateBucketResponse bucket = s3.createBucket(bucketRequest);
    assertTrue(s3.headBucket(HeadBucketRequest.builder().bucket("my-bucket").build()).sdkHttpResponse().isSuccessful());

    assertThrows(NoSuchBucketException.class,
        () -> s3.headBucket(HeadBucketRequest.builder().bucket("bucket2").build()).sdkHttpResponse().isSuccessful());
    CreateBucketResponse bucket2 = s3.createBucket(CreateBucketRequest.builder().bucket("bucket2").build());
    assertTrue(s3.headBucket(HeadBucketRequest.builder().bucket("bucket2").build()).sdkHttpResponse().isSuccessful());

    HeadBucketResponse headBucketResult = s3.headBucket(HeadBucketRequest.builder().bucket("my-bucket").build());
    s3.deleteBucket(DeleteBucketRequest.builder().bucket("my-bucket").build());
    assertThrows(NoSuchBucketException.class, () -> 
        s3.headBucket(HeadBucketRequest.builder().bucket("my-bucket").build()));
  }

  @Test
  @LocalS3
  void testVersioningEnabled(S3Client s3) {
    s3.createBucket(CreateBucketRequest.builder().bucket("my-bucket").build());
    
    GetBucketVersioningResponse versioning = s3.getBucketVersioning(
        GetBucketVersioningRequest.builder().bucket("my-bucket").build());
    assertNull(versioning.status());

    s3.putBucketVersioning(PutBucketVersioningRequest.builder()
        .bucket("my-bucket")
        .versioningConfiguration(VersioningConfiguration.builder()
            .status(BucketVersioningStatus.ENABLED)
            .build())
        .build());
    
    versioning = s3.getBucketVersioning(
        GetBucketVersioningRequest.builder().bucket("my-bucket").build());
    assertEquals(BucketVersioningStatus.ENABLED, versioning.status());
  }

  @Test
  @LocalS3
  void testBucketTagging(S3Client s3) {
    s3.createBucket(CreateBucketRequest.builder().bucket("my-bucket").build());
    
    assertThrows(S3Exception.class, () -> 
        s3.getBucketTagging(GetBucketTaggingRequest.builder().bucket("my-bucket").build()));

    Map<String, String> tags = Map.of("Name", "Bob", "Profession", "Doctor");

    s3.putBucketTagging(PutBucketTaggingRequest.builder()
        .bucket("my-bucket")
        .tagging(Tagging.builder()
            .tagSet(tags.entrySet().stream()
                .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
                .collect(Collectors.toList()))
            .build())
        .build());

    GetBucketTaggingResponse tagging = s3.getBucketTagging(
        GetBucketTaggingRequest.builder().bucket("my-bucket").build());
    assertEquals(2, tagging.tagSet().size());
    Set<String> keys = tagging.tagSet().stream().map(Tag::key).collect(Collectors.toSet());
    assertTrue(keys.contains("Name"));
    assertTrue(keys.contains("Profession"));
    assertEquals("Bob", tagging.tagSet().stream().filter(t -> t.key().equals("Name")).findFirst().get().value());
    assertEquals("Doctor", tagging.tagSet().stream().filter(t -> t.key().equals("Profession")).findFirst().get().value());
  }

  @Test
  @LocalS3
  void bucketAcl(S3Client s3) {
    String bucketName = "my-bucket";
    s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
    
    GetBucketAclResponse bucketAcl = s3.getBucketAcl(
        GetBucketAclRequest.builder().bucket(bucketName).build());
    Owner owner = bucketAcl.owner();
    assertNotNull(owner);
    assertEquals("LocalS3", owner.displayName());
    assertNotNull(owner.id());
    
    s3.putBucketAcl(PutBucketAclRequest.builder()
        .bucket(bucketName)
        .accessControlPolicy(AccessControlPolicy.builder()
            .owner(owner)
            .grants(Grant.builder()
                .grantee(Grantee.builder()
                    .id("123")
                    .type(Type.CANONICAL_USER)
                    .build())
                .permission(Permission.FULL_CONTROL)
                .build())
            .build())
        .build());
    
    GetBucketAclResponse updatedAcl = s3.getBucketAcl(
        GetBucketAclRequest.builder().bucket(bucketName).build());
    assertEquals(1, updatedAcl.grants().size());
  }

  @Test
  @LocalS3
  void bucketPolicy(S3Client s3) {
    String bucketName = "my-bucket";
    assertThrows(NoSuchBucketException.class, () ->
        s3.getBucketPolicy(GetBucketPolicyRequest.builder().bucket(bucketName).build()));

    s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
    
    String policyText = "Policy JSON";
    s3.putBucketPolicy(PutBucketPolicyRequest.builder()
        .bucket(bucketName)
        .policy(policyText)
        .build());

    GetBucketPolicyResponse policy = s3.getBucketPolicy(
        GetBucketPolicyRequest.builder().bucket(bucketName).build());
    assertEquals(policyText, policy.policy());

    s3.deleteBucketPolicy(DeleteBucketPolicyRequest.builder().bucket(bucketName).build());
  }

  @Test
  @LocalS3
  void testBucketReplication(S3Client s3) {
    String bucketName = "my-bucket";
    s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());

    ReplicationRule rule = ReplicationRule.builder()
        .status(ReplicationRuleStatus.ENABLED)
        .priority(1)
        .destination(Destination.builder()
            .bucket("arn:aws:s3:::exampletargetbucket")
            .build())
        .build();

    s3.putBucketReplication(PutBucketReplicationRequest.builder()
        .bucket(bucketName)
        .replicationConfiguration(ReplicationConfiguration.builder()
            .rules(rule)
            .role("arn:aws:iam::123456789012:role/roleName")
            .build())
        .build());

    GetBucketReplicationResponse replication = s3.getBucketReplication(
        GetBucketReplicationRequest.builder().bucket(bucketName).build());
    assertEquals(1, replication.replicationConfiguration().rules().size());

    s3.deleteBucketReplication(DeleteBucketReplicationRequest.builder()
        .bucket(bucketName)
        .build());
  }

  @Test
  @LocalS3
  void testBucketEncryption(S3Client s3) {
    String bucketName = "my-bucket";
    s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());

    s3.putBucketEncryption(PutBucketEncryptionRequest.builder()
        .bucket(bucketName)
        .serverSideEncryptionConfiguration(ServerSideEncryptionConfiguration.builder()
            .rules(ServerSideEncryptionRule.builder()
                .applyServerSideEncryptionByDefault(ServerSideEncryptionByDefault.builder()
                    .sseAlgorithm(ServerSideEncryption.AES256)
                    .kmsMasterKeyID("arn:aws:kms:us-east-1:1234/5678example")
                    .build())
                .bucketKeyEnabled(true)
                .build())
            .build())
        .build());

    GetBucketEncryptionResponse encryption = s3.getBucketEncryption(
        GetBucketEncryptionRequest.builder().bucket(bucketName).build());
    List<ServerSideEncryptionRule> rules = encryption.serverSideEncryptionConfiguration().rules();
    assertEquals(1, rules.size());

    s3.deleteBucketEncryption(DeleteBucketEncryptionRequest.builder()
        .bucket(bucketName)
        .build());
  }

  @Test
  @LocalS3
  void testListBuckets(S3Client s3) {
    ListBucketsResponse buckets = s3.listBuckets();
    assertEquals(0, buckets.buckets().size());

    s3.createBucket(CreateBucketRequest.builder().bucket("test-bucket1").build());
    s3.createBucket(CreateBucketRequest.builder().bucket("test-bucket2").build());
    
    ListBucketsResponse buckets1 = s3.listBuckets();
    assertEquals(2, buckets1.buckets().size());
    assertEquals("test-bucket1", buckets1.buckets().get(0).name());
    assertTrue(buckets1.buckets().get(0).creationDate().isBefore(Instant.now()));
    assertEquals("test-bucket2", buckets1.buckets().get(1).name());
    assertTrue(buckets1.buckets().get(1).creationDate().isBefore(Instant.now()));

    s3.deleteBucket(DeleteBucketRequest.builder().bucket("test-bucket1").build());
    ListBucketsResponse buckets2 = s3.listBuckets();
    assertEquals(1, buckets2.buckets().size());
    assertEquals("test-bucket2", buckets2.buckets().get(0).name());
  }
}
