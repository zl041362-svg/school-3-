package com.zhhs.nong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhhs.nong.common.exception.BizException;
import com.zhhs.nong.mapper.NewsMapper;
import com.zhhs.nong.model.News;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
public class NewsService {

    private final NewsMapper newsMapper;

    public NewsService(NewsMapper newsMapper) {
        this.newsMapper = newsMapper;
    }

    public Map<String, Object> list(Integer page, Integer pageSize, String category, String keyword) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);

        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<News>()
                .eq(News::getStatus, "published")
                .orderByDesc(News::getPublishedAt);

        if (StringUtils.hasText(category)) {
            wrapper.eq(News::getCategory, category);
        }

        if (StringUtils.hasText(keyword)) {
            wrapper.like(News::getTitle, keyword);
        }

        List<News> all = newsMapper.selectList(wrapper);

        int total = all.size();
        int from = Math.min((safePage - 1) * safePageSize, total);
        int to = Math.min(from + safePageSize, total);

        return Map.of(
                "items", all.subList(from, to),
                "total", total,
                "page", safePage,
                "pageSize", safePageSize
        );
    }

    public News detail(Long id) {
        News news = newsMapper.selectById(id);
        if (news == null) {
            throw new BizException(4042, "news not found");
        }
        return news;
    }
}

