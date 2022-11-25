package com.robothy.s3.rest.handler;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.model.answers.CompleteMultipartUploadAns;
import com.robothy.s3.core.model.request.CompleteMultipartUploadPartOption;
import com.robothy.s3.core.service.CompleteMultipartUploadService;
import com.robothy.s3.core.service.ObjectService;
import com.robothy.s3.rest.assertions.RequestAssertions;
import com.robothy.s3.rest.constants.AmzHeaderNames;
import com.robothy.s3.rest.model.request.CompleteMultipartUpload;
import com.robothy.s3.rest.model.response.CompleteMultipartUploadResult;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.ResponseUtils;
import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handle <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_CompleteMultipartUpload.html">CompleteMultipartUpload</a>
 */
class CompleteMultipartUploadController implements HttpRequestHandler {

  private final CompleteMultipartUploadService uploadService;

  private final XmlMapper xmlMapper;

  CompleteMultipartUploadController(ServiceFactory serviceFactory) {
    this.uploadService = serviceFactory.getInstance(ObjectService.class);
    this.xmlMapper = serviceFactory.getInstance(XmlMapper.class);
  }

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    String bucket = RequestAssertions.assertBucketNameProvided(request);
    String key = RequestAssertions.assertObjectKeyProvided(request);
    String uploadId = RequestAssertions.assertUploadIdIsProvided(request);

    CompleteMultipartUploadAns completeMultipartUploadAns;
    try(InputStream in = new ByteBufInputStream(request.getBody())) {
      CompleteMultipartUpload completeMultipartUpload = xmlMapper.readValue(in, CompleteMultipartUpload.class);
      List<CompleteMultipartUploadPartOption> parts = completeMultipartUpload.getParts().stream().map(part -> CompleteMultipartUploadPartOption.builder()
                  .etag(part.getEtag())
                  .partNumber(part.getPartNumber())
                  .build())
              .collect(Collectors.toList());
      completeMultipartUploadAns = uploadService.completeMultipartUpload(bucket, key, uploadId, parts);
    }

    CompleteMultipartUploadResult result = CompleteMultipartUploadResult.builder()
        .bucket(bucket)
        .key(key)
        .etag(completeMultipartUploadAns.getEtag())
        .location(completeMultipartUploadAns.getLocation())
        .build();
    response.status(HttpResponseStatus.OK)
        .write(xmlMapper.writeValueAsString(result))
        .putHeader(AmzHeaderNames.X_AMZ_VERSION_ID, completeMultipartUploadAns.getVersionId());

    ResponseUtils.addDateHeader(response);
    ResponseUtils.addAmzRequestId(response);
    ResponseUtils.addServerHeader(response);
  }

}
