package com.robothy.s3.core.service;

import com.robothy.s3.core.model.ContinuationParameters;
import com.robothy.s3.core.model.answers.ListObjectsV2Ans;
import com.robothy.s3.core.model.request.PutObjectOptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

class ListObjectsV2ServiceTest extends LocalS3ServiceTestBase {

    @MethodSource("localS3Services")
    @ParameterizedTest
    void listObjectsV2FromEmptyBucket(BucketService bucketService, ObjectService objectService) {
        String bucket = "test-list-objects-v2";
        bucketService.createBucket(bucket);
        ListObjectsV2Ans listObjectsV2Ans = objectService.listObjectsV2(bucket, null, null, null, false, 100, null, null);
        assertNotNull(listObjectsV2Ans);
    }

    @MethodSource("localS3Services")
    @ParameterizedTest
    void listObjectsV2WithContinuationToken(BucketService bucketService, ObjectService objectService) {
        String bucket = prepareKeys(bucketService, objectService);
        ListObjectsV2Ans listObjectsV2Ans = objectService.listObjectsV2(bucket, null, null, null, false, 5, null, null);
        assertNotNull(listObjectsV2Ans);
        assertEquals(5, listObjectsV2Ans.getObjects().size());
        assertEquals(0, listObjectsV2Ans.getCommonPrefixes().size());
        assertTrue(listObjectsV2Ans.getNextContinuationToken().isEmpty());

        listObjectsV2Ans = objectService.listObjectsV2(bucket, null, null, null, false, 4, null, null);
        assertNotNull(listObjectsV2Ans);
        assertEquals(4, listObjectsV2Ans.getObjects().size());
        assertEquals(0, listObjectsV2Ans.getCommonPrefixes().size());
        assertTrue(listObjectsV2Ans.getNextContinuationToken().isPresent());
        ContinuationParameters continuationParameters = ContinuationParameters.decode(listObjectsV2Ans.getNextContinuationToken().get());
        assertNull(continuationParameters.getDelimiter());
        assertNull(continuationParameters.getEncodingType());
        assertFalse(continuationParameters.isFetchOwner());
        assertEquals(4, continuationParameters.getMaxKeys());
        assertNull(continuationParameters.getPrefix());
        assertEquals("dir3#key1", continuationParameters.getStartAfter());

        listObjectsV2Ans = objectService.listObjectsV2(bucket, listObjectsV2Ans.getNextContinuationToken().get(), null, null, false, 4, null, null);
        assertNotNull(listObjectsV2Ans);
        assertEquals(1, listObjectsV2Ans.getObjects().size());
        assertEquals(0, listObjectsV2Ans.getCommonPrefixes().size());
        assertFalse(listObjectsV2Ans.getNextContinuationToken().isPresent());
    }

    /**
     * <pre>{@code
     *
     * dir1/key1
     * dir1/key2
     * dir2/key1
     * dir3#key1
     * dir3#key2
     *
     * }</pre>
     * @return bucket
     */
    String prepareKeys(BucketService bucketService, ObjectService objectService) {
        String bucket = "test-list-objects-v2";
        bucketService.createBucket(bucket);
        objectService.putObject(bucket, "dir1/key1", PutObjectOptions.builder()
            .content(new ByteArrayInputStream("content".getBytes()))
            .build());
        objectService.putObject(bucket, "dir1/key2", PutObjectOptions.builder()
            .content(new ByteArrayInputStream("content".getBytes()))
            .build());
        objectService.putObject(bucket, "dir2/key1", PutObjectOptions.builder()
            .content(new ByteArrayInputStream("content".getBytes()))
            .build());
        objectService.putObject(bucket, "dir3#key1", PutObjectOptions.builder()
            .content(new ByteArrayInputStream("content".getBytes()))
            .build());
        objectService.putObject(bucket, "dir3#key2", PutObjectOptions.builder()
            .content(new ByteArrayInputStream("content".getBytes()))
            .build());
        return bucket;
    }

}