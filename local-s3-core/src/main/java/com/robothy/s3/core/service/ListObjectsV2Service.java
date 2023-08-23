package com.robothy.s3.core.service;

import com.robothy.s3.core.model.answers.ListObjectsV2Ans;

public interface ListObjectsV2Service {

  default ListObjectsV2Ans listObjectsV2(String bucket, String continuationToken,
                                         Character delimiter, String encodingType,
                                         boolean fetchOwner, int maxKeys,
                                         String prefix, String startAfter) {
    return null;
  }

}
