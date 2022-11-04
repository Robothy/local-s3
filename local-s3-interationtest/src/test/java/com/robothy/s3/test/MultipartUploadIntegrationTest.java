package com.robothy.s3.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.robothy.s3.jupiter.LocalS3;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.junit.jupiter.api.Test;

public class MultipartUploadIntegrationTest {

  @Test
  @LocalS3
  void multipartUpload(AmazonS3 s3) throws IOException {

    URL url = s3.getUrl("a", "b");
    AwsClientBuilder.EndpointConfiguration endpointConfiguration =
        new AwsClientBuilder.EndpointConfiguration("http://" + url.getHost() + ":" + url.getPort(), "local");
    s3 = AmazonS3ClientBuilder.standard().withEndpointConfiguration(endpointConfiguration)
        .withClientConfiguration(new ClientConfiguration().withProxyHost("127.0.0.1")
            .withProxyPort(15555))
        .enablePathStyleAccess()
        .build();


    String bucket = "my-bucket";
    String key1 = "a.txt";
    s3.createBucket(bucket);
    ObjectMetadata objectMetadata1 = new ObjectMetadata();
    objectMetadata1.setContentType("plain/text");
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


    CompleteMultipartUploadResult completeResult = s3.completeMultipartUpload(new CompleteMultipartUploadRequest(bucket, key1, initResult.getUploadId(), List.of(
            new PartETag(1, ""),
            new PartETag(2, "")
        )));
    assertNotNull(completeResult.getVersionId());
    assertNotNull(completeResult.getLocation());


    S3Object object = s3.getObject(bucket, key1);
    assertEquals("HelloWorld", new String(object.getObjectContent().readAllBytes()));
  }

}
