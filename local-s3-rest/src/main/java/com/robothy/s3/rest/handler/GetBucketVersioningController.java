package com.robothy.s3.rest.handler;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.service.BucketService;
import com.robothy.s3.datatypes.VersioningConfiguration;
import com.robothy.s3.rest.assertions.RequestAssertions;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.ResponseUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.Objects;

/**
 * Handle <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_GetBucketVersioning.html">GetBucketVersioning</a>
 */
class GetBucketVersioningController implements HttpRequestHandler {

  private final BucketService bucketService = ServiceFactory.getInstance(BucketService.class);

  private final XmlMapper xmlMapper = ServiceFactory.getInstance(XmlMapper.class);

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    String bucketName = RequestAssertions.assertBucketNameProvided(request);
    Boolean versioningEnabled = bucketService.getVersioningEnabled(bucketName);
    VersioningConfiguration.VersioningConfigurationBuilder builder = VersioningConfiguration.builder();
    if (Objects.isNull(versioningEnabled)) {
      builder.status(null);
    } else{
      builder.status(versioningEnabled ? VersioningConfiguration.Enabled : VersioningConfiguration.Suspended);
    }

    String responseBody = xmlMapper.writeValueAsString(builder.build());
    response.status(HttpResponseStatus.OK)
        .write(responseBody);
    ResponseUtils.addAmzRequestId(response);
    ResponseUtils.addDateHeader(response);
  }

}
