package me.mmtr.koala.abstraction;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.Setter;

import java.util.List;

@Setter
@Transactional
public abstract class AbstractJPADataAccessObject<T> {

    private Class<T> clazz;

    @PersistenceContext
    private EntityManager entityManager;

    public T findById(Long id) {
        return entityManager.find(clazz, id);
    }

    public List<T> findAll() {
        return entityManager.createQuery("from " + clazz.getName()).getResultList();
    }

    public void create(T entity) {
        entityManager.persist(entity);
    }

    public void update(T entity) {
        entityManager.merge(entity);
    }

    public void delete(T entity) {
        entityManager.remove(entity);
    }

    public void deleteById(Long id) {
        T entity = findById(id);
        delete(entity);
    }
}
