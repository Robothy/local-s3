package com.robothy.s3.core.asserionts;

import com.robothy.s3.core.exception.InvalidObjectKeyException;
import com.robothy.s3.core.exception.ObjectNotExistException;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.core.model.internal.ObjectMetadata;
import org.apache.commons.lang3.StringUtils;

public class ObjectAssertions {

  public static void assertObjectKeyIsValid(String key) {
    if (StringUtils.isBlank(key)) {
      throw new InvalidObjectKeyException(key);
    }
  }

  public static ObjectMetadata assertObjectExists(BucketMetadata bucketMetadata, String key) {
    return bucketMetadata.getObjectMetadata(key).orElseThrow(() -> new ObjectNotExistException(key));
  }

}
