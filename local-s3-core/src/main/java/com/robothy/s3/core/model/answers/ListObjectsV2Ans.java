package com.robothy.s3.core.model.answers;

import com.robothy.s3.datatypes.response.S3Object;
import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Builder
@Data
public class ListObjectsV2Ans {

    private String delimiter;

    private String encodingType;

    private boolean isTruncated;

    private int keyCount;

    private int maxKeys;

    private String nextContinuationToken;

    private String prefix;

    private String startAfter;

    @Builder.Default
    private List<S3Object> objects = Collections.emptyList();

    @Builder.Default
    private List<String> commonPrefixes = Collections.emptyList();


    public Optional<String> getNextContinuationToken() {
        return Optional.ofNullable(nextContinuationToken);
    }

}
