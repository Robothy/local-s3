package com.robothy.s3.core.service;

import com.robothy.s3.core.annotations.BucketChanged;
import com.robothy.s3.core.asserionts.BucketAssertions;
import com.robothy.s3.core.model.internal.BucketMetadata;
import com.robothy.s3.datatypes.AccessControlPolicy;
import com.robothy.s3.datatypes.Owner;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

/**
 * Bucket access control service.
 */
public interface BucketAclService extends LocalS3MetadataApplicable {

  /**
   * Put ACL to the specified bucket.
   *
   * @param bucketName bucket that associate with the ACL.
   * @param acl new ACL.
   */
  @BucketChanged
  default void putBucketAcl(String bucketName, AccessControlPolicy acl) {
    BucketAssertions.assertBucketNameIsValid(bucketName);
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata(), bucketName);
    bucketMetadata.setAcl(acl);
  }

  /**
   * Get bucket access control.
   *
   * @param bucketName the bucket associate with the ACL.
   * @return the ACL of the bucket. If the ACL or owner is null,
   * then set the default owner 'LocalS3' to the ACL and return.
   */
  default AccessControlPolicy getBucketAcl(String bucketName) {
    BucketAssertions.assertBucketNameIsValid(bucketName);
    BucketMetadata bucketMetadata = BucketAssertions.assertBucketExists(localS3Metadata(), bucketName);

    Owner defaultOwner = new Owner("LocalS3", "001");
    Optional<AccessControlPolicy> aclOpt = bucketMetadata.getAcl();
    if (aclOpt.isEmpty()) {
      return AccessControlPolicy.builder()
          .owner(defaultOwner)
          .grants(Collections.emptyList())
          .build();
    } else {
      AccessControlPolicy acl = aclOpt.get();
      if (Objects.isNull(acl.getOwner())) {
        acl.setOwner(defaultOwner);
      }

      if (Objects.isNull(acl.getGrants())) {
        acl.setGrants(Collections.emptyList());
      }

      return acl;
    }
  }

}
