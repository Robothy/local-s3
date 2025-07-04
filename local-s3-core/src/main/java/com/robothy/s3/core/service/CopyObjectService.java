package com.robothy.s3.core.service;

import com.robothy.s3.core.annotations.BucketChanged;
import com.robothy.s3.core.annotations.BucketWriteLock;
import com.robothy.s3.core.model.answers.CopyObjectAns;
import com.robothy.s3.core.model.answers.GetObjectAns;
import com.robothy.s3.core.model.answers.PutObjectAns;
import com.robothy.s3.core.model.request.CopyObjectOptions;
import com.robothy.s3.core.model.request.GetObjectOptions;
import com.robothy.s3.core.model.request.PutObjectOptions;
import java.util.Map;

public interface CopyObjectService extends GetObjectService, PutObjectService, LocalS3MetadataApplicable, StorageApplicable {

  /**
   * Creates a copy of an object that is already stored in Local S3.
   *
   * @param bucket destination bucket.
   * @param key destination object key.
   * @param options copy options.
   * @return copy result.
   */
  @BucketChanged
  @BucketWriteLock
  default CopyObjectAns copyObject(String bucket, String key, CopyObjectOptions options) {
    String srcVersion = options.getSourceVersion().orElse(null);
    GetObjectAns srcObjectAns = getObject(options.getSourceBucket(), options.getSourceKey(),
        GetObjectOptions.builder().versionId(srcVersion).build());

    if (srcObjectAns.isDeleteMarker()) {
      throw new IllegalArgumentException("The source of a copy request may not specifically refer to a delete marker by version id.");
    }

    // Determine which metadata to use based on the metadata directive
    Map<String, String> metadataToUse;
    if (options.getMetadataDirective() == CopyObjectOptions.MetadataDirective.REPLACE) {
      // Use the metadata provided in the request
      metadataToUse = options.getUserMetadata();
    } else {
      // Use the metadata from the source object
      metadataToUse = srcObjectAns.getUserMetadata();
    }

    PutObjectAns putObjectAns = putObject(bucket, key, PutObjectOptions.builder()
        .content(srcObjectAns.getContent())
        .contentType(srcObjectAns.getContentType())
        .size(srcObjectAns.getSize())
        .userMetadata(metadataToUse)
        .build());

    return CopyObjectAns.builder()
        .sourceVersionId(srcObjectAns.getVersionId())
        .versionId(putObjectAns.getVersionId())
        .lastModified(putObjectAns.getCreationDate())
        .etag(putObjectAns.getEtag())
        .build();
  }

}
