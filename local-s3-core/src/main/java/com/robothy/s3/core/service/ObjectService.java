package com.robothy.s3.core.service;

/**
 * LocalS3 object service abstraction.
 */
public interface ObjectService extends LocalS3MetadataApplicable, StorageApplicable,
    PutObjectService, GetObjectService, DeleteObjectService, ListObjectsService, ListObjectVersionsService,
    CreateMultipartUploadService, UploadPartService, CompleteMultipartUploadService, CopyObjectService,
    ObjectTaggingService, DeleteObjectsService, AbortMultipartUploadService, ListPartsService, ListObjectsV2Service {


}
