# LocalS3

[![Build](https://github.com/Robothy/local-s3/actions/workflows/build.yml/badge.svg)](https://github.com/Robothy/local-s3/actions/workflows/build.yml)
[![Apache 2.0 License](https://img.shields.io/badge/license-Apache%202.0-green.svg)](https://github.com/robothy/local-s3/blob/main/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.robothy/local-s3-rest.svg)](https://search.maven.org/artifact/io.github.robothy/local-s3-rest/)
[![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?logo=docker&logoColor=white)](https://hub.docker.com/r/luofuxiang/local-s3)
[![codecov](https://codecov.io/gh/Robothy/local-s3/branch/main/graph/badge.svg?token=9YLOKDU03D)](https://codecov.io/gh/Robothy/local-s3)

LocalS3 is an Amazon S3 mock service for testing and local development. LocalS3 is based on Netty
and without heavy dependencies, it starts up quickly and handles requests efficiently.

<details>
<summary><b>Supported Amazon S3 APIs</b></summary>

+ AbortMultipartUpload
+ CopyObject
+ CreateBucket
+ CreateMultipartUpload
+ CompleteMultipartUpload
+ DeleteBucket
+ DeleteBucketEncryption
+ DeleteBucketPolicy
+ DeleteBucketReplication
+ DeleteBucketTagging
+ DeleteObject
+ DeleteObjects
+ DeleteObjectTagging
+ GetObject
+ GetObjectTagging
+ GetBucketAcl
+ GetBucketEncryption
+ GetBucketPolicy
+ GetBucketReplication
+ GetBucketVersioning
+ GetBucketTagging
+ GetBucketLocation
+ HeadBucket
+ HeadObject
+ ListBuckets
+ ListObjects
+ ListObjectsV2
+ ListObjectVersions
+ ListParts
+ PutBucketAcl
+ PutBucketEncryption
+ PutBucketPolicy
+ PutBucketReplication
+ PutBucketVersioning
+ PutBucketTagging
+ PutObject
+ PutObjectTagging
+ UploadPart

</details>


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
localS3.shutdown();
```


#### Run LocalS3 in Persistence mode

When LocalS3 runs in persistence mode, a data path is required. LocalS3 loads data from and stores all data into 
the specified path.

```java
LocalS3 localS3 = LocalS3.builder()
    .port(19090)
    .mode(LocalS3Mode.PERSISTENCE)
    .dataDirectory("C://local-s3")
    .build();

localS3.start();
```

#### Run LocalS3 in In-Memory mode with initial data.

LocalS3 loads initial data from the specified path. Changes on such LocalS3 instance only modify the
data in memory, not persist to the disk.

```java
LocalS3 localS3 = LocalS3.builder()
    .port(-1) // assign a random port
    .dataDirectory("/data")
    .build();

localS3.start();
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

When you annotate it on test classes or test methods, the LocalS3 extension automatically inject instances with 
the following parameter types of test methods.

+ `AmazonS3`
+ `S3Client`
+ `LocalS3Endpoint`

Example 1: Inject a `AmazonS3` object to the test method parameter.

```java
@LocalS3
class AppTest {
  @Test
  void test(AmazonS3 s3) {
    s3.createBucket("my-bucket");
  }
}

```

Example 2: Inject a `S3Client` object to the test method parameter

```java
class AppTest {
  @Test
  @LocalS3
  void test(S3Client client) {
    client.createBucket(b -> b.bucket("my-bucket"));
  }
}
```

#### Difference between `@LocalS3` on test classes and test methods

If `@LocalS3` is on a test class, the Junit5 extension will create a shared service for all test methods in the class
and shut it down in the "after all" callback.
If `@LocalS3` is on a test method, the extension creates an exclusive service for the method and shut down the
service in the "after each" callback.

### Run LocalS3 in Docker

You can run LocalS3 in Docker since it's image is published to [DockerHub](https://hub.docker.com/r/luofuxiang/local-s3).

```shell
docker run --name s3 -d -v C:\\local-s3:/data -p 8080:80 luofuxiang/local-s3
```

### LocalS3 test container

LocalS3 provides a [testcontainers](https://www.testcontainers.org/) implementation. You can run LocalS3 in your tests 
with testcontainers API.

#### Dependency

```xml
<dependency>
    <groupId>io.github.robothy</groupId>
    <artifactId>local-s3-testcontainers</artifactId>
</dependency>
```

#### Start LocalS3 with testcontainers

```java
@Testcontainers
public class AppTest {

  @Container
  public LocalS3Container container = new LocalS3Container("latest")
      .withMode(LocalS3Container.Mode.IN_MEMORY)
      .withRandomHttpPort();
  
  @Test
  void test() {
    assertTrue(container.isRunning());
    int port = container.getPort();
    AmazonS3 s3 = AmazonS3ClientBuilder.standard()
        .enablePathStyleAccess()
        .withClientConfiguration(new ClientConfiguration().withSocketTimeout(1000).withConnectionTimeout(1000))
        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
            "http://localhost:" + port, "local"
        )).build();
    s3.createBucket("my-bucket");
  }
  
}
```

## Sponsors

[![JetBrains](https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg)](https://jb.gg/OpenSourceSupport)


