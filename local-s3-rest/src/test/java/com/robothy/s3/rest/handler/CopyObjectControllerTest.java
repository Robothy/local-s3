package com.robothy.s3.rest.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import com.robothy.netty.http.HttpRequest;
import com.robothy.s3.core.exception.LocalS3InvalidArgumentException;
import com.robothy.s3.core.model.request.CopyObjectOptions;
import com.robothy.s3.rest.constants.AmzHeaderNames;
import com.robothy.s3.rest.service.ServiceFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

class CopyObjectControllerTest {

  @MethodSource("copySources")
  @ParameterizedTest
  void parseCopyOptions(String copySource, CopyObjectOptions result) {
    ServiceFactory serviceFactory = Mockito.mock(ServiceFactory.class);
    CopyObjectController copyObjectController = new CopyObjectController(serviceFactory);
    HttpRequest request = HttpRequest.builder().build();
    request.getHeaders().put(AmzHeaderNames.X_AMZ_COPY_SOURCE, copySource);
    if (result == null) {
      assertThrows(LocalS3InvalidArgumentException.class, () -> copyObjectController.parseCopyOptions(request));
    } else {
      assertEquals(result, copyObjectController.parseCopyOptions(request));
    }
  }

  @Test
  void testParseMetadataDirectiveAndUserMetadata() {
    ServiceFactory serviceFactory = Mockito.mock(ServiceFactory.class);
    CopyObjectController copyObjectController = new CopyObjectController(serviceFactory);

    // Test REPLACE directive with user metadata
    HttpRequest request = HttpRequest.builder().build();
    request.getHeaders().put(AmzHeaderNames.X_AMZ_COPY_SOURCE, "/my-bucket/a.txt");
    request.getHeaders().put(AmzHeaderNames.X_AMZ_METADATA_DIRECTIVE, "REPLACE");
    request.getHeaders().put(AmzHeaderNames.X_AMZ_META_PREFIX + "key1", "value1");
    request.getHeaders().put(AmzHeaderNames.X_AMZ_META_PREFIX + "key2", "value2");

    Map<String, String> expectedMetadata = new HashMap<>();
    expectedMetadata.put("key1", "value1");
    expectedMetadata.put("key2", "value2");

    CopyObjectOptions options = copyObjectController.parseCopyOptions(request);
    assertEquals(CopyObjectOptions.MetadataDirective.REPLACE, options.getMetadataDirective());
    assertEquals(expectedMetadata, options.getUserMetadata());

    // Test COPY directive (no user metadata should be parsed)
    request = HttpRequest.builder().build();
    request.getHeaders().put(AmzHeaderNames.X_AMZ_COPY_SOURCE, "/my-bucket/a.txt");
    request.getHeaders().put(AmzHeaderNames.X_AMZ_METADATA_DIRECTIVE, "COPY");
    request.getHeaders().put(AmzHeaderNames.X_AMZ_META_PREFIX + "key3", "value3");

    options = copyObjectController.parseCopyOptions(request);
    assertEquals(CopyObjectOptions.MetadataDirective.COPY, options.getMetadataDirective());
    assertEquals(Collections.emptyMap(), options.getUserMetadata());

    // Test default directive (should be COPY)
    request = HttpRequest.builder().build();
    request.getHeaders().put(AmzHeaderNames.X_AMZ_COPY_SOURCE, "/my-bucket/a.txt");
    options = copyObjectController.parseCopyOptions(request);
    assertEquals(CopyObjectOptions.MetadataDirective.COPY, options.getMetadataDirective());
  }

  @Test
  void testInvalidMetadataDirective() {
    ServiceFactory serviceFactory = Mockito.mock(ServiceFactory.class);
    CopyObjectController copyObjectController = new CopyObjectController(serviceFactory);

    HttpRequest request = HttpRequest.builder().build();
    request.getHeaders().put(AmzHeaderNames.X_AMZ_COPY_SOURCE, "/my-bucket/a.txt");
    request.getHeaders().put(AmzHeaderNames.X_AMZ_METADATA_DIRECTIVE, "INVALID");

    assertThrows(LocalS3InvalidArgumentException.class, () -> copyObjectController.parseCopyOptions(request));
  }

  static Stream<Arguments> copySources() {

    CopyObjectOptions parsedOptionsWithVersion = CopyObjectOptions.builder()
        .sourceBucket("my-bucket").sourceKey("a.txt").sourceVersion("666").build();

    CopyObjectOptions parsedOptionsWithoutVersion = CopyObjectOptions.builder()
        .sourceBucket("my-bucket").sourceKey("a.txt").build();

    return Stream.of(
      arguments("/my-bucket/a.txt?versionId=666", parsedOptionsWithVersion),
      arguments("my-bucket/a.txt?versionId=666", parsedOptionsWithVersion),
      arguments("my-bucket/a.txt/?versionId=666", parsedOptionsWithVersion),
      arguments("my-bucket/a.txt/?versionId", parsedOptionsWithoutVersion),
      arguments("my-bucket/a.txt/", parsedOptionsWithoutVersion),
      arguments("/my-bucket/a.txt/", parsedOptionsWithoutVersion),
      arguments("a.txt", null),
      arguments("/a.txt/", null),
      arguments("/a.txt", null),
      arguments("a.txt/", null)
    );
  }


}