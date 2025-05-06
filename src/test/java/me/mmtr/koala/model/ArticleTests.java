package me.mmtr.koala.model;

import me.mmtr.koala.model.record.IndexedArticleChapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class ArticleTests {

    private User firstUser;
    private User secondUser;

    private Article article;

    private ArticleChapter firstArticleChapter;
    private ArticleChapter secondArticleChapter;

    private ArticleRating firstArticleRating;
    private ArticleRating secondArticleRating;

    @BeforeEach
    public void setUp() {
        article = new Article();

        firstUser = new User();
        firstUser.setName("Test1");

        secondUser = new User();
        secondUser.setName("Test2");

        firstArticleRating = new ArticleRating();
        firstArticleRating.setUser(firstUser);
        firstArticleRating.setRating(5);

        secondArticleRating = new ArticleRating();
        secondArticleRating.setUser(secondUser);
        secondArticleRating.setRating(3);

        firstArticleChapter = new ArticleChapter();
        firstArticleChapter.setCreatedAtDateTime(LocalDateTime.now());

        secondArticleChapter = new ArticleChapter();
        secondArticleChapter.setCreatedAtDateTime(LocalDateTime.now().minusDays(1));

        article.addChapter(firstArticleChapter);
        article.addChapter(secondArticleChapter);

        article.setRatings(List.of(firstArticleRating, secondArticleRating));
    }

    @Test
    public void shouldSuccessfullyAddChaptersToTheArticleTest() {
        Assertions.assertEquals(2, article.getChapters().size());

        Assertions.assertEquals(firstArticleChapter.getArticle(), article);
        Assertions.assertEquals(secondArticleChapter.getArticle(), article);
    }

    @Test
    public void chaptersShouldBeSortedCorrectlyTest() {
        //Article chapters are sorted as follows:
        //chapters.sort(Comparator.comparing(ArticleChapter::getCreatedAtDateTime));

        List<ArticleChapter> articleChapters = article.getChapters();

        Assertions.assertEquals(secondArticleChapter, articleChapters.getFirst());
        Assertions.assertEquals(firstArticleChapter, articleChapters.getLast());
    }

    @Test
    public void shouldSuccessfullyAddRatingsToTheArticleTest() {
        Assertions.assertEquals(2, article.getNumberOfRatings());
    }

    @Test
    public void isRatedByUserTest() {
        User thirdUser = new User();

        Assertions.assertTrue(article.isArticleRatedByUser(firstUser));
        Assertions.assertTrue(article.isArticleRatedByUser(secondUser));

        Assertions.assertFalse(article.isArticleRatedByUser(thirdUser));
    }

    @Test
    public void ratingsCorrectlyCalculateOverallRatingTest() {
        Assertions.assertEquals(4.0, article.calculateOverallRating());

        article.setRatings(Collections.emptyList());
        Assertions.assertEquals(0.0, article.calculateOverallRating());
    }

    @Test
    public void getRatingFromTest() {
        Assertions.assertEquals(firstArticleRating, article.getRatingFrom(firstUser));
        Assertions.assertEquals(secondArticleRating, article.getRatingFrom(secondUser));
    }

    @Test
    public void mapArticleChapterListToIndexedArticleChapterListTest() {
        List<IndexedArticleChapter> indexedArticleChapters =
                article.mapArticleChapterListToIndexedArticleChapterList();

        List<ArticleChapter> articleChapters = article.getChapters();

        Assertions.assertEquals(
                indexedArticleChapters.getFirst().index(),
                articleChapters.indexOf(secondArticleChapter)
        );

        Assertions.assertEquals(
                indexedArticleChapters.getLast().index(),
                articleChapters.indexOf(firstArticleChapter)
        );
    }
}
