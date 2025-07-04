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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  /**
   * Parse the request and build CopyObjectOptions.
   *
   * @param request The HTTP request
   * @return CopyObjectOptions instance
   */
  CopyObjectOptions parseCopyOptions(HttpRequest request) {
    // Parse copy source information (bucket, key, version)
    String copySource = extractCopySourceHeader(request);
    SourceObjectInfo sourceInfo = parseCopySourcePath(copySource);

    // Parse metadata directive and user metadata
    CopyObjectOptions.MetadataDirective metadataDirective = parseMetadataDirective(request);
    Map<String, String> userMetadata = extractUserMetadata(request, metadataDirective);

    return CopyObjectOptions.builder()
        .sourceBucket(urlDecode(sourceInfo.bucket))
        .sourceKey(urlDecode(sourceInfo.key))
        .sourceVersion(urlDecode(sourceInfo.versionId))
        .metadataDirective(metadataDirective)
        .userMetadata(userMetadata)
        .build();
  }

  /**
   * Extract the x-amz-copy-source header from the request.
   *
   * @param request The HTTP request
   * @return Copy source string
   */
  private String extractCopySourceHeader(HttpRequest request) {
    return request.header(AmzHeaderNames.X_AMZ_COPY_SOURCE).orElseThrow(() ->
        new IllegalArgumentException(AmzHeaderNames.X_AMZ_COPY_SOURCE + " header is required."));
  }

  /**
   * Parse the copy source path to extract bucket, key and version.
   *
   * @param copySource The copy source string
   * @return SourceObjectInfo containing bucket, key and version
   */
  private SourceObjectInfo parseCopySourcePath(String copySource) {
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

    String srcVersionId = extractVersionId(slices);
    
    return new SourceObjectInfo(srcBucket, srcKey, srcVersionId);
  }

  /**
   * Extract version ID from query parameters if present.
   *
   * @param pathSlices Array containing path and optional query string
   * @return Version ID or null if not present
   */
  private String extractVersionId(String[] pathSlices) {
    if (pathSlices.length <= 1) {
      return null;
    }
    
    String queryParams = pathSlices[1];
    String[] pairs = queryParams.split("\\&");
    return Stream.of(pairs)
        .filter(pair -> pair.startsWith("versionId") && pair.contains("="))
        .map(pair -> pair.split("=")[1])
        .findAny()
        .orElse(null);
  }
  
  /**
   * Parse the x-amz-metadata-directive header.
   *
   * @param request The HTTP request
   * @return MetadataDirective enum value (defaults to COPY)
   */
  private CopyObjectOptions.MetadataDirective parseMetadataDirective(HttpRequest request) {
    String metadataDirectiveHeader = request.header(AmzHeaderNames.X_AMZ_METADATA_DIRECTIVE).orElse(null);
    if (metadataDirectiveHeader == null) {
      return CopyObjectOptions.MetadataDirective.COPY;
    }
    
    try {
      return CopyObjectOptions.MetadataDirective.valueOf(metadataDirectiveHeader);
    } catch (IllegalArgumentException e) {
      throw new LocalS3InvalidArgumentException(AmzHeaderNames.X_AMZ_METADATA_DIRECTIVE, 
          metadataDirectiveHeader, "Invalid metadata directive.");
    }
  }
  
  /**
   * Extract user metadata from request headers if directive is REPLACE.
   *
   * @param request The HTTP request
   * @param metadataDirective The metadata directive
   * @return Map of user metadata key-value pairs
   */
  private Map<String, String> extractUserMetadata(HttpRequest request, 
                                                  CopyObjectOptions.MetadataDirective metadataDirective) {
    if (metadataDirective != CopyObjectOptions.MetadataDirective.REPLACE) {
      return Collections.emptyMap();
    }
    
    Map<String, String> userMetadata = new HashMap<>();
    for (Map.Entry<CharSequence, String> entry : request.getHeaders().entrySet()) {
      String name = entry.getKey().toString();
      String value = entry.getValue();
      
      if (name != null && name.startsWith(AmzHeaderNames.X_AMZ_META_PREFIX)) {
        String key = name.substring(AmzHeaderNames.X_AMZ_META_PREFIX.length());
        userMetadata.put(key, value);
      }
    }
    
    return userMetadata;
  }

  private String urlDecode(String value) {
    try {
      if (Objects.isNull(value)) {
        return null;
      }

      return URLDecoder.decode(value, StandardCharsets.UTF_8.displayName());
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException(e);
    }
  }
  
  /**
   * Immutable class to hold source object information.
   */
  private static class SourceObjectInfo {
    private final String bucket;
    private final String key;
    private final String versionId;
    
    public SourceObjectInfo(String bucket, String key, String versionId) {
      this.bucket = bucket;
      this.key = key;
      this.versionId = versionId;
    }
  }
}
