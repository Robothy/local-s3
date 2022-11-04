package com.robothy.s3.core.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.robothy.s3.core.model.answers.GetObjectAns;
import com.robothy.s3.core.model.answers.PutObjectAns;
import com.robothy.s3.core.model.request.GetObjectOptions;
import com.robothy.s3.core.model.request.PutObjectOptions;
import com.robothy.s3.core.service.manager.LocalS3Manager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ObjectServiceTest extends LocalS3ServiceTestBase {

  private static final List<Path> tmpDirs = new LinkedList<>();

  @AfterAll
  static void cleanUp() {
    tmpDirs.forEach(it -> {
      try {
        FileUtils.deleteDirectory(it.toFile());
      } catch (Throwable e) {
        e.printStackTrace();
      }
    });
  }


  @MethodSource("localS3Managers")
  @ParameterizedTest
  void putObject(LocalS3Manager localS3Manager) throws IOException {
    BucketService bucketService = localS3Manager.bucketService();
    ObjectService objectService = localS3Manager.objectService();

    String bucketName = "bucket";
    String key = "abc.txt";

    bucketService.createBucket(bucketName);
    PutObjectOptions.PutObjectOptionsBuilder putObjectOptionsBuilder = PutObjectOptions
        .builder()
        .contentType("plain/text")
        .content(new ByteArrayInputStream("Hello World".getBytes()));
    PutObjectOptions putAbcTxt = putObjectOptionsBuilder.build();
    PutObjectAns putAbcTxtAns = objectService.putObject(bucketName, key, putAbcTxt);
    assertNotNull(putAbcTxtAns);
    assertEquals(key, putAbcTxtAns.getKey());
    assertNotNull(putAbcTxtAns.getVersionId());

    GetObjectOptions getObjectOptions = GetObjectOptions.builder()
        .bucketName(bucketName)
        .key(key)
        .build();

    GetObjectAns getObjectAns = objectService.getObject(bucketName, key, getObjectOptions);
    assertEquals(bucketName, getObjectAns.getBucketName());
    assertEquals(key, getObjectAns.getKey());
    assertEquals(putAbcTxt.getContentType(), getObjectAns.getContentType());
    assertArrayEquals("Hello World".getBytes(), getObjectAns.getContent().readAllBytes());

    putObjectOptionsBuilder.contentType("application/json")
        .content(new ByteArrayInputStream("嗨嗨害".getBytes(StandardCharsets.UTF_8)));
    PutObjectOptions abcJson = putObjectOptionsBuilder.build();
    objectService.putObject(bucketName, key, abcJson);
    GetObjectAns getJsonContentAns = objectService.getObject(bucketName, key, getObjectOptions);
    assertEquals("application/json", getJsonContentAns.getContentType());
    assertArrayEquals("嗨嗨害".getBytes(StandardCharsets.UTF_8), getJsonContentAns.getContent().readAllBytes());
    assertEquals(getObjectAns.getVersionId(), getJsonContentAns.getVersionId());
  }

  @MethodSource("localS3Managers")
  @ParameterizedTest
  void putObjectWithVersioningEnabled(LocalS3Manager localS3Manager) {
    BucketService bucketService = localS3Manager.bucketService();
    ObjectService objectService = localS3Manager.objectService();
    String bucketName = "test-bucket";
    String key = "a.txt";
    bucketService.createBucket(bucketName);

    PutObjectAns putAns = objectService.putObject(bucketName, key, PutObjectOptions.builder()
        .contentType("plain/text")
        .content(new ByteArrayInputStream("Hello".getBytes()))
        .build());
    GetObjectAns getObjectAns = objectService.getObject(bucketName, key, GetObjectOptions.builder()
        .bucketName(bucketName)
        .key(putAns.getKey())
        .build());
    assertEquals(putAns.getVersionId(), getObjectAns.getVersionId());

    PutObjectAns putObjectAns = objectService.putObject(bucketName, key, PutObjectOptions.builder()
        .contentType("plain/text")
        .content(new ByteArrayInputStream("World".getBytes()))
        .build());
    // Versioning not enabled, the version ID is not changed.
    assertEquals(putAns.getVersionId(), putObjectAns.getVersionId());

    // Enable versioning
    bucketService.setVersioningEnabled(bucketName, true);

    // Put a new version of a.txt
    PutObjectAns putAns1 = objectService.putObject(bucketName, key, PutObjectOptions.builder()
        .contentType("application/json")
        .content(new ByteArrayInputStream("{\"name\": \"Hello\"}".getBytes()))
        .build());
    GetObjectAns getObjectAns1 = objectService.getObject(bucketName, key, GetObjectOptions.builder()
        .bucketName(bucketName)
        .key(putAns1.getKey())
        .build());
    assertEquals(putAns1.getVersionId(), getObjectAns1.getVersionId());
    // Versioning enabled, the version ID changed.
    assertNotEquals(putAns.getVersionId(), putAns1.getVersionId());
  }



}