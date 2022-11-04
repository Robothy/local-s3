package com.robothy.s3.core.service;

import com.robothy.s3.core.annotations.BucketChanged;
import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.asserionts.UploadAssertions;
import com.robothy.s3.core.exception.InvalidPartOrderException;
import com.robothy.s3.core.model.answers.CompleteMultipartUploadAns;
import com.robothy.s3.core.model.answers.PutObjectAns;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.UploadMetadata;
import com.robothy.s3.core.model.internal.UploadPartMetadata;
import com.robothy.s3.core.model.request.CompleteMultipartUploadPartOption;
import com.robothy.s3.core.model.request.PutObjectOptions;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.stream.Collectors;

/**
 * Complete a multipart upload.
 */
public interface CompleteMultipartUploadService extends LocalS3MetadataApplicable, StorageApplicable, PutObjectService {

  /**
   * Compete a multipart upload.
   *
   * @param bucket the bucket name.
   * @param key the object key.
   * @param uploadId multipart upload ID.
   * @param completeParts multipart upload parts to complete.
   * @return result of the complete multipart operation.
   */
  @BucketChanged
  default CompleteMultipartUploadAns completeMultipartUpload(String bucket, String key, String uploadId,
                                                             List<CompleteMultipartUploadPartOption> completeParts) {
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata(), bucket);
    UploadMetadata uploadMetadata = UploadAssertions.assertUploadExists(bucketMetadata, key, uploadId);

    if (completeParts.isEmpty()) {
      throw new IllegalArgumentException("You must specify at least 1 multipart upload part.");
    }

    int pre = -1;
    // Check part numbers.
    for (var partOption : completeParts) {
      if (partOption.getPartNumber() <= pre) {
        throw new InvalidPartOrderException();
      }
      pre = partOption.getPartNumber();
      UploadAssertions.assertPartNumberExists(uploadMetadata, partOption.getPartNumber());
    }

    Map<Integer, UploadPartMetadata> uploadedParts = uploadMetadata.getParts();

    List<InputStream> inputStreams = completeParts.stream().map(completePart -> uploadedParts.get(completePart.getPartNumber()))
        .map(uploadPartMetadata -> storage().getInputStream(uploadPartMetadata.getFileId()))
        .collect(Collectors.toList());

    long size = completeParts.stream().map(CompleteMultipartUploadPartOption::getPartNumber)
        .map(uploadedParts::get).map(UploadPartMetadata::getSize).reduce(0L, Long::sum);

    PutObjectAns putObjectAns;
    try(InputStream in = new SequenceInputStream(Collections.enumeration(inputStreams))) {
      PutObjectOptions putObjectOptions = PutObjectOptions.builder()
          .size(size)
          .content(in)
          .contentType(uploadMetadata.getContentType())
          .build();

      putObjectAns = putObject(bucket, key, putObjectOptions);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to concat multipart upload parts.", e);
    }

    // Cleanup
//    uploadedParts.values().forEach(part -> storage().delete(part.getFileId()));
//    Map<String, NavigableMap<String, UploadMetadata>> uploads = bucketMetadata.getUploads();
//    uploads.get(key).remove(uploadId);
//    if (uploads.get(key).isEmpty()) {
//      uploads.remove(key);
//    }

    return CompleteMultipartUploadAns.builder()
        .location("/" + bucket + "/" + key)
        .versionId(putObjectAns.getVersionId())
        .etag(putObjectAns.getEtag())
        .build();
  }

}
