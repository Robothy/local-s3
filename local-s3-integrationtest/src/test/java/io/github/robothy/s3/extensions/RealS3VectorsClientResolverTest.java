package io.github.robothy.s3.extensions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.robothy.s3.RealS3;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3vectors.S3VectorsClient;

/**
 * Unit tests for RealS3VectorsClientResolver.
 */
class RealS3VectorsClientResolverTest {

  @Test
  @SuppressWarnings("unchecked")
  void supportsParameter_withS3VectorsClient_returnsTrue() throws ParameterResolutionException {
    RealS3VectorsClientResolver resolver = new RealS3VectorsClientResolver();
    ParameterContext parameterContext = mock(ParameterContext.class);
    ExtensionContext extensionContext = mock(ExtensionContext.class);
    java.lang.reflect.Parameter parameter = mock(java.lang.reflect.Parameter.class);
    
    when(parameterContext.getParameter()).thenReturn(parameter);
    when(parameter.getType()).thenReturn((Class) S3VectorsClient.class);

    boolean result = resolver.supportsParameter(parameterContext, extensionContext);

    assertTrue(result);
  }

  @Test
  @SuppressWarnings("unchecked")
  void supportsParameter_withS3Client_returnsFalse() throws ParameterResolutionException {
    RealS3VectorsClientResolver resolver = new RealS3VectorsClientResolver();
    ParameterContext parameterContext = mock(ParameterContext.class);
    ExtensionContext extensionContext = mock(ExtensionContext.class);
    java.lang.reflect.Parameter parameter = mock(java.lang.reflect.Parameter.class);
    
    when(parameterContext.getParameter()).thenReturn(parameter);
    when(parameter.getType()).thenReturn((Class) S3Client.class);

    boolean result = resolver.supportsParameter(parameterContext, extensionContext);

    assertFalse(result);
  }

  @Test
  @SuppressWarnings("unchecked")
  void supportsParameter_withStringParameter_returnsFalse() throws ParameterResolutionException {
    RealS3VectorsClientResolver resolver = new RealS3VectorsClientResolver();
    ParameterContext parameterContext = mock(ParameterContext.class);
    ExtensionContext extensionContext = mock(ExtensionContext.class);
    java.lang.reflect.Parameter parameter = mock(java.lang.reflect.Parameter.class);
    
    when(parameterContext.getParameter()).thenReturn(parameter);
    when(parameter.getType()).thenReturn((Class) String.class);

    boolean result = resolver.supportsParameter(parameterContext, extensionContext);

    assertFalse(result);
  }

  @Test
  void createClient_withBasicConfiguration_createsS3VectorsClient() {
    System.setProperty(AbstractRealS3ParameterResolver.AWS_ACCESS_KEY_ENV, "fake-access-key");
    System.setProperty(AbstractRealS3ParameterResolver.AWS_SECRET_KEY_ENV, "fake-secret-key");
    
    RealS3VectorsClientResolver resolver = new RealS3VectorsClientResolver();
    RealS3 config = createMockRealS3Config("us-east-1", "", "", "", "", false);

    Object client = resolver.createClient(config);

    assertNotNull(client);
    assertInstanceOf(S3VectorsClient.class, client);
    
    S3VectorsClient s3VectorsClient = (S3VectorsClient) client;
    assertDoesNotThrow(s3VectorsClient::close);
    
    System.clearProperty(AbstractRealS3ParameterResolver.AWS_ACCESS_KEY_ENV);
    System.clearProperty(AbstractRealS3ParameterResolver.AWS_SECRET_KEY_ENV);
  }

  @Test
  void createClient_withCustomEndpoint_createsS3VectorsClientWithEndpoint() {
    System.setProperty(AbstractRealS3ParameterResolver.AWS_ACCESS_KEY_ENV, "fake-access-key");
    System.setProperty(AbstractRealS3ParameterResolver.AWS_SECRET_KEY_ENV, "fake-secret-key");
    
    RealS3VectorsClientResolver resolver = new RealS3VectorsClientResolver();
    String customEndpoint = "https://custom-s3-endpoint.com";
    RealS3 config = createMockRealS3Config("us-west-2", "", "", "", customEndpoint, false);

    Object client = resolver.createClient(config);

    assertNotNull(client);
    assertInstanceOf(S3VectorsClient.class, client);
    
    S3VectorsClient s3VectorsClient = (S3VectorsClient) client;
    assertDoesNotThrow(s3VectorsClient::close);
    
    System.clearProperty(AbstractRealS3ParameterResolver.AWS_ACCESS_KEY_ENV);
    System.clearProperty(AbstractRealS3ParameterResolver.AWS_SECRET_KEY_ENV);
  }

  @Test
  void createClient_withCredentials_createsS3VectorsClient() {
    RealS3VectorsClientResolver resolver = new RealS3VectorsClientResolver();
    RealS3 config = createMockRealS3Config("eu-west-1", "test-access-key", "test-secret-key", "", "", false);

    Object client = resolver.createClient(config);

    assertNotNull(client);
    assertInstanceOf(S3VectorsClient.class, client);
    
    S3VectorsClient s3VectorsClient = (S3VectorsClient) client;
    assertDoesNotThrow(s3VectorsClient::close);
  }

  @Test
  void createClient_withSessionToken_createsS3VectorsClient() {
    RealS3VectorsClientResolver resolver = new RealS3VectorsClientResolver();
    RealS3 config = createMockRealS3Config("ap-southeast-1", "test-access-key", "test-secret-key", "test-session-token", "", false);

    Object client = resolver.createClient(config);

    assertNotNull(client);
    assertInstanceOf(S3VectorsClient.class, client);
    
    S3VectorsClient s3VectorsClient = (S3VectorsClient) client;
    assertDoesNotThrow(s3VectorsClient::close);
  }

  @Test
  void createClient_pathStyleAccessIgnored_createsS3VectorsClient() {
    System.setProperty(AbstractRealS3ParameterResolver.AWS_ACCESS_KEY_ENV, "fake-access-key");
    System.setProperty(AbstractRealS3ParameterResolver.AWS_SECRET_KEY_ENV, "fake-secret-key");
    
    RealS3VectorsClientResolver resolver = new RealS3VectorsClientResolver();
    RealS3 config = createMockRealS3Config("us-east-1", "", "", "", "", true);

    Object client = resolver.createClient(config);

    assertNotNull(client);
    assertInstanceOf(S3VectorsClient.class, client);
    
    S3VectorsClient s3VectorsClient = (S3VectorsClient) client;
    assertDoesNotThrow(s3VectorsClient::close);
    
    System.clearProperty(AbstractRealS3ParameterResolver.AWS_ACCESS_KEY_ENV);
    System.clearProperty(AbstractRealS3ParameterResolver.AWS_SECRET_KEY_ENV);
  }

  /**
   * Integration test using the @RealS3 annotation
   */
  @RealS3(region = "us-east-1")
  @Test
  void integrationTest_withRealS3Annotation_injectsS3VectorsClient(S3VectorsClient client) {
    assertNotNull(client);
    assertInstanceOf(S3VectorsClient.class, client);
  }

  /**
   * Integration test with custom endpoint
   */
  @RealS3(region = "us-west-2", endpointUrl = "https://s3.us-west-2.amazonaws.com")
  @Test
  void integrationTest_withCustomEndpoint_injectsS3VectorsClient(S3VectorsClient client) {
    assertNotNull(client);
    assertInstanceOf(S3VectorsClient.class, client);
  }

  /**
   * Creates a mock RealS3 configuration for testing
   */
  private RealS3 createMockRealS3Config(String region, String accessKey, String secretKey, 
                                        String sessionToken, String endpointUrl, boolean pathStyleAccess) {
    return new RealS3() {
      @Override
      public Class<? extends java.lang.annotation.Annotation> annotationType() {
        return RealS3.class;
      }

      @Override
      public String region() {
        return region;
      }

      @Override
      public String accessKey() {
        return accessKey;
      }

      @Override
      public String secretKey() {
        return secretKey;
      }

      @Override
      public String sessionToken() {
        return sessionToken;
      }

      @Override
      public String endpointUrl() {
        return endpointUrl;
      }

      @Override
      public boolean pathStyleAccess() {
        return pathStyleAccess;
      }
    };
  }
}
