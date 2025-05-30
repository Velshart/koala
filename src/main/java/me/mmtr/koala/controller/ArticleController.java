package me.mmtr.koala.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import me.mmtr.koala.model.Article;
import me.mmtr.koala.model.User;
import me.mmtr.koala.repository.UserRepository;
import me.mmtr.koala.repository.dao.ArticleDAO;
import me.mmtr.koala.util.FormatUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping(path = "/articles")
public class ArticleController {
    private final ArticleDAO articleDAO;
    private final UserRepository userRepository;

    public ArticleController(ArticleDAO articleDAO, UserRepository userRepository) {
        this.articleDAO = articleDAO;
        this.userRepository = userRepository;
    }

    @GetMapping("/user-articles/{username}")
    public String articles(@PathVariable String username, HttpServletRequest request,
                           @RequestParam(required = false) String keyword,
                           Model model) {
        User user = userRepository.findByName(username).orElseThrow();

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

        model.addAttribute("username", user.getName());
        model.addAttribute("articles", articles);
        model.addAttribute("requestURI", request.getRequestURI());
        model.addAttribute("searchRedirectionURI", request.getRequestURI());
        return "user-articles";
    }

    @GetMapping("/new")
    public String newArticle(HttpSession session, Model model) {
        User user = (User) session.getAttribute("principalUser");

        model.addAttribute("username", Objects.requireNonNull(user).getName());
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
    public String updateArticle(HttpSession session, @PathVariable Long articleId, Model model) {
        User user = (User) session.getAttribute("principalUser");

        Article article = articleDAO.findById(articleId);
        model.addAttribute("username", Objects.requireNonNull(user).getName());
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
    public String deleteArticle(@PathVariable Long articleId, HttpSession session) {
        this.articleDAO.deleteById(articleId);
        return String.format("redirect:/articles/user-articles/%s",
                ((User) session.getAttribute("principalUser")).getName());
    }

    @GetMapping("/view/{articleId}")
    public String viewArticle(HttpSession session, @RequestParam(required = false) String requestURI,
                              @PathVariable Long articleId, Model model) {
        Article article = articleDAO.findById(articleId);
        User user = (User) session.getAttribute("principalUser");

        model.addAttribute("username", user.getName());
        model.addAttribute("article", article);
        //model.addAttribute("requestURI", requestURI);

        model.addAttribute("isPrincipalAnAuthor", user.getName().equals(article.getAuthor()));
        model.addAttribute("isArticleRatedByPrincipal", article.isArticleRatedByUser(user));
        model.addAttribute("ratingFromPrincipal", article.getRatingFrom(user));

        model.addAttribute("articleChapters",
                article.mapArticleChapterListToIndexedArticleChapterList());
        return "article-view";
    }
}
