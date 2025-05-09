package me.mmtr.koala.controller;

import me.mmtr.koala.model.Article;
import me.mmtr.koala.model.ArticleRating;
import me.mmtr.koala.model.User;
import me.mmtr.koala.repository.dao.ArticleDAO;
import me.mmtr.koala.repository.dao.ArticleRatingDAO;
import me.mmtr.koala.util.FormatUtil;
import me.mmtr.koala.utils.Oauth2Util;
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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ArticleRatingController.class)
public class ArticleRatingControllerTests {

    private static final String TEST_USER_NAME = "testUser";
    private static final String TEST_USER_EMAIL = "test@example.com";
    private static final String TEST_USER_PICTURE = "https://example.com/test.jpg";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArticleRatingDAO articleRatingDAO;

    @MockitoBean
    private ArticleDAO articleDAO;

    private User testUser;

    private Article testArticle;

    private MockHttpSession session;

    private ArticleRating firstArticleRating;

    private ArticleRating secondArticleRating;

    @BeforeEach
    public void setUp() {
        OAuth2AuthenticationToken authentication = Oauth2Util.getOAuth2AuthenticationToken(
                TEST_USER_NAME, TEST_USER_EMAIL, TEST_USER_PICTURE
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        testUser = new User();
        testUser.setName(TEST_USER_NAME);
        testUser.setEmail(TEST_USER_EMAIL);
        testUser.setPicture(TEST_USER_PICTURE);


        testArticle = new Article();
        testArticle.setId(1L);
        testArticle.setTitle("Test Article");
        testArticle.setAuthor(TEST_USER_NAME);
        testArticle.setCreatedAt(FormatUtil.formatDateTime(LocalDateTime.now()));
        testArticle.setChapters(new ArrayList<>());

        firstArticleRating = new ArticleRating();
        firstArticleRating.setId(1L);
        firstArticleRating.setArticle(testArticle);
        firstArticleRating.setUser(
                new User(
                        2L,
                        "Rating User1",
                        "rating1@example.com",
                        TEST_USER_PICTURE,
                        LocalDateTime.now(),
                        Collections.emptyList(),
                        Collections.emptyList()
                ));
        firstArticleRating.setRating(5);

        secondArticleRating = new ArticleRating();
        secondArticleRating.setId(2L);
        secondArticleRating.setArticle(testArticle);
        secondArticleRating.setUser(
                new User(
                        3L,
                        "Rating User2",
                        "rating2@example.com",
                        TEST_USER_PICTURE,
                        LocalDateTime.now(),
                        Collections.emptyList(),
                        Collections.emptyList()
                ));
        secondArticleRating.setRating(3);

        testArticle.setRatings(List.of(firstArticleRating, secondArticleRating));

        session = new MockHttpSession();
        session.setAttribute("principalUser", testUser);

        when(articleDAO.findById(1L)).thenReturn(testArticle);
    }

    @Test
    public void shouldCorrectlyCreateNewArticleRatingTestAndRedirect() throws Exception {
        when(articleDAO.findById(1L)).thenReturn(testArticle);

        mockMvc.perform(post("/ratings/new")
                        .session(session)
                        .with(csrf())
                        .param("rating", String.valueOf(5))
                        .param("ratedArticleId", String.valueOf(testArticle.getId()))
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/articles/view/" + testArticle.getId()));

        ArgumentCaptor<ArticleRating> articleRatingArgumentCaptor =
                ArgumentCaptor.forClass(ArticleRating.class);
        verify(articleRatingDAO).create(articleRatingArgumentCaptor.capture());

        ArticleRating capturedRating = articleRatingArgumentCaptor.getValue();
        assertEquals(capturedRating.getArticle(), testArticle);
        assertEquals(5, capturedRating.getRating());
        assertEquals(capturedRating.getUser(), testUser);
    }

    @Test
    void deleteArticleRatingTest() throws Exception {
        mockMvc.perform(post("/ratings/delete/" + firstArticleRating.getId())
                        .session(session)
                        .with(csrf())
                        .param("articleRatingId", String.valueOf(firstArticleRating.getId()))
                        .param("articleId", String.valueOf(testArticle.getId()))
                ).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/articles/view/" + testArticle.getId()));
    }

    @Test
    void shouldCorrectlyReturnAllRatingsForArticle() throws Exception {
        when(articleRatingDAO.findAll()).thenReturn(List.of(firstArticleRating, secondArticleRating));

        mockMvc.perform(get("/ratings/article-all/" + testArticle.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("article-ratings"))
                .andExpect(model().attribute(
                        "articleRatings",
                        equalTo(testArticle.getRatings())
                ));
    }
}
