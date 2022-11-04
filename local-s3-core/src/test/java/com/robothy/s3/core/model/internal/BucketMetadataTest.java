package com.robothy.s3.core.model.internal;

import static org.junit.jupiter.api.Assertions.*;
import com.robothy.s3.core.exception.InvalidObjectKeyException;
import com.robothy.s3.core.util.JsonUtils;
import com.robothy.s3.datatypes.AccessControlPolicy;
import com.robothy.s3.datatypes.Grant;
import com.robothy.s3.datatypes.Grantee;
import com.robothy.s3.datatypes.Owner;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class BucketMetadataTest {

  @Test
  void addObjectMetadata() {
    BucketMetadata bucketMetadata = new BucketMetadata();
    assertThrows(InvalidObjectKeyException.class, () -> bucketMetadata.addObjectMetadata(new ObjectMetadata()));

    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setKey("abc.txt");
    bucketMetadata.addObjectMetadata(objectMetadata);
    Optional<ObjectMetadata> optionalObjectMetadata = bucketMetadata.getObjectMetadata("abc.txt");
    assertTrue(optionalObjectMetadata.isPresent());
    assertTrue(bucketMetadata.getObjectMap().containsKey("abc.txt"));

    optionalObjectMetadata = bucketMetadata.getObjectMetadata("not exists");
    assertTrue(optionalObjectMetadata.isEmpty());
  }

  @Test
  void serialize() {

    BucketMetadata bucketMetadata = new BucketMetadata();
    bucketMetadata.setBucketName("bucket");
    bucketMetadata.setCreationDate(System.currentTimeMillis());
    bucketMetadata.setTagging(List.of(Map.of("A", "a"), Map.of("B", "b")));

    Grantee grantee = new Grantee();
    grantee.setType("AA");
    grantee.setUri("http://localhost");
    Grant grant = new Grant();
    grant.setGrantee(grantee);
    grant.setPermission("Read");
    AccessControlPolicy acl = AccessControlPolicy.builder()
        .owner(new Owner("Alice", "101"))
        .grants(List.of(grant))
        .build();
    bucketMetadata.setAcl(acl);
    bucketMetadata.setPolicy("Policy JSON text");

    VersionedObjectMetadata versionedObj1 = new VersionedObjectMetadata();
    versionedObj1.setFileId(111L);
    versionedObj1.setContentType("application/json");
    versionedObj1.setModificationDate(1L);;
    versionedObj1.setCreationDate(2L);
    ObjectMetadata obj1 = new ObjectMetadata("a.txt", "12", versionedObj1);
    ObjectMetadata obj2 = new ObjectMetadata("b.json", "11", versionedObj1);
    bucketMetadata.addObjectMetadata(obj1);
    bucketMetadata.addObjectMetadata(obj2);

    UploadMetadata uploadMetadata = new UploadMetadata();
    uploadMetadata.setCreateDate(1000);
    UploadPartMetadata part1 = new UploadPartMetadata();
    UploadPartMetadata part2 = new UploadPartMetadata();
    uploadMetadata.getParts().put(1, part1);
    uploadMetadata.getParts().put(2, part2);
    part2.setEtag("123");
    part2.setLastModified(102);
    part2.setFileId(1L);
    part2.setSize(10);

    String json = JsonUtils.toJson(bucketMetadata);
    BucketMetadata deserialized = JsonUtils.fromJson(json, BucketMetadata.class);
    assertEquals(bucketMetadata, deserialized);
  }

}