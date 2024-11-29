package com.robothy.s3.rest.handler;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.exception.LocalS3InvalidArgumentException;
import com.robothy.s3.core.model.answers.CopyObjectAns;
import com.robothy.s3.core.model.request.CopyObjectOptions;
import com.robothy.s3.core.service.CopyObjectService;
import com.robothy.s3.core.service.ObjectService;
import com.robothy.s3.rest.assertions.RequestAssertions;
import com.robothy.s3.rest.constants.AmzHeaderNames;
import com.robothy.s3.rest.model.response.CopyObjectResult;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.ResponseUtils;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Handle <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_CopyObject.html">CopyObject</a>
 */
class CopyObjectController implements HttpRequestHandler {

  private final CopyObjectService objectService;

  private final XmlMapper xmlMapper;

  CopyObjectController(ServiceFactory serviceFactory) {
    this.objectService = serviceFactory.getInstance(ObjectService.class);
    this.xmlMapper = serviceFactory.getInstance(XmlMapper.class);
  }

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    String destinationBucket = RequestAssertions.assertBucketNameProvided(request);
    String destinationKey = RequestAssertions.assertObjectKeyProvided(request);
    CopyObjectOptions copyObjectOptions = parseCopyOptions(request);
    CopyObjectAns copyObjectAns = objectService.copyObject(destinationBucket, destinationKey, copyObjectOptions);
    CopyObjectResult result = CopyObjectResult.builder()
        .lastModified(Instant.ofEpochMilli(copyObjectAns.getLastModified()))
        .etag(copyObjectAns.getEtag())
        .build();

    response.status(HttpResponseStatus.OK)
        .write(xmlMapper.writeValueAsString(result))
        .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_XML)
        .putHeader(AmzHeaderNames.X_AMZ_VERSION_ID, copyObjectAns.getVersionId())
        .putHeader(AmzHeaderNames.X_AMZ_COPY_SOURCE_VERSION_ID, copyObjectAns.getSourceVersionId());
    ResponseUtils.addAmzRequestId(response);
    ResponseUtils.addDateHeader(response);
    ResponseUtils.addServerHeader(response);
  }

  CopyObjectOptions parseCopyOptions(HttpRequest request) {
    String copySource = request.header(AmzHeaderNames.X_AMZ_COPY_SOURCE).orElseThrow(() ->
        new IllegalArgumentException(AmzHeaderNames.X_AMZ_COPY_SOURCE + " header is required."));

    String[] slices = copySource.split("\\?");
    String path = slices[0];

    int delimiterIndex;
    if (-1 == (delimiterIndex = path.indexOf('/', 1)) || delimiterIndex == path.length() - 1) {
      throw new LocalS3InvalidArgumentException(AmzHeaderNames.X_AMZ_COPY_SOURCE, copySource, "Invalid copy source.");
    }

    String srcBucket = path.charAt(0) == '/' ? path.substring(1, delimiterIndex)
        : path.substring(0, delimiterIndex);
    String srcKey = path.charAt(path.length() - 1) == '/' ? path.substring(delimiterIndex + 1, path.length() - 1)
        : path.substring(delimiterIndex + 1);

    String srcVersionId = null;
    if (slices.length > 1) {
      String queryParams = slices[1];
      String[] pairs = queryParams.split("\\&");
      srcVersionId = Stream.of(pairs).filter(pair -> pair.startsWith("versionId") && pair.contains("="))
          .map(pair -> pair.split("=")[1]).findAny().orElse(null);
    }

    return CopyObjectOptions.builder()
        .sourceBucket(urlDecode(srcBucket))
        .sourceKey(urlDecode(srcKey))
        .sourceVersion(urlDecode(srcVersionId))
        .build();
  }

  private String urlDecode(String value) {
    try {
      if (Objects.isNull(value)) {
        return null;
      }

      return URLDecoder.decode(value, StandardCharsets.UTF_8.displayName());
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

}
