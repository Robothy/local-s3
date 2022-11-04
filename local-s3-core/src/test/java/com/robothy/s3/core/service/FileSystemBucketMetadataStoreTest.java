package com.robothy.s3.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.ObjectMetadata;
import com.robothy.s3.core.storage.FileSystemBucketMetadataStore;
import com.robothy.s3.core.storage.MetadataStore;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

class FileSystemBucketMetadataStoreTest {

  @Test
  void create() throws IOException {
    Path abc = Files.createTempFile("abc", "");
    Files.delete(abc);
    MetadataStore<BucketMetadata> metadataStore = FileSystemBucketMetadataStore.create(abc);
    assertNotNull(metadataStore);
    FileUtils.deleteDirectory(abc.toFile());
  }

  @Test
  @SneakyThrows
  void fetch() {
    Path tempDirectory = Files.createTempDirectory("bucket-meta");
    MetadataStore<BucketMetadata> store = FileSystemBucketMetadataStore.create(tempDirectory);
    BucketMetadata bucketMetadata = new BucketMetadata();
    bucketMetadata.setBucketName("bucket");
    bucketMetadata.setVersioningEnabled(false);
    bucketMetadata.setCreationDate(System.currentTimeMillis());
    bucketMetadata.getObjectMap().put("123", new ObjectMetadata());
    store.store(bucketMetadata.getBucketName(), bucketMetadata);
    assertEquals(bucketMetadata, store.fetch("bucket"));


    bucketMetadata.setVersioningEnabled(true);
    store.store(bucketMetadata.getBucketName(), bucketMetadata);
    assertEquals(bucketMetadata, store.fetch("bucket"));

    assertThrows(IllegalArgumentException.class, () -> store.store(bucketMetadata.getBucketName(), new BucketMetadata()));

    FileUtils.deleteDirectory(tempDirectory.toFile());
  }

  @Test
  @SneakyThrows
  void delete() {
    Path tempDirectory = Files.createTempDirectory("bucket-meta");
    MetadataStore<BucketMetadata> bucketStore = FileSystemBucketMetadataStore.create(tempDirectory);
    BucketMetadata bucketMetadata = new BucketMetadata();
    bucketMetadata.setBucketName("bucket");
    bucketStore.store(bucketMetadata.getBucketName(), bucketMetadata);
    bucketStore.delete("bucket");
    assertThrows(IllegalStateException.class, () -> bucketStore.delete("bucket"));
    FileUtils.deleteDirectory(tempDirectory.toFile());
  }

  @Test
  void fetchAll() throws Exception {

    Path tempDirectory = Files.createTempDirectory("bucket-meta");
    MetadataStore<BucketMetadata> bucketStore = FileSystemBucketMetadataStore.create(tempDirectory);
    BucketMetadata bucketMetadata = new BucketMetadata();
    bucketMetadata.setBucketName("bucket");
    bucketStore.store(bucketMetadata.getBucketName(), bucketMetadata);

    bucketMetadata.setBucketName("bucket1");
    bucketStore.store(bucketMetadata.getBucketName(), bucketMetadata);

    assertEquals(2, bucketStore.fetchAll().size());
    FileUtils.deleteDirectory(tempDirectory.toFile());
  }
}