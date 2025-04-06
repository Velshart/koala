package me.mmtr.koala.controller;

import me.mmtr.koala.data.Article;
import me.mmtr.koala.repository.dao.ArticleDAO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public String saveNewArticle(@ModelAttribute("article") Article article) {
        this.articleDAO.create(article);
        return "redirect:/home";
    }

    @GetMapping("/update/{articleId}")
    public String updateArticle(@PathVariable Long articleId, Model model) {
        Article article = articleDAO.findById(articleId);
        model.addAttribute("article", article);
        return "update-article";
    }

    @PostMapping("/update/{articleId}")
    public String updateArticle(@ModelAttribute("article") Article article) {
        this.articleDAO.update(article);
        return "redirect:/home";
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
        return "article-view";
    }
}
