package me.mmtr.koala.controller;

import me.mmtr.koala.model.Article;
import me.mmtr.koala.model.User;
import me.mmtr.koala.repository.UserRepository;
import me.mmtr.koala.repository.dao.ArticleDAO;
import me.mmtr.koala.util.FormatUtil;
import me.mmtr.koala.utils.Oauth2Util;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ArticleController.class)
public class ArticleControllerTests {

    private static final String TEST_USER_NAME = "testUser";
    private static final String TEST_USER_EMAIL = "test@example.com";
    private static final String TEST_USER_PICTURE = "https://example.com/test.jpg";
    private static User testUser;
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private ArticleDAO articleDAO;
    private MockHttpSession session;

    private final Article firstArticle = new Article(
            1L,
            "Article title1",
            TEST_USER_NAME,
            FormatUtil.formatDateTime(LocalDateTime.now()),
            new ArrayList<>(),
            new ArrayList<>()
    );

    private final Article secondArticle = new Article(
            2L,
            "Article title2",
            "Some other user",
            FormatUtil.formatDateTime(LocalDateTime.now()),
            new ArrayList<>(),
            new ArrayList<>()
    );

    @BeforeAll
    static void setup() {
        OAuth2AuthenticationToken authentication = Oauth2Util.getOAuth2AuthenticationToken(
                TEST_USER_NAME, TEST_USER_EMAIL, TEST_USER_PICTURE
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName(TEST_USER_NAME);
        testUser.setEmail(TEST_USER_EMAIL);
        testUser.setPicture(TEST_USER_PICTURE);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setArticleRatings(new ArrayList<>());
        testUser.setFollowers(new ArrayList<>());

        session = new MockHttpSession();
        session.setAttribute("principalUser", testUser);

        when(articleDAO.findById(firstArticle.getId())).thenReturn(firstArticle);
    }

    @Test
    void shouldCorrectlyFilterOutUserArticlesWithAndWithoutKeyword() throws Exception {
        when(articleDAO.findAll()).thenReturn(List.of(firstArticle, secondArticle));
        when(userRepository.findByName(TEST_USER_NAME)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/articles/user-articles/" + TEST_USER_NAME))
                .andExpect(status().isOk())
                .andExpect(view().name("user-articles"))
                .andExpect(model().attribute("articles", equalTo(List.of(firstArticle))))
                .andDo(result ->
                        mockMvc.perform(get("/articles/user-articles/" + TEST_USER_NAME)
                                        .param("keyword", "Non-existent article")
                                )
                                .andExpect(status().isOk())
                                .andExpect(view().name("user-articles"))
                                .andExpect(model().attribute("articles", equalTo(Collections.emptyList()))));
    }

    @Test
    void shouldCorrectlyCreateNewArticle() throws Exception {
        mockMvc.perform(post("/articles/new")
                        .session(session)
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection());
        ArgumentCaptor<Article> articleArgumentCaptor = ArgumentCaptor.forClass(Article.class);
        verify(articleDAO).create(articleArgumentCaptor.capture());

        Article capturedArticle = articleArgumentCaptor.getValue();
        assertEquals(TEST_USER_NAME, capturedArticle.getAuthor());
        assertEquals(FormatUtil.formatDateTime(LocalDateTime.now()), capturedArticle.getCreatedAt());
    }

    @Test
    void shouldCorrectlyUpdateArticle() throws Exception {
        mockMvc.perform(get("/articles/update/" + firstArticle.getId())
                        .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(view().name("update-article-title"))
                .andExpect(model().attribute("article", equalTo(firstArticle)))
                .andDo(result ->
                        mockMvc.perform(post("/articles/update/" + firstArticle.getId())
                                        .session(session)
                                        .param("createdAt", firstArticle.getCreatedAt())
                                        .with(csrf())
                                )
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/articles/view/" + firstArticle.getId())));
        ArgumentCaptor<Article> articleArgumentCaptor = ArgumentCaptor.forClass(Article.class);
        verify(articleDAO).update(articleArgumentCaptor.capture());

        Article capturedArticle = articleArgumentCaptor.getValue();
        assertEquals(capturedArticle.getId(), firstArticle.getId());
        assertEquals(capturedArticle.getAuthor(), firstArticle.getAuthor());
        assertEquals(capturedArticle.getCreatedAt(), firstArticle.getCreatedAt());
        assertEquals(capturedArticle.getChapters(), firstArticle.getChapters());
    }

    @Test
    void shouldCorrectlyDeleteArticle() throws Exception {
        mockMvc.perform(post("/articles/delete/" + firstArticle.getId())
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void shouldCorrectlyViewArticle() throws Exception {
        mockMvc.perform(get("/articles/view/" + firstArticle.getId())
                        .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(view().name("article-view"))
                .andExpect(model().attribute("username", equalTo(testUser.getName())))
                .andExpect(model().attribute("article", equalTo(firstArticle)))
                .andExpect(model().attribute("isPrincipalAnAuthor", equalTo(true)))
                .andExpect(model().attribute("isArticleRatedByPrincipal", equalTo(false)))
                .andExpect(model().attribute("ratingFromPrincipal", equalTo(null)))
                .andExpect(model().attribute("articleChapters", equalTo(Collections.emptyList())));
    }
}
