package com.robothy.s3.core.exception;

/**
 * Represents <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_Error.html">S3 defined errors</a>.
 */
public enum S3ErrorCode {

  AccessDenied("AccessDenied", 403, "Access Denied"),
  //AccountProblem("AccountProblem", 403, "There is a problem with your AWS account that prevents the action from completing successfully. Contact AWS Support for further assistance."),
  //AllAccessDisabled("AllAccessDisabled", 403, "All access to this Amazon S3 resource has been disabled. Contact AWS Support for further assistance."),
  //AmbiguousGrantByEmailAddress("AmbiguousGrantByEmailAddress", 400, "The email address you provided is associated with more than one account."),
  //AuthorizationHeaderMalformed("AuthorizationHeaderMalformed", 400, "The authorization header you provided is invalid."),
  //BadDigest("BadDigest", 400, "The Content-MD5 you specified did not match what we received."),
  BucketAlreadyExists("BucketAlreadyExists", 409, "The requested bucket name is not available. The bucket namespace is shared by all users of the system. Please select a different name and try again."),
  //BucketAlreadyOwnedByYou("BucketAlreadyOwnedByYou", 409,"The bucket you tried to create already exists, and you own it. Amazon S3 returns this error in all AWS Regions except in the North Virginia Region. For legacy compatibility, if you re-create an existing bucket that you already own in the North Virginia Region, Amazon S3 returns 200 OK and resets the bucket access control lists (ACLs)."),
  BucketNotEmpty("BucketNotEmpty", 409, "The bucket you tried to delete is not empty."),
  //CredentialsNotSupported("CredentialsNotSupported", 400, "This request does not support credentials."),
  //CrossLocationLoggingProhibited("CrossLocationLoggingProhibited", 403, "Cross-location logging not allowed. Buckets in one geographic location cannot log information to a bucket in another location."),
  EntityTooSmall("EntityTooSmall", 400, "Your proposed upload is smaller than the minimum allowed object size."),
  EntityTooLarge("EntityTooLarge", 400, "Your proposed upload exceeds the maximum allowed object size."),
  //ExpiredToken("ExpiredToken", 400, "The provided token has expired."),
  IllegalVersioningConfigurationException("IllegalVersioningConfigurationException", 400, "Indicates that the versioning configuration specified in the request is invalid."),
  //IncompleteBody("IncompleteBody", 400, "You did not provide the number of bytes specified by the Content-Length HTTP header"),
  //IncorrectNumberOfFilesInPostRequest("IncorrectNumberOfFilesInPostRequest", 400, "POST requires exactly one file upload per request."),
  //InlineDataTooLarge("InlineDataTooLarge", 400, "Inline data exceeds the maximum allowed size."),
  InternalError("InternalError", 500, "We encountered an internal error. Please try again."),
  //InvalidAccessKeyId("InvalidAccessKeyId", 403, "The AWS access key ID you provided does not exist in our records."),
  //InvalidAddressingHeader("InvalidAddressingHeader", 400, "You must specify the Anonymous role."),
  InvalidArgument("InvalidArgument", 400, "Invalid Argument"),
  InvalidBucketName("InvalidBucketName", 400, "The specified bucket is not valid."),
  //InvalidBucketState("InvalidBucketState", 409, "The request is not valid with the current state of the bucket."),
  //InvalidDigest("InvalidDigest", 400, "The Content-MD5 you specified is not valid."),
  //InvalidEncryptionAlgorithmError("InvalidEncryptionAlgorithmError", 400, "The encryption request you specified is not valid. The valid value is AES256."),
  //InvalidLocationConstraint("InvalidLocationConstraint", 400, "The specified location constraint is not valid. For more information about Regions, see How to Select a Region for Your Buckets."),
  //InvalidObjectState("InvalidObjectState", 403, "The action is not valid for the current state of the object."),
  InvalidPart("InvalidPart", 400, "One or more of the specified parts could not be found. The part might not have been uploaded, or the specified entity tag might not have matched the part's entity tag."),
  InvalidPartOrder("InvalidPartOrder", 400, "The list of parts was not in ascending order. Parts list must be specified in order by part number."),
  //InvalidPayer("InvalidPayer", 403, "All access to this object has been disabled. Please contact AWS Support for further assistance."),
  InvalidPolicyDocument("InvalidPolicyDocument", 400, "The content of the form does not meet the conditions specified in the policy document."),
  InvalidRange("InvalidRange", 416, "The requested range cannot be satisfied."),
  InvalidRequest("InvalidRequest", 400, ""),
  InvalidSecurity("InvalidSecurity", 403, "The provided security credentials are not valid."),
  //InvalidSOAPRequest("InvalidSOAPRequest", 400, "The SOAP request body is invalid."),
  InvalidStorageClass("InvalidStorageClass", 400, "The storage class you specified is not valid."),
  //InvalidTargetBucketForLogging("InvalidTargetBucketForLogging", 400, "The target bucket for logging does not exist, is not owned by you, or does not have the appropriate grants for the log-delivery group."),
  //InvalidToken("InvalidToken", 400, "The provided token is malformed or otherwise invalid."),
  //InvalidURI("InvalidURI", 400, "Couldn't parse the specified URI."),
  KeyTooLongError("KeyTooLongError", 400, "Your key is too long."),
  //MalformedACLError("MalformedACLError", 400, "The XML you provided was not well-formed or did not validate against our published schema."),
  //MalformedPOSTRequest("MalformedPOSTRequest", 400, "The body of your POST request is not well-formed multipart/form-data."),
  //MalformedXML("MalformedXML", 400, "This happens when the user sends malformed XML (XML that doesn't conform to the published XSD) for the configuration. The error message is, \"The XML you provided was not well-formed or did not validate against our published schema.\""),
  MaxMessageLengthExceeded("MaxMessageLengthExceeded", 400, "Your request was too big."),
  MaxPostPreDataLengthExceededError("MaxPostPreDataLengthExceededError", 400, "Your POST request fields preceding the upload file were too large."),
  MetadataTooLarge("MetadataTooLarge", 400, "Your metadata headers exceed the maximum allowed metadata size."),
  MethodNotAllowed("MethodNotAllowed", 405, "The specified method is not allowed against this resource."),
  MissingContentLength("MissingContentLength", 411, "You must provide the Content-Length HTTP header."),
  MissingRequestBodyError("MissingRequestBodyError", 400, "This happens when the user sends an empty XML document as a request. The error message is, \"Request body is empty.\""),
  NoLoggingStatusForKey("NoLoggingStatusForKey", 400, "There is no such thing as a logging status subresource for a key."),
  NoSuchBucket("NoSuchBucket", 404, "The specified bucket does not exist."),
  NoSuchBucketPolicy("NoSuchBucketPolicy", 404, "The specified bucket does not have a bucket policy."),
  NoSuchTagSet("NoSuchTagSet", 404, "The TagSet does not exist."),
  NoSuchKey("NoSuchKey", 404, "The specified key does not exist."),
  NoSuchLifecycleConfiguration("NoSuchLifecycleConfiguration", 404, "The lifecycle configuration does not exist."),
  NoSuchUpload("NoSuchUpload", 404, "The specified multipart upload does not exist. The upload ID might be invalid, or the multipart upload might have been aborted or completed."),
  NoSuchVersion("NoSuchVersion", 404, "Indicates that the version ID specified in the request does not match an existing version."),
  NotImplemented("NotImplemented", 501, "A header you provided implies functionality that is not implemented."),
  //NotSignedUp("NotSignedUp", 403, "Your account is not signed up for the Amazon S3 service. You must sign up before you can use Amazon S3. You can sign up at the following URL: Amazon S3"),
  OperationAborted("OperationAborted", 409, "A conflicting conditional action is currently in progress against this resource. Try again."),
  //PermanentRedirect("PermanentRedirect", 301, "The bucket you are attempting to access must be addressed using the specified endpoint. Send all future requests to this endpoint."),
  PreconditionFailed("PreconditionFailed", 412, "At least one of the preconditions you specified did not hold."),
  Redirect("Redirect", 307, "Temporary redirect."),
  //RestoreAlreadyInProgress("RestoreAlreadyInProgress", 409, "Object restore is already in progress."),
  RequestIsNotMultiPartContent("RequestIsNotMultiPartContent", 400, "Bucket POST must be of the enclosure-type multipart/form-data."),
  RequestTimeout("RequestTimeout", 400, "Your socket connection to the server was not read from or written to within the timeout period."),
  RequestTimeTooSkewed("RequestTimeTooSkewed", 403, "The difference between the request time and the server's time is too large."),
  RequestTorrentOfBucketError("RequestTorrentOfBucketError", 400, "Requesting the torrent file of a bucket is not permitted."),
  ReplicationConfigurationNotFoundError("ReplicationConfigurationNotFoundError", 404, "The replication configuration was not found"),
  //SignatureDoesNotMatch("SignatureDoesNotMatch", 403, "The request signature we calculated does not match the signature you provided. Check your AWS secret access key and signing method. For more information, see REST Authentication and "),
  ServerSideEncryptionConfigurationNotFoundError("ServerSideEncryptionConfigurationNotFoundError", 404, "The server side encryption configuration was not found"),
  ServiceUnavailable("ServiceUnavailable", 503, "Service is unable to handle request."),
  //SlowDown("SlowDown", 503, "Reduce your request rate."),
  //TemporaryRedirect("TemporaryRedirect", 307, "You are being redirected to the bucket while DNS updates."),
  //TokenRefreshRequired("TokenRefreshRequired", 400, "The provided token must be refreshed."),
  TooManyBuckets("TooManyBuckets", 400, "You have attempted to create more buckets than allowed."),
  UnexpectedContent("UnexpectedContent", 400, "This request does not support content."),
  //UnresolvableGrantByEmailAddress("UnresolvableGrantByEmailAddress", 400, "The email address you provided does not match any account on record."),
  UserKeyMustBeSpecified("UserKeyMustBeSpecified", 400, "The bucket POST must contain the specified field name. If it is specified, check the order of the fields."),
  ;


  private final int httpStatus;
  private final String code;

  private final String description;

  S3ErrorCode(String code, int httpStatus, String description) {
    this.code = code;
    this.description = description;
    this.httpStatus = httpStatus;
  }

  public String code() {
    return this.code;
  }

  public int httpStatus() {
    return this.httpStatus;
  }

  public String description() {
    return this.description;
  }

}
