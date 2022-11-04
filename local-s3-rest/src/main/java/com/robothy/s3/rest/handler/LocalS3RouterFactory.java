package com.robothy.s3.rest.handler;

import com.robothy.netty.router.Route;
import com.robothy.netty.router.Router;
import com.robothy.s3.core.exception.LocalS3Exception;
import com.robothy.s3.rest.constants.AmzHeaderNames;
import io.netty.handler.codec.http.HttpMethod;

public class LocalS3RouterFactory {

  private final BucketPolicyController bucketPolicy = new BucketPolicyController();

  private final Route CopyObject = Route.builder()
      .method(HttpMethod.PUT)
      .path("/{bucket}/{*key}")
      .headerMatcher(headers -> headers.containsKey(AmzHeaderNames.X_AMZ_COPY_SOURCE))
      .handler(new CopyObjectController())
      .build();

  private final Route CreateBucket = Route.builder()
      .method(HttpMethod.PUT)
      .path("/{bucket}")
      .handler(new CreateBucketController())
      .build();

  private final Route CreateMultipartUpload = Route.builder()
      .method(HttpMethod.POST)
      .path("/{bucket}/{*key}")
      .paramMatcher(params -> params.containsKey("uploads"))
      .handler(new CreateMultipartUploadController())
      .build();

  private final Route CompleteMultipartUpload = Route.builder()
      .method(HttpMethod.POST)
      .path("/{bucket}/{*key}")
      .handler(new CompleteMultipartUploadController())
      .build();

  private final Route DeleteBucket = Route.builder()
      .method(HttpMethod.DELETE)
      .path("/{bucket}")
      .handler(new DeleteBucketController())
      .build();

  private final Route DeleteBucketPolicy = Route.builder()
      .method(HttpMethod.DELETE)
      .path("/{bucket}")
      .paramMatcher(params -> params.containsKey("policy"))
      .handler(bucketPolicy::delete)
      .build();

  private final Route DeleteBucketPolicy_ = Route.builder()
      .method(HttpMethod.DELETE)
      .path("/{bucket}/")
      .paramMatcher(params -> params.containsKey("policy"))
      .handler(bucketPolicy::delete)
      .build();

  private final Route DeleteBucketTagging = Route.builder()
      .method(HttpMethod.DELETE)
      .path("/{bucket}")
      .paramMatcher(params -> params.containsKey("tagging"))
      .handler(new DeleteBucketTaggingController())
      .build();

  private final Route DeleteBucketTagging_ = Route.builder()
      .method(HttpMethod.DELETE)
      .path("/{bucket}/")
      .paramMatcher(params -> params.containsKey("tagging"))
      .handler(new DeleteBucketTaggingController())
      .build();

  private final Route DeleteObject = Route.builder()
      .method(HttpMethod.DELETE)
      .path("/{bucket}/{*key}")
      .handler(new DeleteObjectController())
      .build();

  private final Route GetBucket = Route.builder()
      .method(HttpMethod.GET)
      .path("/v20180820/bucket/{bucket}")
      .handler(new GetBucketController())
      .build();

  private final Route GetBucketAcl = Route.builder()
      .method(HttpMethod.GET)
      .path("/{bucket}")
      .paramMatcher(params -> params.containsKey("acl"))
      .handler(new GetBucketAclController())
      .build();

  private final Route GetBucketAcl_ = Route.builder()
      .method(HttpMethod.GET)
      .path("/{bucket}/")
      .paramMatcher(params -> params.containsKey("acl"))
      .handler(new GetBucketAclController())
      .build();

  private final Route GetBucketPolicy = Route.builder()
      .method(HttpMethod.GET)
      .path("/{bucket}")
      .paramMatcher(params -> params.containsKey("policy"))
      .handler(bucketPolicy::get)
      .build();

  private final Route GetBucketPolicy_ = Route.builder()
      .method(HttpMethod.GET)
      .path("/{bucket}/")
      .paramMatcher(params -> params.containsKey("policy"))
      .handler(bucketPolicy::get)
      .build();

  private final Route GetBucketVersioning = Route.builder()
      .method(HttpMethod.GET)
      .path("/{bucket}")
      .paramMatcher(params -> params.containsKey("versioning"))
      .handler(new GetBucketVersioningController())
      .build();

  private final Route GetBucketVersioning_ = Route.builder()
      .method(HttpMethod.GET)
      .path("/{bucket}/")
      .paramMatcher(params -> params.containsKey("versioning"))
      .handler(new GetBucketVersioningController())
      .build();

  private final Route GetBucketTagging = Route.builder()
      .method(HttpMethod.GET)
      .path("/{bucket}")
      .paramMatcher(params -> params.containsKey("tagging"))
      .handler(new GetBucketTaggingController())
      .build();

  private final Route GetBucketTagging_ = Route.builder()
      .method(HttpMethod.GET)
      .path("/{bucket}/")
      .paramMatcher(params -> params.containsKey("tagging"))
      .handler(new GetBucketTaggingController())
      .build();

  private final Route GetObject = Route.builder()
      .method(HttpMethod.GET)
      .path("/{bucket}/{*key}")
      .handler(new GetObjectController())
      .build();

  private final Route HeadBucket = Route.builder()
      .method(HttpMethod.HEAD)
      .path("/{bucket}")
      .handler(new HeadBucketController())
      .build();

  private final Route HeadObject = Route.builder()
      .method(HttpMethod.HEAD)
      .path("/{bucket}/{*key}")
      .handler(new HeadObjectController())
      .build();

  private final Route ListObjects = Route.builder()
      .method(HttpMethod.GET)
      .path("/{bucket}")
      .handler(new ListObjectsController())
      .build();

  private final Route ListObjects_ = Route.builder()
      .method(HttpMethod.GET)
      .path("/{bucket}/")
      .handler(new ListObjectsController())
      .build();

  private final Route ListObjectVersions = Route.builder()
      .method(HttpMethod.GET)
      .path("/{bucket}")
      .paramMatcher(params -> params.containsKey("versions"))
      .handler(new ListObjectVersionsController())
      .build();

  private final Route ListObjectVersions_ = Route.builder()
      .method(HttpMethod.GET)
      .path("/{bucket}/")
      .paramMatcher(params -> params.containsKey("versions"))
      .handler(new ListObjectVersionsController())
      .build();

  private final Route PutBucketAcl = Route.builder()
      .method(HttpMethod.PUT)
      .path("/{bucket}")
      .paramMatcher(params -> params.containsKey("acl"))
      .handler(new PutBucketAclController())
      .build();

  private final Route PutBucketAcl_ = Route.builder()
      .method(HttpMethod.PUT)
      .path("/{bucket}/")
      .paramMatcher(params -> params.containsKey("acl"))
      .handler(new PutBucketAclController())
      .build();

  private final Route PutBucketPolicy = Route.builder()
      .method(HttpMethod.PUT)
      .path("/{bucket}")
      .paramMatcher(params -> params.containsKey("policy"))
      .handler(bucketPolicy::put)
      .build();

  private final Route PutBucketPolicy_ = Route.builder()
      .method(HttpMethod.PUT)
      .path("/{bucket}/")
      .paramMatcher(params -> params.containsKey("policy"))
      .handler(bucketPolicy::put)
      .build();

  private final Route PutBucketVersioning = Route.builder()
      .method(HttpMethod.PUT)
      .path("/{bucket}")
      .paramMatcher(params -> params.containsKey("versioning"))
      .handler(new PutBucketVersioningController())
      .build();

  private final Route PutBucketVersioning_ = Route.builder()
      .method(HttpMethod.PUT)
      .path("/{bucket}/")
      .paramMatcher(params -> params.containsKey("versioning"))
      .handler(new PutBucketVersioningController())
      .build();

  private final Route PutBucketTagging = Route.builder()
      .method(HttpMethod.PUT)
      .path("/{bucket}")
      .paramMatcher(params -> params.containsKey("tagging"))
      .handler(new PutBucketTaggingController())
      .build();

  private final Route PutBucketTagging_ = Route.builder()
      .method(HttpMethod.PUT)
      .path("/{bucket}/")
      .paramMatcher(params -> params.containsKey("tagging"))
      .handler(new PutBucketTaggingController())
      .build();

  private final Route PutObject = Route.builder()
      .method(HttpMethod.PUT)
      .path("/{bucket}/{*key}")
      .handler(new PutObjectController())
      .build();

  private final Route UploadPart = Route.builder()
      .method(HttpMethod.PUT)
      .path("/{bucket}/{*key}")
      .paramMatcher(params -> params.containsKey("uploadId") && params.containsKey("partNumber"))
      .handler(new UploadPartController())
      .build();

  private final Router router = Router.router()
      .route(CopyObject)
      .route(CreateBucket)
      .route(CreateMultipartUpload)
      .route(CompleteMultipartUpload)
      .route(DeleteBucket)
      .route(DeleteBucketPolicy)
      .route(DeleteBucketPolicy_)
      .route(DeleteBucketTagging)
      .route(DeleteBucketTagging_)
      .route(DeleteObject)
      .route(GetBucket)
      .route(GetObject)
      .route(GetBucketAcl)
      .route(GetBucketAcl_)
      .route(GetBucketPolicy)
      .route(GetBucketPolicy_)
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
      .route(PutBucketPolicy)
      .route(PutBucketPolicy_)
      .route(PutBucketVersioning)
      .route(PutBucketVersioning_)
      .route(PutBucketTagging)
      .route(PutBucketTagging_)
      .route(UploadPart)

      .notFound(new NotFoundHandler())
      .exceptionHandler(LocalS3Exception.class, new LocalS3ExceptionHandler())
      .exceptionHandler(IllegalArgumentException.class, new IllegalArgumentExceptionHandler())
      .exceptionHandler(Exception.class, new ExceptionHandler())
      ;

  /**
   * Create a new LocalS3Router instance.
   */
  public static Router create() {
    return new LocalS3RouterFactory().router;
  }
  
}
