package me.mmtr.koala.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import me.mmtr.koala.model.Article;
import me.mmtr.koala.model.ArticleRating;
import me.mmtr.koala.model.User;
import me.mmtr.koala.repository.dao.ArticleDAO;
import me.mmtr.koala.repository.dao.ArticleRatingDAO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/ratings")
public class ArticleRatingController {

    private final ArticleRatingDAO articleRatingDAO;
    private final ArticleDAO articleDAO;

    public ArticleRatingController(ArticleRatingDAO articleRatingDAO, ArticleDAO articleDAO) {
        this.articleRatingDAO = articleRatingDAO;
        this.articleDAO = articleDAO;
    }

    @PostMapping("/new")
    public String newArticleRating(HttpServletRequest request, HttpSession session,
                                   @RequestParam(name = "rating") int rating,
                                   @RequestParam Long ratedArticleId, Model model) {
        User user = (User) session.getAttribute("principalUser");
        Article article = articleDAO.findById(ratedArticleId);

        ArticleRating articleRating = new ArticleRating();
        articleRating.setArticle(article);
        articleRating.setUser(user);
        articleRating.setRating(rating);

        articleRatingDAO.create(articleRating);
        model.addAttribute("requestURI", request.getRequestURI());
        return "redirect:/articles/view/" + article.getId();
    }

    @PostMapping("/delete/{articleRatingId}")
    public String deleteArticleRating(@PathVariable Long articleRatingId,
                                      @RequestParam Long articleId) {
        articleRatingDAO.deleteById(articleRatingId);
        return "redirect:/articles/view/" + articleId;
    }

    @GetMapping("/all")
    public String allArticleRatings(Model model) {
        model.addAttribute("articleRatings", articleRatingDAO.findAll());
        return "article-ratings";
    }
}
