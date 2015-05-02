package ru.inheaven.aida.coin.service;

import ru.inheaven.aida.coin.entity.AbstractEntity;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author inheaven on 16.02.2015 23:09.
 */
@Stateless
public class EntityBean {
    @PersistenceContext
    private EntityManager em;

    public void save(AbstractEntity abstractEntity){
        if (abstractEntity.getId() == null) {
            em.persist(abstractEntity);
        }else {
            em.merge(abstractEntity);
            em.flush();
            em.clear();
        }
    }
}
