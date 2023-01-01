package com.robothy.s3.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.robothy.s3.core.exception.S3ErrorCode;
import com.robothy.s3.core.model.internal.ObjectMetadata;
import com.robothy.s3.datatypes.ObjectIdentifier;
import com.robothy.s3.datatypes.request.DeleteObjectsRequest;
import com.robothy.s3.datatypes.response.DeleteResult;
import com.robothy.s3.datatypes.response.S3Error;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class DeleteObjectsServiceTest extends LocalS3ServiceTestBase {

  @MethodSource("localS3Services")
  @ParameterizedTest
  void testDeleteObjects(BucketService bucketService, ObjectService objectService) {
    String bucketName = "my-bucket";
    bucketService.createBucket(bucketName);

    List<ObjectIdentifier> objectsToDelete = List.of(
        new ObjectIdentifier("a.txt", "123"),
        new ObjectIdentifier("b.txt", null)
    );
    DeleteObjectsRequest request1 = new DeleteObjectsRequest(objectsToDelete, false);

    List<Object> results = objectService.deleteObjects(bucketName, request1);

    assertInstanceOf(S3Error.class, results.get(0));
    S3Error s3Error = (S3Error) results.get(0);
    assertEquals("a.txt", s3Error.getKey());
    assertEquals("123", s3Error.getVersionId());
    assertEquals(S3ErrorCode.NoSuchKey.code(), s3Error.getCode());

    assertInstanceOf(DeleteResult.Deleted.class, results.get(1));
    DeleteResult.Deleted deleted = (DeleteResult.Deleted) results.get(1);
    assertTrue(deleted.isDeleteMarker());
    assertEquals(ObjectMetadata.NULL_VERSION, deleted.getDeleteMarkerVersionId());

    DeleteObjectsRequest request2 = new DeleteObjectsRequest(objectsToDelete, true);
    List<Object> results2 = objectService.deleteObjects(bucketName, request2);
    assertEquals(1, results2.size());
    assertInstanceOf(S3Error.class, results2.get(0));
  }

}