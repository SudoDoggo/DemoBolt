package com.example.courseprifs.hibernateControl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Pagalbinė klasė testams: kiekvienam testui sukuria atskirą (tuščią) H2 in-memory DB.
 */
final class H2TestSupport {

    private H2TestSupport() {
    }

    static EntityManagerFactory newEntityManagerFactory() {
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER";
        return Persistence.createEntityManagerFactory("wolt-test",
                Map.of("jakarta.persistence.jdbc.url", url));
    }

    /** Įrašo duomenis tiesiai per EntityManager (nenaudojant testuojamos klasės). */
    static void inTransaction(EntityManagerFactory emf, Consumer<EntityManager> action) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            action.accept(em);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
