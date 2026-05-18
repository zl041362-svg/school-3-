package com.zhhs.nong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhhs.nong.common.exception.BizException;
import com.zhhs.nong.mapper.NewsMapper;
import com.zhhs.nong.model.News;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.zhhs.nong.common.PageUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NewsService {

    private final NewsMapper newsMapper;

    public NewsService(NewsMapper newsMapper) {
        this.newsMapper = newsMapper;
    }

    public Map<String, Object> list(Integer page, Integer pageSize, String category, String keyword) {
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
        List<News> items = PageUtils.slice(all, page, pageSize, 20);
        return PageUtils.pageResponse(items, all.size(), page, pageSize, 20);
    }

    public News detail(Long id) {
        News news = newsMapper.selectById(id);
        if (news == null) {
            throw new BizException(4042, "news not found");
        }
        return news;
    }
}

