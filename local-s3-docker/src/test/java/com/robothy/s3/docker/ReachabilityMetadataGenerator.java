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
import java.util.List;
import java.util.Map;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

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
    s3.createBucket(new CreateBucketRequest(bucketName, Region.AF_CapeTown));
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
