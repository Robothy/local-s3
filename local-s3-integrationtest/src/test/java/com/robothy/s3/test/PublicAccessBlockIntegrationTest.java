package com.robothy.s3.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.robothy.s3.jupiter.LocalS3;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetPublicAccessBlockRequest;
import software.amazon.awssdk.services.s3.model.GetPublicAccessBlockResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PublicAccessBlockConfiguration;
import software.amazon.awssdk.services.s3.model.PutPublicAccessBlockRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

public class PublicAccessBlockIntegrationTest {
  @Test
  @LocalS3
  void testPublicAccessBlockOperations(S3Client s3Client) {
    
    // Create a bucket
    String bucketName = "test-public-access-block";
    s3Client.createBucket(b -> b.bucket(bucketName));
      // Test 1: No public access block configuration initially
    S3Exception exception = assertThrows(S3Exception.class, 
        () -> s3Client.getPublicAccessBlock(GetPublicAccessBlockRequest.builder()
            .bucket(bucketName)
            .build()));
    assertEquals("NoSuchPublicAccessBlockConfiguration", exception.awsErrorDetails().errorCode());
    
    // Test 2: Put public access block configuration
    PublicAccessBlockConfiguration config = PublicAccessBlockConfiguration.builder()
        .blockPublicAcls(true)
        .ignorePublicAcls(true)
        .blockPublicPolicy(false)
        .restrictPublicBuckets(false)
        .build();
    
    s3Client.putPublicAccessBlock(PutPublicAccessBlockRequest.builder()
        .bucket(bucketName)
        .publicAccessBlockConfiguration(config)
        .build());
    
    // Test 3: Get public access block configuration
    GetPublicAccessBlockResponse response = s3Client.getPublicAccessBlock(
        GetPublicAccessBlockRequest.builder().bucket(bucketName).build());
    
    PublicAccessBlockConfiguration retrievedConfig = response.publicAccessBlockConfiguration();
    assertTrue(retrievedConfig.blockPublicAcls());
    assertTrue(retrievedConfig.ignorePublicAcls());
    assertFalse(retrievedConfig.blockPublicPolicy());
    assertFalse(retrievedConfig.restrictPublicBuckets());
    
    // Test 4: Delete public access block configuration
    s3Client.deletePublicAccessBlock(b -> b.bucket(bucketName));    // Test 5: Verify configuration was deleted
    S3Exception exception2 = assertThrows(S3Exception.class, 
        () -> s3Client.getPublicAccessBlock(GetPublicAccessBlockRequest.builder()
            .bucket(bucketName)
            .build()));
    assertEquals("NoSuchPublicAccessBlockConfiguration", exception2.awsErrorDetails().errorCode());    // Test 6: Non-existent bucket throws an exception (specific type could be S3Exception)
    assertThrows(Exception.class, 
        () -> s3Client.getPublicAccessBlock(GetPublicAccessBlockRequest.builder()
            .bucket("non-existent-bucket")
            .build()));
  }
    @Test
  @LocalS3
  void testGetBucketPolicyStatus(S3Client s3Client) {
    
    // Create a bucket
    String bucketName = "test-policy-status";
    s3Client.createBucket(b -> b.bucket(bucketName));
    
    // Set bucket policy (a minimal valid policy)
    String policy = "{ \"Version\": \"2012-10-17\", \"Statement\": [ { \"Effect\": \"Allow\", \"Principal\": \"*\", \"Action\": \"s3:GetObject\", \"Resource\": \"arn:aws:s3:::" + bucketName + "/*\" } ] }";
    s3Client.putBucketPolicy(b -> b.bucket(bucketName).policy(policy));
    
    // Without public access blocks, the bucket should be public
    assertTrue(s3Client.getBucketPolicyStatus(b -> b.bucket(bucketName)).policyStatus().isPublic());
    
    // Add public access blocks
    PublicAccessBlockConfiguration config = PublicAccessBlockConfiguration.builder()
        .blockPublicPolicy(true)
        .build();
    
    s3Client.putPublicAccessBlock(PutPublicAccessBlockRequest.builder()
        .bucket(bucketName)
        .publicAccessBlockConfiguration(config)
        .build());
    
    // Now the bucket should not be public
    assertFalse(s3Client.getBucketPolicyStatus(b -> b.bucket(bucketName)).policyStatus().isPublic());
  }
}
