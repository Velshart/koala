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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
        if (keyword != null) {
            articles = getAllArticlesExceptPrincipal(user)
                    .filter(article -> article.getTitle().contains(keyword))
                    .toList();
        } else {
            articles = getAllArticlesExceptPrincipal(user).toList();
        }

        session.setAttribute("principalUser", user);
        session.setAttribute("authTokenPrincipal", authenticationToken.getPrincipal());
        model.addAttribute("articles", articles);
        model.addAttribute("user", user);
        model.addAttribute("username", username);
        model.addAttribute("requestURI", request.getRequestURI());
        model.addAttribute("searchRedirectionURI", request.getRequestURI());
        return "index";
    }

    @GetMapping("/your-profile")
    public String profile(HttpServletRequest request, HttpSession session, Model model) {
        User user = (User) session.getAttribute("principalUser");

        model.addAttribute("username", user.getName());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("picture", user.getPicture());
        model.addAttribute("followers", user.getFollowers());
        model.addAttribute("requestURI", request.getRequestURI());

        return "profile";
    }

    @GetMapping("/profile/{username}")
    public String profile(HttpServletRequest request, HttpSession session, @PathVariable String username, Model model) {

        User viewedUser = userRepository.findByName(username).orElseThrow();
        User principalUser = (User) session.getAttribute("principalUser");

        model.addAttribute("username", viewedUser.getName());
        model.addAttribute("email", viewedUser.getEmail());
        model.addAttribute("picture", viewedUser.getPicture());
        model.addAttribute("followers", viewedUser.getFollowers());
        model.addAttribute(
                "isSelfProfileView",
                viewedUser.getName().equals(principalUser.getName())
        );
        model.addAttribute(
                "isFollowedByPrincipal",
                viewedUser.isFollowedByUser(principalUser)
        );
        model.addAttribute("requestURI", request.getRequestURI());

        return "profile";
    }

    @GetMapping("/all-people")
    public String allPeople(@RequestParam(required = false) String keyword,
                            HttpServletRequest request,
                            HttpSession session, Model model) {
        User user = (User) session.getAttribute("principalUser");

        List<User> allPeople;
        if (keyword != null) {
            allPeople = getAllPeopleExcept(user)
                    .filter(u -> u.getName().contains(keyword) || u.getEmail().contains(keyword))
                    .toList();
        } else {
            allPeople = getAllPeopleExcept(user).toList();
        }

        model.addAttribute("requestURI", request.getRequestURI());
        model.addAttribute("searchRedirectionURI", request.getRequestURI());
        model.addAttribute("people", allPeople);
        model.addAttribute("username", user.getName());
        return "people";
    }

    @GetMapping("/followers/{username}")
    public String followers(@PathVariable String username, @RequestParam(required = false) String keyword,
                            HttpServletRequest request, Model model) {
        User user = userRepository.findByName(username).orElseThrow();

        List<User> followers;
        if (keyword != null) {
            followers = user.getFollowers().stream()
                    .filter(u -> u.getName().contains(keyword) || u.getEmail().contains(keyword))
                    .toList();
        } else {
            followers = user.getFollowers();
        }

        model.addAttribute("requestURI", request.getRequestURI());
        model.addAttribute("searchRedirectionURI", request.getRequestURI());
        model.addAttribute("people", followers);
        model.addAttribute("username", user.getName());
        return "people";
    }

    @GetMapping("/followed")
    public String followed(@RequestParam(required = false) String keyword,
                           HttpServletRequest request,
                           HttpSession session, Model model) {
        User user = (User) session.getAttribute("principalUser");

        List<User> followed;
        if (keyword != null) {
            followed = getPeopleFollowedBy(user)
                    .filter(u ->
                            u.getName().contains(keyword) || u.getEmail().contains(keyword))
                    .toList();
        } else {
            followed = getPeopleFollowedBy(user).toList();
        }

        model.addAttribute("requestURI", request.getRequestURI());
        model.addAttribute("searchRedirectionURI", request.getRequestURI());
        model.addAttribute("people", followed);
        model.addAttribute("username", user.getName());
        return "people";
    }

    @PostMapping("/follow")
    public String follow(HttpSession session, @RequestParam String username) {

        User followedUser = userRepository.findByName(username).orElseThrow();
        followedUser.addFollower((User) session.getAttribute("principalUser"));

        userRepository.save(followedUser);
        return "redirect:/profile/" + username;
    }

    @PostMapping("/unfollow")
    public String unfollow(HttpSession session, @RequestParam String username) {
        User unfollowedUser = userRepository.findByName(username).orElseThrow();
        unfollowedUser.removeFollower((User) session.getAttribute("principalUser"));

        userRepository.save(unfollowedUser);
        return "redirect:/profile/" + username;
    }

    private Stream<Article> getAllArticlesExceptPrincipal(User user) {
        return articleDAO.findAll()
                .stream()
                .filter(article -> !article.getAuthor().equals(user.getName()));
    }

    private Stream<User> getAllPeopleExcept(User user) {
        return userRepository.findAll()
                .stream()
                .filter(u -> !u.getName().equals(user.getName()));
    }

    private Stream<User> getPeopleFollowedBy(User user) {
        return getAllPeopleExcept(user)
                .filter(u -> u.getFollowers().contains(user));
    }
}
