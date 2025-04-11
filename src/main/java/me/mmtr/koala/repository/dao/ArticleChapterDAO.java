package me.mmtr.koala.repository.dao;

import me.mmtr.koala.abstraction.AbstractJPADataAccessObject;
import me.mmtr.koala.model.ArticleChapter;
import org.springframework.stereotype.Repository;

@Repository
public class ArticleChapterDAO extends AbstractJPADataAccessObject<ArticleChapter> {
    public ArticleChapterDAO() {
        setClazz(ArticleChapter.class);
    }
}
