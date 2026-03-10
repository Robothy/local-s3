package com.robothy.s3.core.service;

import com.robothy.s3.core.model.answers.ListObjectsV2Ans;
import com.robothy.s3.core.model.request.PutObjectOptions;
import com.robothy.s3.datatypes.response.S3Object;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.util.UUID;

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
        String bucket = prepareKeys(bucketService, objectService,
            "dir1/key1",
            "dir1/key2",
            "dir2/key1",
            "dir3#key1",
            "dir3#key2");
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

        listObjectsV2Ans = objectService.listObjectsV2(bucket, listObjectsV2Ans.getNextContinuationToken().get(), null, null, false, 4, null, null);
        assertNotNull(listObjectsV2Ans);
        assertEquals(1, listObjectsV2Ans.getObjects().size());
        assertEquals(0, listObjectsV2Ans.getCommonPrefixes().size());
        assertFalse(listObjectsV2Ans.getNextContinuationToken().isPresent());
    }

    @MethodSource("localS3Services")
    @ParameterizedTest
    void listObjectsV2WithDelimiter(BucketService bucketService, ObjectService objectService) {
        String bucket = prepareKeys(bucketService, objectService,
                "key1",
                "dir1/key1",
                "dir1/key2",
                "dir1/folder1/key1",
                "dir1/folder2/key1",
                "dir2/a/b/c/key1");
        ListObjectsV2Ans listObjectsV2Ans = objectService.listObjectsV2(bucket, null, null, null, false, 10, null, null);
        assertNotNull(listObjectsV2Ans);
        assertEquals(6, listObjectsV2Ans.getObjects().size());
        assertEquals(0, listObjectsV2Ans.getCommonPrefixes().size());

        listObjectsV2Ans = objectService.listObjectsV2(bucket, null, null, null, false, 10, "dir1", null);
        assertNotNull(listObjectsV2Ans);
        assertEquals(4, listObjectsV2Ans.getObjects().size());
        assertEquals(0, listObjectsV2Ans.getCommonPrefixes().size());

        listObjectsV2Ans = objectService.listObjectsV2(bucket, null, null, null, false, 10, "dir1/", null);
        assertNotNull(listObjectsV2Ans);
        assertEquals(4, listObjectsV2Ans.getObjects().size());
        assertEquals(0, listObjectsV2Ans.getCommonPrefixes().size());

        listObjectsV2Ans = objectService.listObjectsV2(bucket, null, "/", null, false, 10, "dir1/", null);
        assertNotNull(listObjectsV2Ans);
        assertEquals(List.of("dir1/key1", "dir1/key2"), listObjectsV2Ans.getObjects().stream().map(S3Object::getKey).toList());
        assertEquals(List.of("dir1/folder1/", "dir1/folder2/"), listObjectsV2Ans.getCommonPrefixes());

        listObjectsV2Ans = objectService.listObjectsV2(bucket, null, "/", null, false, 10, "dir1/folder1/", null);
        assertNotNull(listObjectsV2Ans);
        assertEquals(List.of("dir1/folder1/key1"), listObjectsV2Ans.getObjects().stream().map(S3Object::getKey).toList());
        assertEquals(0, listObjectsV2Ans.getCommonPrefixes().size());

        listObjectsV2Ans = objectService.listObjectsV2(bucket, null, "/", null, false, 10, "dir2/", null);
        assertNotNull(listObjectsV2Ans);
        assertEquals(0, listObjectsV2Ans.getObjects().size());
        assertEquals(List.of("dir2/a/"), listObjectsV2Ans.getCommonPrefixes());

        listObjectsV2Ans = objectService.listObjectsV2(bucket, null, "/", null, false, 10, "dir2/a", null);
        assertNotNull(listObjectsV2Ans);
        assertEquals(0, listObjectsV2Ans.getObjects().size());
        assertEquals(List.of("dir2/a/"), listObjectsV2Ans.getCommonPrefixes());

        listObjectsV2Ans = objectService.listObjectsV2(bucket, null, "/", null, false, 10, "dir2/a/", null);
        assertNotNull(listObjectsV2Ans);
        assertEquals(0, listObjectsV2Ans.getObjects().size());
        assertEquals(List.of("dir2/a/b/"), listObjectsV2Ans.getCommonPrefixes());
    }

    @MethodSource("localS3Services")
    @ParameterizedTest
    void listObjectsV2WithDelimiterPaginationPageAtFile(BucketService bucketService, ObjectService objectService) {
        String bucket = prepareKeys(bucketService, objectService,
                "key1",
                "dir1/folder1/key1",
                "dir1/folder2/key1",
                "dir1/key1",
                "dir1/key2",
                "dir1/zoo",
                "dir2/a/b/c/key1");
        ListObjectsV2Ans listObjectsV2Ans = objectService.listObjectsV2(bucket, null, "/", null, false, 3, "dir1/", null);
        assertNotNull(listObjectsV2Ans);
        assertEquals(List.of("dir1/key1"), listObjectsV2Ans.getObjects().stream().map(S3Object::getKey).toList());
        assertEquals(List.of("dir1/folder1/", "dir1/folder2/"), listObjectsV2Ans.getCommonPrefixes());
        assertTrue(listObjectsV2Ans.getNextContinuationToken().isPresent());

        listObjectsV2Ans = objectService.listObjectsV2(bucket, listObjectsV2Ans.getNextContinuationToken().get(),  "/", null, false, 10, "dir1/", null);
        assertNotNull(listObjectsV2Ans);
        assertEquals(List.of("dir1/key2", "dir1/zoo"), listObjectsV2Ans.getObjects().stream().map(S3Object::getKey).toList());
        assertEquals(0, listObjectsV2Ans.getCommonPrefixes().size());
        assertTrue(listObjectsV2Ans.getNextContinuationToken().isEmpty());
    }

    @MethodSource("localS3Services")
    @ParameterizedTest
    void listObjectsV2WithDelimiterPaginationPageAtCommonPrefix(BucketService bucketService, ObjectService objectService) {
        String bucket = prepareKeys(bucketService, objectService,
                "key1",
                "dir1/a",
                "dir1/folder1/key1",
                "dir1/folder2/key1",
                "dir1/folder2/key2",
                "dir1/folder3/key3",
                "dir1/z",
                "dir2/a/b/c/key1");
        ListObjectsV2Ans listObjectsV2Ans = objectService.listObjectsV2(bucket, null, "/", null, false, 3, "dir1/", null);
        assertNotNull(listObjectsV2Ans);
        assertEquals(List.of("dir1/a"), listObjectsV2Ans.getObjects().stream().map(S3Object::getKey).toList());
        assertEquals(List.of("dir1/folder1/", "dir1/folder2/"), listObjectsV2Ans.getCommonPrefixes());
        assertTrue(listObjectsV2Ans.getNextContinuationToken().isPresent());

        listObjectsV2Ans = objectService.listObjectsV2(bucket, listObjectsV2Ans.getNextContinuationToken().get(),  "/", null, false, 10, "dir1/", null);
        assertNotNull(listObjectsV2Ans);
        assertEquals(List.of("dir1/z"), listObjectsV2Ans.getObjects().stream().map(S3Object::getKey).toList());
        assertEquals(List.of("dir1/folder3/"), listObjectsV2Ans.getCommonPrefixes());
        assertTrue(listObjectsV2Ans.getNextContinuationToken().isEmpty());
    }

    String prepareKeys(BucketService bucketService, ObjectService objectService, String... keys) {
        String bucket = "test-list-objects-v2" + UUID.randomUUID();
        bucketService.createBucket(bucket);
        for (String key : keys) {
            objectService.putObject(bucket, key, PutObjectOptions.builder()
                .content(new ByteArrayInputStream("test".getBytes()))
                .size(4L)
                .build());
        }
        return bucket;
    }

}