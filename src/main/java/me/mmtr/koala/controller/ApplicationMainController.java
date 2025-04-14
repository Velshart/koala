package me.mmtr.koala.controller;

import jakarta.servlet.http.HttpSession;
import me.mmtr.koala.model.Article;
import me.mmtr.koala.model.User;
import me.mmtr.koala.repository.UserRepository;
import me.mmtr.koala.repository.dao.ArticleDAO;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ApplicationMainController {
    private final UserRepository userRepository;
    private final ArticleDAO articleDAO;

    public ApplicationMainController(UserRepository userRepository, ArticleDAO articleDAO) {
        this.userRepository = userRepository;
        this.articleDAO = articleDAO;
    }

    @GetMapping("/home")
    public String home(HttpSession session, OAuth2AuthenticationToken authenticationToken, Model model) {
        OAuth2User principal = authenticationToken.getPrincipal();

        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email).orElse(null);
        List<Article> userArticles;
        if (user != null) {
             userArticles = articleDAO.findAll()
                    .stream()
                    .filter(article -> article.getAuthor().equals(user.getName()))
                    .toList();
        }else {
            userArticles = new ArrayList<>();
        }
        session.setAttribute("principalUser", user);
        session.setAttribute("authTokenPrincipal", authenticationToken.getPrincipal());
        model.addAttribute("userArticles", userArticles);
        model.addAttribute("user", user);
        return "home";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        OAuth2User principal = (OAuth2User) session.getAttribute("authTokenPrincipal");

        model.addAttribute("authTokenPrincipalName", principal.getAttribute("name"));
        model.addAttribute("authTokenPrincipalEmail", principal.getAttribute("email"));
        model.addAttribute("authTokenPrincipalPicture", principal.getAttribute("picture"));

        return "profile";
    }
}
