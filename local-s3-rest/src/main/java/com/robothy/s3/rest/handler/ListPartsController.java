package com.robothy.s3.rest.handler;

import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.http.HttpResponse;
import com.robothy.s3.core.model.answers.ListPartsAns;
import com.robothy.s3.core.service.ObjectService;
import com.robothy.s3.datatypes.Owner;
import com.robothy.s3.datatypes.enums.StorageClass;
import com.robothy.s3.rest.assertions.RequestAssertions;
import com.robothy.s3.rest.model.response.ListPartsResult;
import com.robothy.s3.rest.service.ServiceFactory;
import com.robothy.s3.rest.utils.ResponseUtils;
import com.robothy.s3.rest.utils.XmlUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ListPartsController implements HttpRequestHandler {

  private static final String MAX_PARTS = "max-parts";

  private static final String PART_NUMBER_MARKER = "part-number-marker";

  private final ObjectService objectService;

  public ListPartsController(ServiceFactory serviceFactory) {
    this.objectService = serviceFactory.getInstance(ObjectService.class);
  }

  @Override
  public void handle(HttpRequest request, HttpResponse response) throws Exception {
    String bucketName = RequestAssertions.assertBucketNameProvided(request);
    String objectKey = RequestAssertions.assertObjectKeyProvided(request);
    String uploadId = RequestAssertions.assertUploadIdIsProvided(request);
    Integer maxParts = RequestAssertions.assertIntegerParameterOrNull(request, MAX_PARTS);
    Integer partNumberMarker = RequestAssertions.assertIntegerParameterOrNull(request, PART_NUMBER_MARKER);
    ListPartsAns ans = this.objectService.listParts(bucketName, objectKey, uploadId, maxParts, partNumberMarker);

    List<ListPartsResult.Part> parts = new ArrayList<>(ans.getParts().size());
    for (ListPartsAns.Part part : ans.getParts()) {
      ListPartsResult.Part p = ListPartsResult.Part.builder()
          .partNumber(part.getPartNumber())
          .lastModified(Instant.ofEpochSecond(part.getLastModified()))
          .size(part.getSize())
          .etag(part.getETag())
          .build();
      parts.add(p);
    }

    ListPartsResult listPartsResult = ListPartsResult.builder()
        .bucket(bucketName)
        .key(objectKey)
        .uploadId(uploadId)
        .isTruncated(ans.isTruncated())
        .maxParts(ans.getMaxParts())
        .nextPartNumberMarker(ans.getNextPartNumberMarker())
        .partNumberMarker(ans.getPartNumberMarker())
        .initiator(Owner.DEFAULT_OWNER)
        .owner(Owner.DEFAULT_OWNER)
        .storageClass(StorageClass.STANDARD)
        .parts(parts)
        .build();
    response.status(HttpResponseStatus.OK)
        .write(XmlUtils.toXml(listPartsResult));
    ResponseUtils.addCommonHeaders(response);
  }

}
