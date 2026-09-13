package com.robothy.s3.test;

import static org.junit.jupiter.api.Assertions.*;

import com.robothy.s3.jupiter.LocalS3;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Integration tests for S3 conditional writes: {@code If-Match} and {@code If-None-Match}
 * on PutObject and CopyObject.
 *
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_PutObject.html">PutObject</a>
 */
public class ConditionalRequestIntegrationTest {

  private static String md5(String content) {
    return DigestUtils.md5Hex(content);
  }

  private static int statusOf(Executable executable) {
    S3Exception exception = assertThrows(S3Exception.class, () -> executable.run());
    return exception.statusCode();
  }

  @FunctionalInterface
  private interface Executable {
    void run();
  }

  @Test
  @LocalS3
  void ifNoneMatchWildcardSucceedsWhenObjectAbsent(S3Client s3) {
    String bucket = "conditional-bucket-1";
    s3.createBucket(b -> b.bucket(bucket));

    s3.putObject(b -> b.bucket(bucket).key("new.txt").ifNoneMatch("*"), RequestBody.fromString("hello"));

    assertEquals("hello", s3.getObjectAsBytes(b -> b.bucket(bucket).key("new.txt")).asUtf8String());
  }

  @Test
  @LocalS3
  void ifNoneMatchWildcardFailsWhenObjectExists(S3Client s3) {
    String bucket = "conditional-bucket-2";
    s3.createBucket(b -> b.bucket(bucket));
    s3.putObject(b -> b.bucket(bucket).key("exists.txt"), RequestBody.fromString("first"));

    PutObjectRequest request = PutObjectRequest.builder()
        .bucket(bucket).key("exists.txt").ifNoneMatch("*").build();
    assertEquals(412, statusOf(() -> s3.putObject(request, RequestBody.fromString("second"))));

    // the original content must be untouched
    assertEquals("first", s3.getObjectAsBytes(b -> b.bucket(bucket).key("exists.txt")).asUtf8String());
  }

  @Test
  @LocalS3
  void ifNoneMatchExplicitEtagFailsOnMatchAndSucceedsOnMismatch(S3Client s3) {
    String bucket = "conditional-bucket-3";
    s3.createBucket(b -> b.bucket(bucket));
    s3.putObject(b -> b.bucket(bucket).key("k.txt"), RequestBody.fromString("v1"));

    // matching ETag -> 412
    PutObjectRequest matching = PutObjectRequest.builder()
        .bucket(bucket).key("k.txt").ifNoneMatch(md5("v1")).build();
    assertEquals(412, statusOf(() -> s3.putObject(matching, RequestBody.fromString("v2"))));

    // non-matching ETag -> allowed
    PutObjectRequest nonMatching = PutObjectRequest.builder()
        .bucket(bucket).key("k.txt").ifNoneMatch(md5("v0")).build();
    s3.putObject(nonMatching, RequestBody.fromString("v3"));
    assertEquals("v3", s3.getObjectAsBytes(b -> b.bucket(bucket).key("k.txt")).asUtf8String());
  }

  @Test
  @LocalS3
  void ifMatchWildcardSucceedsWhenObjectExistsAndFailsWhenAbsent(S3Client s3) {
    String bucket = "conditional-bucket-4";
    s3.createBucket(b -> b.bucket(bucket));

    PutObjectRequest absent = PutObjectRequest.builder()
        .bucket(bucket).key("absent.txt").ifMatch("*").build();
    assertEquals(412, statusOf(() -> s3.putObject(absent, RequestBody.fromString("x"))));

    s3.putObject(b -> b.bucket(bucket).key("present.txt"), RequestBody.fromString("v1"));
    s3.putObject(b -> b.bucket(bucket).key("present.txt").ifMatch("*"), RequestBody.fromString("v2"));
    assertEquals("v2", s3.getObjectAsBytes(b -> b.bucket(bucket).key("present.txt")).asUtf8String());
  }

  @Test
  @LocalS3
  void ifMatchExplicitEtagSucceedsOnMatchAndFailsOnMismatch(S3Client s3) {
    String bucket = "conditional-bucket-5";
    s3.createBucket(b -> b.bucket(bucket));
    s3.putObject(b -> b.bucket(bucket).key("k.txt"), RequestBody.fromString("v1"));

    PutObjectRequest mismatch = PutObjectRequest.builder()
        .bucket(bucket).key("k.txt").ifMatch(md5("wrong")).build();
    assertEquals(412, statusOf(() -> s3.putObject(mismatch, RequestBody.fromString("v2"))));

    PutObjectRequest match = PutObjectRequest.builder()
        .bucket(bucket).key("k.txt").ifMatch(md5("v1")).build();
    s3.putObject(match, RequestBody.fromString("v3"));
    assertEquals("v3", s3.getObjectAsBytes(b -> b.bucket(bucket).key("k.txt")).asUtf8String());
  }

  @Test
  @LocalS3
  void quotedEtagFromServerIsAcceptedVerbatim(S3Client s3) {
    String bucket = "conditional-bucket-6";
    s3.createBucket(b -> b.bucket(bucket));
    String serverEtag = s3.putObject(b -> b.bucket(bucket).key("k.txt"), RequestBody.fromString("v1")).eTag();

    // SDK returns the ETag verbatim, which is quoted by S3 compatible servers.
    String quoted = serverEtag.startsWith("\"") ? serverEtag : "\"" + serverEtag + "\"";
    PutObjectRequest matching = PutObjectRequest.builder()
        .bucket(bucket).key("k.txt").ifMatch(quoted).build();
    s3.putObject(matching, RequestBody.fromString("v2"));
    assertEquals("v2", s3.getObjectAsBytes(b -> b.bucket(bucket).key("k.txt")).asUtf8String());

    PutObjectRequest notMatching = PutObjectRequest.builder()
        .bucket(bucket).key("k.txt").ifMatch("\"" + md5("nope") + "\"").build();
    assertEquals(412, statusOf(() -> s3.putObject(notMatching, RequestBody.fromString("v3"))));
  }

  @Test
  @LocalS3
  void copyObjectHonoursConditionalHeadersOnDestination(S3Client s3) {
    String bucket = "conditional-bucket-7";
    s3.createBucket(b -> b.bucket(bucket));
    s3.putObject(b -> b.bucket(bucket).key("src.txt"), RequestBody.fromString("source"));
    s3.putObject(b -> b.bucket(bucket).key("dst.txt"), RequestBody.fromString("existing"));

    // destination exists -> If-None-Match: * fails
    CopyObjectRequest blocked = CopyObjectRequest.builder()
        .sourceBucket(bucket).sourceKey("src.txt")
        .destinationBucket(bucket).destinationKey("dst.txt")
        .ifNoneMatch("*")
        .build();
    assertEquals(412, statusOf(() -> s3.copyObject(blocked)));
    assertEquals("existing", s3.getObjectAsBytes(b -> b.bucket(bucket).key("dst.txt")).asUtf8String());

    // destination ETag matches -> If-Match succeeds
    CopyObjectRequest allowed = CopyObjectRequest.builder()
        .sourceBucket(bucket).sourceKey("src.txt")
        .destinationBucket(bucket).destinationKey("dst.txt")
        .ifMatch(md5("existing"))
        .build();
    s3.copyObject(allowed);
    assertEquals("source", s3.getObjectAsBytes(b -> b.bucket(bucket).key("dst.txt")).asUtf8String());
  }

  @Test
  @LocalS3
  void conditionalPutOnNonExistentBucketStillReports404(S3Client s3) {
    PutObjectRequest request = PutObjectRequest.builder()
        .bucket("no-such-bucket").key("k.txt").ifNoneMatch("*").build();
    assertThrows(NoSuchBucketException.class, () -> s3.putObject(request, RequestBody.fromString("x")));
  }

}
