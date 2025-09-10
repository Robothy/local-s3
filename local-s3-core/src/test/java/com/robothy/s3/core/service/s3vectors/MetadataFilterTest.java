package com.robothy.s3.core.service.s3vectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import com.robothy.s3.core.model.internal.s3vectors.VectorObjectMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for MetadataFilter implementation.
 * Tests various S3 Vectors metadata filtering operators according to AWS specification.
 */
class MetadataFilterTest {

    private ObjectMapper objectMapper;
    private List<VectorObjectMetadata> testVectors;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        testVectors = createTestVectors();
    }

    private List<VectorObjectMetadata> createTestVectors() {
        // Vector 1: Documentary, year 2020, genres array
        ObjectNode metadata1 = objectMapper.createObjectNode();
        metadata1.put("genre", "documentary");
        metadata1.put("year", 2020);
        metadata1.set("categories", objectMapper.createArrayNode().add("film").add("educational"));
        metadata1.put("rating", 8.5);
        metadata1.put("available", true);
        VectorObjectMetadata vector1 = new VectorObjectMetadata("vector1", 3, 1L, metadata1);

        // Vector 2: Drama, year 2019, no categories
        ObjectNode metadata2 = objectMapper.createObjectNode();
        metadata2.put("genre", "drama");
        metadata2.put("year", 2019);
        metadata2.put("rating", 7.8);
        metadata2.put("available", false);
        VectorObjectMetadata vector2 = new VectorObjectMetadata("vector2", 3, 2L, metadata2);

        // Vector 3: Comedy, year 2021, genres array including documentary
        ObjectNode metadata3 = objectMapper.createObjectNode();
        metadata3.put("genre", "comedy");
        metadata3.put("year", 2021);
        metadata3.set("categories", objectMapper.createArrayNode().add("documentary").add("humor"));
        metadata3.put("rating", 6.2);
        metadata3.put("available", true);
        VectorObjectMetadata vector3 = new VectorObjectMetadata("vector3", 3, 3L, metadata3);

        // Vector 4: No metadata (null)
        VectorObjectMetadata vector4 = new VectorObjectMetadata("vector4", 3, 4L, null);

        return Arrays.asList(vector1, vector2, vector3, vector4);
    }

    @Test
    void testNullOrEmptyFilter() {
        // Null filter should return all vectors
        List<VectorObjectMetadata> result = MetadataFilter.applyFilter(testVectors, null);
        assertEquals(4, result.size());

        // Empty object filter should return all vectors
        ObjectNode emptyFilter = objectMapper.createObjectNode();
        result = MetadataFilter.applyFilter(testVectors, emptyFilter);
        assertEquals(4, result.size());
    }

    @Test
    void testSimpleEquality() throws Exception {
        // Test implicit $eq (documentary)
        ObjectNode filter = objectMapper.createObjectNode();
        filter.put("genre", "documentary");

        List<VectorObjectMetadata> result = MetadataFilter.applyFilter(testVectors, filter);
        assertEquals(1, result.size());
        assertEquals("vector1", result.get(0).getVectorId());
    }

    @Test
    void testExplicitEquality() throws Exception {
        // Test explicit $eq
        ObjectNode filter = objectMapper.createObjectNode();
        ObjectNode genreFilter = objectMapper.createObjectNode();
        genreFilter.put("$eq", "drama");
        filter.set("genre", genreFilter);

        List<VectorObjectMetadata> result = MetadataFilter.applyFilter(testVectors, filter);
        assertEquals(1, result.size());
        assertEquals("vector2", result.get(0).getVectorId());
    }

    @Test
    void testNotEqual() throws Exception {
        // Test $ne (not equal to documentary)
        ObjectNode filter = objectMapper.createObjectNode();
        ObjectNode genreFilter = objectMapper.createObjectNode();
        genreFilter.put("$ne", "documentary");
        filter.set("genre", genreFilter);

        List<VectorObjectMetadata> result = MetadataFilter.applyFilter(testVectors, filter);
        assertEquals(2, result.size()); // drama and comedy, but not the one with null metadata
        assertTrue(result.stream().anyMatch(v -> v.getVectorId().equals("vector2")));
        assertTrue(result.stream().anyMatch(v -> v.getVectorId().equals("vector3")));
    }

    @Test
    void testNumericComparisons() throws Exception {
        // Test $gt (year > 2019)
        ObjectNode filter = objectMapper.createObjectNode();
        ObjectNode yearFilter = objectMapper.createObjectNode();
        yearFilter.put("$gt", 2019);
        filter.set("year", yearFilter);

        List<VectorObjectMetadata> result = MetadataFilter.applyFilter(testVectors, filter);
        assertEquals(2, result.size()); // 2020 and 2021
        assertTrue(result.stream().anyMatch(v -> v.getVectorId().equals("vector1")));
        assertTrue(result.stream().anyMatch(v -> v.getVectorId().equals("vector3")));

        // Test $lte (rating <= 7.8)
        filter = objectMapper.createObjectNode();
        ObjectNode ratingFilter = objectMapper.createObjectNode();
        ratingFilter.put("$lte", 7.8);
        filter.set("rating", ratingFilter);

        result = MetadataFilter.applyFilter(testVectors, filter);
        assertEquals(2, result.size()); // drama (7.8) and comedy (6.2)
    }

    @Test
    void testInOperator() throws Exception {
        // Test $in
        ObjectNode filter = objectMapper.createObjectNode();
        ObjectNode genreFilter = objectMapper.createObjectNode();
        ArrayNode genreArray = objectMapper.createArrayNode();
        genreArray.add("comedy");
        genreArray.add("documentary");
        genreFilter.set("$in", genreArray);
        filter.set("genre", genreFilter);

        List<VectorObjectMetadata> result = MetadataFilter.applyFilter(testVectors, filter);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(v -> v.getVectorId().equals("vector1")));
        assertTrue(result.stream().anyMatch(v -> v.getVectorId().equals("vector3")));
    }

    @Test
    void testNotInOperator() throws Exception {
        // Test $nin
        ObjectNode filter = objectMapper.createObjectNode();
        ObjectNode genreFilter = objectMapper.createObjectNode();
        ArrayNode genreArray = objectMapper.createArrayNode();
        genreArray.add("comedy");
        genreArray.add("documentary");
        genreFilter.set("$nin", genreArray);
        filter.set("genre", genreFilter);

        List<VectorObjectMetadata> result = MetadataFilter.applyFilter(testVectors, filter);
        assertEquals(1, result.size());
        assertEquals("vector2", result.get(0).getVectorId()); // only drama
    }

    @Test
    void testExistsOperator() throws Exception {
        // Test $exists true
        ObjectNode filter = objectMapper.createObjectNode();
        ObjectNode categoriesFilter = objectMapper.createObjectNode();
        categoriesFilter.put("$exists", true);
        filter.set("categories", categoriesFilter);

        List<VectorObjectMetadata> result = MetadataFilter.applyFilter(testVectors, filter);
        assertEquals(2, result.size()); // vector1 and vector3 have categories
        assertTrue(result.stream().anyMatch(v -> v.getVectorId().equals("vector1")));
        assertTrue(result.stream().anyMatch(v -> v.getVectorId().equals("vector3")));

        // Test $exists false
        categoriesFilter.put("$exists", false);
        result = MetadataFilter.applyFilter(testVectors, filter);
        assertEquals(2, result.size()); // vector2 and vector4 don't have categories
        assertTrue(result.stream().anyMatch(v -> v.getVectorId().equals("vector2")));
        assertTrue(result.stream().anyMatch(v -> v.getVectorId().equals("vector4")));
    }

    @Test
    void testArrayEquality() throws Exception {
        // Test equality with array field - should match if any element equals target
        ObjectNode filter = objectMapper.createObjectNode();
        filter.put("categories", "documentary");

        List<VectorObjectMetadata> result = MetadataFilter.applyFilter(testVectors, filter);
        assertEquals(1, result.size());
        assertEquals("vector3", result.get(0).getVectorId()); // has documentary in categories array
    }

    @Test
    void testAndOperator() throws Exception {
        // Test $and: genre = documentary AND year >= 2020
        ObjectNode filter = objectMapper.createObjectNode();
        ArrayNode andArray = objectMapper.createArrayNode();

        ObjectNode genreCondition = objectMapper.createObjectNode();
        genreCondition.put("genre", "documentary");
        andArray.add(genreCondition);

        ObjectNode yearCondition = objectMapper.createObjectNode();
        ObjectNode yearFilter = objectMapper.createObjectNode();
        yearFilter.put("$gte", 2020);
        yearCondition.set("year", yearFilter);
        andArray.add(yearCondition);

        filter.set("$and", andArray);

        List<VectorObjectMetadata> result = MetadataFilter.applyFilter(testVectors, filter);
        assertEquals(1, result.size());
        assertEquals("vector1", result.get(0).getVectorId());
    }

    @Test
    void testOrOperator() throws Exception {
        // Test $or: genre = documentary OR year = 2021
        ObjectNode filter = objectMapper.createObjectNode();
        ArrayNode orArray = objectMapper.createArrayNode();

        ObjectNode genreCondition = objectMapper.createObjectNode();
        genreCondition.put("genre", "documentary");
        orArray.add(genreCondition);

        ObjectNode yearCondition = objectMapper.createObjectNode();
        yearCondition.put("year", 2021);
        orArray.add(yearCondition);

        filter.set("$or", orArray);

        List<VectorObjectMetadata> result = MetadataFilter.applyFilter(testVectors, filter);
        assertEquals(2, result.size()); // vector1 (documentary) and vector3 (2021)
        assertTrue(result.stream().anyMatch(v -> v.getVectorId().equals("vector1")));
        assertTrue(result.stream().anyMatch(v -> v.getVectorId().equals("vector3")));
    }

    @Test
    void testMultipleConditionsOnSameField() throws Exception {
        // Test price range: rating >= 7.0 AND rating <= 8.0
        ObjectNode filter = objectMapper.createObjectNode();
        ObjectNode ratingFilter = objectMapper.createObjectNode();
        ratingFilter.put("$gte", 7.0);
        ratingFilter.put("$lte", 8.0);
        filter.set("rating", ratingFilter);

        List<VectorObjectMetadata> result = MetadataFilter.applyFilter(testVectors, filter);
        assertEquals(1, result.size());
        assertEquals("vector2", result.get(0).getVectorId()); // rating 7.8
    }

    @Test
    void testBooleanFiltering() throws Exception {
        // Test boolean equality
        ObjectNode filter = objectMapper.createObjectNode();
        filter.put("available", true);

        List<VectorObjectMetadata> result = MetadataFilter.applyFilter(testVectors, filter);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(v -> v.getVectorId().equals("vector1")));
        assertTrue(result.stream().anyMatch(v -> v.getVectorId().equals("vector3")));
    }

    @Test
    void testInvalidFilters() {
        // Test invalid $and (not an array)
        ObjectNode filter1 = objectMapper.createObjectNode();
        filter1.put("$and", "invalid");

        assertThrows(LocalS3VectorException.class, () -> 
            MetadataFilter.applyFilter(testVectors, filter1));

        // Test invalid $or (empty array)
        ObjectNode filter2 = objectMapper.createObjectNode();
        filter2.set("$or", objectMapper.createArrayNode());

        assertThrows(LocalS3VectorException.class, () -> 
            MetadataFilter.applyFilter(testVectors, filter2));

        // Test invalid $in (not an array)
        ObjectNode filter3 = objectMapper.createObjectNode();
        ObjectNode genreFilter = objectMapper.createObjectNode();
        genreFilter.put("$in", "invalid");
        filter3.set("genre", genreFilter);

        assertThrows(LocalS3VectorException.class, () -> 
            MetadataFilter.applyFilter(testVectors, filter3));

        // Test invalid $exists (not boolean)
        ObjectNode filter4 = objectMapper.createObjectNode();
        ObjectNode existsFilter = objectMapper.createObjectNode();
        existsFilter.put("$exists", "invalid");
        filter4.set("genre", existsFilter);

        assertThrows(LocalS3VectorException.class, () -> 
            MetadataFilter.applyFilter(testVectors, filter4));

        // Test unsupported operator
        ObjectNode filter5 = objectMapper.createObjectNode();
        ObjectNode invalidFilter = objectMapper.createObjectNode();
        invalidFilter.put("$invalid", "value");
        filter5.set("genre", invalidFilter);

        assertThrows(LocalS3VectorException.class, () -> 
            MetadataFilter.applyFilter(testVectors, filter5));
    }

    @Test
    void testNumericComparisonWithNonNumericField() {
        // Test numeric comparison on non-numeric field should throw exception
        ObjectNode filter = objectMapper.createObjectNode();
        ObjectNode genreFilter = objectMapper.createObjectNode();
        genreFilter.put("$gt", 5);
        filter.set("genre", genreFilter);

        assertThrows(LocalS3VectorException.class, () -> 
            MetadataFilter.applyFilter(testVectors, filter));
    }

    @Test
    void testEmptyMetadata() throws Exception {
        // Test filter on vector with null metadata
        ObjectNode filter = objectMapper.createObjectNode();
        filter.put("genre", "documentary");

        List<VectorObjectMetadata> result = MetadataFilter.applyFilter(testVectors, filter);
        // Should not include vector4 which has null metadata
        assertFalse(result.stream().anyMatch(v -> v.getVectorId().equals("vector4")));
    }
}
