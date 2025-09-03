package io.github.robothy.s3.vectors.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.robothy.s3.jupiter.LocalS3;
import io.github.robothy.s3.RealS3;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3vectors.S3VectorsClient;
import software.amazon.awssdk.services.s3vectors.model.NotFoundException;

public class BucketPolicyIntegrationTest {

  @LocalS3
  @Test
  void testPutGetDeleteVectorBucketPolicy(S3VectorsClient vectorsClient) {
    String bucketName = "test-vector-bucket-policy-" + UUID.randomUUID().toString().substring(0, 8);

    // Create vector bucket first
    vectorsClient.createVectorBucket(b -> b.vectorBucketName(bucketName));

    try {
      String testPolicy = """
          {
              "Version": "2012-10-17",
              "Statement": [{
                      "Effect": "Allow",
                      "Action": "s3vectors:*"
                  }
              ]
          }
          """;

      // Set the policy first
      vectorsClient.putVectorBucketPolicy(request -> request
          .vectorBucketName(bucketName)
          .policy(testPolicy));

      // Verify policy exists
      String retrievedPolicy = vectorsClient.getVectorBucketPolicy(request -> request
          .vectorBucketName(bucketName)).policy();
      assertNotNull(retrievedPolicy);

      // Test deleting vector bucket policy
      assertDoesNotThrow(() -> {
        vectorsClient.deleteVectorBucketPolicy(request -> request
            .vectorBucketName(bucketName));
      });

      // Verify policy is deleted - should throw NotFoundException
      assertThrows(NotFoundException.class, () -> {
        vectorsClient.getVectorBucketPolicy(request -> request.vectorBucketName(bucketName));
      });

      // Test deleting policy for non-existent bucket should fail
      String nonExistentBucket = "non-existent-bucket-" + UUID.randomUUID().toString().substring(0, 8);
      assertThrows(NotFoundException.class, () -> {
        vectorsClient.deleteVectorBucketPolicy(request -> request.vectorBucketName(nonExistentBucket));
      });

    } finally {
      // Clean up: delete bucket (policy should already be deleted by test)
      vectorsClient.deleteVectorBucket(b -> b.vectorBucketName(bucketName));
    }
  }


}
