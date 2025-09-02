package com.robothy.s3.core.service.loader;

import static org.junit.jupiter.api.Assertions.*;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.LocalS3Metadata;
import com.robothy.s3.core.storage.FileSystemBucketMetadataStore;
import com.robothy.s3.core.storage.MetadataStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FileSystemLocalS3MetadataLoaderTest {

  @Test
  void load() throws Exception {
    Path tempDirectory = Files.createTempDirectory("local-s3");
    MetadataStore<BucketMetadata> bucketMetaStore = FileSystemBucketMetadataStore.create(tempDirectory);
    BucketMetadata bucket1 = new BucketMetadata();
    bucket1.setBucketName("bucket1");
    bucket1.setCreationDate(System.currentTimeMillis());
    bucketMetaStore.store(bucket1.getBucketName(), bucket1);

    BucketMetadata bucket2 = new BucketMetadata();
    bucket2.setBucketName("bucket2");
    bucket2.setCreationDate(System.currentTimeMillis());
    bucketMetaStore.store(bucket2.getBucketName(), bucket2);

    LocalS3Metadata s3Metadata = MetadataLoader.create(LocalS3Metadata.class).load(tempDirectory);
    Optional<BucketMetadata> loadedBucket1 = s3Metadata.getBucketMetadata(bucket1.getBucketName());
    Optional<BucketMetadata> loadedBucket2 = s3Metadata.getBucketMetadata(bucket2.getBucketName());
    assertTrue(loadedBucket1.isPresent());
    assertEquals(bucket1, loadedBucket1.get());
    assertTrue(loadedBucket2.isPresent());
    assertEquals(bucket2, loadedBucket2.get());
  }


}