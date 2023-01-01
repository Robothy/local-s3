package com.robothy.s3.rest.handler;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.service.DeleteObjectsService;
import com.robothy.s3.core.service.ObjectService;
import com.robothy.s3.datatypes.request.DeleteObjectsRequest;
import com.robothy.s3.datatypes.response.DeleteResult;
import com.robothy.s3.rest.assertions.RequestAssertions;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.RequestUtils;
import com.robothy.s3.rest.utils.ResponseUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.io.InputStream;
import java.util.List;

class DeleteObjectsController implements HttpRequestHandler {

  private final DeleteObjectsService deleteObjectsService;

  private final XmlMapper xmlMapper;

  DeleteObjectsController(ServiceFactory serviceFactory) {
    this.deleteObjectsService = serviceFactory.getInstance(ObjectService.class);
    this.xmlMapper = serviceFactory.getInstance(XmlMapper.class);
  }

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    String bucketName = RequestAssertions.assertBucketNameProvided(request);

    try (InputStream decodedBody = RequestUtils.getBody(request).getDecodedBody()) {
      DeleteObjectsRequest deleteObjectsRequest =
          xmlMapper.readValue(decodedBody, DeleteObjectsRequest.class);
      List<Object> deletedList = this.deleteObjectsService.deleteObjects(bucketName, deleteObjectsRequest);
      DeleteResult deleteResult = new DeleteResult(deletedList);
      String xml = xmlMapper.writeValueAsString(deleteResult);
      response.status(HttpResponseStatus.OK)
          .write(xml);
      ResponseUtils.addCommonHeaders(response);
    }

  }

}
