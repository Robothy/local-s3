package com.robothy.s3.rest.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import org.junit.jupiter.api.Test;

class HttpRequestUtilsTest {

  @Test
  void extractRequestBody_withValidRequest_returnsBodyBytes() {
    HttpRequest request = mock(HttpRequest.class);
    ByteBuf bodyBuf = Unpooled.wrappedBuffer("test body".getBytes());
    when(request.getBody()).thenReturn(bodyBuf);
    
    byte[] result = HttpRequestUtils.extractRequestBody(request);
    
    assertEquals("test body", new String(result));
  }

  @Test
  void extractRequestBody_withEmptyBody_returnsEmptyArray() {
    HttpRequest request = mock(HttpRequest.class);
    ByteBuf bodyBuf = Unpooled.buffer(0);
    when(request.getBody()).thenReturn(bodyBuf);
    
    byte[] result = HttpRequestUtils.extractRequestBody(request);
    
    assertEquals(0, result.length);
  }

  @Test
  void extractRequestBody_doesNotModifyReaderIndex() {
    HttpRequest request = mock(HttpRequest.class);
    ByteBuf bodyBuf = Unpooled.wrappedBuffer("test body".getBytes());
    int originalReaderIndex = bodyBuf.readerIndex();
    when(request.getBody()).thenReturn(bodyBuf);
    
    HttpRequestUtils.extractRequestBody(request);
    
    assertEquals(originalReaderIndex, bodyBuf.readerIndex());
  }

  @Test
  void parseRequestOrDefault_withValidJson_returnsDeserializedObject() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    String json = "{\"name\":\"test\",\"value\":123}";
    byte[] bodyBytes = json.getBytes();
    
    TestRequest result = HttpRequestUtils.parseRequestOrDefault(bodyBytes, TestRequest.class, objectMapper);
    
    assertEquals("test", result.getName());
    assertEquals(123, result.getValue());
  }

  @Test
  void parseRequestOrDefault_withEmptyBody_returnsDefaultInstance() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    byte[] bodyBytes = new byte[0];
    
    TestRequest result = HttpRequestUtils.parseRequestOrDefault(bodyBytes, TestRequest.class, objectMapper);
    
    assertNotNull(result);
    assertNull(result.getName());
    assertEquals(0, result.getValue());
  }

  @Test
  void parseRequestOrDefault_withInvalidJson_throwsException() {
    ObjectMapper objectMapper = new ObjectMapper();
    byte[] bodyBytes = "invalid json".getBytes();
    
    assertThrows(Exception.class, 
        () -> HttpRequestUtils.parseRequestOrDefault(bodyBytes, TestRequest.class, objectMapper));
  }

  @Test
  void parseRequiredRequest_withValidJson_returnsDeserializedObject() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    String json = "{\"name\":\"test\",\"value\":456}";
    byte[] bodyBytes = json.getBytes();
    
    TestRequest result = HttpRequestUtils.parseRequiredRequest(bodyBytes, TestRequest.class, objectMapper);
    
    assertEquals("test", result.getName());
    assertEquals(456, result.getValue());
  }

  @Test
  void parseRequiredRequest_withEmptyBody_throwsException() {
    ObjectMapper objectMapper = new ObjectMapper();
    byte[] bodyBytes = new byte[0];
    
    assertThrows(Exception.class,
        () -> HttpRequestUtils.parseRequiredRequest(bodyBytes, TestRequest.class, objectMapper));
  }

  @Test
  void parseRequiredRequest_withInvalidJson_throwsException() {
    ObjectMapper objectMapper = new ObjectMapper();
    byte[] bodyBytes = "invalid json".getBytes();
    
    assertThrows(Exception.class,
        () -> HttpRequestUtils.parseRequiredRequest(bodyBytes, TestRequest.class, objectMapper));
  }

  @Test
  void sendJsonResponse_withValidObject_setsHeadersAndWritesJson() throws Exception {
    HttpResponse response = mock(HttpResponse.class);
    ObjectMapper objectMapper = new ObjectMapper();
    TestRequest testObject = TestRequest.builder().name("test").value(789).build();
    
    when(response.putHeader(anyString(), anyString())).thenReturn(response);
    when(response.write(anyString())).thenReturn(response);
    
    HttpRequestUtils.sendJsonResponse(response, testObject, objectMapper);
    
    verify(response).putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_JSON);
    verify(response).write(contains("\"name\":\"test\""));
    verify(response).write(contains("\"value\":789"));
  }

  @Test
  void sendJsonResponse_setsContentTypeHeader() throws Exception {
    HttpResponse response = mock(HttpResponse.class);
    ObjectMapper objectMapper = new ObjectMapper();
    TestRequest testObject = new TestRequest();
    
    when(response.putHeader(anyString(), anyString())).thenReturn(response);
    when(response.write(anyString())).thenReturn(response);
    
    HttpRequestUtils.sendJsonResponse(response, testObject, objectMapper);
    
    verify(response).putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_JSON);
  }

  @Test
  void sendEmptyJsonResponse_writesEmptyJsonObject() {
    HttpResponse response = mock(HttpResponse.class);
    
    when(response.putHeader(anyString(), anyString())).thenReturn(response);
    when(response.write(anyString())).thenReturn(response);
    
    HttpRequestUtils.sendEmptyJsonResponse(response);
    
    verify(response).putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_JSON);
    verify(response).write("{}");
  }

  @Test
  void sendEmptyJsonResponse_setsContentTypeHeader() {
    HttpResponse response = mock(HttpResponse.class);
    
    when(response.putHeader(anyString(), anyString())).thenReturn(response);
    when(response.write(anyString())).thenReturn(response);
    
    HttpRequestUtils.sendEmptyJsonResponse(response);
    
    verify(response).putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_JSON);
  }

  @Test
  void parseRequestOrDefault_withWhitespaceOnlyBody_returnsDefaultInstance() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    byte[] bodyBytes = "   ".getBytes();
    
    TestRequest result = HttpRequestUtils.parseRequestOrDefault(bodyBytes, TestRequest.class, objectMapper);
    
    assertNotNull(result);
    assertNull(result.getName());
    assertEquals(0, result.getValue());
  }

  @Test
  void parseRequiredRequest_withWhitespaceOnlyBody_throwsException() {
    ObjectMapper objectMapper = new ObjectMapper();
    byte[] bodyBytes = "   ".getBytes();
    
    assertThrows(Exception.class,
        () -> HttpRequestUtils.parseRequiredRequest(bodyBytes, TestRequest.class, objectMapper));
  }

  @Test
  void sendJsonResponse_withNullObject_handlesCorrectly() throws Exception {
    HttpResponse response = mock(HttpResponse.class);
    ObjectMapper objectMapper = new ObjectMapper();
    
    when(response.putHeader(anyString(), anyString())).thenReturn(response);
    when(response.write(anyString())).thenReturn(response);
    
    HttpRequestUtils.sendJsonResponse(response, null, objectMapper);
    
    verify(response).putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_JSON);
    verify(response).write("null");
  }

  @Test
  void extractRequestBody_withLargeBody_handlesCorrectly() {
    HttpRequest request = mock(HttpRequest.class);
    String largeContent = "x".repeat(10000);
    ByteBuf bodyBuf = Unpooled.wrappedBuffer(largeContent.getBytes());
    when(request.getBody()).thenReturn(bodyBuf);
    
    byte[] result = HttpRequestUtils.extractRequestBody(request);
    
    assertEquals(largeContent, new String(result));
    assertEquals(10000, result.length);
  }

  // Test helper class
  public static class TestRequest {
    private String name;
    private int value;
    
    public TestRequest() {}
    
    public static TestRequestBuilder builder() {
      return new TestRequestBuilder();
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
    
    public static class TestRequestBuilder {
      private String name;
      private int value;
      
      public TestRequestBuilder name(String name) {
        this.name = name;
        return this;
      }
      
      public TestRequestBuilder value(int value) {
        this.value = value;
        return this;
      }
      
      public TestRequest build() {
        TestRequest request = new TestRequest();
        request.setName(this.name);
        request.setValue(this.value);
        return request;
      }
    }
  }
}
