package com.robothy.s3.rest.handler;

import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.model.answers.DeleteObjectAns;
import com.robothy.s3.core.service.DeleteObjectService;
import com.robothy.s3.core.service.ObjectService;
import com.robothy.s3.rest.assertions.RequestAssertions;
import com.robothy.s3.rest.constants.AmzHeaderNames;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.ResponseUtils;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Handle <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_DeleteObject.html">DeleteObject</a>
 */
class DeleteObjectController implements HttpRequestHandler {

  private final DeleteObjectService deleteObjectService;

  DeleteObjectController(ServiceFactory serviceFactory) {
    this.deleteObjectService = serviceFactory.getInstance(ObjectService.class);
  }

  @Override
  public void handle(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
    String bucketName = RequestAssertions.assertBucketNameProvided(httpRequest);
    String key = RequestAssertions.assertObjectKeyProvided(httpRequest);
    String versionId = httpRequest.parameter("versionId").orElse(null);
    DeleteObjectAns deleteObjectAns = deleteObjectService.deleteObject(bucketName, key, versionId);
    httpResponse.status(HttpResponseStatus.NO_CONTENT)
        .putHeader(AmzHeaderNames.X_AMZ_DELETE_MARKER, deleteObjectAns.isDeleteMarker())
        .putHeader(AmzHeaderNames.X_AMZ_VERSION_ID, deleteObjectAns.getVersionId());
    ResponseUtils.addAmzRequestId(httpResponse);
    ResponseUtils.addDateHeader(httpResponse);
    ResponseUtils.addServerHeader(httpResponse);
  }


}
