package com.robothy.s3.rest.handler;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.service.BucketService;
import com.robothy.s3.datatypes.Owner;
import com.robothy.s3.rest.model.response.ListAllMyBucketsResult;
import com.robothy.s3.rest.model.response.S3Bucket;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.ResponseUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_ListBuckets.html">ListBuckets</a>
 */
class ListBucketsController implements HttpRequestHandler {

  private final BucketService bucketService;

  private final XmlMapper xmlMapper;

  ListBucketsController(ServiceFactory factory) {
    this.bucketService = factory.getInstance(BucketService.class);
    this.xmlMapper = factory.getInstance(XmlMapper.class);
  }

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    List<S3Bucket> buckets = bucketService.listBuckets()
        .stream().map(bucket -> new S3Bucket(bucket.getName(), Instant.ofEpochMilli(bucket.getCreationDate())))
        .collect(Collectors.toList());
    ListAllMyBucketsResult result = new ListAllMyBucketsResult(buckets, Owner.DEFAULT_OWNER);
    String body = xmlMapper.writeValueAsString(result);
    response.status(HttpResponseStatus.OK)
        .write(body);
    ResponseUtils.addCommonHeaders(response);
  }
}
