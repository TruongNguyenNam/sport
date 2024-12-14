package com.example.storesports.infrastructure.utils;

import org.springframework.data.domain.Page;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for handling pagination logic in RESTful APIs.
 */
public class PageUtils {

    /**
     * Validates the page number, ensuring it is non-negative.
     *
     * @param page the requested page number.
     * @return a valid page number (>= 0).
     * @throws PaginationException if the page number is negative.
     */
    public static int validatePageNumber(int page) {
        if (page < 0) {
            throw new PaginationException("Page number cannot be negative.");
        }
        return page;
    }

    /**
     * Validates the page size, ensuring it falls within allowed limits.
     *
     * @param size the requested page size.
     * @param defaultSize the default size if size is invalid.
     * @return a valid page size.
     * @throws PaginationException if the size is invalid.
     */
    public static int validatePageSize(int size, int defaultSize) {
        if (size <= 0) {
            if (defaultSize <= 0) {
                throw new PaginationException("Default page size must be greater than 0.");
            }
            return defaultSize;
        }
        if (size > 100) {
            throw new PaginationException("Page size cannot exceed 100.");
        }
        return size;
    }

    /**
     * Constructs a standardized response for paginated results.
     *
     * @param <T>      the type of data in the page.
     * @param pageData the page object containing data and metadata.
     * @return a map containing pagination details and the current page content.
     * @throws PaginationException if the pageData is null.
     */
    public static <T> Map<String, Object> createPageResponse(Page<T> pageData) {
        if (pageData == null) {
            throw new PaginationException("Page data cannot be null.");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("content", pageData.getContent());
        response.put("currentPage", pageData.getNumber());
        response.put("totalItems", pageData.getTotalElements());
        response.put("totalPages", pageData.getTotalPages());
        response.put("pageSize", pageData.getSize());
        response.put("isFirstPage", pageData.isFirst());
        response.put("isLastPage", pageData.isLast());
        return response;
    }

    /**
     * Custom exception for pagination-related errors.
     */
    public static class PaginationException extends RuntimeException {
        public PaginationException(String message) {
            super(message);
        }
    }
}
