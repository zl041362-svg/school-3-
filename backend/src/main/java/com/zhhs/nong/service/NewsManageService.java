package com.zhhs.nong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhhs.nong.common.PageUtils;
import com.zhhs.nong.common.exception.BizException;
import com.zhhs.nong.dto.admin.SaveNewsAdminRequest;
import com.zhhs.nong.mapper.NewsMapper;
import com.zhhs.nong.model.News;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class NewsManageService {

    private final NewsMapper newsMapper;
    private final OperationLogService logService;

    public NewsManageService(NewsMapper newsMapper, OperationLogService logService) {
        this.newsMapper = newsMapper;
        this.logService = logService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getNews(Integer page, Integer pageSize) {
        List<News> all = newsMapper.selectList(new LambdaQueryWrapper<News>()
                .orderByDesc(News::getId));
        return PageUtils.pageResponse(PageUtils.slice(all, page, pageSize, 20), all.size(), page, pageSize, 20);
    }

    @Transactional
    public News update(Long id, SaveNewsAdminRequest request, String operator) {
        News news = newsMapper.selectById(id);
        if (news == null) {
            throw new BizException(4042, "news not found");
        }
        news.setTitle(request.title());
        if (request.category() != null) news.setCategory(request.category());
        if (request.summary() != null) news.setSummary(request.summary());
        if (request.content() != null) news.setContent(request.content());
        if (request.author() != null) news.setAuthor(request.author());
        if (request.status() != null) news.setStatus(request.status());
        news.setUpdatedAt(LocalDateTime.now());
        newsMapper.updateById(news);
        logService.log(operator, "update_news", "编辑资讯 " + id);
        return news;
    }

    @Transactional
    public News updateStatus(Long id, String status, String operator) {
        News news = newsMapper.selectById(id);
        if (news == null) {
            throw new BizException(4042, "news not found");
        }
        news.setStatus(status);
        if ("published".equals(status) && news.getPublishedAt() == null) {
            news.setPublishedAt(LocalDateTime.now());
        }
        news.setUpdatedAt(LocalDateTime.now());
        newsMapper.updateById(news);
        logService.log(operator, "update_news_status", "资讯 " + id + " 状态更新为 " + status);
        return news;
    }
}
