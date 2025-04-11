package me.mmtr.koala.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.mmtr.koala.model.record.IndexedArticleChapter;
import org.hibernate.validator.constraints.Length;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "articles")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Length(max = 1000)
    private String title;

    private String author;

    private String createdAt;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArticleChapter> chapters = new ArrayList<>();

    public void addChapter(ArticleChapter chapter) {
        chapter.setArticle(this);
        chapters.add(chapter);

        chapters.sort(Comparator.comparing(ArticleChapter::getCreatedAt).reversed());
    }

    public List<IndexedArticleChapter> mapArticleChapterListToIndexedArticleChapterList() {

        List<IndexedArticleChapter> indexedArticleChapters = new ArrayList<>();
        chapters.forEach(articleChapter -> indexedArticleChapters.add(
                new IndexedArticleChapter(articleChapter, chapters.indexOf(articleChapter))
        ));
        return indexedArticleChapters;
    }
}
