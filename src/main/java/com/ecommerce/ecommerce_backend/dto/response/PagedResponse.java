package com.ecommerce.ecommerce_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Generic pagination wrapper for all list endpoints
// @NoArgsConstructor + @AllArgsConstructor are required alongside @Builder
// for Lombok to correctly generate the builder for a generic class
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> content;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
}