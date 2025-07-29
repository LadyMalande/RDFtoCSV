package com.miklosova.rdftocsvw.support;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;

public class GuavaCache {
    private final Cache<String, String> cache = CacheBuilder.newBuilder()
            .maximumSize(1000) // maximum entries
            .expireAfterWrite(1, TimeUnit.HOURS) // expire after 1 hour
            .build();

    public String get(String id) {
        try {
            return cache.get(id, () -> computeExpensiveValue(id));
        } catch (Exception e) {
            throw new RuntimeException("Failed to get value from cache", e);
        }
    }

    private String computeExpensiveValue(String id) {
        return null;
        // Expensive computation here
    }
}
