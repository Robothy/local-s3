package com.robothy.s3.core.service;

import com.robothy.s3.core.exception.LocalS3Exception;
import com.robothy.s3.core.model.answers.DeleteObjectAns;
import com.robothy.s3.datatypes.ObjectIdentifier;
import com.robothy.s3.datatypes.request.DeleteObjectsRequest;
import com.robothy.s3.datatypes.response.DeleteResult;
import com.robothy.s3.datatypes.response.S3Error;
import java.util.ArrayList;
import java.util.List;

public interface DeleteObjectsService extends DeleteObjectService {

  /**
   * Delete objects from a specified bucket.
   *
   * @param bucketName bucket name.
   * @param request    delete objects request.
   * @return delete results.
   */
  default List<Object> deleteObjects(String bucketName, DeleteObjectsRequest request) {

    List<Object> results = new ArrayList<>(request.getObjects().size());
    for (ObjectIdentifier id : request.getObjects()) {
      String key = id.getKey();
      String versionId = id.getVersionId().orElse(null);
      try {
        DeleteObjectAns deleteObjectAns = deleteObject(bucketName, key, versionId);
        if (request.isQuiet()) {
          continue;
        }

        DeleteResult.Deleted deleted = new DeleteResult.Deleted();
        deleted.setKey(key);
        deleted.setVersionId(versionId);
        if (deleteObjectAns.isDeleteMarker()) {
          deleted.setDeleteMarker(true);
          deleted.setDeleteMarkerVersionId(deleteObjectAns.getVersionId());
        }
        results.add(deleted);
      } catch (Throwable e) {
        S3Error.S3ErrorBuilder builder = S3Error.builder()
            .bucketName(bucketName)
            .message(e.getMessage())
            .key(key)
            .versionId(versionId);
        if (e instanceof LocalS3Exception) {
          builder.code(((LocalS3Exception) e).getS3ErrorCode().code());
        }
        results.add(builder.build());
      }
    }
    return results;
  }

}
