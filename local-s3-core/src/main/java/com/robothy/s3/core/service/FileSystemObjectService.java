package com.robothy.s3.core.service;

import com.robothy.s3.core.model.VersionedObject;
import com.robothy.s3.core.model.answers.GetObjectAns;
import com.robothy.s3.core.model.answers.PutObjectAns;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.LocalS3Metadata;
import com.robothy.s3.core.model.request.GetObjectOptions;
import com.robothy.s3.core.model.request.ListObjectVersionsOptions;
import com.robothy.s3.core.model.request.PutObjectOptions;
import com.robothy.s3.core.storage.MetadataStore;
import com.robothy.s3.core.storage.Storage;
import java.util.List;

/**
 * File system implementation of {@linkplain ObjectService}.
 * All related object changes will be persisted in the file system.
 */
@Deprecated
public class FileSystemObjectService implements ObjectService {

  /**
   * Create a file system based {@linkplain ObjectService}.
   *
   * @param s3Metadata LocalS3 metadata.
   * @param storage file storage.
   * @param bucketMetadataStore bucket metadata store.
   * @return created {@linkplain ObjectService}.
   */
  public static ObjectService create(LocalS3Metadata s3Metadata, Storage storage, MetadataStore<BucketMetadata> bucketMetadataStore) {
    return new FileSystemObjectService(s3Metadata, storage, bucketMetadataStore);
  }

  private final ObjectService inMemoService;

  private final LocalS3Metadata s3Metadata;

  private final MetadataStore<BucketMetadata> bucketMetadataStore;


  private FileSystemObjectService(LocalS3Metadata s3Metadata, Storage storage, MetadataStore<BucketMetadata> bucketMetadataStore) {
    this.s3Metadata = s3Metadata;
    this.inMemoService = InMemoryObjectService.create(s3Metadata, storage);
    this.bucketMetadataStore = bucketMetadataStore;
  }

  @Override
  public LocalS3Metadata localS3Metadata() {
    return this.s3Metadata;
  }

  @Override
  public Storage storage() {
    return this.storage();
  }

  @Override
  public PutObjectAns putObject(String bucketName, String key, PutObjectOptions options) {
    return null;
  }


  @Override
  public GetObjectAns getObject(String bucketName, String key, GetObjectOptions options) {
    return inMemoService.getObject(bucketName, key, options);
  }
}