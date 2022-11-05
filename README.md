# LocalS3

[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/Robothy/local-s3?display_name=tag&color=blueviolet)](https://github.com/Robothy?tab=packages&repo_name=local-s3)

LocalS3 is a lightweight Amazon S3 mock service based on Netty.

## Features

+ Support S3 object versioning.
+ In memory and persistence mode.

## Usages

### Programming with LocalS3

Developers could integrate LocalS3 into their own Java applications or testing frameworks via Java APIs.

#### Dependency

```xml
<dependency>
    <groupId>com.robothy</groupId>
    <artifactId>local-s3-rest</artifactId>
</dependency>
```
#### Run LocalS3 in In-Memory mode

By default, LocalS3 runs in In-Memory mode; all data and metadata retain in the memory.

```java
LocalS3 localS3 = LocalS3.builder()
    .port(19090)
    .build();

localS3.start();
```

Call the stop method to shut down the service gracefully.

```java
localS3.stop();
```


#### Run LocalS3 in Persistent mode

When a data directory is specified, LocalS3 tries to load data from and store all data in the path.


```java
LocalS3 localS3 = LocalS3.builder()
    .port(19090)
    .dataDirectory(Paths.get("C://local-s3"))
    .build()
    .start();
```

### LocalS3 for Junit5

LocalS3 for Junit5 exports a Java annotation `@LocalS3` helps you easily integrate S3 services into your tests.

#### Dependency

```xml
<dependency>
    <groupId>com.robothy</groupId>
    <artifactId>local-s3-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

When you annotate it on test classes or test methods, the LocalS3 extension automatically inject instances
for AmazonS3 parameters of test methods.

```java
@LocalS3
class AppTest {
  @Test
  void test(AmazonS3 s3) {
    s3.createBucket("my-bucket");
  }
}
```

#### Difference between `@LocalS3` on test classes and test methods

If `@LocalS3` is on a test class, the Junit5 extension will create a shared service for all test methods in the class
and shut it down in the "after all" callback.
If `@LocalS3` is on a test method, the extension creates an exclusive service for the method and shut down the
service in the "after each" callback.

## Supported S3 APIs

+ CopyObject
+ CreateBucket
+ CreateMultipartUpload
+ CompleteMultipartUpload
+ DeleteBucket
+ DeleteBucketPolicy
+ DeleteBucketTagging
+ DeleteObject
+ GetObject
+ GetBucketAcl
+ GetBucketPolicy
+ GetBucketVersioning
+ GetBucketTagging
+ HeadBucket
+ HeadObject
+ ListObjects
+ ListObjectVersions
+ PutObject
+ PutBucketAcl
+ PutBucketPolicy
+ PutBucketVersioning
+ PutBucketTagging
+ UploadPart

## Incoming features

+ Replication configuration.
+ Enabled encryption.
+ Set default expiration for multipart uploads.