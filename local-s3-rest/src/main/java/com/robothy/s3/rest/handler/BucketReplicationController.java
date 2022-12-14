package com.robothy.s3.rest.handler;

import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.service.BucketReplicationService;
import com.robothy.s3.core.service.BucketService;
import com.robothy.s3.rest.assertions.RequestAssertions;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.ResponseUtils;
import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class BucketReplicationController {

  private final BucketReplicationService replicationService;

  BucketReplicationController(ServiceFactory serviceFactory) {
    this.replicationService = serviceFactory.getInstance(BucketService.class);
  }

  /**
   * <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_PutBucketReplication.html">PutBucketReplication</a>
   */
  public void put(HttpRequest request, HttpResponse response) throws Exception {
    String bucketName = RequestAssertions.assertBucketNameProvided(request);
    try(InputStream in = new ByteBufInputStream(request.getBody())) {
      String replication = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      replicationService.putBucketReplication(bucketName, replication);
    }

    ResponseUtils.addCommonHeaders(response)
        .status(HttpResponseStatus.OK);
  }

  /**
   * <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_GetBucketReplication.html">GetBucketReplication</a>
   */
  public void get(HttpRequest request, HttpResponse response) throws Exception {
    String bucketName = RequestAssertions.assertBucketNameProvided(request);
    String bucketReplication = this.replicationService.getBucketReplication(bucketName);
    ResponseUtils.addCommonHeaders(response)
        .status(HttpResponseStatus.OK)
        .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_XML)
        .write(bucketReplication);
  }

  /**
   * <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_DeleteBucketReplication.html">DeleteBucketReplication</a>
   */
  public void delete(HttpRequest request, HttpResponse response) throws Exception {
    String bucketName = RequestAssertions.assertBucketNameProvided(request);
    this.replicationService.deleteBucketReplication(bucketName);
    ResponseUtils.addCommonHeaders(response)
        .status(HttpResponseStatus.NO_CONTENT);
  }


}
