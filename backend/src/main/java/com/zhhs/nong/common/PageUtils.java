package com.zhhs.nong.common;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PageUtils {

    private PageUtils() {}

    public static <T> List<T> slice(List<T> items, Integer page, Integer pageSize, int defaultPageSize) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? defaultPageSize : Math.min(pageSize, 100);
        int from = Math.min((safePage - 1) * safePageSize, items.size());
        int to = Math.min(from + safePageSize, items.size());
        return new ArrayList<>(items.subList(from, to));
    }

    public static Map<String, Object> pageResponse(List<?> items, int total, Integer page, Integer pageSize, int defaultPageSize) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", items);
        result.put("total", total);
        result.put("page", page == null || page < 1 ? 1 : page);
        result.put("pageSize", pageSize == null || pageSize < 1 ? defaultPageSize : Math.min(pageSize, 100));
        return result;
    }
}
