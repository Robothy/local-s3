package com.robothy.s3.jupiter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.Test;

/**
 * Integration test for the health check endpoint, see issue #292.
 * The request carries no credentials and requires no bucket to exist.
 */
@LocalS3
class HealthCheckIntegrationTest {

  @Test
  void healthEndpointReturns200WithoutCredentials(LocalS3Endpoint endpoint) throws Exception {
    HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:" + endpoint.port() + "/_health"))
        .GET()
        .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(200, response.statusCode());
    assertEquals("OK", response.body());
  }

}
