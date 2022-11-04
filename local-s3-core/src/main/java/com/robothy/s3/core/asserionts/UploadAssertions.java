package com.robothy.s3.core.asserionts;

import com.robothy.s3.core.exception.ObjectNotExistException;
import com.robothy.s3.core.exception.UploadNotExistException;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.UploadMetadata;
import com.robothy.s3.core.model.internal.UploadPartMetadata;
import java.util.NavigableMap;

/**
 * Multipart upload related assertions.
 */
public class UploadAssertions {

  /**
   * Assert the object key is exists.
   *
   * @param bucketMetadata the bucket metadata.
   * @param key the object key.
   * @return the {@linkplain UploadMetadata} map of the specified key.
   */
  public static NavigableMap<String, UploadMetadata> assertKeyExists(BucketMetadata bucketMetadata, String key) {
    if (!bucketMetadata.getUploads().containsKey(key)) {
      throw new ObjectNotExistException(key);
    }
    return bucketMetadata.getUploads().get(key);
  }

  /**
   * Assert that the give upload exists.
   *
   * @param bucketMetadata the bucket metadata.
   * @param key the object key of the specified upload ID.
   * @param uploadId the generated upload ID when creating multipart upload.
   * @return the upload metadata of specified upload ID.
   */
  public static UploadMetadata assertUploadExists(BucketMetadata bucketMetadata, String key, String uploadId) {
    NavigableMap<String, UploadMetadata> uploadMetadataMap = assertKeyExists(bucketMetadata, key);
    if (!uploadMetadataMap.containsKey(uploadId)) {
      throw new UploadNotExistException(key, uploadId);
    }
    return uploadMetadataMap.get(uploadId);
  }

  /**
   * Assert that the specified part number is exists in the {@code uploadMetadata}.
   *
   * @param uploadMetadata the upload metadata.
   * @param partNumber part number to verify.
   * @return the {@linkplain UploadPartMetadata} of the specified part number.
   */
  public static UploadPartMetadata assertPartNumberExists(UploadMetadata uploadMetadata, Integer partNumber) {
    if (!uploadMetadata.getParts().containsKey(partNumber)) {
      throw new IllegalArgumentException("Part number " + partNumber + " not exists.");
    }
    return uploadMetadata.getParts().get(partNumber);
  }

}
