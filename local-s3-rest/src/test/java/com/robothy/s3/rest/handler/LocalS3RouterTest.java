package com.robothy.s3.rest.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.router.Route;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LocalS3RouterTest {

  @Test
  void match() throws IllegalAccessException {
    LocalS3Router localS3Router = new LocalS3Router();

    HttpRequestHandler handler1 = mock(HttpRequestHandler.class, "handler1");
    localS3Router.route(Route.builder()
        .method(HttpMethod.GET).path("/a")
        .paramMatcher(params -> params.containsKey("versioning"))
        .handler(handler1)
        .build());

    HttpRequestHandler handler2 = mock(HttpRequestHandler.class, "handler2");
    localS3Router.route(Route.builder()
        .method(HttpMethod.GET).path("/a")
        .paramMatcher(params -> params.containsKey("versioning"))
        .headerMatcher(headers -> headers.containsKey("x-header"))
        .handler(handler2)
        .build());

    HttpRequestHandler matchedHandler1 = localS3Router.match(HttpRequest.builder()
        .method(HttpMethod.GET).path("/a")
        .params(Map.of("versioning", List.of("true")))
        .build());
    assertSame(handler1, matchedHandler1);

    HttpRequestHandler matchedHandler2 = localS3Router.match(HttpRequest.builder()
        .method(HttpMethod.GET).path("/a")
        .params(Map.of("versioning", List.of("true")))
        .headers(Map.of("x-header", "value"))
        .build());
    assertSame(handler2, matchedHandler2);
  }

  @Test
  void matchPath() {
    LocalS3Router localS3Router = new LocalS3Router();
    Map<String, List<Route>> rules = new HashMap<>();

    List<Route> pathRule1 = mock(List.class, "pathRule1");
    List<Route> objectPathRule = mock(List.class, "objectPathRule");
    List<Route> bucketPathRule = mock(List.class, "bucketPathRule");
    rules.put("/a/b", pathRule1);
    rules.put(LocalS3Router.BUCKET_KEY_PATH, objectPathRule);
    rules.put(LocalS3Router.BUCKET_PATH, bucketPathRule);

    assertSame(pathRule1, localS3Router.matchPath(rules, HttpRequest.builder().path("/a/b/").build()));
    assertSame(pathRule1, localS3Router.matchPath(rules, HttpRequest.builder().path("/a/b").build()));

    HttpRequest bucketOperation1 = HttpRequest.builder().path("/a").build();
    assertSame(bucketPathRule, localS3Router.matchPath(rules, bucketOperation1));
    assertEquals("a", bucketOperation1.parameter("bucket").get());
    assertTrue(bucketOperation1.parameter("key").isEmpty());

    HttpRequest bucketOperation2 = HttpRequest.builder().path("/a/").build();
    assertSame(bucketPathRule, localS3Router.matchPath(rules, bucketOperation2));
    assertEquals("a", bucketOperation2.parameter("bucket").get());
    assertTrue(bucketOperation2.parameter("key").isEmpty());

    HttpRequest bucketOperation3 = HttpRequest.builder().path("/").build();
    bucketOperation3.getHeaders().put(HttpHeaderNames.HOST.toString(), "images.example.com.s3.us-east-1.amazonaws.com");
    assertSame(bucketPathRule, localS3Router.matchPath(rules, bucketOperation3));
    assertEquals("images.example.com", bucketOperation3.parameter("bucket").get());
    assertFalse(bucketOperation3.parameter("key").isPresent());

    HttpRequest objectOperation1 = HttpRequest.builder().path("/a/key").build();
    assertSame(objectPathRule, localS3Router.matchPath(rules, objectOperation1));
    assertEquals("a", objectOperation1.parameter("bucket").get());
    assertEquals("key", objectOperation1.parameter("key").get());

    HttpRequest objectOperation2 = HttpRequest.builder().path("/a/key/").build();
    assertSame(objectPathRule, localS3Router.matchPath(rules, objectOperation2));
    assertEquals("a", objectOperation2.parameter("bucket").get());
    assertEquals("key/", objectOperation2.parameter("key").get());

    HttpRequest objectOperation3 = HttpRequest.builder().path("/a/dir/a.txt").build();
    assertSame(objectPathRule, localS3Router.matchPath(rules, objectOperation3));
    assertEquals("a", objectOperation3.parameter("bucket").get());
    assertEquals("dir/a.txt", objectOperation3.parameter("key").get());

    HttpRequest objectOperation4 = HttpRequest.builder().path("/a/dir/sub-dir/").build();
    assertSame(objectPathRule, localS3Router.matchPath(rules, objectOperation4));
    assertEquals("a", objectOperation4.parameter("bucket").get());
    assertEquals("dir/sub-dir/", objectOperation4.parameter("key").get());

    HttpRequest objectOperation5 = HttpRequest.builder().path("/a/dir/sub-dir/")
        .headers(Map.of(HttpHeaderNames.HOST.toString(), "bucket1.s3.localhost")).build();
    assertSame(objectPathRule, localS3Router.matchPath(rules, objectOperation5));
    assertEquals("bucket1.s3", objectOperation5.parameter("bucket").get());
    assertEquals("a/dir/sub-dir/", objectOperation5.parameter("key").get());
  }

  @Test
  void trimPath() {
    LocalS3Router localS3Router = new LocalS3Router();
    localS3Router.trimPath("/").equals("");
    localS3Router.trimPath("/a").equals("/a");
    localS3Router.trimPath("/a/").equals("/a");
    localS3Router.trimPath("/a/b").equals("/a/b");
    localS3Router.trimPath("/a/b/ ").equals("/a/b");
  }


}