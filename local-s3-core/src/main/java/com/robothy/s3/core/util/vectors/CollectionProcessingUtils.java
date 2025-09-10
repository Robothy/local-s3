package com.robothy.s3.core.util.vectors;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility class for processing collections in S3 Vectors operations.
 * Provides filtering, sorting, and pagination logic.
 */
public class CollectionProcessingUtils {

  /**
   * Applies prefix filtering to a list of items.
   * 
   * @param items the items to filter
   * @param prefix the prefix to filter by
   * @param nameExtractor function to extract name from each item
   * @param <T> the type of items
   * @return filtered list
   */
  public static <T> List<T> applyPrefixFilter(List<T> items, String prefix, 
      java.util.function.Function<T, String> nameExtractor) {
    if (!ValidationUtils.shouldApplyPrefixFilter(prefix)) {
      return items;
    }
    
    return items.stream()
        .filter(item -> nameExtractor.apply(item).startsWith(prefix))
        .collect(Collectors.toList());
  }

  /**
   * Sorts items by name for consistent ordering.
   * 
   * @param items the items to sort
   * @param nameExtractor function to extract name from each item
   * @param <T> the type of items
   * @return sorted list (modifies original list)
   */
  public static <T> List<T> sortByName(List<T> items, java.util.function.Function<T, String> nameExtractor) {
    items.sort(Comparator.comparing(nameExtractor));
    return items;
  }

  /**
   * Applies pagination to a list of items.
   * 
   * @param items the items to paginate
   * @param startIndex the starting index
   * @param maxResults the maximum number of results
   * @param <T> the type of items
   * @return paginated list
   */
  public static <T> List<T> applyPagination(List<T> items, int startIndex, int maxResults) {
    return items.stream()
        .skip(startIndex)
        .limit(maxResults)
        .collect(Collectors.toList());
  }
}
