package com.ecommerce.ecommerce_backend.util;

import org.springframework.stereotype.Component;

@Component
public class SlugUtil {

    // Converts "Mobile Phones" → "mobile-phones"
    // Used when creating category slugs
    public static String toSlug(String input) {
        if (input == null) return "";
        return input.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")   // remove special chars
                .replaceAll("\\s+", "-")             // spaces to hyphens
                .replaceAll("-+", "-");              // collapse multiple hyphens
    }
}