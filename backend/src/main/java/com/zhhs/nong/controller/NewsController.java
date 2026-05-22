package com.zhhs.nong.controller;
import com.zhhs.nong.model.News;
import com.zhhs.nong.service.NewsService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    @PostMapping("/{id}/favorite")
    public Map<String, Object> toggleFavorite(Authentication authentication, @PathVariable Long id) {
        Long userId = Long.parseLong(String.valueOf(authentication.getPrincipal()));
        Map<String, Object> result = newsService.toggleFavorite(userId, id);
        boolean isFav = (boolean) result.get("favorited");
        return result;
    }
    @GetMapping("/{id}/favorited")
    public Map<String, Object> isFavorited(Authentication authentication, @PathVariable Long id) {
        Long userId = Long.parseLong(String.valueOf(authentication.getPrincipal()));
        return Map.of("favorited", newsService.isFavorited(userId, id));
    }
    @GetMapping("/favorites")
    public Map<String, Object> favorites(Authentication authentication,
                                          @RequestParam(required = false) Integer page,
                                          @RequestParam(required = false) Integer pageSize) {
        Long userId = Long.parseLong(String.valueOf(authentication.getPrincipal()));
        return newsService.getFavorites(userId, page, pageSize);
    }
}
