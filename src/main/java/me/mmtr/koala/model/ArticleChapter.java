package me.mmtr.koala.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "chapters")
public class ArticleChapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    private String delta;

    @Lob
    private String htmlContent;

    private String createdAt;

    private LocalDateTime createdAtDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    @Override
    public String toString() {
        return "ArticleChapter{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", delta='" + delta + '\'' +
                ", htmlContent='" + htmlContent + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", createdAtDateTime=" + createdAtDateTime +
                ", article=" + article.getId() +
                '}';
    }
}