package com.robothy.s3.core.service;

import com.robothy.s3.core.model.VersionedObject;
import com.robothy.s3.core.model.internal.LocalS3Metadata;
import com.robothy.s3.core.model.request.ListObjectVersionsOptions;
import com.robothy.s3.core.storage.Storage;
import java.util.List;

public class InMemoryObjectService implements ObjectService {

  public static ObjectService create(LocalS3Metadata s3Metadata, Storage storage) {
    return new InMemoryObjectService(s3Metadata, storage);
  }

  private final LocalS3Metadata s3Metadata;

  private final Storage storage;

  private InMemoryObjectService(LocalS3Metadata s3Metadata, Storage storage) {
    this.s3Metadata = s3Metadata;
    this.storage = storage;
  }

  @Override
  public LocalS3Metadata localS3Metadata() {
    return this.s3Metadata;
  }

  @Override
  public Storage storage() {
    return this.storage;
  }



}
