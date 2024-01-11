package com.robothy.s3.jupiter.extensions;

import com.robothy.s3.jupiter.LocalS3;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@LocalS3
public class S3ClientResolverTest {

  @Test
  void test(S3Client client){
    assertNotNull(client);
    CreateBucketResponse createBucketResponse = client.createBucket(b -> b.bucket("my-bucket"));
    assertNotNull(createBucketResponse);
    PutObjectResponse putObjectResponse = client.putObject(PutObjectRequest.builder()
      .bucket("my-bucket").key("a.txt").build(), RequestBody.empty());
    assertNotNull(putObjectResponse);
  }

}
