package me.mmtr.koala.controller;

import me.mmtr.koala.model.Article;
import me.mmtr.koala.model.User;
import me.mmtr.koala.repository.UserRepository;
import me.mmtr.koala.repository.dao.ArticleDAO;
import me.mmtr.koala.utils.Oauth2Util;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ApplicationMainController.class)
public class ApplicationMainControllerTests {

    private final String TEST_USER_NAME = "testUser";
    private final String TEST_USER_EMAIL = "test@example.com";
    private final String TEST_USER_PICTURE = "https://example.com/test.jpg";

    private final String FIRST_FOLLOWER_NAME = "First Follower";
    private final String FIRST_FOLLOWER_EMAIL = "first@example.com";

    private final String SECOND_FOLLOWER_NAME = "Second Follower";
    private final String SECOND_FOLLOWER_EMAIL = "second@example.com";

    private final String FOLLOWED_BY_TEST_USER_NAME = "Followed by testUser";
    private final String FOLLOWED_BY_TEST_USER_EMAIL = "followed@example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ArticleDAO articleDAO;

    private MockHttpSession session;

    private User testUser;
    private User firstFollower;
    private User secondFollower;
    private User followedByTestUser;

    private Article firstTestArticle;
    private Article secondTestArticle;
    private Article thirdTestArticle;

    @BeforeEach
    void setUp() {
        OAuth2AuthenticationToken authentication = Oauth2Util.getOAuth2AuthenticationToken(
                TEST_USER_NAME, TEST_USER_EMAIL, TEST_USER_PICTURE
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        firstFollower = new User();
        firstFollower.setName(FIRST_FOLLOWER_NAME);
        firstFollower.setEmail(FIRST_FOLLOWER_EMAIL);
        firstFollower.setFollowers(new ArrayList<>());

        secondFollower = new User();
        secondFollower.setName(SECOND_FOLLOWER_NAME);
        secondFollower.setEmail(SECOND_FOLLOWER_EMAIL);
        secondFollower.setFollowers(new ArrayList<>());

        testUser = new User();
        testUser.setName(TEST_USER_NAME);
        testUser.setEmail(TEST_USER_EMAIL);
        testUser.setPicture(TEST_USER_PICTURE);
        List<User> testUserFollowers = new ArrayList<>();
        testUserFollowers.add(firstFollower);
        testUserFollowers.add(secondFollower);
        testUser.setFollowers(testUserFollowers);

        followedByTestUser = new User();
        followedByTestUser.setName(FOLLOWED_BY_TEST_USER_NAME);
        followedByTestUser.setEmail(FOLLOWED_BY_TEST_USER_EMAIL);
        List<User> followedByTestUserFollowers = new ArrayList<>();
        followedByTestUserFollowers.add(testUser);
        followedByTestUser.setFollowers(followedByTestUserFollowers);

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
        Mockito.when(userRepository.findByName(TEST_USER_NAME)).thenReturn(Optional.of(testUser));
        Mockito.when(userRepository.findByName(SECOND_FOLLOWER_NAME))
                .thenReturn(Optional.of(secondFollower));
        Mockito.when(userRepository.findByName(FOLLOWED_BY_TEST_USER_NAME))
                .thenReturn(Optional.of(followedByTestUser));
        Mockito.when(userRepository.findAll())
                .thenReturn(List.of(testUser, firstFollower, secondFollower, followedByTestUser));

        session = new MockHttpSession();
        session.setAttribute("principalUser", testUser);

        Mockito.when(articleDAO.findAll()).thenReturn(List.of(firstTestArticle, secondTestArticle, thirdTestArticle));
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
    void shouldFilterOutArticlesCorrectlyWithAndWithoutKeyword() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("articles", hasSize(2)))
                .andExpect(model().attribute("articles", List.of(secondTestArticle, thirdTestArticle)))
                .andDo(result ->
                        mockMvc.perform(get("/").param("keyword", "Third"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("index"))
                                .andExpect(model().attribute("articles", hasSize(1)))
                                .andExpect(model().attribute("articles", List.of(thirdTestArticle))));
    }

    @Test
    void shouldCorrectlyNavigateToPrincipalAndNonPrincipalUserProfile() throws Exception {
        mockMvc.perform(get("/profile/" + TEST_USER_NAME)
                        .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attribute(
                        "followers",
                        equalTo(List.of(firstFollower, secondFollower)))
                ).andExpect(model().attribute("isFollowedByPrincipal", equalTo(false)))
                .andDo(result ->
                        mockMvc.perform(get("/profile/" + FOLLOWED_BY_TEST_USER_NAME)
                                        .session(session)
                                )
                                .andExpect(status().isOk())
                                .andExpect(view().name("profile"))
                                .andExpect(model().attribute(
                                        "followers",
                                        equalTo(List.of(testUser)))
                                ).andExpect(model().attribute(
                                        "isFollowedByPrincipal",
                                        equalTo(true)))
                );
    }

    @Test
    void shouldFilterOutPeopleCorrectlyWithAndWithoutKeyword() throws Exception {
        mockMvc.perform(get("/all-people")
                        .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(view().name("people"))
                .andExpect(model().attribute("people",
                        equalTo(List.of(firstFollower, secondFollower, followedByTestUser)))
                ).andDo(result ->
                        mockMvc.perform(get("/all-people")
                                        .session(session)
                                        .param("keyword", "First")
                                )
                                .andExpect(status().isOk())
                                .andExpect(view().name("people"))
                                .andExpect(model().attribute("people",
                                        equalTo(List.of(firstFollower)))
                                ));
    }

    @Test
    void shouldFilterOutFollowersCorrectlyWithAndWithoutKeyword() throws Exception {
        mockMvc.perform(get("/followers/" + TEST_USER_NAME)
                        .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(model().attribute(
                        "people",
                        equalTo(List.of(firstFollower, secondFollower)))
                ).andDo(result ->
                        mockMvc.perform(get("/followers/" + TEST_USER_NAME)
                                        .session(session)
                                        .param("keyword", "First")
                                )
                                .andExpect(status().isOk())
                                .andExpect(model().attribute(
                                        "people",
                                        equalTo(List.of(firstFollower)))
                                ));
    }

    @Test
    void shouldFilterOutFollowedPeopleCorrectlyWithAndWithoutKeyword() throws Exception {
        mockMvc.perform(get("/followed")
                        .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(model().attribute("people", equalTo(List.of(followedByTestUser))))
                .andDo(result -> mockMvc.perform(get("/followed")
                                .session(session)
                                .param("keyword", "Non-existent user")
                        )
                        .andExpect(status().isOk())
                        .andExpect(model().attribute("people", equalTo(Collections.emptyList()))));
    }

    @Test
    void followUnfollowTest() throws Exception {
        mockMvc.perform(post("/follow")
                        .session(session)
                        .with(csrf())
                        .param("username", SECOND_FOLLOWER_NAME)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile/" + SECOND_FOLLOWER_NAME));
        Assertions.assertTrue(secondFollower.getFollowers().contains(testUser));

        mockMvc.perform(post("/unfollow")
                        .session(session)
                        .with(csrf())
                        .param("username", SECOND_FOLLOWER_NAME)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile/" + SECOND_FOLLOWER_NAME));
        Assertions.assertFalse(secondFollower.getFollowers().contains(testUser));
    }
}
