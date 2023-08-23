package com.robothy.s3.core.service;

import com.robothy.s3.core.annotations.BucketChanged;
import com.robothy.s3.core.model.ContinuationParameters;
import com.robothy.s3.core.model.answers.ListObjectsAns;
import com.robothy.s3.core.model.answers.ListObjectsV2Ans;
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
                                           Character delimiter, String encodingType,
                                           boolean fetchOwner, int maxKeys,
                                           String prefix, String startAfter) {
        if (StringUtils.isNotBlank(continuationToken)) {
            ContinuationParameters params = ContinuationParameters.decode(continuationToken);
            delimiter = params.getDelimiter();
            encodingType = params.getEncodingType();
            fetchOwner = params.isFetchOwner();
            maxKeys = params.getMaxKeys();
            prefix = params.getPrefix();
            startAfter = params.getStartAfter();
        }

        ListObjectsAns listObjectsAns = listObjects(bucket, delimiter, encodingType, startAfter, maxKeys, prefix);
        return ListObjectsV2Ans.builder()
            .objects(listObjectsAns.getObjects())
            .commonPrefixes(listObjectsAns.getCommonPrefixes())
            .nextContinuationToken(generateNextContinuationToken(delimiter, encodingType, fetchOwner, maxKeys, prefix, listObjectsAns))
            .build();
    }

    static String generateNextContinuationToken(Character delimiter, String encodingType, boolean fetchOwner, int maxKeys, String prefix, ListObjectsAns ans) {
        if (ans.getNextMarker().isEmpty()) {
            return null;
        }

        return ContinuationParameters.builder()
            .delimiter(delimiter)
            .encodingType(encodingType)
            .fetchOwner(fetchOwner)
            .maxKeys(maxKeys)
            .prefix(prefix)
            .startAfter(ans.getNextMarker().get())
            .build()
            .encode();
    }

}
