package me.mmtr.koala.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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

    private String title;

    private String author;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "article_chapters", joinColumns = {
            @JoinColumn(name = "article_id", referencedColumnName = "id")
    }, inverseJoinColumns = {
            @JoinColumn(name = "chapter_id", referencedColumnName = "id")
    })
    private List<ArticleChapter> chapters = new ArrayList<>();

    public void addChapter(ArticleChapter chapter) {
        chapters.add(chapter);
    }
}
