package com.robothy.s3.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
import com.amazonaws.services.s3.model.CopyPartRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ListPartsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.PartListing;
import com.amazonaws.services.s3.model.PartSummary;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.robothy.s3.jupiter.LocalS3;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ObjectVersion;
import software.amazon.awssdk.services.s3.model.Tag;
import software.amazon.awssdk.services.s3.model.Tagging;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

public class MultipartUploadIntegrationTest {

  @Test
  @LocalS3
  void multipartUpload(AmazonS3 s3) throws IOException {

    URL url = s3.getUrl("a", "b");
    AwsClientBuilder.EndpointConfiguration endpointConfiguration =
        new AwsClientBuilder.EndpointConfiguration("http://" + url.getHost() + ":" + url.getPort(), "local");

    String bucket = "my-bucket";
    String key1 = "a.txt";
    s3.createBucket(bucket);
    ObjectMetadata objectMetadata1 = new ObjectMetadata();
    objectMetadata1.setContentType("plain/text");
    objectMetadata1.addUserMetadata("x-filename", "a.txt");
    objectMetadata1.addUserMetadata("x-other", "other-value");
    InitiateMultipartUploadResult initResult =
        s3.initiateMultipartUpload(new InitiateMultipartUploadRequest(bucket, key1, objectMetadata1));
    assertNotNull(initResult.getUploadId());


    UploadPartRequest part1 = new UploadPartRequest()
        .withBucketName(bucket)
        .withKey(key1)
        .withUploadId(initResult.getUploadId())
        .withPartNumber(1)
        .withInputStream(new ByteArrayInputStream("Hello".getBytes()))
        .withPartSize(5L)
        .withLastPart(true);

    UploadPartRequest part2 = new UploadPartRequest()
        .withBucketName(bucket)
        .withKey(key1)
        .withUploadId(initResult.getUploadId())
        .withPartNumber(2)
        .withInputStream(new ByteArrayInputStream("World".getBytes()))
        .withPartSize(5L)
        .withLastPart(true);

    //part2.setInputStream(new ByteArrayInputStream("World".getBytes()));

    UploadPartResult uploadPartResult1 = s3.uploadPart(part1);
    assertEquals(1, uploadPartResult1.getPartNumber());

    UploadPartResult uploadPartResult2 = s3.uploadPart(part2);
    assertEquals(2, uploadPartResult2.getPartNumber());


    CompleteMultipartUploadResult completeResult =
        s3.completeMultipartUpload(new CompleteMultipartUploadRequest(bucket, key1, initResult.getUploadId(), List.of(
            new PartETag(1, ""),
            new PartETag(2, "")
        )));
    assertNotNull(completeResult.getVersionId());
    assertNotNull(completeResult.getLocation());


    S3Object object = s3.getObject(bucket, key1);
    assertEquals("HelloWorld", new String(object.getObjectContent().readAllBytes()));
    assertEquals("plain/text", object.getObjectMetadata().getContentType());
    Map<String, String> userMetadata = object.getObjectMetadata().getUserMetadata();
    assertEquals(2, userMetadata.size());
    assertEquals("a.txt", userMetadata.get("x-filename"));
    assertEquals("other-value", userMetadata.get("x-other"));

    assertThrows(AmazonS3Exception.class, () -> {
      s3.copyPart(new CopyPartRequest().withUploadId(initResult.getUploadId())
          .withPartNumber(1)
          .withSourceBucketName(bucket)
          .withSourceKey(key1)
          .withDestinationBucketName(bucket)
          .withDestinationKey(key1));
    });
  }

  @Test
  @LocalS3
  void testAbortMultipartUpload(AmazonS3 s3) {
    String bucketName = "my-bucket";
    s3.createBucket(bucketName);
    InitiateMultipartUploadResult initiateMultipartUploadResult =
        s3.initiateMultipartUpload(new InitiateMultipartUploadRequest(bucketName, "a.txt"));
    assertDoesNotThrow(() -> s3.abortMultipartUpload(
        new AbortMultipartUploadRequest(bucketName, "a.txt", initiateMultipartUploadResult.getUploadId())));
    // abort multiple times
    assertDoesNotThrow(() -> s3.abortMultipartUpload(
        new AbortMultipartUploadRequest(bucketName, "a.txt", initiateMultipartUploadResult.getUploadId())));

    // upload to an aborted multipart upload.
    assertThrows(AmazonS3Exception.class, () -> s3.uploadPart(new UploadPartRequest()
        .withBucketName(bucketName)
        .withKey("a.txt")
        .withUploadId(initiateMultipartUploadResult.getUploadId())
        .withPartNumber(1)
        .withInputStream(new ByteArrayInputStream("Hello".getBytes()))
        .withPartSize(5L)
        .withLastPart(true)));


    InitiateMultipartUploadResult initiateMultipartUploadResult1 =
        s3.initiateMultipartUpload(new InitiateMultipartUploadRequest(bucketName, "b.txt"));
    UploadPartRequest uploadPartRequest1 = new UploadPartRequest()
        .withBucketName(bucketName)
        .withKey("b.txt")
        .withUploadId(initiateMultipartUploadResult1.getUploadId())
        .withPartNumber(1)
        .withInputStream(new ByteArrayInputStream("Hello".getBytes()))
        .withPartSize(5L);
    s3.uploadPart(uploadPartRequest1);
    s3.abortMultipartUpload(new AbortMultipartUploadRequest(bucketName, "b.txt", initiateMultipartUploadResult1.getUploadId()));
    assertThrows(AmazonS3Exception.class, () ->
        s3.listParts(new ListPartsRequest(bucketName, "b.txt", initiateMultipartUploadResult1.getUploadId())));
  }


  @LocalS3
  @Test
  void testListParts(AmazonS3 s3) {
    String bucketName = "it-list-parts";
    s3.createBucket(bucketName);

    // list parts of a non-exist multipart upload.
    assertThrows(AmazonS3Exception.class, () ->
        s3.listParts(new ListPartsRequest(bucketName, "a.txt", "non-exist-upload-id")));


    InitiateMultipartUploadResult initiateMultipartUploadResult =
        s3.initiateMultipartUpload(new InitiateMultipartUploadRequest(bucketName, "a.txt"));
    UploadPartRequest uploadPartRequest1 = new UploadPartRequest()
        .withBucketName(bucketName)
        .withKey("a.txt")
        .withUploadId(initiateMultipartUploadResult.getUploadId())
        .withPartNumber(1)
        .withInputStream(new ByteArrayInputStream("Hello".getBytes()))
        .withPartSize(5L)
        .withLastPart(false);
    UploadPartRequest uploadPartRequest2 = new UploadPartRequest()
        .withBucketName(bucketName)
        .withKey("a.txt")
        .withUploadId(initiateMultipartUploadResult.getUploadId())
        .withPartNumber(2)
        .withInputStream(new ByteArrayInputStream("World".getBytes()))
        .withPartSize(5L)
        .withLastPart(false);
    UploadPartRequest uploadPartRequest3 = new UploadPartRequest()
        .withBucketName(bucketName)
        .withKey("a.txt")
        .withUploadId(initiateMultipartUploadResult.getUploadId())
        .withPartNumber(3)
        .withInputStream(new ByteArrayInputStream("World".getBytes()))
        .withPartSize(5L)
        .withLastPart(false);

    UploadPartResult uploadPartResult1 = s3.uploadPart(uploadPartRequest1);
    UploadPartResult uploadPartResult2 = s3.uploadPart(uploadPartRequest2);
    UploadPartResult uploadPartResult3 = s3.uploadPart(uploadPartRequest3);

    // give: 3 parts
    // list parts with set max-parts and part-number-marker.
    PartListing partListing =
        s3.listParts(new ListPartsRequest(bucketName, "a.txt", initiateMultipartUploadResult.getUploadId()));
    assertEquals(3, partListing.getParts().size());
    assertEquals(0, partListing.getPartNumberMarker());
    assertEquals(1000, partListing.getMaxParts());
    assertEquals(3, partListing.getNextPartNumberMarker());
    assertFalse(partListing.isTruncated());

    PartSummary part1 = partListing.getParts().get(0);
    assertEquals(1, part1.getPartNumber());
    assertEquals(uploadPartResult1.getETag(), part1.getETag());
    assertEquals(uploadPartRequest1.getPartSize(), part1.getSize());

    PartSummary part2 = partListing.getParts().get(1);
    assertEquals(2, part2.getPartNumber());
    assertEquals(uploadPartResult2.getETag(), part2.getETag());
    assertEquals(uploadPartRequest2.getPartSize(), part2.getSize());

    PartSummary part3 = partListing.getParts().get(2);
    assertEquals(3, part3.getPartNumber());
    assertEquals(uploadPartResult3.getETag(), part3.getETag());
    assertEquals(uploadPartRequest3.getPartSize(), part3.getSize());


    // given: 3 parts
    // list parts with max-parts and part-number-marker.
    partListing = s3.listParts(new ListPartsRequest(bucketName, "a.txt", initiateMultipartUploadResult.getUploadId())
        .withMaxParts(2)
        .withPartNumberMarker(0));
    assertEquals(2, partListing.getParts().size());
    assertEquals(0, partListing.getPartNumberMarker());
    assertEquals(2, partListing.getMaxParts());
    assertEquals(2, partListing.getNextPartNumberMarker());
    assertTrue(partListing.isTruncated());

    // given: completed multipart upload
    List<PartETag> partETags = List.of(new PartETag(1, uploadPartResult1.getETag()),
        new PartETag(2, uploadPartResult2.getETag()),
        new PartETag(3, uploadPartResult3.getETag()));
    s3.completeMultipartUpload(new CompleteMultipartUploadRequest(bucketName, "a.txt", initiateMultipartUploadResult.getUploadId(), partETags));
    assertThrows(AmazonS3Exception.class, () ->
        s3.listParts(new ListPartsRequest(bucketName, "a.txt", initiateMultipartUploadResult.getUploadId())));
  }

  @Test
  @LocalS3
  void testCreateMultipartUploadsWithTagging(S3Client s3) throws Exception {
    s3.createBucket(builder -> builder.bucket("my-bucket"));
    Tag tag1 = Tag.builder().key("k1").value("v1").build();
    Tag tag2 = Tag.builder().key("k2").value("v2").build();
    CreateMultipartUploadResponse multipartUpload = s3.createMultipartUpload(builder -> builder.bucket("my-bucket").key("a.txt")
        .tagging(Tagging.builder().tagSet(tag1, tag2).build()));
    UploadPartResponse part1 = s3.uploadPart(b -> b.bucket("my-bucket")
            .uploadId(multipartUpload.uploadId()).key("a.txt").partNumber(1), RequestBody.fromString("Hello"));
    UploadPartResponse part2 = s3.uploadPart(b -> b.bucket("my-bucket").uploadId(multipartUpload.uploadId()).key("a.txt").partNumber(2),
            RequestBody.fromString("World"));

    CompletedPart completedPart1 = CompletedPart.builder().partNumber(1).eTag(part1.eTag()).build();
    CompletedPart completedPart2 = CompletedPart.builder().partNumber(2).eTag(part2.eTag()).build();
    s3.completeMultipartUpload(b -> b.bucket("my-bucket").key("a.txt").multipartUpload(upload -> upload.parts(
        completedPart1, completedPart2)).uploadId(multipartUpload.uploadId()));

    ResponseInputStream<GetObjectResponse> completedObject =
        s3.getObject(b -> b.bucket("my-bucket").key("a.txt"));
    assertEquals("HelloWorld", new String(completedObject.readAllBytes()));
    Integer tagCount = completedObject.response().tagCount();
    assertEquals(2, tagCount);

    List<Tag> tags = s3.getObjectTagging(b -> b.bucket("my-bucket").key("a.txt")).tagSet();
    assertEquals(2, tags.size());
    assertTrue(tags.contains(tag1));
    assertTrue(tags.contains(tag2));
  }

  @Test
  @LocalS3
  void testMultipartUploadWithMultiVersions(S3Client s3Client) throws Exception {
    s3Client.createBucket(builder -> builder.bucket("my-bucket"));
    s3Client.putBucketVersioning(builder -> builder.bucket("my-bucket").versioningConfiguration(c -> c.status("Enabled")));
    CreateMultipartUploadResponse multipartUploadV1 = s3Client.createMultipartUpload(builder -> builder.bucket("my-bucket").key("a.txt"));
    UploadPartResponse part1V1 = s3Client.uploadPart(b -> b.bucket("my-bucket")
            .uploadId(multipartUploadV1.uploadId()).key("a.txt").partNumber(1), RequestBody.fromString("v1"));
    UploadPartResponse part2V1 = s3Client.uploadPart(b -> b.bucket("my-bucket").uploadId(multipartUploadV1.uploadId()).key("a.txt").partNumber(2),
            RequestBody .fromString("v1"));
    CompleteMultipartUploadResponse multipartUploadResponseV1 = s3Client.completeMultipartUpload(b -> b
        .bucket("my-bucket")
        .key("a.txt")
        .multipartUpload(upload -> upload.parts(CompletedPart.builder().partNumber(1).eTag(part1V1.eTag()).build(),
            CompletedPart.builder().partNumber(2).eTag(part2V1.eTag()).build()))
        .uploadId(multipartUploadV1.uploadId()));


    CreateMultipartUploadResponse multipartUploadV2 =
        s3Client.createMultipartUpload(builder -> builder.bucket("my-bucket").key("a.txt"));
    UploadPartResponse part1V2 = s3Client.uploadPart(b -> b.bucket("my-bucket")
            .uploadId(multipartUploadV2.uploadId()).key("a.txt").partNumber(1), RequestBody.fromString("v2"));
    UploadPartResponse part2V2 = s3Client.uploadPart(b -> b.bucket("my-bucket").uploadId(multipartUploadV2.uploadId()).key("a.txt").partNumber(2),
            RequestBody.fromString("v2"));
    CompleteMultipartUploadResponse multipartUploadResponseV2 = s3Client.completeMultipartUpload(b -> b
        .bucket("my-bucket")
        .key("a.txt")
        .multipartUpload(upload -> upload.parts(CompletedPart.builder().partNumber(1).eTag(part1V2.eTag()).build(),
            CompletedPart.builder().partNumber(2).eTag(part2V2.eTag()).build()))
        .uploadId(multipartUploadV2.uploadId()));

    List<ObjectVersion> versions = s3Client.listObjectVersions(builder -> builder.bucket("my-bucket").prefix("a.txt")).versions();
    assertEquals(2, versions.size());
    assertEquals(multipartUploadResponseV1.versionId(), versions.get(1).versionId());
    assertEquals(multipartUploadResponseV2.versionId(), versions.get(0).versionId());
    ResponseInputStream<GetObjectResponse> objectV1 =
        s3Client.getObject(builder -> builder.bucket("my-bucket").key("a.txt").versionId(multipartUploadResponseV1.versionId()));
    assertEquals("v1v1", new String(objectV1.readAllBytes()));
    ResponseInputStream<GetObjectResponse> objectV2 =
        s3Client.getObject(builder -> builder.bucket("my-bucket").key("a.txt").versionId(multipartUploadResponseV2.versionId()));
    assertEquals("v2v2", new String(objectV2.readAllBytes()));
  }

}
