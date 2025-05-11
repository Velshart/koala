package me.mmtr.koala.controller;

import me.mmtr.koala.model.Article;
import me.mmtr.koala.model.ArticleChapter;
import me.mmtr.koala.model.User;
import me.mmtr.koala.repository.dao.ArticleChapterDAO;
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
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ArticleChapterController.class)
public class ArticleChapterControllerTests {

    private static final String TEST_USER_NAME = "testUser";
    private static final String TEST_USER_EMAIL = "test@example.com";
    private static final String TEST_USER_PICTURE = "https://example.com/test.jpg";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArticleDAO articleDAO;

    @MockitoBean
    private ArticleChapterDAO articleChapterDAO;

    private MockHttpSession session;

    private User testUser;

    private Article testArticle;

    private ArticleChapter firstTestArticleChapter;

    private ArticleChapter secondTestArticleChapter;

    @BeforeAll
    public static void setup() {
        OAuth2AuthenticationToken authentication = Oauth2Util.getOAuth2AuthenticationToken(
                TEST_USER_NAME, TEST_USER_EMAIL, TEST_USER_PICTURE
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName(TEST_USER_NAME);
        testUser.setEmail(TEST_USER_EMAIL);
        testUser.setPicture(TEST_USER_PICTURE);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setFollowers(new ArrayList<>());
        testUser.setArticleRatings(new ArrayList<>());

        firstTestArticleChapter = new ArticleChapter();
        firstTestArticleChapter.setId(1L);
        firstTestArticleChapter.setTitle("Title1");
        firstTestArticleChapter.setCreatedAtDateTime(LocalDateTime.now());
        firstTestArticleChapter.setCreatedAt(FormatUtil.formatDateTime(firstTestArticleChapter.getCreatedAtDateTime()));
        firstTestArticleChapter.setHtmlContent("<p>Test</p>");
        firstTestArticleChapter.setDelta("{\"ops\":[{\"insert\":\"Test\\n\"}]}");

        secondTestArticleChapter = new ArticleChapter();
        secondTestArticleChapter.setId(2L);
        secondTestArticleChapter.setTitle("Title2");
        secondTestArticleChapter.setCreatedAtDateTime(LocalDateTime.now());
        secondTestArticleChapter.setCreatedAt(FormatUtil.formatDateTime(firstTestArticleChapter.getCreatedAtDateTime()));
        secondTestArticleChapter.setHtmlContent("<p>Test</p>");
        secondTestArticleChapter.setDelta("{\"ops\":[{\"insert\":\"Test\\n\"}]}");

        testArticle = new Article();
        testArticle.setId(1L);
        testArticle.setTitle("Title");
        testArticle.setAuthor(TEST_USER_NAME);
        testArticle.setCreatedAt(FormatUtil.formatDateTime(LocalDateTime.now()));
        testArticle.setRatings(new ArrayList<>());

        List<ArticleChapter> testArticleChapters = new ArrayList<>();
        testArticleChapters.add(firstTestArticleChapter);
        testArticleChapters.add(secondTestArticleChapter);
        testArticle.setChapters(testArticleChapters);

        firstTestArticleChapter.setArticle(testArticle);
        secondTestArticleChapter.setArticle(testArticle);

        session = new MockHttpSession();
        session.setAttribute("principalUser", testUser);

        when(articleDAO.findById(testArticle.getId())).thenReturn(testArticle);
    }

    @Test
    void shouldCorrectlyCreateNewArticleChapter() throws Exception {
        mockMvc.perform(get("/article-chapters/new/1")
                        .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(view().name("new-article-chapter"))
                .andExpect(model().attribute("username", equalTo(testUser.getName())))
                .andExpect(model().attribute("articleChapter", equalTo(new ArticleChapter())))
                .andExpect(model().attribute("articleId", equalTo(testArticle.getId())))
                .andDo(result -> mockMvc.perform(post("/article-chapters/new/1")
                                .session(session)
                                .with(csrf())
                                .param("delta", "{\"ops\":[{\"insert\":\"Test\\n\"}]}")
                                .param("htmlContent", "<p>Test</p>")
                        )
                        .andExpect(status().is3xxRedirection())
                        .andExpect(redirectedUrl("/articles/view/" + testArticle.getId())));
        ArgumentCaptor<Article> articleCaptor = ArgumentCaptor.forClass(Article.class);
        verify(articleDAO).update(articleCaptor.capture());

        Article capturedArticle = articleCaptor.getValue();
        assertEquals(capturedArticle, testArticle);
    }

    @Test
    void shouldCorrectlyUpdateArticleChapter() throws Exception {
        when(articleChapterDAO.findById(firstTestArticleChapter.getId())).thenReturn(firstTestArticleChapter);

        int articleIndex = 0;
        String delta = "{\"ops\":[{\"insert\":\"Updated Test\\n\"}]}";
        String html = "<p>Updated Test</p>";

        mockMvc.perform(
                        get("/article-chapters/update/" +
                                testArticle.getId() +
                                "/" +
                                firstTestArticleChapter.getId())
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(view().name("update-article-chapter"))
                .andExpect(model().attribute("username", equalTo(testUser.getName())))
                .andExpect(model().attribute("articleChapter", equalTo(firstTestArticleChapter)))
                .andExpect(model().attribute("articleId", equalTo(testArticle.getId())))
                .andDo(result -> mockMvc.perform(
                                post("/article-chapters/update/" +
                                        testArticle.getId() +
                                        "/" +
                                        firstTestArticleChapter.getId())
                                        .session(session)
                                        .with(csrf())
                                        .param(
                                                "delta",
                                                delta
                                        )
                                        .param("html", html)
                                        .param("createdAt",
                                                FormatUtil.formatDateTime(
                                                        firstTestArticleChapter.getCreatedAtDateTime())
                                        )
                                        .param(
                                                "createdAtDateTime",
                                                firstTestArticleChapter.getCreatedAtDateTime().toString()
                                        )
                                        .param("articleIndex", String.valueOf(articleIndex))
                        )
                        .andExpect(status().is3xxRedirection())
                        .andExpect(redirectedUrl("/article-chapters/view/" + testArticle.getId() + "/" + articleIndex)));

        ArgumentCaptor<ArticleChapter> articleChapterCaptor = ArgumentCaptor.forClass(ArticleChapter.class);
        verify(articleChapterDAO).update(articleChapterCaptor.capture());
        ArticleChapter capturedArticleChapter = articleChapterCaptor.getValue();

        assertEquals(capturedArticleChapter.getId(), firstTestArticleChapter.getId());
        assertEquals(capturedArticleChapter.getCreatedAtDateTime(), firstTestArticleChapter.getCreatedAtDateTime());
        assertEquals(capturedArticleChapter.getCreatedAt(), firstTestArticleChapter.getCreatedAt());

        assertEquals(delta, capturedArticleChapter.getDelta());
        assertEquals(html, capturedArticleChapter.getHtmlContent());
    }

    @Test
    void shouldCorrectlyDeleteArticleChapter() throws Exception {
        mockMvc.perform(post("/article-chapters/delete/" + firstTestArticleChapter.getId())
                        .session(session)
                        .with(csrf())
                        .param("articleId", String.valueOf(testArticle.getId()))
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/articles/view/" + testArticle.getId()));
    }

    @Test
    void shouldCorrectlyReturnArticleChapterViewWithValidAttributes() throws Exception {
        int index = 0;
        mockMvc.perform(get("/article-chapters/view/" + testArticle.getId() + "/" + index)
                        .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(view().name("article-chapter-view"))
                .andExpect(model().attribute("username", equalTo(testUser.getName())))
                .andExpect(model().attribute("isPrincipalAnAuthor", equalTo(true)))
                .andExpect(model().attribute("articleId", equalTo(testArticle.getId())))
                .andExpect(model().attribute("currentIndex", equalTo(index)))
                .andExpect(model().attribute("articleChapter", equalTo(firstTestArticleChapter)))
                .andExpect(model().attribute("currentIndex", equalTo(index)))
                .andExpect(model().attribute("previousIndex", equalTo(index - 1)))
                .andExpect(model().attribute("nextIndex", equalTo(index + 1)))
                .andExpect(model().attribute("hasPrevious", equalTo(false)))
                .andExpect(model().attribute("hasNext", equalTo(true)));
    }

    @Test
    void shouldCorrectlyCalculateIndexesWhenViewingArticleChapterIfIndexLessThanZero() throws Exception {
        int index = -1;
        mockMvc.perform(get("/article-chapters/view/" + testArticle.getId() + "/" + index)
                        .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(view().name("article-chapter-view"))
                .andExpect(model().attribute("currentIndex", equalTo(0)))
                .andExpect(model().attribute("previousIndex", equalTo(-1)))
                .andExpect(model().attribute("nextIndex", equalTo(1)))
                .andExpect(model().attribute("hasPrevious", equalTo(false)))
                .andExpect(model().attribute("hasNext", equalTo(true)));
    }

    @Test
    void shouldCorrectlyCalculateIndexesWhenViewingArticleChapterIfIndexGreaterOrEqualToArticleChapterListSize() throws Exception {
        int indexEqualToChaptersListSize = testArticle.getChapters().size();
        int indexGreaterThanChaptersListSize = testArticle.getChapters().size() + 1;
        mockMvc.perform(get("/article-chapters/view/" + testArticle.getId() + "/" +
                        indexEqualToChaptersListSize)
                        .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(view().name("article-chapter-view"))
                .andExpect(model().attribute("currentIndex", equalTo(1)))
                .andExpect(model().attribute("previousIndex", equalTo(0)))
                .andExpect(model().attribute("nextIndex", equalTo(2)))
                .andExpect(model().attribute("hasPrevious", equalTo(true)))
                .andExpect(model().attribute("hasNext", equalTo(false)))
                .andDo(result -> mockMvc.perform(get("/article-chapters/view/" +
                                testArticle.getId() + "/" +
                                indexGreaterThanChaptersListSize)
                                .session(session)
                        )
                        .andExpect(status().isOk())
                        .andExpect(view().name("article-chapter-view"))
                        .andExpect(model().attribute("currentIndex", equalTo(1)))
                        .andExpect(model().attribute("previousIndex", equalTo(0)))
                        .andExpect(model().attribute("nextIndex", equalTo(2)))
                        .andExpect(model().attribute("hasPrevious", equalTo(true)))
                        .andExpect(model().attribute("hasNext", equalTo(false))));
    }
}
