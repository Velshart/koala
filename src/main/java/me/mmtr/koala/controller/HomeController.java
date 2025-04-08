package me.mmtr.koala.controller;

import jakarta.servlet.http.HttpSession;
import me.mmtr.koala.data.Article;
import me.mmtr.koala.data.User;
import me.mmtr.koala.repository.UserRepository;
import me.mmtr.koala.repository.dao.ArticleDAO;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {
    private final UserRepository userRepository;
    private final ArticleDAO articleDAO;

    public HomeController(UserRepository userRepository, ArticleDAO articleDAO) {
        this.userRepository = userRepository;
        this.articleDAO = articleDAO;
    }

    @GetMapping("/home")
    public String home(HttpSession session, OAuth2AuthenticationToken authenticationToken, Model model) {
        OAuth2User principal = authenticationToken.getPrincipal();

        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();
        List<Article> userArticles = articleDAO.findAll()
                .stream()
                        .filter(article -> article.getAuthor().equals(user.getName()))
                                .toList();

        session.setAttribute("principalUser", user);
        model.addAttribute("userArticles", userArticles);
        model.addAttribute("user", user);

        return "home";
    }
}
