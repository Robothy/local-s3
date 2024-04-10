package com.robothy.s3.rest.utils;

import com.robothy.s3.rest.model.request.BucketRegion;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.Set;

// bucket-name.s3.{region-id}.{domain}
public class VirtualHostParser {

  static Set<String> SUPPORTED_DOMAINS = Set.of(".localhost", ".amazonaws.com", ".local");

  public static Optional<BucketRegion> getBucketRegionFromHost(String host) {
    if (StringUtils.isBlank(host)) {
      return Optional.empty();
    }

    host = host.trim();
    Optional<String> supportedDomainOpt = SUPPORTED_DOMAINS.stream().filter(host::endsWith)
        .map(domainWithDotPrefix -> domainWithDotPrefix.substring(1)).findFirst();
    if (!supportedDomainOpt.isPresent()) {
      return Optional.empty();
    }

    String domain = supportedDomainOpt.get();
    if (isLegacyGlobalEndpoint(host, domain)) { // {bucketName}.s3.{domain}
      return parseLegacyEndpoint(host, domain);
    }


    String hostWithoutDomain = host.substring(0, host.length() - domain.length() - 1);
    Optional<String> regionOpt = getRegion(hostWithoutDomain);
    if (!regionOpt.isPresent()) {
      return Optional.empty();
    }

    String region = regionOpt.get();
    String s3RegionDomain = ".s3." + region + "." + domain;
    if (s3RegionDomain.equals("." + host)) {
      // s3.{region}.{domain}
      return Optional.of(new BucketRegion(region, null));
    }

    // {bucketName}.s3.{region}.{domain}
    String bucketName = host.substring(0, host.length() - s3RegionDomain.length());
    return Optional.of(new BucketRegion(region, bucketName));
  }

  private static Optional<BucketRegion> parseLegacyEndpoint(String host, String domain) {
    int bucketNameLength = host.length() - ".s3.".length() - domain.length();
    if (bucketNameLength > 0) {
      return Optional.of(new BucketRegion("local", host.substring(0, bucketNameLength)));
    }
    // .s3.{domain}
    return Optional.empty();
  }


  static Optional<String> getRegion(String hostWithoutDomain) {
    if (hostWithoutDomain.startsWith("s3.")) {
      // s3.{region}.domain
      return Optional.of(hostWithoutDomain.substring(3));
    }

    int regionDelimiterIdx = hostWithoutDomain.lastIndexOf(".s3.");
    if (regionDelimiterIdx <= 0) {
      // .s3.{region}.domain
      return Optional.empty();
    }
    return Optional.of(hostWithoutDomain.substring(regionDelimiterIdx + ".s3.".length()));
  }

  /**
   * <a href="https://docs.aws.amazon.com/AmazonS3/latest/userguide/VirtualHosting.html#VirtualHostingBackwardsCompatibility">Backward compatibility</a>
   */
  static boolean isLegacyGlobalEndpoint(String hostWithDomain, String domain) {
    String s3WithDomain = ".s3." + domain;
    return hostWithDomain.endsWith(s3WithDomain);
  }

}
