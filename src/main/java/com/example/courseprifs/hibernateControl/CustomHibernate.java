package com.example.courseprifs.hibernateControl;

import com.example.courseprifs.model.User;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class CustomHibernate extends GenericHibernate {
    public CustomHibernate(EntityManagerFactory emf) {
        super(emf);
    }

    public User getUserByCredentials(String login, String psw) {
        User user = null;
        try {
            em = emf.createEntityManager();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<User> query = cb.createQuery(User.class);
            Root<User> root = query.from(User.class);

            query.select(root).where(cb.and(cb.equal(root.get("login"), login),
                    cb.equal(root.get("password"), psw)));
            Query q = em.createQuery(query);
            user = (User) q.getSingleResult();
        } catch (Exception e) {

        }
        return user;
    }

    }
