package com.robothy.s3.test;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.robothy.s3.jupiter.LocalS3;
import com.robothy.s3.jupiter.LocalS3Endpoint;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class VirtualHostIntegrationTest {

  @Test
  @LocalS3
  void testVirtualHostWithLocalEndpoint(LocalS3Endpoint endpoint) {
    AmazonS3ClientBuilder builder = AmazonS3Client.builder()
        .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("local-s3-access-key", "local-s3-secret-key")))
        .withClientConfiguration(new ClientConfiguration()
            .withDnsResolver(host -> InetAddress.getAllByName("localhost"))
            .withConnectionTimeout(1000)
            .withSocketTimeout(1000));
    builder.setEndpointConfiguration(endpoint.toAmazonS3EndpointConfiguration());

    AmazonS3 client = builder.build();
    client.createBucket("my-bucket");
    assertTrue(client.doesBucketExistV2("my-bucket"));
  }

  @Test
  @LocalS3
  void testVirtualHostWithAwsLegacyGlobalEndpoint(LocalS3Endpoint endpoint) {
    AmazonS3ClientBuilder builder = AmazonS3Client.builder()
        .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("local-s3-access-key", "local-s3-secret-key")))
        .withClientConfiguration(new ClientConfiguration()
            .withDnsResolver(host -> InetAddress.getAllByName("localhost"))
            .withConnectionTimeout(1000)
            .withSocketTimeout(1000));
    builder.setEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://s3.amazonaws.com:" + endpoint.port(), "local"));
    AmazonS3 s3 = builder.build();
    s3.createBucket("my-bucket");
    assertTrue(s3.doesBucketExistV2("my-bucket"));

    s3.putObject("my-bucket", "my-key", "Hello, World!");
    assertTrue(s3.doesObjectExist("my-bucket", "my-key"));
  }

  @Test
  @LocalS3
  void testVirtualHostWithAwsEndpoint(LocalS3Endpoint endpoint) {
    AmazonS3ClientBuilder builder = AmazonS3Client.builder()
        .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("local-s3-access-key", "local-s3-secret-key")))
        .withClientConfiguration(new ClientConfiguration()
            .withDnsResolver(host -> InetAddress.getAllByName("localhost"))
            .withConnectionTimeout(1000)
            .withSocketTimeout(1000))
        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://s3.ap-southeast-1.amazonaws.com:" + endpoint.port(), "region1"));
    AmazonS3 s3 = builder.build();
    s3.createBucket("my-bucket");
    assertTrue(s3.doesBucketExistV2("my-bucket"));
    s3.putObject("my-bucket", "my-key", "Hello, World!");
    assertTrue(s3.doesObjectExist("my-bucket", "my-key"));
  }

}
