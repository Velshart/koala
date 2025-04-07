package me.mmtr.koala.controller;

import jakarta.transaction.Transactional;
import me.mmtr.koala.data.Article;
import me.mmtr.koala.data.ArticleChapter;
import me.mmtr.koala.repository.dao.ArticleChapterDAO;
import me.mmtr.koala.repository.dao.ArticleDAO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping(path = "/article-chapters")
public class ArticleChapterController {

    private final ArticleDAO articleDAO;
    private final ArticleChapterDAO articleChapterDAO;

    public ArticleChapterController(ArticleDAO articleDAO, ArticleChapterDAO articleChapterDAO) {
        this.articleDAO = articleDAO;
        this.articleChapterDAO = articleChapterDAO;
    }

    @GetMapping("/new/{articleId}")
    public String newArticleChapter(@PathVariable Long articleId, Model model) {
        model.addAttribute("articleChapter", new ArticleChapter());
        model.addAttribute("articleId", articleId);

        return "new-article-chapter";
    }

    @PostMapping("/new/{articleId}")
    public String saveArticleChapter(@PathVariable Long articleId,
                                     @ModelAttribute("articleChapter") ArticleChapter articleChapter) {
        Article article = articleDAO.findById(articleId);

        articleChapter.setCreatedAt(LocalDateTime.now());
        article.addChapter(articleChapter);

        article.addChapter(articleChapter);

        articleDAO.update(article);

        return "redirect:/articles/view/" + articleId;
    }

    @GetMapping("/update/{articleId}/{articleChapterId}")
    public String updateArticleChapter(@PathVariable Long articleId,
                                       @PathVariable Long articleChapterId,
                                       Model model) {
        ArticleChapter articleChapter = articleChapterDAO.findById(articleChapterId);
        model.addAttribute("articleChapter", articleChapter);
        model.addAttribute("articleId", articleId);

        return "update-article-chapter";
    }

    @PostMapping("/update/{articleId}/{articleChapterId}")
    public String updateArticleChapter(@PathVariable Long articleId, ArticleChapter articleChapter) {
        articleChapter.setCreatedAt(LocalDateTime.now());
        articleChapterDAO.update(articleChapter);

        return "redirect:/articles/view/" + articleId;
    }

    @PostMapping("/delete/{articleChapterId}")
    public String deleteArticleChapter(@RequestParam Long articleId, @PathVariable Long articleChapterId) {
        articleChapterDAO.deleteById(articleChapterId);
        return "redirect:/articles/view/" + articleId;
    }

    @GetMapping("/view/{articleChapterId}")
    @Transactional
    public String viewArticleChapter(@PathVariable Long articleChapterId, @RequestParam Long articleId, Model model) {
        ArticleChapter articleChapter = articleChapterDAO.findById(articleChapterId);

        model.addAttribute("articleChapter", articleChapter);
        model.addAttribute("articleId", articleId);

        return "article-chapter-view";
    }
}
