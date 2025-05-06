package me.mmtr.koala.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Set;

public class ValidatorTests {

    private Validator validator;

    @BeforeEach
    public void init() {
        validator = createValidator();
    }

    @Test
    void shouldNotValidateWhenRatingOutOfBounds_article() {
        ArticleRating rating = new ArticleRating();
        rating.setRating(6);

        Set<ConstraintViolation<ArticleRating>> constraintViolations = validator.validate(rating);

        Assertions.assertThat(constraintViolations).hasSize(1);

        ConstraintViolation<ArticleRating> violation = constraintViolations.iterator().next();
        Assertions.assertThat(violation.getPropertyPath().toString()).isEqualTo("rating");
        Assertions.assertThat(violation.getMessage()).isEqualTo(
                "The rating must be greater than or equal to 1 and less than or equal to 5."
        );
    }

    @Test
    void shouldNotValidateWhenTitleLongerThan1000_article() {
        Article article = new Article();
        article.setTitle("Test".repeat(26));

        Set<ConstraintViolation<Article>> constraintViolations = validator.validate(article);
        Assertions.assertThat(constraintViolations).hasSize(1);
        ConstraintViolation<Article> violation = constraintViolations.iterator().next();

        Assertions.assertThat(violation.getPropertyPath().toString()).isEqualTo("title");
        Assertions.assertThat(violation.getMessage()).isEqualTo("Title too long.");
    }

    private Validator createValidator() {
        LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
        localValidatorFactoryBean.afterPropertiesSet();
        return localValidatorFactoryBean;
    }
}
