package com.robothy.s3.core.service.loader;

import static org.junit.jupiter.api.Assertions.*;

import com.robothy.s3.core.model.internal.LocalS3Metadata;
import com.robothy.s3.core.model.internal.s3vectors.LocalS3VectorsMetadata;
import com.robothy.s3.core.service.loader.vectors.S3VectorsMetadataLoader;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for MetadataLoader static factory method.
 */
class MetadataLoaderTest {

  @Test
  void create_withLocalS3MetadataClass_returnsS3MetadataLoader() {
    MetadataLoader<LocalS3Metadata> loader = MetadataLoader.create(LocalS3Metadata.class);

    assertNotNull(loader);
    assertInstanceOf(S3MetadataLoader.class, loader);
  }

  @Test
  void create_withLocalS3VectorsMetadataClass_returnsS3VectorsMetadataLoader() {
    MetadataLoader<LocalS3VectorsMetadata> loader = MetadataLoader.create(LocalS3VectorsMetadata.class);

    assertNotNull(loader);
    assertInstanceOf(S3VectorsMetadataLoader.class, loader);
  }

  @Test
  void create_withUnsupportedClass_throwsIllegalArgumentException() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> MetadataLoader.create(String.class));

    assertEquals("Unsupported metadata class: class java.lang.String", exception.getMessage());
  }

  @Test
  void create_withNullClass_throwsNullPointerException() {
    assertThrows(NullPointerException.class,
        () -> MetadataLoader.create(null));
  }

  @Test
  void create_withObjectClass_throwsIllegalArgumentException() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> MetadataLoader.create(Object.class));

    assertEquals("Unsupported metadata class: class java.lang.Object", exception.getMessage());
  }

  @Test
  void create_withInterfaceClass_throwsIllegalArgumentException() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> MetadataLoader.create(MetadataLoader.class));

    assertTrue(exception.getMessage().contains("Unsupported metadata class"));
  }

  @Test
  void create_multipleCallsWithSameClass_returnsNewInstances() {
    MetadataLoader<LocalS3Metadata> loader1 = MetadataLoader.create(LocalS3Metadata.class);
    MetadataLoader<LocalS3Metadata> loader2 = MetadataLoader.create(LocalS3Metadata.class);

    assertNotNull(loader1);
    assertNotNull(loader2);
    assertNotSame(loader1, loader2);
    assertInstanceOf(S3MetadataLoader.class, loader1);
    assertInstanceOf(S3MetadataLoader.class, loader2);
  }

  @Test
  void create_multipleCallsWithVectorsClass_returnsNewInstances() {
    MetadataLoader<LocalS3VectorsMetadata> loader1 = MetadataLoader.create(LocalS3VectorsMetadata.class);
    MetadataLoader<LocalS3VectorsMetadata> loader2 = MetadataLoader.create(LocalS3VectorsMetadata.class);

    assertNotNull(loader1);
    assertNotNull(loader2);
    assertNotSame(loader1, loader2);
    assertInstanceOf(S3VectorsMetadataLoader.class, loader1);
    assertInstanceOf(S3VectorsMetadataLoader.class, loader2);
  }

  @Test
  void versionFileName_hasCorrectValue() {
    assertEquals("version", MetadataLoader.VERSION_FILE_NAME);
  }
}
