package com.robothy.s3.core.service;

import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.asserionts.UploadAssertions;
import com.robothy.s3.core.exception.ObjectNotExistException;
import com.robothy.s3.core.exception.UploadNotExistException;
import com.robothy.s3.core.model.answers.ListPartsAns;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.LocalS3Metadata;
import com.robothy.s3.core.model.internal.UploadMetadata;
import com.robothy.s3.core.model.internal.UploadPartMetadata;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;

/**
 * <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_ListParts.html">ListParts</a>
 **/
public interface ListPartsService extends LocalS3MetadataApplicable {

  default ListPartsAns listParts(String bucket, String key, String uploadId, Integer maxParts, Integer partNumberMarker) {
    LocalS3Metadata s3Metadata = localS3Metadata();
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(s3Metadata, bucket);
    UploadMetadata uploadMetadata;
    try{
      uploadMetadata = UploadAssertions.assertUploadExists(bucketMetadata, key, uploadId);
    } catch (ObjectNotExistException e) {
      throw new UploadNotExistException(key, uploadId);
    }

    if (Objects.isNull(partNumberMarker)) {
      partNumberMarker = 0;
    }

    if (Objects.isNull(maxParts) || maxParts > 1000) {
      maxParts = 1000;
    }

    int fromKeyInclusive = partNumberMarker + 1;
    int toKeyExclusive = fromKeyInclusive + maxParts;

    SortedMap<Integer, UploadPartMetadata> fetchedUploadPartMap = uploadMetadata.getParts().subMap(fromKeyInclusive, toKeyExclusive);
    List<ListPartsAns.Part> parts = new ArrayList<>(fetchedUploadPartMap.size());
    fetchedUploadPartMap.forEach((partNumber, partMeta) -> parts.add(ListPartsAns.Part.builder()
        .partNumber(partNumber)
        .eTag(partMeta.getEtag())
        .lastModified(partMeta.getLastModified())
        .size(partMeta.getSize())
        .build()));

    return ListPartsAns.builder()
        .bucket(bucket)
        .key(key)
        .uploadId(uploadId)
        .partNumberMarker(partNumberMarker)
        .nextPartNumberMarker(fetchedUploadPartMap.isEmpty() ? 0 : fetchedUploadPartMap.lastKey())
        .maxParts(maxParts)
        .isTruncated(!fetchedUploadPartMap.isEmpty() &&
            fetchedUploadPartMap.lastKey() < uploadMetadata.getParts().lastKey())
        .parts(parts)
        .build();
  }


}
