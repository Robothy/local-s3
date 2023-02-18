package com.robothy.s3.core.model.internal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.robothy.s3.core.converters.deserializer.VersionedObjectMetadataMapConverter;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import lombok.Data;

/**
 * Represents local-s3 Object metadata.
 */
@Data
public class ObjectMetadata {

  /**
   * When bucket versioning is not enabled, the object version ID is "null".
   */
  public static final String NULL_VERSION = "null";

  /**
   * The most recent instance on the top.
   */
  @JsonDeserialize(converter = VersionedObjectMetadataMapConverter.class)
  private ConcurrentSkipListMap<String, VersionedObjectMetadata> versionedObjectMap =
      new ConcurrentSkipListMap<>(Comparator.reverseOrder());

  /**
   * Represents a virtual version when bucket versioning is not enabled.
   * The virtual version is an internal field, which is used for sorting
   * versions. It will be mapped to string "null" when returned to the client.
   */
  private String virtualVersion;


  /**
   * Construct an {@linkplain ObjectMetadata} instance. A new {@linkplain ObjectMetadata} instance must
   * associate with a {@linkplain VersionedObjectMetadata}.
   *
   * @param version the version ID. If the bucket versioning is not enabled, it
   *                should be set to virtual version via {@linkplain #setVirtualVersion(String)}.
   * @param firstVersion first versioned instance of the {@linkplain ObjectMetadata}.
   */
  public ObjectMetadata(String version, VersionedObjectMetadata firstVersion) {
    this.putVersionedObjectMetadata(version, firstVersion);
  }

  /**
   * No args constructor for jackson. Do not use this constructor.
   */
  public ObjectMetadata() {

  }


  /**
   * Get a {@linkplain VersionedObjectMetadata} instance by version ID.
   *
   * @param versionId version ID of the {@linkplain VersionedObjectMetadata}.
   * @return the {@linkplain VersionedObjectMetadata} object.
   */
  public Optional<VersionedObjectMetadata> getVersionedObjectMetadata(String versionId) {
    return Optional.ofNullable(versionedObjectMap.get(versionId));
  }

  /**
   * The same as {@code  getVersionedObjectMap().put(versionId, versionedObjectMetadata)}.
   *
   * @param versionId version ID.
   * @param versionedObjectMetadata versioned object metadata instance.
   */
  public void putVersionedObjectMetadata(String versionId, VersionedObjectMetadata versionedObjectMetadata) {
    this.versionedObjectMap.put(versionId, versionedObjectMetadata);
  }

  /**
   * Get the latest versioned object. Always exists.
   */
  @JsonIgnore
  public VersionedObjectMetadata getLatest() {
    return versionedObjectMap.firstEntry().getValue();
  }

  /**
   * Get the latest version ID of this object. It may be a virtual version.
   *
   * @return the latest version of this object.
   */
  @JsonIgnore
  public String getLatestVersion() {
    return versionedObjectMap.firstKey();
  }

  /**
   * Get the virtual version of this object.
   *
   * @return the virtual version of the object.
   */
  public Optional<String> getVirtualVersion() {
    return Optional.ofNullable(virtualVersion);
  }
}
