package com.robothy.s3.core.model.internal.s3vectors;

import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import com.robothy.s3.core.exception.vectors.LocalS3VectorErrorType;
import com.robothy.s3.core.util.S3VectorsArnUtils;
import org.apache.commons.lang3.StringUtils;


public record IndexIdentifier(String bucketName, String indexName) {

  public IndexIdentifier {

    if (StringUtils.isBlank(bucketName)) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST,
          "Vector bucket name is required");
    }

    if (StringUtils.isBlank(indexName)) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST,
          "Index name is required");
    }

  }

  public static IndexIdentifier fromIndexArn(String indexArn) {
    String[] parts = S3VectorsArnUtils.extractFromIndexArn(indexArn);
    String bucketName = parts[0];
    String actualIndexName = parts[1];

    if (StringUtils.isBlank(bucketName)) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST,
          "Vector bucket name is required");
    }

    if (StringUtils.isBlank(actualIndexName)) {
      throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST,
          "Index name is required");
    }

    return new IndexIdentifier(bucketName, actualIndexName);
  }

}
