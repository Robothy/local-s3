package com.robothy.s3.core.service;

import static org.junit.jupiter.api.Assertions.*;
import com.robothy.s3.core.model.Bucket;
import com.robothy.s3.core.model.answers.DeleteObjectAns;
import com.robothy.s3.core.model.answers.ListObjectVersionsAns;
import com.robothy.s3.core.model.answers.PutObjectAns;
import com.robothy.s3.core.model.request.PutObjectOptions;
import com.robothy.s3.datatypes.response.DeleteMarkerEntry;
import com.robothy.s3.datatypes.response.ObjectVersion;
import java.io.ByteArrayInputStream;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ListObjectVersionsServiceTest extends LocalS3ServiceTestBase {

  @ParameterizedTest
  @MethodSource("localS3Services")
  void listObjectVersions(BucketService bucketService, ObjectService objectService) {
    String bucket = "my-bucket";
    bucketService.createBucket(bucket);
    bucketService.setVersioningEnabled(bucket, true);

    String key1 = "dir1/key1";
    String key2 = "dir2/key2";

    ByteArrayInputStream data = new ByteArrayInputStream("Robothy".getBytes());
    PutObjectOptions putObjectOptions = PutObjectOptions.builder()
        .contentType("plain/text")
        .content(data)
        .size(7)
        .build();
    PutObjectAns putObjectAns1 = objectService.putObject(bucket, key1, putObjectOptions); // key1 version 1
    data.reset();
    PutObjectAns putObjectAns2 = objectService.putObject(bucket, key1, putObjectOptions); // key1 version 2
    DeleteObjectAns deleteObjectAns = objectService.deleteObject(bucket, key1); // key1 version 3

    data.reset();
    PutObjectAns putObjectAns3 = objectService.putObject(bucket, key2, putObjectOptions); // key2 version 1

    data.reset();
    PutObjectAns putObjectAns4 = objectService.putObject(bucket, key2, putObjectOptions); // key2 version 2

    /*-- List all versions. --*/

    // List the 1st batch
    ListObjectVersionsAns listObjectVersionsAns = objectService.listObjectVersions(bucket, null, null, 2, null, null);
    assertEquals(2, listObjectVersionsAns.getVersions().size());
    assertEquals(0, listObjectVersionsAns.getCommonPrefixes().size());
    assertInstanceOf(DeleteMarkerEntry.class, listObjectVersionsAns.getVersions().get(0));
    assertInstanceOf(ObjectVersion.class, listObjectVersionsAns.getVersions().get(1));

    assertEquals(deleteObjectAns.getVersionId(), ((DeleteMarkerEntry)listObjectVersionsAns.getVersions().get(0)).getVersionId());
    ObjectVersion key1Version2 = (ObjectVersion) listObjectVersionsAns.getVersions().get(1);
    assertEquals(putObjectAns2.getVersionId(), key1Version2.getVersionId());
    assertEquals(DigestUtils.md5Hex("Robothy"), key1Version2.getEtag());
    assertTrue(listObjectVersionsAns.getNextKeyMarker().isPresent());
    assertEquals(key1, listObjectVersionsAns.getNextKeyMarker().get());
    assertTrue(listObjectVersionsAns.getNextVersionIdMarker().isPresent());
    assertEquals(putObjectAns2.getVersionId(), listObjectVersionsAns.getNextVersionIdMarker().get());

    // List the 2nd batch.
    ListObjectVersionsAns listObjectVersionsAns1 = objectService.listObjectVersions(bucket, null,
        listObjectVersionsAns.getNextKeyMarker().get(), 2, null, listObjectVersionsAns.getNextVersionIdMarker().get());
    assertEquals(2, listObjectVersionsAns1.getVersions().size());
    assertEquals(0, listObjectVersionsAns1.getCommonPrefixes().size());
    assertInstanceOf(ObjectVersion.class, listObjectVersionsAns1.getVersions().get(0));
    assertInstanceOf(ObjectVersion.class, listObjectVersionsAns1.getVersions().get(1));
    assertEquals(putObjectAns1.getVersionId(), ((ObjectVersion)listObjectVersionsAns1.getVersions().get(0)).getVersionId());
    assertEquals(putObjectAns4.getVersionId(), ((ObjectVersion)listObjectVersionsAns1.getVersions().get(1)).getVersionId());
    assertTrue(listObjectVersionsAns1.getNextKeyMarker().isPresent());
    assertEquals(key2, listObjectVersionsAns1.getNextKeyMarker().get());
    assertTrue(listObjectVersionsAns1.getNextVersionIdMarker().isPresent());
    assertEquals(putObjectAns4.getVersionId(), listObjectVersionsAns1.getNextVersionIdMarker().get());

    // List the last batch
    ListObjectVersionsAns listObjectVersionsAns2 = objectService.listObjectVersions(bucket, null,
        listObjectVersionsAns1.getNextKeyMarker().get(), 2, null, listObjectVersionsAns1.getNextVersionIdMarker().get());
    assertEquals(1, listObjectVersionsAns2.getVersions().size());
    assertEquals(0, listObjectVersionsAns2.getCommonPrefixes().size());
    assertInstanceOf(ObjectVersion.class, listObjectVersionsAns2.getVersions().get(0));
    assertEquals(putObjectAns3.getVersionId(), ((ObjectVersion) listObjectVersionsAns2.getVersions().get(0)).getVersionId());
    assertTrue(listObjectVersionsAns2.getNextKeyMarker().isEmpty());
    assertTrue(listObjectVersionsAns2.getNextVersionIdMarker().isEmpty());


    /*------- List with prefix -------*/
    ListObjectVersionsAns listObjectVersionsAns3 = objectService.listObjectVersions(bucket, null, null, 2, "dir1", null);
    assertEquals(2, listObjectVersionsAns3.getVersions().size());
    assertEquals(0, listObjectVersionsAns3.getCommonPrefixes().size());
    assertInstanceOf(DeleteMarkerEntry.class, listObjectVersionsAns3.getVersions().get(0));
    assertEquals(deleteObjectAns.getVersionId(), ((DeleteMarkerEntry)listObjectVersionsAns3.getVersions().get(0)).getVersionId());
    assertInstanceOf(ObjectVersion.class, listObjectVersionsAns3.getVersions().get(1));
    assertEquals(putObjectAns2.getVersionId(), ((ObjectVersion) listObjectVersionsAns3.getVersions().get(1)).getVersionId());
    assertTrue(listObjectVersionsAns3.getNextKeyMarker().isPresent());
    assertEquals(key1, listObjectVersionsAns3.getNextKeyMarker().get());
    assertTrue(listObjectVersionsAns3.getNextVersionIdMarker().isPresent());
    assertEquals(putObjectAns2.getVersionId(), listObjectVersionsAns3.getNextVersionIdMarker().get());

    ListObjectVersionsAns listObjectVersionsAns4 = objectService.listObjectVersions(bucket, null, listObjectVersionsAns3.getNextKeyMarker().get(),
        2, "dir1", listObjectVersionsAns3.getNextVersionIdMarker().get());
    assertEquals(1, listObjectVersionsAns4.getVersions().size());
    assertEquals(0, listObjectVersionsAns4.getCommonPrefixes().size());
    assertInstanceOf(ObjectVersion.class, listObjectVersionsAns4.getVersions().get(0));
    assertEquals(putObjectAns1.getVersionId(), ((ObjectVersion) listObjectVersionsAns4.getVersions().get(0)).getVersionId());
    assertTrue(listObjectVersionsAns4.getNextKeyMarker().isEmpty());
    assertTrue(listObjectVersionsAns4.getNextVersionIdMarker().isEmpty());

    /*-- List versions with delimiter --*/
    ListObjectVersionsAns listObjectVersionsAns5 = objectService.listObjectVersions(bucket, '/', null, 1, null, null);
    assertEquals(0, listObjectVersionsAns5.getVersions().size());
    assertEquals(1, listObjectVersionsAns5.getCommonPrefixes().size());
    assertEquals("dir1/", listObjectVersionsAns5.getCommonPrefixes().get(0));
    assertTrue(listObjectVersionsAns5.getNextKeyMarker().isPresent());
    assertEquals(key1, listObjectVersionsAns5.getNextKeyMarker().get());
    assertTrue(listObjectVersionsAns5.getNextVersionIdMarker().isEmpty());
  }

}