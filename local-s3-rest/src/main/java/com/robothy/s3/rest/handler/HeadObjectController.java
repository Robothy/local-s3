package com.robothy.s3.rest.handler;

import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.model.answers.GetObjectAns;
import com.robothy.s3.core.model.request.GetObjectOptions;
import com.robothy.s3.core.service.GetObjectService;
import com.robothy.s3.core.service.ObjectService;
import com.robothy.s3.core.model.request.Range;
import com.robothy.s3.rest.assertions.RequestAssertions;
import com.robothy.s3.rest.constants.AmzHeaderNames;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.ResponseUtils;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Handle <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_HeadObject.html">HeadObject</a>
 */
class HeadObjectController implements HttpRequestHandler {

  private final GetObjectService objectService;

  HeadObjectController(ServiceFactory serviceFactory) {
    this.objectService = serviceFactory.getInstance(ObjectService.class);
  }


  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    String bucket = RequestAssertions.assertBucketNameProvided(request);
    String key = RequestAssertions.assertObjectKeyProvided(request);

    GetObjectOptions options = GetObjectOptions.builder()
        .versionId(request.parameter("versionId").orElse(null))
        .range(request.header(HttpHeaderNames.RANGE.toString()).map(Range::parse).orElse(null))
        .build();
    GetObjectAns object = objectService.headObject(bucket, key, options);

    response.putHeader(AmzHeaderNames.X_AMZ_DELETE_MARKER, object.isDeleteMarker())
        .putHeader(HttpHeaderNames.LAST_MODIFIED.toString(), ResponseUtils.toRfc1123DateTime(object.getLastModified()))
        .putHeader(AmzHeaderNames.X_AMZ_VERSION_ID, object.getVersionId());

    if (!object.isDeleteMarker()) {
      if (object.getContentRange() != null) {
        response.status(HttpResponseStatus.PARTIAL_CONTENT)
            .putHeader(HttpHeaderNames.CONTENT_RANGE.toString(), object.getContentRange());
      } else {
        response.status(HttpResponseStatus.OK);
      }

      response.putHeader(HttpHeaderNames.CONTENT_LENGTH.toString(), object.getSize())
          .putHeader(HttpHeaderNames.ETAG.toString(), object.getEtag())
          .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), object.getContentType())
          .putHeader("Accept-Ranges", "bytes");
      ResponseUtils.addETag(response, object.getEtag());
      object.getUserMetadata().forEach((k, v) -> response.putHeader(AmzHeaderNames.X_AMZ_META_PREFIX + k, v));
    } else {
      response.status(HttpResponseStatus.METHOD_NOT_ALLOWED);
    }

    ResponseUtils.addServerHeader(response);
    ResponseUtils.addDateHeader(response);
    ResponseUtils.addAmzRequestId(response);
  }

}
