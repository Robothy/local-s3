package com.robothy.s3.core.model;

import com.robothy.s3.core.exception.LocalS3InvalidArgumentException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Builder
@Getter
@EqualsAndHashCode
public class ContinuationParameters {

    private Character delimiter;

    private String encodingType;

    private boolean fetchOwner;

    private int maxKeys;

    private String prefix;

    private String startAfter;


    public String encode() {
        String joined = Stream.of(encodeDelimiter(), encodeEncodingType(),
                encodeFetchOwner(), encodeMaxKeys(),
                encodePrefix(), encodeStartAfter())
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.joining("/"));

        String hashAppended = joined + "/" + joined.hashCode();
        return Base64.getEncoder().encodeToString(hashAppended.getBytes(StandardCharsets.UTF_8));
    }

    private String encodeDelimiter() {
        return Objects.isNull(delimiter) ? "" : 1 + URLEncoder.encode(delimiter + "", StandardCharsets.UTF_8);
    }

    private String encodeEncodingType() {
        return Objects.isNull(encodingType) ? "" : 2 + URLEncoder.encode(encodingType, StandardCharsets.UTF_8);
    }

    private String encodeFetchOwner() {
        return fetchOwner ? "3" : "";
    }

    private String encodeMaxKeys() {
        return 4 + "" + maxKeys;
    }

    private String encodePrefix() {
        return Objects.isNull(prefix) ? "" : 5 + URLEncoder.encode(prefix, StandardCharsets.UTF_8);
    }

    private String encodeStartAfter() {
        return Objects.isNull(startAfter) ? "" : 6 + URLEncoder.encode(startAfter, StandardCharsets.UTF_8);
    }

    public static ContinuationParameters decode(String continuationToken) {
        String hashAppended = ensureContinuationTokenIsBase64Encoded(continuationToken);
        String joined = verifyHash(hashAppended);
        return decodeParameters(joined);
    }

    private static String ensureContinuationTokenIsBase64Encoded(String continuationToken) {
        try {
            return new String(Base64.getDecoder().decode(continuationToken), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new LocalS3InvalidArgumentException("continuation-token", continuationToken, "The continuation token provided is incorrect.");
        }
    }

    private static String verifyHash(String hashAppended) {
        int lastSlash;
        if (-1 == (lastSlash = hashAppended.lastIndexOf("/"))) {
            throw new LocalS3InvalidArgumentException("continuation-token", hashAppended, "The continuation token provided is incorrect.");
        }
        int hash = Integer.parseInt(hashAppended.substring(lastSlash + 1));
        String joined = hashAppended.substring(0, lastSlash);
        if (hash != joined.hashCode()) {
            throw new LocalS3InvalidArgumentException("continuation-token", hashAppended, "The continuation token provided is incorrect.");
        }
        return joined;
    }

    private static ContinuationParameters decodeParameters(String joined) {
        String[] parts = joined.split("/");
        if (parts.length == 0) {
            return ContinuationParameters.builder().build();
        }
        ContinuationParametersBuilder builder = ContinuationParameters.builder();
        Arrays.stream(parts).forEach(part -> {
            if (StringUtils.isBlank(part)) return;
            switch (part.charAt(0)) {
                case '1':
                    builder.delimiter(URLDecoder.decode(part.substring(1), StandardCharsets.UTF_8).charAt(0));
                    break;
                case '2':
                    builder.encodingType(URLDecoder.decode(part.substring(1), StandardCharsets.UTF_8));
                    break;
                case '3':
                    builder.fetchOwner(true);
                    break;
                case '4':
                    builder.maxKeys(Integer.parseInt(part.substring(1)));
                    break;
                case '5':
                    builder.prefix(URLDecoder.decode(part.substring(1), StandardCharsets.UTF_8));
                    break;
                case '6':
                    builder.startAfter(URLDecoder.decode(part.substring(1), StandardCharsets.UTF_8));
                    break;
                default:
                    throw new LocalS3InvalidArgumentException("continuation-token", joined, "The continuation token provided is incorrect.");
            }
        });
        return builder.build();
    }

}
