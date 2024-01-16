package com.robothy.s3.rest.handler;

import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.model.answers.PutObjectAns;
import com.robothy.s3.core.model.request.PutObjectOptions;
import com.robothy.s3.core.service.ObjectService;
import com.robothy.s3.rest.assertions.RequestAssertions;
import com.robothy.s3.rest.constants.AmzHeaderNames;
import com.robothy.s3.rest.model.request.DecodedAmzRequestBody;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.RequestUtils;
import com.robothy.s3.rest.utils.ResponseUtils;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Handle <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_PutObject.html">PutObject<a/>.
 */
class PutObjectController implements HttpRequestHandler {

  private final ObjectService objectService;

  PutObjectController(ServiceFactory serviceFactory) {
    this.objectService = serviceFactory.getInstance(ObjectService.class);
  }

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    String bucketName = RequestAssertions.assertBucketNameProvided(request);
    String key = RequestAssertions.assertObjectKeyProvided(request);

    DecodedAmzRequestBody decodedBody = RequestUtils.getBody(request);

    PutObjectOptions options = PutObjectOptions.builder()
        .contentType(request.header(HttpHeaderNames.CONTENT_TYPE).orElse(null))
        .size(decodedBody.getDecodedContentLength())
        .content(decodedBody.getDecodedBody())
        .tagging(RequestUtils.extractTagging(request).orElse(null))
        .userMetadata(extractUserMetadata(request))
        .build();

    PutObjectAns ans = objectService.putObject(bucketName, key, options);
    response.status(HttpResponseStatus.OK)
        .putHeader(HttpHeaderNames.CONTENT_LENGTH.toString(), 0);

    if (Objects.nonNull(ans.getVersionId())) {
      response.putHeader(AmzHeaderNames.X_AMZ_VERSION_ID, ans.getVersionId());
    }

    ResponseUtils.addETag(response, ans.getEtag());
    ResponseUtils.addServerHeader(response);
    ResponseUtils.addDateHeader(response);
    ResponseUtils.addAmzRequestId(response);
  }

  Map<String, String> extractUserMetadata(HttpRequest request) {
    Map<String, String> userMetadata = new HashMap<>();
    request.getHeaders()
        .forEach((k, v) -> {
            if (k.toString().startsWith(AmzHeaderNames.X_AMZ_META_PREFIX)) {
              String metaName = RequestAssertions.assertUserMetadataHeaderIsValid(k.toString());
              userMetadata.put(metaName, v);
            }
        });
    return userMetadata;
  }

}
