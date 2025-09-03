package com.robothy.s3.rest.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpResponse;

/**
 * Utility class for common HTTP request/response operations in S3 Vectors controllers.
 * Provides reusable methods for request parsing and response handling.
 */
public final class HttpRequestUtils {

    private HttpRequestUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Extracts the request body as a byte array from the HTTP request.
     * Safely reads the ByteBuf without modifying its reader index.
     *
     * @param request the HTTP request
     * @return the request body as a byte array
     */
    public static byte[] extractRequestBody(HttpRequest request) {
        ByteBuf bodyBuf = request.getBody();
        byte[] bodyBytes = new byte[bodyBuf.readableBytes()];
        bodyBuf.getBytes(bodyBuf.readerIndex(), bodyBytes);
        return bodyBytes;
    }

    /**
     * Parses a request object from JSON bytes.
     * If the body is empty or contains only whitespace, returns a default instance using the builder pattern.
     *
     * @param bodyBytes the request body bytes
     * @param requestClass the class of the request object
     * @param objectMapper the JSON object mapper
     * @param <T> the type of the request object
     * @return the parsed request object or a default instance
     * @throws Exception if JSON parsing fails
     */
    public static <T> T parseRequestOrDefault(byte[] bodyBytes, Class<T> requestClass, 
                                              ObjectMapper objectMapper) throws Exception {
        if (bodyBytes.length == 0 || new String(bodyBytes).trim().isEmpty()) {
            return createDefaultRequest(requestClass);
        }
        return objectMapper.readValue(bodyBytes, requestClass);
    }

    /**
     * Parses a required request object from JSON bytes.
     * Throws an exception if the body is empty or contains only whitespace, as the request is required.
     *
     * @param bodyBytes the request body bytes
     * @param requestClass the class of the request object
     * @param objectMapper the JSON object mapper
     * @param <T> the type of the request object
     * @return the parsed request object
     * @throws Exception if JSON parsing fails or body is empty
     */
    public static <T> T parseRequiredRequest(byte[] bodyBytes, Class<T> requestClass, 
                                             ObjectMapper objectMapper) throws Exception {
        if (bodyBytes.length == 0 || new String(bodyBytes).trim().isEmpty()) {
            throw new IllegalArgumentException("Request body is required for " + requestClass.getSimpleName());
        }
        return objectMapper.readValue(bodyBytes, requestClass);
    }

    /**
     * Creates a default request instance using the builder pattern.
     * Assumes the request class has a static builder() method.
     *
     * @param requestClass the class of the request object
     * @param <T> the type of the request object
     * @return a default instance of the request
     */
    @SuppressWarnings("unchecked")
    private static <T> T createDefaultRequest(Class<T> requestClass) {
        try {
            // Use reflection to call the static builder() method and build()
            Object builder = requestClass.getMethod("builder").invoke(null);
            return (T) builder.getClass().getMethod("build").invoke(builder);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create default request for " + requestClass.getSimpleName(), e);
        }
    }

    /**
     * Sends a JSON response with the appropriate headers.
     * Adds Content-Type, Date, and AMZ Request ID headers.
     *
     * @param response the HTTP response
     * @param responseObject the object to serialize as JSON
     * @param objectMapper the JSON object mapper
     * @throws Exception if JSON serialization fails
     */
    public static void sendJsonResponse(HttpResponse response, Object responseObject, 
                                        ObjectMapper objectMapper) throws Exception {
        response.putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_JSON);
        response.write(objectMapper.writeValueAsString(responseObject));
        ResponseUtils.addDateHeader(response);
        ResponseUtils.addAmzRequestId(response);
    }

    /**
     * Sends an empty JSON response ({}) for operations that don't return data.
     * Used for delete operations and other void operations.
     *
     * @param response the HTTP response
     */
    public static void sendEmptyJsonResponse(HttpResponse response) {
        response.putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_JSON);
        response.write("{}");
        ResponseUtils.addDateHeader(response);
        ResponseUtils.addAmzRequestId(response);
    }
}
