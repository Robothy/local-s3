package com.robothy.s3.rest.handler;

import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.service.BucketEncryptionService;
import com.robothy.s3.core.service.BucketService;
import com.robothy.s3.rest.assertions.RequestAssertions;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.ResponseUtils;
import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.io.InputStream;


class BucketEncryptionController {

  private final BucketEncryptionService encryptionService;

  BucketEncryptionController(ServiceFactory serviceFactory) {
    this.encryptionService = serviceFactory.getInstance(BucketService.class);
  }

  /**
   * <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_PutBucketEncryption.html">PutBucketEncryption</a>
   */
  void put(HttpRequest request, HttpResponse response) throws Exception {
    String bucketName = RequestAssertions.assertBucketNameProvided(request);
    try(InputStream in = new ByteBufInputStream(request.getBody())) {
      this.encryptionService.putBucketEncryption(bucketName, new String(in.readAllBytes()));
    }
    ResponseUtils.addCommonHeaders(response)
        .status(HttpResponseStatus.OK);
  }

  /**
   * <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_GetBucketEncryption.html">GetBucketEncryption</a>
   */
  void get(HttpRequest request, HttpResponse response) {
    String bucketName = RequestAssertions.assertBucketNameProvided(request);
    String encryption = this.encryptionService.getBucketEncryption(bucketName);
    ResponseUtils.addCommonHeaders(response)
        .write(encryption)
        .status(HttpResponseStatus.OK);
  }

  /**
   * <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_DeleteBucketEncryption.html">DeleteBucketEncryption</a>
   */
  void delete(HttpRequest request, HttpResponse response) {
    String bucketName = RequestAssertions.assertBucketNameProvided(request);
    this.encryptionService.deleteBucketEncryption(bucketName);
    ResponseUtils.addCommonHeaders(response)
        .status(HttpResponseStatus.NO_CONTENT);
  }

}
