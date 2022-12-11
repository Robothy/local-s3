package com.robothy.s3.rest.handler;

import com.robothy.netty.router.Route;
import com.robothy.netty.router.Router;
import com.robothy.s3.core.exception.LocalS3Exception;
import com.robothy.s3.rest.constants.AmzHeaderNames;
import com.robothy.s3.rest.service.ServiceFactory;
import io.netty.handler.codec.http.HttpMethod;
import java.util.Objects;

public class LocalS3RouterFactory {

  /**
   * Create a new LocalS3Router instance.
   */
  public static Router create(ServiceFactory serviceFactory) {
    Objects.requireNonNull(serviceFactory);
    BucketPolicyController bucketPolicy = new BucketPolicyController(serviceFactory);
    BucketReplicationController bucketReplicationController = new BucketReplicationController(serviceFactory);
    BucketEncryptionController bucketEncryptionController = new BucketEncryptionController(serviceFactory);

    Route CopyObject = Route.builder()
        .method(HttpMethod.PUT)
        .path("/{bucket}/{*key}")
        .headerMatcher(headers -> headers.containsKey(AmzHeaderNames.X_AMZ_COPY_SOURCE))
        .handler(new CopyObjectController(serviceFactory))
        .build();

    Route CreateBucket = Route.builder()
        .method(HttpMethod.PUT)
        .path("/{bucket}")
        .handler(new CreateBucketController(serviceFactory))
        .build();

    Route PutBucketReplication = Route.builder().method(HttpMethod.PUT)
        .path("/{bucket}")
        .paramMatcher(params -> params.containsKey("replication"))
        .handler(bucketReplicationController::put)
        .build();

    Route PutBucketReplication_ = Route.builder().method(HttpMethod.PUT)
        .path("/{bucket}/")
        .paramMatcher(params -> params.containsKey("replication"))
        .handler(bucketReplicationController::put)
        .build();

    Route PutBucketEncryption = Route.builder().method(HttpMethod.PUT)
        .path("/{bucket}")
        .paramMatcher(params -> params.containsKey("encryption"))
        .handler(bucketEncryptionController::put)
        .build();

    Route PutBucketEncryption_ = Route.builder().method(HttpMethod.PUT)
        .path("/{bucket}/")
        .paramMatcher(params -> params.containsKey("encryption"))
        .handler(bucketEncryptionController::put)
        .build();

    Route CreateMultipartUpload = Route.builder()
        .method(HttpMethod.POST)
        .path("/{bucket}/{*key}")
        .paramMatcher(params -> params.containsKey("uploads"))
        .handler(new CreateMultipartUploadController(serviceFactory))
        .build();

    Route CompleteMultipartUpload = Route.builder()
        .method(HttpMethod.POST)
        .path("/{bucket}/{*key}")
        .handler(new CompleteMultipartUploadController(serviceFactory))
        .build();

    Route DeleteBucket = Route.builder()
        .method(HttpMethod.DELETE)
        .path("/{bucket}")
        .handler(new DeleteBucketController(serviceFactory))
        .build();

    Route DeleteBucketEncryption = Route.builder()
        .method(HttpMethod.DELETE)
        .path("/{bucket}")
        .paramMatcher(params -> params.containsKey("encryption"))
        .handler(bucketEncryptionController::delete)
        .build();

    Route DeleteBucketEncryption_ = Route.builder()
        .method(HttpMethod.DELETE)
        .path("/{bucket}/")
        .paramMatcher(params -> params.containsKey("encryption"))
        .handler(bucketEncryptionController::delete)
        .build();

    Route DeleteBucketPolicy = Route.builder()
        .method(HttpMethod.DELETE)
        .path("/{bucket}")
        .paramMatcher(params -> params.containsKey("policy"))
        .handler(bucketPolicy::delete)
        .build();

    Route DeleteBucketPolicy_ = Route.builder()
        .method(HttpMethod.DELETE)
        .path("/{bucket}/")
        .paramMatcher(params -> params.containsKey("policy"))
        .handler(bucketPolicy::delete)
        .build();

    Route DeleteBucketReplication = Route.builder()
        .method(HttpMethod.DELETE)
        .path("/{bucket}/")
        .paramMatcher(params -> params.containsKey("replication"))
        .handler(bucketReplicationController::delete)
        .build();

    Route DeleteBucketReplication_ = Route.builder()
        .method(HttpMethod.DELETE)
        .path("/{bucket}/")
        .paramMatcher(params -> params.containsKey("replication"))
        .handler(bucketReplicationController::delete)
        .build();

    Route DeleteBucketTagging = Route.builder()
        .method(HttpMethod.DELETE)
        .path("/{bucket}")
        .paramMatcher(params -> params.containsKey("tagging"))
        .handler(new DeleteBucketTaggingController(serviceFactory))
        .build();

    Route DeleteBucketTagging_ = Route.builder()
        .method(HttpMethod.DELETE)
        .path("/{bucket}/")
        .paramMatcher(params -> params.containsKey("tagging"))
        .handler(new DeleteBucketTaggingController(serviceFactory))
        .build();

    Route DeleteObject = Route.builder()
        .method(HttpMethod.DELETE)
        .path("/{bucket}/{*key}")
        .handler(new DeleteObjectController(serviceFactory))
        .build();

    Route GetBucket = Route.builder()
        .method(HttpMethod.GET)
        .path("/v20180820/bucket/{bucket}")
        .handler(new GetBucketController(serviceFactory))
        .build();

    Route GetBucketAcl = Route.builder()
        .method(HttpMethod.GET)
        .path("/{bucket}")
        .paramMatcher(params -> params.containsKey("acl"))
        .handler(new GetBucketAclController(serviceFactory))
        .build();

    Route GetBucketAcl_ = Route.builder()
        .method(HttpMethod.GET)
        .path("/{bucket}/")
        .paramMatcher(params -> params.containsKey("acl"))
        .handler(new GetBucketAclController(serviceFactory))
        .build();

    Route GetBucketReplication = Route.builder()
        .method(HttpMethod.GET)
        .path("/{bucket}")
        .paramMatcher(params -> params.containsKey("replication"))
        .handler(bucketReplicationController::get)
        .build();

    Route GetBucketReplication_ = Route.builder()
        .method(HttpMethod.GET)
        .path("/{bucket}/")
        .paramMatcher(params -> params.containsKey("replication"))
        .handler(bucketReplicationController::get)
        .build();

    Route GetBucketEncryption = Route.builder()
        .method(HttpMethod.GET)
        .path("/{bucket}")
        .paramMatcher(params -> params.containsKey("encryption"))
        .handler(bucketEncryptionController::get)
        .build();

    Route GetBucketEncryption_ = Route.builder()
        .method(HttpMethod.GET)
        .path("/{bucket}/")
        .paramMatcher(params -> params.containsKey("encryption"))
        .handler(bucketEncryptionController::get)
        .build();

    Route GetBucketPolicy = Route.builder()
        .method(HttpMethod.GET)
        .path("/{bucket}")
        .paramMatcher(params -> params.containsKey("policy"))
        .handler(bucketPolicy::get)
        .build();

    Route GetBucketPolicy_ = Route.builder()
        .method(HttpMethod.GET)
        .path("/{bucket}/")
        .paramMatcher(params -> params.containsKey("policy"))
        .handler(bucketPolicy::get)
        .build();

    Route GetBucketVersioning = Route.builder()
        .method(HttpMethod.GET)
        .path("/{bucket}")
        .paramMatcher(params -> params.containsKey("versioning"))
        .handler(new GetBucketVersioningController(serviceFactory))
        .build();

    Route GetBucketVersioning_ = Route.builder()
        .method(HttpMethod.GET)
        .path("/{bucket}/")
        .paramMatcher(params -> params.containsKey("versioning"))
        .handler(new GetBucketVersioningController(serviceFactory))
        .build();

    Route GetBucketTagging = Route.builder()
        .method(HttpMethod.GET)
        .path("/{bucket}")
        .paramMatcher(params -> params.containsKey("tagging"))
        .handler(new GetBucketTaggingController(serviceFactory))
        .build();

    Route GetBucketTagging_ = Route.builder()
        .method(HttpMethod.GET)
        .path("/{bucket}/")
        .paramMatcher(params -> params.containsKey("tagging"))
        .handler(new GetBucketTaggingController(serviceFactory))
        .build();

    Route GetObject = Route.builder()
        .method(HttpMethod.GET)
        .path("/{bucket}/{*key}")
        .handler(new GetObjectController(serviceFactory))
        .build();

    Route HeadBucket = Route.builder()
        .method(HttpMethod.HEAD)
        .path("/{bucket}")
        .handler(new HeadBucketController(serviceFactory))
        .build();

    Route HeadObject = Route.builder()
        .method(HttpMethod.HEAD)
        .path("/{bucket}/{*key}")
        .handler(new HeadObjectController(serviceFactory))
        .build();

    Route ListObjects = Route.builder()
        .method(HttpMethod.GET)
        .path("/{bucket}")
        .handler(new ListObjectsController(serviceFactory))
        .build();

    Route ListObjects_ = Route.builder()
        .method(HttpMethod.GET)
        .path("/{bucket}/")
        .handler(new ListObjectsController(serviceFactory))
        .build();

    Route ListObjectVersions = Route.builder()
        .method(HttpMethod.GET)
        .path("/{bucket}")
        .paramMatcher(params -> params.containsKey("versions"))
        .handler(new ListObjectVersionsController(serviceFactory))
        .build();

    Route ListObjectVersions_ = Route.builder()
        .method(HttpMethod.GET)
        .path("/{bucket}/")
        .paramMatcher(params -> params.containsKey("versions"))
        .handler(new ListObjectVersionsController(serviceFactory))
        .build();

    Route PutBucketAcl = Route.builder()
        .method(HttpMethod.PUT)
        .path("/{bucket}")
        .paramMatcher(params -> params.containsKey("acl"))
        .handler(new PutBucketAclController(serviceFactory))
        .build();

    Route PutBucketAcl_ = Route.builder()
        .method(HttpMethod.PUT)
        .path("/{bucket}/")
        .paramMatcher(params -> params.containsKey("acl"))
        .handler(new PutBucketAclController(serviceFactory))
        .build();

    Route PutBucketPolicy = Route.builder()
        .method(HttpMethod.PUT)
        .path("/{bucket}")
        .paramMatcher(params -> params.containsKey("policy"))
        .handler(bucketPolicy::put)
        .build();

    Route PutBucketPolicy_ = Route.builder()
        .method(HttpMethod.PUT)
        .path("/{bucket}/")
        .paramMatcher(params -> params.containsKey("policy"))
        .handler(bucketPolicy::put)
        .build();

    Route PutBucketVersioning = Route.builder()
        .method(HttpMethod.PUT)
        .path("/{bucket}")
        .paramMatcher(params -> params.containsKey("versioning"))
        .handler(new PutBucketVersioningController(serviceFactory))
        .build();

    Route PutBucketVersioning_ = Route.builder()
        .method(HttpMethod.PUT)
        .path("/{bucket}/")
        .paramMatcher(params -> params.containsKey("versioning"))
        .handler(new PutBucketVersioningController(serviceFactory))
        .build();

    Route PutBucketTagging = Route.builder()
        .method(HttpMethod.PUT)
        .path("/{bucket}")
        .paramMatcher(params -> params.containsKey("tagging"))
        .handler(new PutBucketTaggingController(serviceFactory))
        .build();

    Route PutBucketTagging_ = Route.builder()
        .method(HttpMethod.PUT)
        .path("/{bucket}/")
        .paramMatcher(params -> params.containsKey("tagging"))
        .handler(new PutBucketTaggingController(serviceFactory))
        .build();

    Route PutObject = Route.builder()
        .method(HttpMethod.PUT)
        .path("/{bucket}/{*key}")
        .handler(new PutObjectController(serviceFactory))
        .build();

    Route UploadPart = Route.builder()
        .method(HttpMethod.PUT)
        .path("/{bucket}/{*key}")
        .paramMatcher(params -> params.containsKey("uploadId") && params.containsKey("partNumber"))
        .handler(new UploadPartController(serviceFactory))
        .build();

    return Router.router()
        .route(CopyObject)
        .route(CreateBucket)
        .route(CreateMultipartUpload)
        .route(CompleteMultipartUpload)
        .route(DeleteBucket)
        .route(DeleteBucketEncryption_)
        .route(DeleteBucketEncryption)
        .route(DeleteBucketPolicy)
        .route(DeleteBucketPolicy_)
        .route(DeleteBucketReplication_)
        .route(DeleteBucketReplication)
        .route(DeleteBucketTagging)
        .route(DeleteBucketTagging_)
        .route(DeleteObject)
        .route(GetBucket)
        .route(GetObject)
        .route(GetBucketAcl)
        .route(GetBucketAcl_)
        .route(GetBucketEncryption)
        .route(GetBucketEncryption_)
        .route(GetBucketPolicy)
        .route(GetBucketPolicy_)
        .route(GetBucketReplication)
        .route(GetBucketReplication_)
        .route(GetBucketVersioning)
        .route(GetBucketVersioning_)
        .route(GetBucketTagging)
        .route(GetBucketTagging_)
        .route(HeadBucket)
        .route(HeadObject)
        .route(ListObjects)
        .route(ListObjects_)
        .route(ListObjectVersions)
        .route(ListObjectVersions_)
        .route(PutObject)
        .route(PutBucketAcl)
        .route(PutBucketAcl_)
        .route(PutBucketEncryption)
        .route(PutBucketEncryption_)
        .route(PutBucketPolicy)
        .route(PutBucketPolicy_)
        .route(PutBucketReplication)
        .route(PutBucketReplication_)
        .route(PutBucketVersioning)
        .route(PutBucketVersioning_)
        .route(PutBucketTagging)
        .route(PutBucketTagging_)
        .route(UploadPart)

        .notFound(new NotFoundHandler())
        .exceptionHandler(LocalS3Exception.class, new LocalS3ExceptionHandler(serviceFactory))
        .exceptionHandler(IllegalArgumentException.class, new IllegalArgumentExceptionHandler())
        .exceptionHandler(Exception.class, new ExceptionHandler())
        ;
  }

}
