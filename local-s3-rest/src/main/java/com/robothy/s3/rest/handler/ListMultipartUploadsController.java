package com.robothy.s3.rest.handler;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.model.answers.ListMultipartUploadsAns;
import com.robothy.s3.core.service.ObjectService;
import com.robothy.s3.datatypes.Owner;
import com.robothy.s3.rest.assertions.RequestAssertions;
import com.robothy.s3.rest.model.response.CommonPrefix;
import com.robothy.s3.rest.model.response.ListMultipartUploadsResult;
import com.robothy.s3.rest.model.response.ListMultipartUploadsResult.Initiator;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.ResponseUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.time.Instant;
import java.util.stream.Collectors;

public class ListMultipartUploadsController implements HttpRequestHandler {

  private final ObjectService objectService;

  private final XmlMapper xmlMapper;

  public ListMultipartUploadsController(ServiceFactory serviceFactory) {
    this.objectService = serviceFactory.getInstance(ObjectService.class);
    this.xmlMapper = serviceFactory.getInstance(XmlMapper.class);
  }
  
  @Override
  public void handle(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
    String bucketName = RequestAssertions.assertBucketNameProvided(httpRequest);
    String delimiter = RequestAssertions.assertDelimiterIsValid(httpRequest).orElse(null);
    String encodingType = RequestAssertions.assertEncodingTypeIsValid(httpRequest).orElse(null);
    String keyMarker = httpRequest.parameter("key-marker").orElse(null);
    String prefix = httpRequest.parameter("prefix").orElse(null);
    String uploadIdMarker = httpRequest.parameter("upload-id-marker").orElse(null);
    int maxUploads = httpRequest.parameter("max-uploads").map(Integer::parseInt).orElse(1000);

    ListMultipartUploadsAns answer = objectService.listMultipartUploads(bucketName, delimiter, encodingType,
        keyMarker, maxUploads, prefix, uploadIdMarker);

    ListMultipartUploadsResult result = ListMultipartUploadsResult.builder()
        .bucket(bucketName)
        .keyMarker(answer.getKeyMarker())
        .uploadIdMarker(answer.getUploadIdMarker())
        .nextKeyMarker(answer.getNextKeyMarker())
        .nextUploadIdMarker(answer.getNextUploadIdMarker())
        .delimiter(answer.getDelimiter())
        .maxUploads(answer.getMaxUploads())
        .isTruncated(answer.isTruncated())
        .commonPrefixes(answer.getCommonPrefixes().stream()
            .map(CommonPrefix::new)
            .collect(Collectors.toList()))
        .uploads(answer.getUploads().stream()
            .map(upload -> ListMultipartUploadsResult.Upload.builder()
                .key(upload.getKey())
                .uploadId(upload.getUploadId())
                .initiated(Instant.ofEpochMilli(upload.getInitiated()).toString())
                .storageClass("STANDARD")
                .owner(Owner.DEFAULT_OWNER)
                .initiator(Initiator.SYSTEM)
                .build())
            .collect(Collectors.toList()))
        .build();

    httpResponse.status(HttpResponseStatus.OK);
    httpResponse.write(xmlMapper.writeValueAsString(result));
    ResponseUtils.addCommonHeaders(httpResponse);
  }

}
