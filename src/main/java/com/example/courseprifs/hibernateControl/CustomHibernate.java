package com.example.courseprifs.hibernateControl;

import com.example.courseprifs.fxControllers.FxUtils;
import com.example.courseprifs.model.*;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import javafx.scene.control.Alert;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CustomHibernate extends GenericHibernate {

    public CustomHibernate(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory);
    }

    public User getUserByCredentials(String login, String psw) {
        User user = null;
        try {
            entityManager = entityManagerFactory.createEntityManager();
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<User> query = cb.createQuery(User.class);
            Root<User> root = query.from(User.class);

            query.select(root).where(cb.and(cb.equal(root.get("login"), login),
                    cb.equal(root.get("password"), psw)));
            Query q = entityManager.createQuery(query);
            user = (User) q.getSingleResult();
        } catch (Exception e) {
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Something went wrong when getting information", e);
        }
        return user;
    }

    public List<FoodOrder> getRestaurantOrders(Restaurant restaurant) {
        List<FoodOrder> orders = new ArrayList<>();
        try {
            entityManager = entityManagerFactory.createEntityManager();
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<FoodOrder> query = cb.createQuery(FoodOrder.class);
            Root<FoodOrder> root = query.from(FoodOrder.class);

            query.select(root).where(cb.equal(root.get("restaurant"), restaurant));
            Query q = entityManager.createQuery(query);
            orders = q.getResultList();
        } catch (Exception e) {
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Something went wrong when getting information", e);
        }
        return orders;
    }

    public List<Cuisine> getRestaurantCuisine(Restaurant restaurant) {
        List<Cuisine> menu = new ArrayList<>();
        try {
            entityManager = entityManagerFactory.createEntityManager();
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Cuisine> query = cb.createQuery(Cuisine.class);
            Root<Cuisine> root = query.from(Cuisine.class);

            query.select(root).where(cb.equal(root.get("restaurant"), restaurant));
            Query q = entityManager.createQuery(query);
            menu = q.getResultList();
        } catch (Exception e) {
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Something went wrong when getting information", e);
        }
        return menu;
    }

    public List<FoodOrder> getFilteredRestaurantOrders(OrderStatus orderStatus, BasicUser client, LocalDate start, LocalDate end, Restaurant restaurant) {
        List<FoodOrder> orders = new ArrayList<>();
        try {
            entityManager = entityManagerFactory.createEntityManager();
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<FoodOrder> query = cb.createQuery(FoodOrder.class);
            Root<FoodOrder> root = query.from(FoodOrder.class);

            if(restaurant != null) {
                query.select(root).where(cb.equal(root.get("restaurant"), restaurant));
            }else{
                query.select(root).where(cb.equal(root.get("restaurant"), restaurant));
            }
            Query q = entityManager.createQuery(query);
            orders = q.getResultList();
        } catch (Exception e) {
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Something went wrong when getting orders", e);
        }
        return orders;
    }

    public List<Review> getReviewsByChatId(int chatId) {
        List<Review> reviews = new ArrayList<>();
        try {
            entityManager = entityManagerFactory.createEntityManager();
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Review> cq = cb.createQuery(Review.class);
            Root<Review> root = cq.from(Review.class);
            cq.select(root).where(cb.equal(root.get("chat").get("id"), chatId));
            reviews = entityManager.createQuery(cq).getResultList();
        } catch (Exception e) {
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Something went wrong when getting information", e);
        }
        return reviews;
    }
    public FoodOrder getFoodOrderByChatId(int chatId) {
        FoodOrder order = null;
        try {
            entityManager = entityManagerFactory.createEntityManager();
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<FoodOrder> query = cb.createQuery(FoodOrder.class);
            Root<FoodOrder> root = query.from(FoodOrder.class);
            query.select(root).where(cb.equal(root.get("chat").get("id"), chatId));
            order = entityManager.createQuery(query).getSingleResult();
        } catch (Exception e) {
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR,
                    "Something went wrong when getting information", e);
        } finally {
            if (entityManager != null) entityManager.close();
        }
        return order;
    }
}
