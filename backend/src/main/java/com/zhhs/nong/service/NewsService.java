package com.zhhs.nong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhhs.nong.common.exception.BizException;
import com.zhhs.nong.mapper.FavoriteMapper;
import com.zhhs.nong.mapper.NewsMapper;
import com.zhhs.nong.model.Favorite;
import com.zhhs.nong.model.News;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.zhhs.nong.common.PageUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NewsService {

    private final NewsMapper newsMapper;
    private final FavoriteMapper favoriteMapper;

    public NewsService(NewsMapper newsMapper, FavoriteMapper favoriteMapper) {
        this.newsMapper = newsMapper;
        this.favoriteMapper = favoriteMapper;
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

    @Transactional
    public Map<String, Object> toggleFavorite(Long userId, Long newsId) {
        News news = newsMapper.selectById(newsId);
        if (news == null) {
            throw new BizException(4042, "news not found");
        }
        Favorite existing = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getNewsId, newsId));
        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            return Map.of("favorited", false);
        }
        Favorite f = new Favorite();
        f.setUserId(userId);
        f.setNewsId(newsId);
        f.setCreatedAt(LocalDateTime.now());
        favoriteMapper.insert(f);
        return Map.of("favorited", true);
    }

    @Transactional(readOnly = true)
    public boolean isFavorited(Long userId, Long newsId) {
        return favoriteMapper.exists(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getNewsId, newsId));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getFavorites(Long userId, Integer page, Integer pageSize) {
        List<Favorite> favs = favoriteMapper.selectList(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getId));
        List<Long> newsIds = favs.stream().map(Favorite::getNewsId).toList();
        Map<Long, News> newsMap = newsIds.isEmpty() ? Map.of() :
                newsMapper.selectBatchIds(newsIds).stream().collect(Collectors.toMap(News::getId, n -> n));

        List<Map<String, Object>> items = new ArrayList<>();
        for (Favorite f : favs) {
            News n = newsMap.get(f.getNewsId());
            if (n != null) {
                items.add(Map.of("newsId", n.getId(), "title", n.getTitle(), "category", n.getCategory(), "createdAt", f.getCreatedAt()));
            }
        }
        List<Map<String, Object>> paged = PageUtils.slice(items, page, pageSize, 20);
        return PageUtils.pageResponse(paged, items.size(), page, pageSize, 20);
    }
}

