package me.mmtr.koala.controller;

import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Stream;

@Controller
public class ApplicationMainController {
    private final UserRepository userRepository;
    private final ArticleDAO articleDAO;

    public ApplicationMainController(UserRepository userRepository, ArticleDAO articleDAO) {
        this.userRepository = userRepository;
        this.articleDAO = articleDAO;
    }

    @GetMapping("/")
    public String home(HttpServletRequest request, @RequestParam(required = false) String keyword, HttpSession session,
                       OAuth2AuthenticationToken authenticationToken, Model model) {
        if (authenticationToken == null) {
            return "redirect:/login";
        }
        OAuth2User principal = authenticationToken.getPrincipal();

        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email).orElse(null);

        String username = "";
        if (user != null) {
            username = user.getName();
        }
        List<Article> articles;
        System.out.println("keyword: " + keyword);
        if (keyword != null) {
            articles = getAllArticlesExceptPrincipal(username)
                    .filter(article -> article.getTitle().contains(keyword))
                    .toList();
        } else {
            articles = getAllArticlesExceptPrincipal(username).toList();
        }

        session.setAttribute("principalUser", user);
        session.setAttribute("authTokenPrincipal", authenticationToken.getPrincipal());
        model.addAttribute("articles", articles);
        model.addAttribute("user", user);
        model.addAttribute("requestURI", request.getRequestURI());
        return "index";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        OAuth2User principal = (OAuth2User) session.getAttribute("authTokenPrincipal");

        model.addAttribute("authTokenPrincipalName", principal.getAttribute("name"));
        model.addAttribute("authTokenPrincipalEmail", principal.getAttribute("email"));
        model.addAttribute("authTokenPrincipalPicture", principal.getAttribute("picture"));

        return "profile";
    }

    private Stream<Article> getAllArticlesExceptPrincipal(String username) {
        return articleDAO.findAll()
                .stream()
                .filter(article -> !article.getAuthor().equals(username));
    }
}
