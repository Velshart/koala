package me.mmtr.koala.controller;

import me.mmtr.koala.model.Article;
import me.mmtr.koala.model.User;
import me.mmtr.koala.repository.UserRepository;
import me.mmtr.koala.repository.dao.ArticleDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ApplicationMainController.class)
public class ApplicationMainControllerTests {

    private final String TEST_USER_NAME = "testUser";
    private final String TEST_USER_EMAIL = "test@example.com";
    private final String TEST_USER_PICTURE = "https://example.com/test.jpg";
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private ArticleDAO articleDAO;
    private User testUser;

    private Article firstTestArticle;
    private Article secondTestArticle;
    private Article thirdTestArticle;

    @BeforeEach
    void setupSecurityContext() {
        OAuth2AuthenticationToken authentication = getOAuth2AuthenticationToken();

        SecurityContextHolder.getContext().setAuthentication(authentication);

        testUser = new User();
        testUser.setName(TEST_USER_NAME);
        testUser.setEmail(TEST_USER_EMAIL);
        testUser.setPicture(TEST_USER_PICTURE);

        firstTestArticle = new Article();
        firstTestArticle.setTitle("First Article");
        firstTestArticle.setAuthor(TEST_USER_NAME);

        secondTestArticle = new Article();
        secondTestArticle.setTitle("Second Article");
        secondTestArticle.setAuthor("Some other user");

        thirdTestArticle = new Article();
        thirdTestArticle.setTitle("Third Article");
        thirdTestArticle.setAuthor("Yet another user");

        Mockito.when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        Mockito.when(articleDAO.findAll()).thenReturn(List.of(firstTestArticle, secondTestArticle, thirdTestArticle));

    }

    private OAuth2AuthenticationToken getOAuth2AuthenticationToken() {
        Map<String, Object> attributes = Map.of(
                "name", TEST_USER_NAME,
                "email", TEST_USER_EMAIL,
                "picture", TEST_USER_PICTURE);
        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "email"
        );

        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
                oauth2User,
                oauth2User.getAuthorities(),
                "google"
        );
        return authentication;
    }

    @Test
    void shouldReturnHomePageWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("articles"))

                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", equalTo(testUser)))

                .andExpect(model().attributeExists("username"))
                .andExpect(model().attribute("username", equalTo(TEST_USER_NAME)))

                .andExpect(model().attributeExists("requestURI"))
                .andExpect(model().attribute("requestURI", equalTo("/")))

                .andExpect(model().attributeExists("searchRedirectionURI"))
                .andExpect(model().attribute("searchRedirectionURI", "/"));
    }

    @Test
    void shouldFilterOutArticlesCorrectlyWithoutKeyword() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("articles", hasSize(2)))
                .andExpect(model().attribute("articles", List.of(secondTestArticle, thirdTestArticle)));
    }

    @Test
    void shouldFilterOutArticlesCorrectlyWithKeyword() throws Exception {
        mockMvc.perform(get("/").param("keyword", "Third"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("articles", hasSize(1)))
                .andExpect(model().attribute("articles", List.of(thirdTestArticle)));
    }
}
