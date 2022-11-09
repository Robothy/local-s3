# LocalS3

[![Apache 2.0 License](https://img.shields.io/badge/license-Apache%202.0-green.svg)](https://github.com/robothy/local-s3/blob/main/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.robothy/local-s3-rest.svg)](https://search.maven.org/artifact/io.github.robothy/local-s3-rest/)
[![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?logo=docker&logoColor=white)](https://hub.docker.com/r/luofuxiang/local-s3)


LocalS3 is an Amazon S3 mock service for testing and local development. LocalS3 is based on Netty
and without heavy dependencies, it starts up quickly and handles requests efficiently.

## Features

+ Support S3 object versioning.
+ In memory and persistence mode.

## Usages

### Programming with LocalS3

Developers could integrate LocalS3 into their own Java applications or testing frameworks via Java APIs.

#### Dependency

```xml
<dependency>
    <groupId>io.github.robothy</groupId>
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


#### Run LocalS3 in Persistence mode

When a data directory is specified, LocalS3 tries to load data from and store all data into the path.


```java
LocalS3 localS3 = LocalS3.builder()
    .port(19090)
    .dataDirectory(Paths.get("C://local-s3"))
    .build()
    .start();
```

### LocalS3 for Junit5

LocalS3 for Junit5 provides a Java annotation `@LocalS3` helps you easily launch S3 services for your tests.

#### Dependency

```xml
<dependency>
    <groupId>io.github.robothy</groupId>
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
