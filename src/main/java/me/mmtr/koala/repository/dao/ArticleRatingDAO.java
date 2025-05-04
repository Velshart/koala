package me.mmtr.koala.repository.dao;

import me.mmtr.koala.abstraction.AbstractJPADataAccessObject;
import me.mmtr.koala.model.ArticleRating;
import org.springframework.stereotype.Repository;

@Repository
public class ArticleRatingDAO extends AbstractJPADataAccessObject<ArticleRating> {
    public ArticleRatingDAO() {
        setClazz(ArticleRating.class);
    }
}
