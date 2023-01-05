package com.robothy.s3.core.service;

import static org.junit.jupiter.api.Assertions.*;
import com.robothy.s3.core.exception.MethodNotAllowedException;
import com.robothy.s3.core.model.answers.GetObjectTaggingAns;
import com.robothy.s3.core.model.answers.PutObjectAns;
import com.robothy.s3.core.model.internal.ObjectMetadata;
import com.robothy.s3.core.model.request.PutObjectOptions;
import java.io.ByteArrayInputStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ObjectTaggingServiceTest extends LocalS3ServiceTestBase {

  @MethodSource("localS3Services")
  @ParameterizedTest
  void testObjectTagging(BucketService bucketService, ObjectService objectService) {
    String bucketName = "my-bucket";
    bucketService.createBucket(bucketName);
    bucketService.setVersioningEnabled(bucketName, Boolean.FALSE); // suspend bucket versioning.
    String key1 = "key1";
    objectService.putObject(bucketName, key1, PutObjectOptions.builder()
        .content(new ByteArrayInputStream("Hello".getBytes()))
        .contentType("plain/text")
        .size(5)
        .build());

    String putObjectTagging1 = objectService.putObjectTagging(bucketName, key1, null, new String[][] {{"A", "a"}, {"B", "b"}});
    assertEquals(ObjectMetadata.NULL_VERSION, putObjectTagging1);

    GetObjectTaggingAns objectTagging = objectService.getObjectTagging(bucketName, key1, null);
    assertArrayEquals(new String[]{"A", "a"}, objectTagging.getTagging()[0]);
    assertArrayEquals(new String[]{"B", "b"}, objectTagging.getTagging()[1]);
    assertEquals("null", objectTagging.getVersionId());

    String deleteObjectTaggingResult1 = objectService.deleteObjectTagging(bucketName, key1, null);
    assertEquals("null", deleteObjectTaggingResult1);
    GetObjectTaggingAns objectTagging1 = objectService.getObjectTagging(bucketName, key1, null);
    assertEquals(0, objectTagging1.getTagging().length);
    assertEquals("null", objectTagging1.getVersionId());

    objectService.deleteObject(bucketName, key1);
    assertThrows(MethodNotAllowedException.class, () -> objectService.getObjectTagging(bucketName, key1, null));
    assertThrows(MethodNotAllowedException.class, () -> objectService.putObjectTagging(bucketName, key1, null, new String[0][0]));
    assertThrows(MethodNotAllowedException.class, () -> objectService.deleteObjectTagging(bucketName, key1, null));

    /*- Enabled bucket versioning -*/
    bucketService.setVersioningEnabled(bucketName, true);

    PutObjectAns putObjectAns1 = objectService.putObject(bucketName, key1, PutObjectOptions.builder()
        .content(new ByteArrayInputStream("Robothy".getBytes()))
        .size(7)
        .contentType("plain/text")
        .build());
    String putObjectTagging2 = objectService.putObjectTagging(bucketName, key1, null, new String[][] {{"k1", "v1"}});
    assertEquals(putObjectAns1.getVersionId(), putObjectTagging2);
    GetObjectTaggingAns objectTagging2 = objectService.getObjectTagging(bucketName, key1, null);
    assertEquals(putObjectAns1.getVersionId(), objectTagging2.getVersionId());
    assertArrayEquals(new String[]{"k1", "v1"}, objectTagging2.getTagging()[0]);

    GetObjectTaggingAns objectTagging3 = objectService.getObjectTagging(bucketName, key1, putObjectAns1.getVersionId());
    assertEquals(putObjectAns1.getVersionId(), objectTagging3.getVersionId());
    assertArrayEquals(new String[]{"k1", "v1"}, objectTagging3.getTagging()[0]);


    PutObjectAns putObjectAns2 = objectService.putObject(bucketName, key1, PutObjectOptions.builder()
        .content(new ByteArrayInputStream("Hello".getBytes()))
        .size(5)
        .contentType("plain/text")
        .build());
    GetObjectTaggingAns objectTagging4 = objectService.getObjectTagging(bucketName, key1, null);
    assertEquals(0, objectTagging4.getTagging().length);
    assertEquals(putObjectAns2.getVersionId(), objectTagging4.getVersionId());
  }

}