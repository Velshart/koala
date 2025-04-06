package me.mmtr.koala.repository.dao;

import me.mmtr.koala.data.Article;
import org.springframework.stereotype.Repository;

@Repository
public class ArticleDAO extends AbstractJPADataAccessObject<Article> {
    public ArticleDAO() {
        setClazz(Article.class);
    }
}
