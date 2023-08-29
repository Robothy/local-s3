package com.robothy.s3.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.transfer.Transfer;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.robothy.s3.jupiter.LocalS3;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TransferManagerIntegrationTest {

  static long _1KB = 1024L;

  static long _1MB = 1024L * _1KB;

  @LocalS3
  @Test
  void testWithUploadFileWithTransferManager(AmazonS3 s3) throws Exception {
    String bucketName = "my-bucket";
    s3.createBucket(bucketName);

    TransferManager transferManager = TransferManagerBuilder
        .standard()
        .withS3Client(s3)
        .withMultipartUploadThreshold(_1MB)
        .build();

    long[] sizes = new long[]{12 * _1MB};

    for (long fileSize : sizes) {
      Path path = createTempFile(fileSize);
      String objectKey = path.getFileName().toString();
      Upload upload = transferManager.upload(bucketName, objectKey, path.toFile());
      upload.waitForCompletion();
      assertEquals(Transfer.TransferState.Completed, upload.getState());
      assertTrue(s3.doesObjectExist(bucketName, objectKey));

      S3Object _256kObject = s3.getObject(bucketName, objectKey);
      assertEquals(fileSize, _256kObject.getObjectMetadata().getContentLength());
      Files.deleteIfExists(path);
    }

  }

  Path createTempFile(long size) throws Exception {
    Path tempFile = Files.createTempFile(size + "-" + "object", ".txt");
    Files.write(tempFile, new byte[(int) size]);
    return tempFile;
  }


}
