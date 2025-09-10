package com.robothy.s3.docker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.BucketReplicationConfiguration;
import com.amazonaws.services.s3.model.BucketTaggingConfiguration;
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.CopyPartRequest;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.DeleteMarkerReplication;
import com.amazonaws.services.s3.model.DeleteObjectTaggingRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.GetObjectTaggingRequest;
import com.amazonaws.services.s3.model.HeadBucketRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ListPartsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ObjectTagging;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.s3.model.ReplicationDestinationConfig;
import com.amazonaws.services.s3.model.ReplicationRule;
import com.amazonaws.services.s3.model.ServerSideEncryptionByDefault;
import com.amazonaws.services.s3.model.ServerSideEncryptionConfiguration;
import com.amazonaws.services.s3.model.ServerSideEncryptionRule;
import com.amazonaws.services.s3.model.SetBucketEncryptionRequest;
import com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest;
import com.amazonaws.services.s3.model.SetObjectTaggingRequest;
import com.amazonaws.services.s3.model.Tag;
import com.amazonaws.services.s3.model.TagSet;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.github.dockerjava.api.command.StopContainerCmd;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.core.document.Document;
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
          .withCommand( "java -DMODE=PERSISTENCE -agentlib:native-image-agent=config-output-dir=/metadata -jar /app/s3.jar")
          .start();

      AmazonS3 s3 = AmazonS3ClientBuilder.standard()
          .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:" + port, "local"))
          .enablePathStyleAccess()
          .withClientConfiguration(new ClientConfiguration()
              .withConnectionTimeout(5000)
              .withSocketTimeout(5000))
          .build();

      // Hit all LocalS3 APIs, cover as many classes as possible to generate reachability metadata.
      run(s3);

      // Hit all S3 Vectors APIs to generate reachability metadata.
      runS3Vectors(port);

      // Stop the container.
      try(StopContainerCmd cmd = container.getDockerClient().stopContainerCmd(container.getContainerId())) {
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
          .withCommand( "java -DMODE=IN_MEMORY -agentlib:native-image-agent=config-merge-dir=/metadata -jar /app/s3.jar")
          .start();

      // Stop the container.
      try(StopContainerCmd cmd = container.getDockerClient().stopContainerCmd(container.getContainerId())) {
        cmd.exec();
      }
    }

  }

  /**
   * Hit all LocalS3 APIs, cover as many classes as possible.
   */
  static void run(AmazonS3 s3) {
    String bucketName = "my-bucket";
    s3.createBucket(new CreateBucketRequest(bucketName, com.amazonaws.services.s3.model.Region.AF_CapeTown));
    s3.listBuckets();
    s3.getBucketLocation(bucketName);
    s3.putObject(bucketName, "my-object", "Hello World!");
    s3.getObject(bucketName, "my-object");
    s3.listObjects(bucketName);
    s3.deleteObject(bucketName, "my-object");
    s3.setBucketVersioningConfiguration(new SetBucketVersioningConfigurationRequest(bucketName, new BucketVersioningConfiguration("Enabled")));
    s3.listObjectsV2(bucketName);

    s3.putObject(bucketName, "my-object", "Hello World!");
    s3.deleteObject(bucketName, "my-object");
    s3.putObject(bucketName, "my-object", "Hello World!");
    s3.listVersions(bucketName, "my-object");
    s3.copyObject(bucketName, "my-object", bucketName, "my-object-copy");
    s3.setObjectTagging(new SetObjectTaggingRequest(bucketName, "my-object",
        new ObjectTagging(List.of(new Tag("k1", "v1")))));
    s3.getObjectTagging(new GetObjectTaggingRequest(bucketName, "my-object"));
    s3.deleteObjectTagging(new DeleteObjectTaggingRequest(bucketName, "my-object"));

    s3.initiateMultipartUpload(new InitiateMultipartUploadRequest(bucketName, "my-object"));

    ObjectMetadata objectMetadata1 = new ObjectMetadata();
    objectMetadata1.setContentType("plain/text");
    InitiateMultipartUploadResult initResult =
        s3.initiateMultipartUpload(new InitiateMultipartUploadRequest(bucketName, "my-object", objectMetadata1));

    UploadPartRequest part1 = new UploadPartRequest()
        .withBucketName(bucketName)
        .withKey("my-object")
        .withUploadId(initResult.getUploadId())
        .withPartNumber(1)
        .withInputStream(new ByteArrayInputStream("Hello".getBytes()))
        .withPartSize(5L)
        .withLastPart(true);

    UploadPartRequest part2 = new UploadPartRequest()
        .withBucketName(bucketName)
        .withKey("my-object")
        .withUploadId(initResult.getUploadId())
        .withPartNumber(2)
        .withInputStream(new ByteArrayInputStream("World".getBytes()))
        .withPartSize(5L)
        .withLastPart(true);

    s3.uploadPart(part1);
    s3.uploadPart(part2);
    s3.listParts(new ListPartsRequest(bucketName, "my-object", initResult.getUploadId()));
    s3.completeMultipartUpload(new CompleteMultipartUploadRequest(bucketName, "my-object", initResult.getUploadId(), List.of(
        new PartETag(1, ""),
        new PartETag(2, "")
    )));
    s3.initiateMultipartUpload(new InitiateMultipartUploadRequest(bucketName, "my-object"));
    s3.abortMultipartUpload(new AbortMultipartUploadRequest(bucketName, "my-object", initResult.getUploadId()));

    SetBucketEncryptionRequest setBucketEncryptionRequest = new SetBucketEncryptionRequest();
    setBucketEncryptionRequest.setBucketName(bucketName);
    setBucketEncryptionRequest.setServerSideEncryptionConfiguration(new ServerSideEncryptionConfiguration()
        .withRules(new ServerSideEncryptionRule().withBucketKeyEnabled(true)
            .withApplyServerSideEncryptionByDefault(new ServerSideEncryptionByDefault()
                .withSSEAlgorithm("AES256").withKMSMasterKeyID("arn:aws:kms:us-east-1:1234/5678example"))));

    assertDoesNotThrow(() -> s3.setBucketEncryption(setBucketEncryptionRequest));
    s3.setBucketEncryption(setBucketEncryptionRequest);
    s3.getBucketEncryption(bucketName);
    s3.deleteBucketEncryption(bucketName);

    s3.setBucketPolicy(bucketName, "policy");
    s3.getBucketPolicy(bucketName);
    s3.deleteBucketPolicy(bucketName);

    ReplicationDestinationConfig destinationConfig =
        new ReplicationDestinationConfig().withBucketARN("arn:aws:s3:::exampletargetbucket");
    s3.setBucketReplicationConfiguration(bucketName, new BucketReplicationConfiguration()
        .addRule("1", new ReplicationRule().withDestinationConfig(destinationConfig)
            .withPriority(1).withDeleteMarkerReplication(new DeleteMarkerReplication().withStatus("Disabled"))));
    s3.getBucketReplicationConfiguration(bucketName);
    s3.deleteBucketReplicationConfiguration(bucketName);

    s3.setBucketTaggingConfiguration(bucketName, new BucketTaggingConfiguration(List.of(new TagSet(Map.of("key", "value")))));
    s3.getBucketTaggingConfiguration(bucketName);
    s3.deleteBucketTaggingConfiguration(bucketName);

    s3.headBucket(new HeadBucketRequest(bucketName));
    s3.getObjectMetadata(bucketName, "my-object");

    s3.deleteObjects(new DeleteObjectsRequest(bucketName).withKeys(List.of(new DeleteObjectsRequest.KeyVersion("my-object", "version"))));

    assertThrows(AmazonS3Exception.class, () -> s3.deleteBucket("not-exist-bucket"));

    assertThrows(AmazonS3Exception.class, () -> {
      s3.copyPart(new CopyPartRequest().withUploadId(initResult.getUploadId())
          .withPartNumber(1)
          .withSourceBucketName(bucketName)
          .withSourceKey("my-object")
          .withDestinationBucketName(bucketName)
          .withDestinationKey("my-object-copy"));
    }); // not implemented yet
  }

  /**
   * Hit all S3 Vectors APIs, cover as many classes as possible.
   */
  static void runS3Vectors(int port) {
    S3VectorsClient vectorsClient = S3VectorsClient.builder()
        .region(software.amazon.awssdk.regions.Region.US_EAST_1)
        .credentialsProvider(AnonymousCredentialsProvider.create())
        .endpointOverride(URI.create("http://localhost:" + port))
        .build();

    String vectorBucketName = "test-vector-bucket";
    String indexName = "test-index";

    try {
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
