package me.mmtr.koala.data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private Long id;

    private String title;

    private String content;

    private LocalDateTime createdAt;
}