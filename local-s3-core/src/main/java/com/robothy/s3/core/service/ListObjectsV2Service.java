package com.robothy.s3.core.service;

import com.robothy.s3.core.annotations.BucketChanged;
import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.model.answers.ListObjectsAns;
import com.robothy.s3.core.model.answers.ListObjectsV2Ans;
import com.robothy.s3.core.model.internal.BucketMetadata;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public interface ListObjectsV2Service extends ListObjectsService {

    /**
     * <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_ListObjectsV2.html">ListObjectsV2</a>
     *
     * @param bucket            the bucket to list objects.
     * @param continuationToken the token indicating where the returned results should begin.
     * @param delimiter         the delimiter for condensing common prefixes in the returned listing results.
     * @param encodingType      the encoding method for keys in the returned listing results.
     * @param fetchOwner        whether to fetch the owner of the object.
     * @param maxKeys           the maximum objects to return.
     * @param prefix            the prefix restricting what keys will be listed.
     * @param startAfter        the key indicating where the returned results should begin.
     * @return a listing of objects from the specified bucket.
     */
    @BucketChanged
    default ListObjectsV2Ans listObjectsV2(String bucket, String continuationToken,
                                           String delimiter, String encodingType,
                                           boolean fetchOwner, int maxKeys,
                                           String prefix, String startAfter) {

        BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata(), bucket);

        String marker = StringUtils.isNotBlank(continuationToken) ? continuationToken : startAfter;
        ListObjectsAns listObjectsAns = listObjects(bucket, delimiter, encodingType, marker, maxKeys, prefix);

        String nextContinuationToken = calculateNextContinuationToken(listObjectsAns.getNextMarker().orElse(null), bucketMetadata);
        ListObjectsV2Ans listObjectsV2Ans = ListObjectsV2Ans.builder()
            .continuationToken(continuationToken)
            .delimiter(listObjectsAns.getDelimiter())
            .encodingType(listObjectsAns.getEncodingType())
            .isTruncated(listObjectsAns.isTruncated())
            .keyCount(listObjectsAns.getObjects().size() + listObjectsAns.getCommonPrefixes().size())
            .maxKeys(listObjectsAns.getMaxKeys())
            .prefix(StringUtils.isBlank(prefix) ? null : prefix)
            .startAfter(StringUtils.isBlank(startAfter) || StringUtils.isNotBlank(continuationToken) ? null : startAfter)
            .objects(listObjectsAns.getObjects())
            .commonPrefixes(listObjectsAns.getCommonPrefixes())
            .nextContinuationToken(nextContinuationToken)
            .build();

        if (!fetchOwner) {
            removeOwner(listObjectsV2Ans);
        }
        return listObjectsV2Ans;
    }

    static String calculateNextContinuationToken(String nextMarker, BucketMetadata bucketMetadata) {
        if (Objects.isNull(nextMarker)) {
            return null;
        }

        return bucketMetadata.getObjectMap().floorKey(nextMarker + Character.MAX_VALUE);
    }

    static void removeOwner(ListObjectsV2Ans listObjectsV2Ans) {
        listObjectsV2Ans.getObjects().forEach(s3Object -> s3Object.setOwner(null));
    }

}
