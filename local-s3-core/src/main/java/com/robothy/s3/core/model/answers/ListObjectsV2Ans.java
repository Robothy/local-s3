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

    @Builder.Default
    private List<S3Object> objects = Collections.emptyList();

    @Builder.Default
    private List<String> commonPrefixes = Collections.emptyList();

    private String nextContinuationToken;

    public Optional<String> getNextContinuationToken() {
        return Optional.ofNullable(nextContinuationToken);
    }

}
