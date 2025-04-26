package me.mmtr.koala.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import me.mmtr.koala.model.Article;
import me.mmtr.koala.model.User;
import me.mmtr.koala.repository.dao.ArticleDAO;
import me.mmtr.koala.util.FormatUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping(path = "/articles")
public class ArticleController {
    private final ArticleDAO articleDAO;

    public ArticleController(ArticleDAO articleDAO) {
        this.articleDAO = articleDAO;
    }

    @GetMapping("/user-articles")
    public String articles(HttpServletRequest request, @RequestParam(required = false) String keyword, HttpSession session, Model model) {
        User user = (User) session.getAttribute("principalUser");

        List<Article> articles = articleDAO.findAll()
                        .stream()
                        .filter(article -> {
                            if (keyword != null) {
                                return article.getAuthor().equalsIgnoreCase(user.getName()) &&
                                        article.getTitle().contains(keyword);
                            }
                            return article.getAuthor().equalsIgnoreCase(user.getName());
                        })
                        .toList();

        model.addAttribute("articles", articles);
        model.addAttribute("requestURI", request.getRequestURI());
        return "user-articles";
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
        return "update-article-title";
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
    public String viewArticle(HttpSession session, @PathVariable Long articleId, Model model) {
        Article article = articleDAO.findById(articleId);
        User user = (User) session.getAttribute("principalUser");

        model.addAttribute("article", article);
        model.addAttribute("isPrincipalAnAuthor", user.getName().equals(article.getAuthor()));
        model.addAttribute("articleChapters",
                article.mapArticleChapterListToIndexedArticleChapterList());
        return "article-view";
    }
}
