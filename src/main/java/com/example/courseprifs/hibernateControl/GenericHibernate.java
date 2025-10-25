package com.example.courseprifs.hibernateControl;

import com.example.courseprifs.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

public class GenericHibernate {
    private EntityManagerFactory emf;
    private EntityManager em;
    public GenericHibernate(EntityManagerFactory emf) {
        this.emf = emf;
    }
    public void createUser(User user){
        try {
            em =  emf.createEntityManager();
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
        }catch(Exception e){
            //Alert
        }finally {
            if(em != null){
                em.close();
            }
        }
    }
}
