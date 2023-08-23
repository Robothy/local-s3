package com.robothy.s3.core.model;

import com.robothy.s3.core.exception.LocalS3InvalidArgumentException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class ContinuationParametersTest {

    @Test
    void testContinuationTokenCodec() {
        ContinuationParameters parameters = ContinuationParameters.builder()
                .delimiter('/')
                .encodingType("url")
                .fetchOwner(true)
                .maxKeys(100)
                .prefix("prefix")
                .startAfter("startAfter")
                .build();
        String token = parameters.encode();
        ContinuationParameters decoded = ContinuationParameters.decode(token);
        assertEquals(parameters.getDelimiter(), decoded.getDelimiter());
        assertEquals(parameters.getEncodingType(), decoded.getEncodingType());
        assertEquals(parameters.isFetchOwner(), decoded.isFetchOwner());
        assertEquals(parameters.getMaxKeys(), decoded.getMaxKeys());
        assertEquals(parameters.getPrefix(), decoded.getPrefix());
        assertEquals(parameters.getStartAfter(), decoded.getStartAfter());

        ContinuationParameters parameters1 = ContinuationParameters.builder()
                .delimiter('#')
                .fetchOwner(false)
                .maxKeys(100)
                .prefix("/key/")
                .startAfter("/folder/key")
                .build();
        String token1 = parameters1.encode();
        ContinuationParameters decoded1 = ContinuationParameters.decode(token1);
        assertEquals(parameters1, decoded1);

        assertThrows(LocalS3InvalidArgumentException.class, () -> ContinuationParameters.decode("invalid token"));
    }

    @Test
    void testDecodeNonBase64Token() {
        assertThrows(LocalS3InvalidArgumentException.class, () -> ContinuationParameters.decode("invalid token"));
    }

    @Test
    void testBase64TokenWithoutSlash() {
        assertThrows(LocalS3InvalidArgumentException.class, () -> ContinuationParameters.decode("aGVsbG8="));
    }

    @Test
    void testBase64TokenWithInvalidHash() {
        String continuationToken = Base64.getEncoder().encodeToString("aaaaa/12".getBytes(StandardCharsets.UTF_8));
        assertThrows(LocalS3InvalidArgumentException.class, () -> ContinuationParameters.decode(continuationToken));
    }

    @Test
    void testBase64TokenWithInvalidParameter() {
        String continuationToken = Base64.getEncoder().encodeToString(("invalid/" + "invalid".hashCode()).getBytes(StandardCharsets.UTF_8));
        assertThrows(LocalS3InvalidArgumentException.class, () -> ContinuationParameters.decode(continuationToken));
    }

}