package me.mmtr.koala.controller;

import me.mmtr.koala.model.Article;
import me.mmtr.koala.model.ArticleChapter;
import me.mmtr.koala.repository.dao.ArticleChapterDAO;
import me.mmtr.koala.repository.dao.ArticleDAO;
import me.mmtr.koala.util.FormatUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

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

        articleChapter.setCreatedAt(FormatUtil.formatDateTime(LocalDateTime.now()));
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
    public String updateArticleChapter(@PathVariable Long articleId,
                                       @PathVariable Long articleChapterId,
                                       @RequestParam String createdAt,
                                       @RequestParam int articleIndex,
                                       @ModelAttribute ArticleChapter articleChapter) {


        articleChapter.setId(articleChapterId);
        articleChapter.setArticle(articleDAO.findById(articleId));
        articleChapter.setCreatedAt(createdAt);
        articleChapterDAO.update(articleChapter);

        return String.format("redirect:/article-chapters/view/%s/%s", articleId, articleIndex);
    }

    @PostMapping("/delete/{articleChapterId}")
    public String deleteArticleChapter(@RequestParam Long articleId, @PathVariable Long articleChapterId) {
        articleChapterDAO.deleteById(articleChapterId);
        return "redirect:/articles/view/" + articleId;
    }

    @GetMapping("/view/{articleId}/{index}")
    public String articleChapterView(@PathVariable Long articleId,
                                     @PathVariable int index, Model model) {
        List<ArticleChapter> articleChapters = articleDAO.findById(articleId).getChapters();

        if (index < 0) index = 0;
        if (index >= articleChapters.size()) index = articleChapters.size() - 1;

        model.addAttribute("articleId", articleId);
        model.addAttribute("articleChapter", articleChapters.get(index));
        model.addAttribute("currentIndex", index);
        model.addAttribute("previousIndex", index - 1);
        model.addAttribute("nextIndex", index + 1);
        model.addAttribute("hasPrevious", index > 0);
        model.addAttribute("hasNext", index < articleChapters.size() - 1);
        return "article-chapter-view";
    }
}
