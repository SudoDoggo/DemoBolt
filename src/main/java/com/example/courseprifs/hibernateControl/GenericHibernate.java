package com.example.courseprifs.hibernateControl;

import com.example.courseprifs.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaQuery;

import java.util.ArrayList;
import java.util.List;

public class GenericHibernate {
    protected EntityManagerFactory emf;
    protected EntityManager em;
    public GenericHibernate(EntityManagerFactory emf) {
        this.emf = emf;
    }
    public <T> void create(T entity){
        try {
            em =  emf.createEntityManager();
            em.getTransaction().begin();
            em.persist(entity);
            em.getTransaction().commit();
        }catch(Exception e){
            //Alert
        }finally {
            if(em != null){
                em.close();
            }
        }
    }
    public <T> void update(T entity){
        try {
            em =  emf.createEntityManager();
            em.getTransaction().begin();
            em.merge(entity);
            em.getTransaction().commit();
        }catch(Exception e){
            //Alert
        }finally {
            if(em != null){
                em.close();
            }
        }
    }
    public <T> void delete(T entity){
        try {
            em =  emf.createEntityManager();
            em.getTransaction().begin();
            em.remove(entity);
            em.getTransaction().commit();
        }catch(Exception e){
            //Alert
        }finally {
            if(em != null){
                em.close();
            }
        }
    }
    public <T> List<T> getAllRecords(Class<T> entityClass){
        List<T> list = new ArrayList<>();
        try{
            EntityManager em = emf.createEntityManager();
            CriteriaQuery query = em.getCriteriaBuilder().createQuery();
            query.select(query.from(entityClass));
            Query q = em.createQuery(query);
            list = q.getResultList();
        }catch (Exception e){
            //E
        }finally {
            if(em != null){
                em.close();
            }
        }
        return list;
    }
}
