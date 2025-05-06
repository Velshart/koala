package me.mmtr.koala.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "article_ratings")
public class ArticleRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "article_id")
    private Article article;

    @Min(value = 1, message = "The rating must be greater than or equal to 1 and less than or equal to 5.")
    @Max(value = 5, message = "The rating must be greater than or equal to 1 and less than or equal to 5.")
    private int rating;
}
