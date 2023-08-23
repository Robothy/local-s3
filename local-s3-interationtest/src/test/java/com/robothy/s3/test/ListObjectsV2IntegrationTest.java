package com.robothy.s3.test;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.robothy.s3.jupiter.LocalS3;
import org.junit.jupiter.api.Test;

public class ListObjectsV2IntegrationTest {

    @LocalS3
    @Test
    void testWithContinuationToken(AmazonS3 s3) {
        s3.listObjectsV2(new ListObjectsV2Request().withContinuationToken("test"));
    }

}
