package com.robothy.s3.core.service.manager.vectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.robothy.s3.core.exception.BucketAlreadyExistsException;
import com.robothy.s3.core.service.s3vectors.S3VectorsService;
import com.robothy.s3.datatypes.s3vectors.EncryptionConfiguration;
import com.robothy.s3.datatypes.s3vectors.VectorBucket;
import com.robothy.s3.datatypes.s3vectors.response.CreateVectorBucketResponse;
import com.robothy.s3.datatypes.s3vectors.response.ListVectorBucketsResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

class FileSystemLocalS3VectorsManagerTest {

  @Test
  void testPersistenceThroughBucketOperations() throws IOException {
    // Setup: Create temporary directory for testing
    Path tempDirectory = Files.createTempDirectory("local-s3-vectors-persistence-test");
    
    try {
      // Test data
      String bucket1Name = "persistence-test-bucket-1";
      String bucket2Name = "persistence-test-bucket-2";
      String bucket3Name = "persistence-test-bucket-3";
      
      EncryptionConfiguration encryptionConfig = EncryptionConfiguration.builder()
          .sseAlgorithm("AES256")
          .bucketKeyEnabled(true)
          .build();

      // Phase 1: Create first manager instance and create buckets
      FileSystemLocalS3VectorsManager manager1 = new FileSystemLocalS3VectorsManager(tempDirectory);
      S3VectorsService service1 = manager1.s3VectorsService();

      // Create multiple buckets with different configurations
      CreateVectorBucketResponse response1 = service1.createVectorBucket(bucket1Name, encryptionConfig);
      CreateVectorBucketResponse response2 = service1.createVectorBucket(bucket2Name, null);
      CreateVectorBucketResponse response3 = service1.createVectorBucket(bucket3Name, encryptionConfig);

      // Verify creation responses
      assertNotNull(response1);
      assertEquals(bucket1Name, response1.getVectorBucketName());
      assertNotNull(response1.getVectorBucketArn());
      assertNotNull(response1.getCreationDate());

      assertNotNull(response2);
      assertEquals(bucket2Name, response2.getVectorBucketName());

      assertNotNull(response3);
      assertEquals(bucket3Name, response3.getVectorBucketName());

      // Verify buckets exist and can be retrieved
      VectorBucket bucket1 = service1.getVectorBucket(bucket1Name);
      VectorBucket bucket2 = service1.getVectorBucket(bucket2Name);
      VectorBucket bucket3 = service1.getVectorBucket(bucket3Name);

      assertNotNull(bucket1);
      assertEquals(bucket1Name, bucket1.getVectorBucketName());
      assertNotNull(bucket1.getEncryptionConfiguration());
      assertTrue(bucket1.getEncryptionConfiguration().isPresent());
      assertEquals("AES256", bucket1.getEncryptionConfiguration().get().getSseAlgorithm());
      assertEquals(true, bucket1.getEncryptionConfiguration().get().getBucketKeyEnabled());

      assertNotNull(bucket2);
      assertEquals(bucket2Name, bucket2.getVectorBucketName());
      // bucket2 should have no encryption configuration
      assertFalse(bucket2.getEncryptionConfiguration().isPresent());

      assertNotNull(bucket3);
      assertEquals(bucket3Name, bucket3.getVectorBucketName());
      assertNotNull(bucket3.getEncryptionConfiguration());

      // Verify list operation shows all buckets
      ListVectorBucketsResponse listResponse1 = service1.listVectorBuckets(null, null, null);
      assertNotNull(listResponse1);
      assertNotNull(listResponse1.getVectorBuckets());
      assertEquals(3, listResponse1.getVectorBuckets().size());

      // Test duplicate bucket creation fails
      assertThrows(BucketAlreadyExistsException.class, 
          () -> service1.createVectorBucket(bucket1Name, null));

      // Delete one bucket to test persistence of delete operations
      assertDoesNotThrow(() -> service1.deleteVectorBucket(bucket2Name));

      // Verify bucket2 is deleted
      assertThrows(Exception.class, () -> service1.getVectorBucket(bucket2Name));

      // Verify remaining buckets still exist
      assertDoesNotThrow(() -> service1.getVectorBucket(bucket1Name));
      assertDoesNotThrow(() -> service1.getVectorBucket(bucket3Name));

      // Phase 2: Create new manager instance with same directory to test persistence
      FileSystemLocalS3VectorsManager manager2 = new FileSystemLocalS3VectorsManager(tempDirectory);
      S3VectorsService service2 = manager2.s3VectorsService();

      // Verify bucket1 and bucket3 still exist after manager restart
      VectorBucket persistedBucket1 = service2.getVectorBucket(bucket1Name);
      VectorBucket persistedBucket3 = service2.getVectorBucket(bucket3Name);

      // Verify persisted bucket1 maintains its properties
      assertNotNull(persistedBucket1);
      assertEquals(bucket1Name, persistedBucket1.getVectorBucketName());
      assertEquals(bucket1.getArn(), persistedBucket1.getArn());
      assertEquals(bucket1.getCreationTime(), persistedBucket1.getCreationTime());
      assertNotNull(persistedBucket1.getEncryptionConfiguration());
      assertEquals(true, persistedBucket1.getEncryptionConfiguration().isPresent());
      assertEquals("AES256", persistedBucket1.getEncryptionConfiguration().get().getSseAlgorithm());
      assertEquals(true, persistedBucket1.getEncryptionConfiguration().get().getBucketKeyEnabled());

      // Verify persisted bucket3 maintains its properties
      assertNotNull(persistedBucket3);
      assertEquals(bucket3Name, persistedBucket3.getVectorBucketName());
      assertEquals(bucket3.getArn(), persistedBucket3.getArn());
      assertEquals(bucket3.getCreationTime(), persistedBucket3.getCreationTime());

      // Verify bucket2 remains deleted
      assertThrows(Exception.class, () -> service2.getVectorBucket(bucket2Name));

      // Verify list operation shows only remaining buckets
      ListVectorBucketsResponse listResponse2 = service2.listVectorBuckets(null, null, null);
      assertNotNull(listResponse2);
      assertNotNull(listResponse2.getVectorBuckets());
      assertEquals(2, listResponse2.getVectorBuckets().size());

      // Verify we can still perform operations with the new manager
      // Test creating a new bucket with the new manager instance
      String newBucketName = "new-bucket-after-restart";
      CreateVectorBucketResponse newResponse = service2.createVectorBucket(newBucketName, null);
      assertNotNull(newResponse);
      assertEquals(newBucketName, newResponse.getVectorBucketName());

      // Test duplicate creation still fails
      assertThrows(BucketAlreadyExistsException.class, 
          () -> service2.createVectorBucket(bucket1Name, null));

      // Test deleting existing bucket with new manager
      assertDoesNotThrow(() -> service2.deleteVectorBucket(bucket3Name));
      assertThrows(Exception.class, () -> service2.getVectorBucket(bucket3Name));

      // Phase 3: Create third manager instance to verify final state persistence
      FileSystemLocalS3VectorsManager manager3 = new FileSystemLocalS3VectorsManager(tempDirectory);
      S3VectorsService service3 = manager3.s3VectorsService();

      // Verify final state: only bucket1 and newBucket should exist
      assertDoesNotThrow(() -> service3.getVectorBucket(bucket1Name));
      assertDoesNotThrow(() -> service3.getVectorBucket(newBucketName));
      assertThrows(Exception.class, () -> service3.getVectorBucket(bucket2Name));
      assertThrows(Exception.class, () -> service3.getVectorBucket(bucket3Name));

      ListVectorBucketsResponse finalListResponse = service3.listVectorBuckets(null, null, null);
      assertEquals(2, finalListResponse.getVectorBuckets().size());

    } finally {
      // Cleanup: Delete temporary directory
      if (tempDirectory != null && Files.exists(tempDirectory)) {
        FileUtils.deleteDirectory(tempDirectory.toFile());
      }
    }
  }
}
