package me.mmtr.koala.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Set;

public class ValidatorTests {

    private Validator createValidator() {
        LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
        localValidatorFactoryBean.afterPropertiesSet();
        return localValidatorFactoryBean;
    }

    @Test
    void shouldNotValidateWhenRatingOutOfBounds() {
        Validator validator = createValidator();

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
}
