package com.zhhs.nong.controller;
import com.zhhs.nong.model.News;
import com.zhhs.nong.service.NewsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
@RestController
@RequestMapping("/api/news")
public class NewsController {
    private final NewsService newsService;
    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }
    @GetMapping
    public Map<String, Object> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) {
        return newsService.list(page, pageSize, category, keyword);
    }
    @GetMapping("/{id}")
    public News detail(@PathVariable Long id) {
        return newsService.detail(id);
    }
}
