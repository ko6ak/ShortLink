package com.example.shortlink.repository;

import com.example.shortlink.entity.Link;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class LinkRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Link create(Link link){
        return entityManager.merge(link);
    }

    @Transactional(readOnly = true)
    public Link get(String endOfShortLink) {
        return entityManager.find(Link.class, endOfShortLink);
    }

    @Transactional
    public void delete(Link link){
        entityManager.remove(link);
    }
}
