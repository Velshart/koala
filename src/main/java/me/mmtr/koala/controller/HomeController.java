package me.mmtr.koala.controller;

import jakarta.servlet.http.HttpSession;
import me.mmtr.koala.data.User;
import me.mmtr.koala.repository.UserRepository;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final UserRepository userRepository;

    public HomeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/home")
    public String home(HttpSession session, OAuth2AuthenticationToken authenticationToken, Model model) {
        OAuth2User principal = authenticationToken.getPrincipal();

        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();
        session.setAttribute("principalUser", user);

        model.addAttribute("user", user);
        return "home";
    }
}
