package com.robothy.s3.rest.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.robothy.s3.datatypes.response.S3Object;
import lombok.Builder;

import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JacksonXmlRootElement(localName = "ListBucketResult")
public class ListBucketV2Result {

    @JacksonXmlProperty(localName = "IsTruncated")
    private boolean isTruncated;

    @JacksonXmlProperty(localName = "Contents")
    private List<S3Object> contents;

    @JacksonXmlProperty(localName = "Name")
    private String name;

    @JacksonXmlProperty(localName = "Prefix")
    private String prefix;

    @JacksonXmlProperty(localName = "Delimiter")
    private String delimiter;

    @JacksonXmlProperty(localName = "MaxKeys")
    private int maxKeys;

    @JacksonXmlProperty(localName = "CommonPrefixes")
    private List<CommonPrefix> commonPrefixes;

    @JacksonXmlProperty(localName = "EncodingType")
    private String encodingType;

    @JacksonXmlProperty(localName = "KeyCount")
    private int keyCount;

    @JacksonXmlProperty(localName = "ContinuationToken")
    private String continuationToken;

    @JacksonXmlProperty(localName = "NextContinuationToken")
    private String nextContinuationToken;

    @JacksonXmlProperty(localName = "StartAfter")
    private String startAfter;
}
