package com.example.courseprifs.hibernateControl;

import com.example.courseprifs.utils.FxUserNotifier;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.criteria.CriteriaQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Bendri CRUD veiksmai visoms esybėms.
 * <p>
 * EntityManager gyvavimo ciklas (sukūrimas, transakcija, rollback, uždarymas) ir klaidų
 * apdorojimas aprašyti vieną kartą metoduose {@link #executeInTransaction} ir
 * {@link #executeQuery}, todėl konkretūs metodai aprašo tik savo DB veiksmą.
 */
public class GenericHibernate {

    private final EntityManagerFactory entityManagerFactory;
    private final UserNotifier notifier;

    public GenericHibernate(EntityManagerFactory entityManagerFactory) {
        this(entityManagerFactory, new FxUserNotifier());
    }

    public GenericHibernate(EntityManagerFactory entityManagerFactory, UserNotifier notifier) {
        this.entityManagerFactory = entityManagerFactory;
        this.notifier = notifier;
    }

    public <T> void create(T entity) {
        executeInTransaction(em -> em.persist(entity), "Something went wrong when creating");
    }

    public <T> void update(T entity) {
        executeInTransaction(em -> em.merge(entity), "Something went wrong when updating");
    }

    public <T> T getEntityById(Class<T> entityClass, int id) {
        return executeQuery(em -> em.find(entityClass, id), null, "Failed to get information from data base");
    }

    /** Jei įrašo nėra, remove(null) meta išimtį ir vartotojui parodomas klaidos langas. */
    public <T> void delete(Class<T> entityClass, int id) {
        executeInTransaction(em -> em.remove(em.find(entityClass, id)), "Something went wrong when deleting");
    }

    public <T> List<T> getAllRecords(Class<T> entityClass) {
        return executeQueryIgnoringErrors(em -> {
            CriteriaQuery<T> query = em.getCriteriaBuilder().createQuery(entityClass);
            query.select(query.from(entityClass));
            return em.createQuery(query).getResultList();
        }, new ArrayList<>());
    }

    protected UserNotifier notifier() {
        return notifier;
    }

    /** Vykdo rašymo veiksmą transakcijoje. Klaidos atveju transakcija atšaukiama ir parodomas langas. */
    protected void executeInTransaction(Consumer<EntityManager> action, String errorHeader) {
        EntityManager em = null;
        try {
            em = entityManagerFactory.createEntityManager();
            em.getTransaction().begin();
            action.accept(em);
            em.getTransaction().commit();
        } catch (Exception e) {
            rollbackIfActive(em);
            notifier.error(errorHeader, e);
        } finally {
            closeIfOpen(em);
        }
    }

    /** Vykdo skaitymo užklausą. Klaidos atveju parodo langą ir grąžina {@code fallback}. */
    protected <R> R executeQuery(Function<EntityManager, R> query, R fallback, String errorHeader) {
        return runQuery(query, fallback, e -> notifier.error(errorHeader, e));
    }

    /** Vykdo skaitymo užklausą. Klaidos atveju tyliai grąžina {@code fallback} (be lango). */
    protected <R> R executeQueryIgnoringErrors(Function<EntityManager, R> query, R fallback) {
        return runQuery(query, fallback, e -> { });
    }

    private <R> R runQuery(Function<EntityManager, R> query, R fallback, Consumer<Exception> onError) {
        EntityManager em = null;
        try {
            em = entityManagerFactory.createEntityManager();
            return query.apply(em);
        } catch (Exception e) {
            onError.accept(e);
            return fallback;
        } finally {
            closeIfOpen(em);
        }
    }

    private static void rollbackIfActive(EntityManager em) {
        if (em != null && em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }

    private static void closeIfOpen(EntityManager em) {
        if (em != null) {
            em.close();
        }
    }
}
