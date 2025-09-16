package com.robothy.s3.docker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.github.dockerjava.api.command.StopContainerCmd;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.document.Document;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3vectors.S3VectorsClient;
import software.amazon.awssdk.services.s3vectors.model.CreateIndexRequest;
import software.amazon.awssdk.services.s3vectors.model.CreateVectorBucketRequest;
import software.amazon.awssdk.services.s3vectors.model.DataType;
import software.amazon.awssdk.services.s3vectors.model.DeleteIndexRequest;
import software.amazon.awssdk.services.s3vectors.model.DeleteVectorBucketRequest;
import software.amazon.awssdk.services.s3vectors.model.DeleteVectorsRequest;
import software.amazon.awssdk.services.s3vectors.model.DistanceMetric;
import software.amazon.awssdk.services.s3vectors.model.GetIndexRequest;
import software.amazon.awssdk.services.s3vectors.model.GetVectorBucketRequest;
import software.amazon.awssdk.services.s3vectors.model.GetVectorsRequest;
import software.amazon.awssdk.services.s3vectors.model.ListIndexesRequest;
import software.amazon.awssdk.services.s3vectors.model.ListVectorBucketsRequest;
import software.amazon.awssdk.services.s3vectors.model.ListVectorsRequest;
import software.amazon.awssdk.services.s3vectors.model.PutInputVector;
import software.amazon.awssdk.services.s3vectors.model.PutVectorsRequest;
import software.amazon.awssdk.services.s3vectors.model.QueryVectorsRequest;
import software.amazon.awssdk.services.s3vectors.model.VectorData;
import java.net.URI;

public class ReachabilityMetadataGenerator {

  private static final String NATIVE_IMAGE_TAG = "24-ol9";

  public static void main(String[] args) throws IOException {
    int port = 38080;
    File dataPath = Files.createTempDirectory("local-s3-data").toFile();
    dataPath.deleteOnExit();

    try (CollectReachabilityMetadataContainer container = new CollectReachabilityMetadataContainer(NATIVE_IMAGE_TAG)) {

      container.port(port)
          .withFileSystemBind("build/reachability-metadata/META-INF/native-image", "/metadata", BindMode.READ_WRITE)
          .withFileSystemBind("build/libs", "/app", BindMode.READ_WRITE)
          .withFileSystemBind(dataPath.getAbsolutePath(), "/data", BindMode.READ_WRITE)
          .withCreateContainerCmdModifier(cmd -> cmd.withEntrypoint(""))
          .withCommand("java -DMODE=PERSISTENCE -agentlib:native-image-agent=config-output-dir=/metadata -jar /app/s3.jar")
          .start();

      // Hit all LocalS3 APIs, cover as many classes as possible to generate reachability metadata.
      run(port);

      // Hit all S3 Vectors APIs to generate reachability metadata.
      runS3Vectors(port);

      // Stop the container.
      try (StopContainerCmd cmd = container.getDockerClient().stopContainerCmd(container.getContainerId())) {
        cmd.exec();
      }

    }

    /*======== Load data from data path. ========*/
    try (CollectReachabilityMetadataContainer container = new CollectReachabilityMetadataContainer(NATIVE_IMAGE_TAG)) {
      container.port(port)
          .withFileSystemBind("build/reachability-metadata/META-INF/native-image", "/metadata", BindMode.READ_WRITE)
          .withFileSystemBind("build/libs", "/app", BindMode.READ_WRITE)
          .withFileSystemBind(dataPath.getAbsolutePath(), "/data", BindMode.READ_WRITE)
          .withCreateContainerCmdModifier(cmd -> cmd.withEntrypoint(""))
          .withCommand("java -DMODE=IN_MEMORY -agentlib:native-image-agent=config-merge-dir=/metadata -jar /app/s3.jar")
          .start();

      // Stop the container.
      try (StopContainerCmd cmd = container.getDockerClient().stopContainerCmd(container.getContainerId())) {
        cmd.exec();
      }
    }

  }

  /**
   * Hit all LocalS3 APIs, cover as many classes as possible.
   */
  static void run(int port) {


    try (S3Client s3 = S3Client.builder()
        .endpointOverride(URI.create("http://localhost:" + port))
        .forcePathStyle(true)
        .region(Region.AP_EAST_1)
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("access-key-id", "secret-key")))
        .build()) {
      String bucketName = "my-bucket";
      s3.createBucket(CreateBucketRequest.builder()
          .bucket(bucketName)
          .createBucketConfiguration(CreateBucketConfiguration.builder()
              .locationConstraint(BucketLocationConstraint.AP_EAST_1)
              .build())
          .build());
      s3.listBuckets();
      s3.getBucketLocation(GetBucketLocationRequest.builder().bucket(bucketName).build());
      s3.putObject(PutObjectRequest.builder().bucket(bucketName).key("my-object").build(),
          RequestBody.fromString("Hello World!"));
      s3.getObject(GetObjectRequest.builder().bucket(bucketName).key("my-object").build());
      s3.listObjects(ListObjectsRequest.builder().bucket(bucketName).build());
      s3.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key("my-object").build());
      s3.putBucketVersioning(PutBucketVersioningRequest.builder()
          .bucket(bucketName)
          .versioningConfiguration(VersioningConfiguration.builder()
              .status(BucketVersioningStatus.ENABLED)
              .build())
          .build());
      s3.listObjectsV2(ListObjectsV2Request.builder().bucket(bucketName).build());

      s3.putObject(PutObjectRequest.builder().bucket(bucketName).key("my-object").build(),
          RequestBody.fromString("Hello World!"));
      s3.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key("my-object").build());
      s3.putObject(PutObjectRequest.builder().bucket(bucketName).key("my-object").build(),
          RequestBody.fromString("Hello World!"));
      s3.listObjectVersions(ListObjectVersionsRequest.builder().bucket(bucketName).prefix("my-object").build());
      s3.copyObject(CopyObjectRequest.builder()
          .sourceBucket(bucketName).sourceKey("my-object")
          .destinationBucket(bucketName).destinationKey("my-object-copy")
          .build());
      s3.putObjectTagging(PutObjectTaggingRequest.builder()
          .bucket(bucketName).key("my-object")
          .tagging(Tagging.builder()
              .tagSet(Tag.builder().key("k1").value("v1").build())
              .build())
          .build());
      s3.getObjectTagging(GetObjectTaggingRequest.builder().bucket(bucketName).key("my-object").build());
      s3.deleteObjectTagging(DeleteObjectTaggingRequest.builder().bucket(bucketName).key("my-object").build());

      s3.createMultipartUpload(CreateMultipartUploadRequest.builder()
          .bucket(bucketName).key("my-object").build());

      CreateMultipartUploadResponse initResult = s3.createMultipartUpload(CreateMultipartUploadRequest.builder()
          .bucket(bucketName).key("my-object")
          .contentType("plain/text")
          .build());

      UploadPartResponse part1Response = s3.uploadPart(UploadPartRequest.builder()
          .bucket(bucketName)
          .key("my-object")
          .uploadId(initResult.uploadId())
          .partNumber(1)
          .build(), RequestBody.fromBytes("Hello".getBytes()));

      UploadPartResponse part2Response = s3.uploadPart(UploadPartRequest.builder()
          .bucket(bucketName)
          .key("my-object")
          .uploadId(initResult.uploadId())
          .partNumber(2)
          .build(), RequestBody.fromBytes("World".getBytes()));

      s3.listParts(ListPartsRequest.builder()
          .bucket(bucketName).key("my-object").uploadId(initResult.uploadId()).build());
      s3.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
          .bucket(bucketName).key("my-object").uploadId(initResult.uploadId())
          .multipartUpload(CompletedMultipartUpload.builder()
              .parts(
                  CompletedPart.builder().partNumber(1).eTag(part1Response.eTag()).build(),
                  CompletedPart.builder().partNumber(2).eTag(part2Response.eTag()).build()
              )
              .build())
          .build());
      CreateMultipartUploadResponse newInitResult = s3.createMultipartUpload(CreateMultipartUploadRequest.builder()
          .bucket(bucketName).key("my-object").build());
      s3.abortMultipartUpload(AbortMultipartUploadRequest.builder()
          .bucket(bucketName).key("my-object").uploadId(newInitResult.uploadId()).build());

      assertDoesNotThrow(() -> s3.putBucketEncryption(PutBucketEncryptionRequest.builder()
          .bucket(bucketName)
          .serverSideEncryptionConfiguration(ServerSideEncryptionConfiguration.builder()
              .rules(ServerSideEncryptionRule.builder()
                  .bucketKeyEnabled(true)
                  .applyServerSideEncryptionByDefault(ServerSideEncryptionByDefault.builder()
                      .sseAlgorithm(ServerSideEncryption.AES256)
                      .kmsMasterKeyID("arn:aws:kms:us-east-1:1234/5678example")
                      .build())
                  .build())
              .build())
          .build()));
      s3.getBucketEncryption(GetBucketEncryptionRequest.builder().bucket(bucketName).build());
      s3.deleteBucketEncryption(DeleteBucketEncryptionRequest.builder().bucket(bucketName).build());

      s3.putBucketPolicy(PutBucketPolicyRequest.builder().bucket(bucketName).policy("policy").build());
      s3.getBucketPolicy(GetBucketPolicyRequest.builder().bucket(bucketName).build());
      s3.deleteBucketPolicy(DeleteBucketPolicyRequest.builder().bucket(bucketName).build());

      s3.putBucketReplication(PutBucketReplicationRequest.builder()
          .bucket(bucketName)
          .replicationConfiguration(ReplicationConfiguration.builder()
              .role("arn:aws:iam::123456789012:role/replication-role")
              .rules(ReplicationRule.builder()
                  .id("1")
                  .priority(1)
                  .status(ReplicationRuleStatus.ENABLED)
                  .deleteMarkerReplication(DeleteMarkerReplication.builder()
                      .status(DeleteMarkerReplicationStatus.DISABLED)
                      .build())
                  .destination(Destination.builder()
                      .bucket("arn:aws:s3:::exampletargetbucket")
                      .build())
                  .build())
              .build())
          .build());
      s3.getBucketReplication(GetBucketReplicationRequest.builder().bucket(bucketName).build());
      s3.deleteBucketReplication(DeleteBucketReplicationRequest.builder().bucket(bucketName).build());

      s3.putBucketTagging(PutBucketTaggingRequest.builder()
          .bucket(bucketName)
          .tagging(Tagging.builder()
              .tagSet(Tag.builder().key("key").value("value").build())
              .build())
          .build());
      s3.getBucketTagging(GetBucketTaggingRequest.builder().bucket(bucketName).build());
      s3.deleteBucketTagging(DeleteBucketTaggingRequest.builder().bucket(bucketName).build());

      s3.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
      s3.headObject(HeadObjectRequest.builder().bucket(bucketName).key("my-object").build());

      s3.deleteObjects(DeleteObjectsRequest.builder()
          .bucket(bucketName)
          .delete(Delete.builder()
              .objects(ObjectIdentifier.builder().key("my-object").versionId("version").build())
              .build())
          .build());

      assertThrows(S3Exception.class, () -> s3.deleteBucket(DeleteBucketRequest.builder().bucket("not-exist-bucket").build()));

      assertThrows(S3Exception.class, () -> {
        s3.uploadPartCopy(UploadPartCopyRequest.builder()
            .uploadId(newInitResult.uploadId())
            .partNumber(1)
            .sourceBucket(bucketName)
            .sourceKey("my-object")
            .destinationBucket(bucketName)
            .destinationKey("my-object-copy")
            .build());
      }); // not implemented yet
    }
  }

  /**
   * Hit all S3 Vectors APIs, cover as many classes as possible.
   */
  static void runS3Vectors(int port) {

    try (S3VectorsClient vectorsClient = S3VectorsClient.builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(AnonymousCredentialsProvider.create())
        .endpointOverride(URI.create("http://localhost:" + port))
        .build()) {
      String vectorBucketName = "test-vector-bucket";
      String indexName = "test-index";
      // Vector Bucket operations
      vectorsClient.createVectorBucket(CreateVectorBucketRequest.builder()
          .vectorBucketName(vectorBucketName)
          .build());

      vectorsClient.getVectorBucket(GetVectorBucketRequest.builder()
          .vectorBucketName(vectorBucketName)
          .build());

      vectorsClient.listVectorBuckets(ListVectorBucketsRequest.builder()
          .maxResults(10)
          .build());

      // Index operations
      vectorsClient.createIndex(CreateIndexRequest.builder()
          .vectorBucketName(vectorBucketName)
          .indexName(indexName)
          .dimension(5)
          .dataType(DataType.FLOAT32)
          .distanceMetric(DistanceMetric.COSINE)
          .metadataConfiguration(m -> m.nonFilterableMetadataKeys("nf1", "nf2"))
          .build());

      vectorsClient.getIndex(GetIndexRequest.builder()
          .vectorBucketName(vectorBucketName)
          .indexName(indexName)
          .build());

      vectorsClient.listIndexes(ListIndexesRequest.builder()
          .vectorBucketName(vectorBucketName)
          .maxResults(10)
          .build());

      // Vector operations with metadata
      Document vectorMetadata = Document.mapBuilder()
          .putString("category", "test")
          .putNumber("priority", 1)
          .putBoolean("active", true)
          .putList("tags", listBuilder -> listBuilder
              .addString("tag1")
              .addString("tag2"))
          .build();

      List<PutInputVector> vectors = List.of(
          PutInputVector.builder()
              .key("vector1")
              .data(VectorData.builder()
                  .float32(List.of(1.0f, 2.0f, 3.0f, 4.0f, 5.0f))
                  .build())
              .metadata(vectorMetadata)
              .build(),
          PutInputVector.builder()
              .key("vector2")
              .data(VectorData.builder()
                  .float32(List.of(2.0f, 3.0f, 4.0f, 5.0f, 6.0f))
                  .build())
              .build()
      );

      vectorsClient.putVectors(PutVectorsRequest.builder()
          .vectorBucketName(vectorBucketName)
          .indexName(indexName)
          .vectors(vectors)
          .build());

      vectorsClient.getVectors(GetVectorsRequest.builder()
          .vectorBucketName(vectorBucketName)
          .indexName(indexName)
          .keys("vector1", "vector2")
          .returnData(true)
          .returnMetadata(true)
          .build());

      vectorsClient.listVectors(ListVectorsRequest.builder()
          .vectorBucketName(vectorBucketName)
          .indexName(indexName)
          .returnData(true)
          .returnMetadata(true)
          .maxResults(10)
          .build());

      // Query vectors with filter
      Document metadataFilter = Document.mapBuilder()
          .putString("category", "test")
          .build();

      vectorsClient.queryVectors(QueryVectorsRequest.builder()
          .vectorBucketName(vectorBucketName)
          .indexName(indexName)
          .queryVector(VectorData.builder()
              .float32(List.of(1.5f, 2.5f, 3.5f, 4.5f, 5.5f))
              .build())
          .topK(5)
          .returnMetadata(true)
          .returnDistance(true)
          .filter(metadataFilter)
          .build());

      // List vectors with segmentation
      vectorsClient.listVectors(ListVectorsRequest.builder()
          .vectorBucketName(vectorBucketName)
          .indexName(indexName)
          .segmentCount(2)
          .segmentIndex(0)
          .build());

      // Cleanup operations
      vectorsClient.deleteVectors(DeleteVectorsRequest.builder()
          .vectorBucketName(vectorBucketName)
          .indexName(indexName)
          .keys("vector1")
          .build());

      vectorsClient.deleteIndex(DeleteIndexRequest.builder()
          .vectorBucketName(vectorBucketName)
          .indexName(indexName)
          .build());

      vectorsClient.deleteVectorBucket(DeleteVectorBucketRequest.builder()
          .vectorBucketName(vectorBucketName)
          .build());

    } catch (Exception e) {
      // Expected for some operations in development
      System.err.println("S3 Vectors operation failed (expected): " + e.getMessage());
    }
  }

  /**
   * Collect reachability metadata container.
   */
  static class CollectReachabilityMetadataContainer extends GenericContainer<CollectReachabilityMetadataContainer> {
    CollectReachabilityMetadataContainer(String tag) {
      super(DockerImageName.parse("ghcr.io/graalvm/native-image-community").withTag(tag));
      this.waitingFor(Wait.forLogMessage("^.{1,}LocalS3 started.\n$", 1));
    }

    CollectReachabilityMetadataContainer port(int port) {
      super.addFixedExposedPort(port, 80);
      return this;
    }

  }

}
