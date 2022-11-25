package com.robothy.s3.rest.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import com.robothy.netty.http.HttpRequest;
import com.robothy.s3.core.exception.InvalidArgumentException;
import com.robothy.s3.core.model.request.CopyObjectOptions;
import com.robothy.s3.rest.constants.AmzHeaderNames;
import com.robothy.s3.rest.service.ServiceFactory;
import java.util.stream.Stream;

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
      assertThrows(InvalidArgumentException.class, () -> copyObjectController.parseCopyOptions(request));
    } else {
      assertEquals(result, copyObjectController.parseCopyOptions(request));
    }
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