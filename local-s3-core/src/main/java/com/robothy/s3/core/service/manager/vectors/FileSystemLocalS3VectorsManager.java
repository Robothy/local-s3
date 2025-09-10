package com.robothy.s3.core.service.manager.vectors;

import com.robothy.s3.core.model.internal.s3vectors.LocalS3VectorsMetadata;
import com.robothy.s3.core.model.internal.s3vectors.VectorBucketMetadata;
import com.robothy.s3.core.service.loader.MetadataLoader;
import com.robothy.s3.core.service.manager.LocalS3ServicesInvocationHandler;
import com.robothy.s3.core.service.s3vectors.S3VectorsService;
import com.robothy.s3.core.storage.MetadataStore;
import com.robothy.s3.core.storage.s3vectors.FileSystemVectorBucketMetadataStore;
import com.robothy.s3.core.storage.s3vectors.VectorStorage;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;

final class FileSystemLocalS3VectorsManager implements LocalS3VectorsManager {

  private static final String VECTOR_STORAGE_DIRECTORY = ".storage";

  private static final int MAX_CACHE_SIZE = 2000;

  private final Path s3VectorsDataPath;

  public FileSystemLocalS3VectorsManager(Path s3VectorsDataPath) {
    this.s3VectorsDataPath = s3VectorsDataPath;
  }

  @Override
  public S3VectorsService s3VectorsService() {
    LocalS3VectorsMetadata vectorsMetadata = MetadataLoader.create(LocalS3VectorsMetadata.class)
        .load(s3VectorsDataPath);

    VectorStorage vectorStorage = VectorStorage.createFileSystem(Paths.get(VECTOR_STORAGE_DIRECTORY), MAX_CACHE_SIZE);
    S3VectorsService s3VectorsService = S3VectorsService.create(vectorsMetadata, vectorStorage);
    MetadataStore<VectorBucketMetadata> metadataStore = FileSystemVectorBucketMetadataStore.create(this.s3VectorsDataPath);

    LocalS3ServicesInvocationHandler<VectorBucketMetadata> invocationHandler =
        new LocalS3ServicesInvocationHandler<>(s3VectorsService, bucketName -> vectorsMetadata.getVectorBucketMetadata(bucketName).get(), metadataStore);
    return (S3VectorsService) Proxy.newProxyInstance(S3VectorsService.class.getClassLoader(),
        new Class[] {S3VectorsService.class}, invocationHandler);
  }

}
