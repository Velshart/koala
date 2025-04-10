package me.mmtr.koala.controller;

import jakarta.servlet.http.HttpSession;
import me.mmtr.koala.data.Article;
import me.mmtr.koala.data.User;
import me.mmtr.koala.repository.dao.ArticleDAO;
import me.mmtr.koala.util.FormatUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping(path = "/articles")
public class ArticleController {
    private final ArticleDAO articleDAO;

    public ArticleController(ArticleDAO articleDAO) {
        this.articleDAO = articleDAO;
    }

    @GetMapping("/new")
    public String newArticle(Model model) {
        model.addAttribute("article", new Article());
        return "new-article";
    }

    @PostMapping("/new")
    public String saveNewArticle(HttpSession session, @ModelAttribute("article") Article article) {
        User user = (User) session.getAttribute("principalUser");

        article.setAuthor(user.getName());
        article.setCreatedAt(FormatUtil.formatDateTime(LocalDateTime.now()));
        this.articleDAO.create(article);
        return "redirect:/articles/view/" + article.getId();
    }

    @GetMapping("/update/{articleId}")
    public String updateArticle(@PathVariable Long articleId, Model model) {
        Article article = articleDAO.findById(articleId);
        model.addAttribute("article", article);
        return "update-article";
    }

    @PostMapping("/update/{articleId}")
    public String updateArticle(HttpSession session,
                                @PathVariable Long articleId,
                                @RequestParam String createdAt,
                                @ModelAttribute("article") Article article) {
        User user = (User) session.getAttribute("principalUser");
        Article articleBeforeChange = articleDAO.findById(articleId);

        article.setId(articleId);
        article.setAuthor(user.getName());
        article.setCreatedAt(createdAt);
        article.setChapters(articleBeforeChange.getChapters());
        this.articleDAO.update(article);
        return "redirect:/articles/view/" + articleId;
    }

    @PostMapping("/delete/{articleId}")
    public String deleteArticle(@PathVariable Long articleId) {
        this.articleDAO.deleteById(articleId);
        return "redirect:/home";
    }

    @GetMapping("/view/{articleId}")
    public String viewArticle(@PathVariable Long articleId, Model model) {
        Article article = articleDAO.findById(articleId);

        model.addAttribute("article", article);
        model.addAttribute("articleChapters", article.getChapters());
        return "article-view";
    }
}
