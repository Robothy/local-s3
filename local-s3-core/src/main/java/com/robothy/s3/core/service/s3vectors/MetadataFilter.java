package com.robothy.s3.core.service.s3vectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.robothy.s3.core.exception.vectors.LocalS3VectorException;
import com.robothy.s3.core.exception.vectors.LocalS3VectorErrorType;
import com.robothy.s3.core.model.internal.s3vectors.VectorObjectMetadata;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements metadata filtering for S3 Vector queries according to AWS S3 Vectors specification.
 * Supports various operators like $eq, $ne, $gt, $gte, $lt, $lte, $in, $nin, $exists, $and, $or.
 * 
 * Filter examples:
 * - Simple equality: {"genre": "documentary"} or {"genre": {"$eq": "documentary"}}
 * - Numeric comparison: {"year": {"$gt": 2019}}
 * - Array operations: {"genre": {"$in": ["comedy", "documentary"]}}
 * - Logical operations: {"$and": [{"genre": {"$eq": "drama"}}, {"year": {"$gte": 2020}}]}
 */
@Slf4j
public class MetadataFilter {

    /**
     * Apply metadata filters to a list of vector candidates.
     * 
     * @param candidateVectors List of vectors to filter
     * @param filter JsonNode representing the filter criteria (can be null)
     * @return Filtered list of vectors that match the criteria
     */
    public static List<VectorObjectMetadata> applyFilter(List<VectorObjectMetadata> candidateVectors, JsonNode filter) {
        if (filter == null || filter.isNull() || filter.isEmpty()) {
            return candidateVectors;
        }

        List<VectorObjectMetadata> result = new ArrayList<>();
        for (VectorObjectMetadata vector : candidateVectors) {
            if (matchesFilter(vector, filter)) {
                result.add(vector);
            }
        }

        return result;
    }

    /**
     * Check if a vector matches the given filter criteria.
     * 
     * @param vector Vector to check
     * @param filter Filter criteria
     * @return true if vector matches the filter
     */
    private static boolean matchesFilter(VectorObjectMetadata vector, JsonNode filter) {
        if (filter == null || filter.isNull()) {
            return true;
        }

        if (!filter.isObject()) {
            throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, 
                "Filter must be a JSON object");
        }

        ObjectNode filterObj = (ObjectNode) filter;

        // Handle logical operators
        if (filterObj.has("$and")) {
            return evaluateAndOperator(vector, filterObj.get("$and"));
        }
        if (filterObj.has("$or")) {
            return evaluateOrOperator(vector, filterObj.get("$or"));
        }

        // Handle field-based filters
        // Check all field filters
        var fieldIterator = filterObj.fieldNames();
        while (fieldIterator.hasNext()) {
            String fieldName = fieldIterator.next();
            JsonNode fieldFilter = filterObj.get(fieldName);
            
            if (!evaluateFieldFilter(vector, fieldName, fieldFilter)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Evaluate $and logical operator.
     */
    private static boolean evaluateAndOperator(VectorObjectMetadata vector, JsonNode andNode) {
        if (!andNode.isArray()) {
            throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, 
                "$and operator requires an array of filters");
        }

        ArrayNode andArray = (ArrayNode) andNode;
        if (andArray.isEmpty()) {
            throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, 
                "$and operator requires a non-empty array");
        }

        for (JsonNode subFilter : andArray) {
            if (!matchesFilter(vector, subFilter)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Evaluate $or logical operator.
     */
    private static boolean evaluateOrOperator(VectorObjectMetadata vector, JsonNode orNode) {
        if (!orNode.isArray()) {
            throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, 
                "$or operator requires an array of filters");
        }

        ArrayNode orArray = (ArrayNode) orNode;
        if (orArray.isEmpty()) {
            throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, 
                "$or operator requires a non-empty array");
        }

        for (JsonNode subFilter : orArray) {
            if (matchesFilter(vector, subFilter)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Evaluate filter for a specific field.
     */
    private static boolean evaluateFieldFilter(VectorObjectMetadata vector, String fieldName, JsonNode fieldFilter) {
        JsonNode vectorMetadata = vector.getMetadata();
        JsonNode fieldValue = vectorMetadata != null ? vectorMetadata.get(fieldName) : null;

        // Handle simple equality (implicit $eq)
        if (!fieldFilter.isObject()) {
            return evaluateEquals(fieldValue, fieldFilter);
        }

        // Handle operator-based filters
        ObjectNode filterObj = (ObjectNode) fieldFilter;
        var operatorIterator = filterObj.fieldNames();
        
        while (operatorIterator.hasNext()) {
            String operator = operatorIterator.next();
            JsonNode operatorValue = filterObj.get(operator);

            boolean operatorResult = switch (operator) {
                case "$eq" -> evaluateEquals(fieldValue, operatorValue);
                case "$ne" -> evaluateNotEquals(fieldValue, operatorValue);
                case "$gt" -> evaluateGreaterThan(fieldValue, operatorValue);
                case "$gte" -> evaluateGreaterThanOrEqual(fieldValue, operatorValue);
                case "$lt" -> evaluateLessThan(fieldValue, operatorValue);
                case "$lte" -> evaluateLessThanOrEqual(fieldValue, operatorValue);
                case "$in" -> evaluateIn(fieldValue, operatorValue);
                case "$nin" -> evaluateNotIn(fieldValue, operatorValue);
                case "$exists" -> evaluateExists(fieldValue, operatorValue);
                default -> throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, 
                    "Unsupported filter operator: " + operator);
            };

            if (!operatorResult) {
                return false;
            }
        }

        return true;
    }

    /**
     * Evaluate equality comparison.
     * If the field value is an array, returns true if any element equals the target value.
     */
    private static boolean evaluateEquals(JsonNode fieldValue, JsonNode targetValue) {
        if (fieldValue == null || fieldValue.isNull()) {
            return targetValue == null || targetValue.isNull();
        }

        if (fieldValue.isArray()) {
            // For arrays, check if any element matches
            for (JsonNode arrayElement : fieldValue) {
                if (nodesEqual(arrayElement, targetValue)) {
                    return true;
                }
            }
            return false;
        }

        return nodesEqual(fieldValue, targetValue);
    }

    /**
     * Evaluate not equal comparison.
     * Returns true only if the field exists and is not equal to the target value.
     */
    private static boolean evaluateNotEquals(JsonNode fieldValue, JsonNode targetValue) {
        // If field doesn't exist, it doesn't match $ne condition
        if (fieldValue == null || fieldValue.isNull()) {
            return false;
        }

        return !evaluateEquals(fieldValue, targetValue);
    }

    /**
     * Evaluate greater than comparison (numeric only).
     */
    private static boolean evaluateGreaterThan(JsonNode fieldValue, JsonNode targetValue) {
        if (fieldValue == null || fieldValue.isNull()) {
            return false;  // Non-existent fields don't match numeric comparisons
        }
        if (!fieldValue.isNumber()) {
            throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, 
                "Numeric comparison requires a numeric field value");
        }
        return compareNumbers(fieldValue, targetValue) > 0;
    }

    /**
     * Evaluate greater than or equal comparison (numeric only).
     */
    private static boolean evaluateGreaterThanOrEqual(JsonNode fieldValue, JsonNode targetValue) {
        if (fieldValue == null || fieldValue.isNull()) {
            return false;  // Non-existent fields don't match numeric comparisons
        }
        if (!fieldValue.isNumber()) {
            throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, 
                "Numeric comparison requires a numeric field value");
        }
        return compareNumbers(fieldValue, targetValue) >= 0;
    }

    /**
     * Evaluate less than comparison (numeric only).
     */
    private static boolean evaluateLessThan(JsonNode fieldValue, JsonNode targetValue) {
        if (fieldValue == null || fieldValue.isNull()) {
            return false;  // Non-existent fields don't match numeric comparisons
        }
        if (!fieldValue.isNumber()) {
            throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, 
                "Numeric comparison requires a numeric field value");
        }
        return compareNumbers(fieldValue, targetValue) < 0;
    }

    /**
     * Evaluate less than or equal comparison (numeric only).
     */
    private static boolean evaluateLessThanOrEqual(JsonNode fieldValue, JsonNode targetValue) {
        if (fieldValue == null || fieldValue.isNull()) {
            return false;  // Non-existent fields don't match numeric comparisons
        }
        if (!fieldValue.isNumber()) {
            throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, 
                "Numeric comparison requires a numeric field value");
        }
        return compareNumbers(fieldValue, targetValue) <= 0;
    }

    /**
     * Evaluate $in operator - check if field value is in the provided array.
     */
    private static boolean evaluateIn(JsonNode fieldValue, JsonNode arrayValue) {
        if (!arrayValue.isArray()) {
            throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, 
                "$in operator requires an array value");
        }

        ArrayNode targetArray = (ArrayNode) arrayValue;
        if (targetArray.isEmpty()) {
            throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, 
                "$in operator requires a non-empty array");
        }

        if (fieldValue == null || fieldValue.isNull()) {
            return false;
        }

        for (JsonNode targetElement : targetArray) {
            if (evaluateEquals(fieldValue, targetElement)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Evaluate $nin operator - check if field value is not in the provided array.
     * Returns true only if the field exists and is not in the array.
     */
    private static boolean evaluateNotIn(JsonNode fieldValue, JsonNode arrayValue) {
        if (!arrayValue.isArray()) {
            throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, 
                "$nin operator requires an array value");
        }

        ArrayNode targetArray = (ArrayNode) arrayValue;
        if (targetArray.isEmpty()) {
            throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, 
                "$nin operator requires a non-empty array");
        }

        // If field doesn't exist, it doesn't match $nin condition
        if (fieldValue == null || fieldValue.isNull()) {
            return false;
        }

        return !evaluateIn(fieldValue, arrayValue);
    }

    /**
     * Evaluate $exists operator - check if field exists.
     */
    private static boolean evaluateExists(JsonNode fieldValue, JsonNode shouldExist) {
        if (!shouldExist.isBoolean()) {
            throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, 
                "$exists operator requires a boolean value");
        }

        boolean fieldExists = fieldValue != null && !fieldValue.isNull();
        return fieldExists == shouldExist.booleanValue();
    }

    /**
     * Compare two numeric values.
     * Returns positive if fieldValue > targetValue, negative if fieldValue < targetValue, 0 if equal.
     */
    private static int compareNumbers(JsonNode fieldValue, JsonNode targetValue) {
        if (fieldValue == null || fieldValue.isNull() || !fieldValue.isNumber()) {
            throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, 
                "Numeric comparison requires a numeric field value");
        }

        if (targetValue == null || targetValue.isNull() || !targetValue.isNumber()) {
            throw new LocalS3VectorException(LocalS3VectorErrorType.INVALID_REQUEST, 
                "Numeric comparison requires a numeric target value");
        }

        return Double.compare(fieldValue.doubleValue(), targetValue.doubleValue());
    }

    /**
     * Check if two JsonNodes are equal, handling different numeric types.
     */
    private static boolean nodesEqual(JsonNode node1, JsonNode node2) {
        if (node1 == null && node2 == null) {
            return true;
        }
        if (node1 == null || node2 == null) {
            return false;
        }

        // Handle numeric comparisons
        if (node1.isNumber() && node2.isNumber()) {
            return Double.compare(node1.doubleValue(), node2.doubleValue()) == 0;
        }

        // Use Jackson's built-in equality for other types
        return node1.equals(node2);
    }
}
