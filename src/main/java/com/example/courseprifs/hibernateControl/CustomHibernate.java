package com.example.courseprifs.hibernateControl;

import com.example.courseprifs.utils.FxUtils;
import com.example.courseprifs.model.*;
import com.example.courseprifs.utils.PasswordUtils;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import javafx.scene.control.Alert;

import java.time.LocalDate;
import java.util.*;

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

            // First, find user by login only (password is hashed, so we can't compare in query)
            query.select(root).where(cb.equal(root.get("login"), login));
            Query q = entityManager.createQuery(query);
            user = (User) q.getSingleResult();
            
            // Then verify the password using BCrypt
            if (user != null && !PasswordUtils.verifyPassword(psw, user.getPassword())) {
                user = null; // Password doesn't match
            }
        } catch (Exception e) {
            // User not found or password doesn't match
            user = null;
        }finally {
            if (entityManager != null) entityManager.close();
        }
        return user;
    }

    public List<FoodOrder> getRestaurantOrders(Restaurant restaurant) {
        List<FoodOrder> orders = new ArrayList<>();
        try {
            entityManager = entityManagerFactory.createEntityManager();
            String jpql = "SELECT DISTINCT o FROM FoodOrder o LEFT JOIN FETCH o.cuisineList WHERE o.restaurant = :restaurant";
            TypedQuery<FoodOrder> query = entityManager.createQuery(jpql, FoodOrder.class);
            query.setParameter("restaurant", restaurant);
            orders = query.getResultList();
        } catch (Exception e) {
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Something went wrong when getting information", e);
        }finally {
            if (entityManager != null) entityManager.close();
        }
        return orders;
    }

    @SuppressWarnings("unchecked")
    public List<Cuisine> getRestaurantCuisine(Restaurant restaurant) {
        List<Cuisine> menu = new ArrayList<>();
        try {
            entityManager = entityManagerFactory.createEntityManager();
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Cuisine> query = cb.createQuery(Cuisine.class);
            Root<Cuisine> root = query.from(Cuisine.class);

            query.select(root).where(cb.equal(root.get("restaurant"), restaurant));
            Query q = entityManager.createQuery(query);
            menu = (List<Cuisine>) q.getResultList();
        } catch (Exception e) {
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Something went wrong when getting information", e);
        }finally{
            if (entityManager != null) entityManager.close();
        }
        return menu;
    }

    public List<FoodOrder> getFilteredRestaurantOrders(OrderStatus status,BasicUser client, LocalDate from, LocalDate to, Restaurant restaurant) {
        List<FoodOrder> orders = new ArrayList<>();
        try {
            entityManager = entityManagerFactory.createEntityManager();

            StringBuilder jpql = new StringBuilder("SELECT DISTINCT o FROM FoodOrder o LEFT JOIN FETCH o.cuisineList WHERE 1=1");
            Map<String, Object> params = new HashMap<>();

            if (status != null) {
                jpql.append(" AND o.orderStatus = :status");
                params.put("status", status);
            }
            if (client != null) {
                jpql.append(" AND o.buyer = :client");
                params.put("client", client);
            }
            if (restaurant != null) {
                jpql.append(" AND o.restaurant = :restaurant");
                params.put("restaurant", restaurant);
            }
            if (from != null) {
                jpql.append(" AND o.dateCreated >= :from");
                params.put("from", from);
            }
            if (to != null) {
                jpql.append(" AND o.dateCreated <= :to");
                params.put("to", to);
            }

            TypedQuery<FoodOrder> query = entityManager.createQuery(jpql.toString(), FoodOrder.class);
            params.forEach(query::setParameter);
            orders = query.getResultList();
        } catch (Exception e) {
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Something went wrong when filtering orders", e);
        } finally {
            if (entityManager != null) entityManager.close();
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
        }finally {
            if (entityManager != null) entityManager.close();
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

    public FoodOrder getFoodOrderWithItems(int orderId) {
        FoodOrder order = null;
        try {
            entityManager = entityManagerFactory.createEntityManager();
            String jpql = "SELECT DISTINCT o FROM FoodOrder o LEFT JOIN FETCH o.cuisineList WHERE o.id = :orderId";
            TypedQuery<FoodOrder> query = entityManager.createQuery(jpql, FoodOrder.class);
            query.setParameter("orderId", orderId);
            order = query.getSingleResult();
        } catch (Exception e) {
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR,
                    "Something went wrong when getting order with items", e);
        } finally {
            if (entityManager != null) entityManager.close();
        }
        return order;
    }

    public void removeCuisineFromOrder(int orderId, int cuisineId) {
        try {
            entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();

            String jpql = "SELECT DISTINCT o FROM FoodOrder o LEFT JOIN FETCH o.cuisineList WHERE o.id = :orderId";
            TypedQuery<FoodOrder> query = entityManager.createQuery(jpql, FoodOrder.class);
            query.setParameter("orderId", orderId);
            FoodOrder order;
            try {
                order = query.getSingleResult();
            } catch (NoResultException e) {
                FxUtils.generateAlert(Alert.AlertType.ERROR, "Error", "Order not found", 
                        "Could not find the order with ID: " + orderId);
                entityManager.getTransaction().rollback();
                return;
            }

            Cuisine cuisineToRemove = entityManager.find(Cuisine.class, cuisineId);
            
            if (cuisineToRemove == null) {
                FxUtils.generateAlert(Alert.AlertType.ERROR, "Error", "Cuisine not found", 
                        "Could not find the cuisine with ID: " + cuisineId);
                entityManager.getTransaction().rollback();
                return;
            }

            if (order.getCuisineList() != null && order.getCuisineList().contains(cuisineToRemove)) {
                order.getCuisineList().remove(cuisineToRemove);

                if (order.getPrice() != null && cuisineToRemove.getPrice() != null) {
                    double newPrice = order.getPrice() - cuisineToRemove.getPrice();
                    order.setPrice(Math.max(0, newPrice));
                }

                order.setDateUpdated(LocalDate.now());

                entityManager.merge(order);
                entityManager.getTransaction().commit();
            } else {
                FxUtils.generateAlert(Alert.AlertType.WARNING, "Warning", "Item not in order", 
                        "The selected cuisine is not part of this order.");
                entityManager.getTransaction().rollback();
            }
        } catch (Exception e) {
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, 
                    "Something went wrong when removing cuisine from order", e);
            if (entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }

    public List<FoodOrder> getAllFoodOrdersWithItems() {
        List<FoodOrder> orders = new ArrayList<>();
        try {
            entityManager = entityManagerFactory.createEntityManager();
            String jpql = "SELECT DISTINCT o FROM FoodOrder o LEFT JOIN FETCH o.cuisineList";
            TypedQuery<FoodOrder> query = entityManager.createQuery(jpql, FoodOrder.class);
            orders = query.getResultList();
        } catch (Exception e) {
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Something went wrong when getting all orders with items", e);
        } finally {
            if (entityManager != null) entityManager.close();
        }
        return orders;
    }

    public List<User> getFilteredUsers(String userType, String login, String name, String surname) {
        List<User> users = new ArrayList<>();
        try {
            entityManager = entityManagerFactory.createEntityManager();
            StringBuilder jpql = new StringBuilder("SELECT u FROM User u WHERE 1=1");
            Map<String, Object> params = new HashMap<>();

            if (userType != null && !userType.trim().isEmpty() && !userType.equals("All users")) {
                Class<? extends User> userTypeClass = null;
                switch (userType) {
                    case "User":
                        userTypeClass = User.class;
                        break;
                    case "BasicUser":
                        userTypeClass = BasicUser.class;
                        break;
                    case "Restaurant":
                        userTypeClass = Restaurant.class;
                        break;
                    case "Driver":
                        userTypeClass = Driver.class;
                        break;
                    default:
                        userTypeClass = null;
                        break;
                }
                if (userTypeClass != null) {
                    jpql.append(" AND TYPE(u) = :userType");
                    params.put("userType", userTypeClass);
                }
            }
            // If userType is "All users" or empty, don't filter by type (show all)
            if (login != null && !login.trim().isEmpty()) {
                jpql.append(" AND LOWER(u.login) LIKE LOWER(:login)");
                params.put("login", "%" + login.trim() + "%");
            }
            if (name != null && !name.trim().isEmpty()) {
                jpql.append(" AND LOWER(u.name) LIKE LOWER(:name)");
                params.put("name", "%" + name.trim() + "%");
            }
            if (surname != null && !surname.trim().isEmpty()) {
                jpql.append(" AND LOWER(u.surname) LIKE LOWER(:surname)");
                params.put("surname", "%" + surname.trim() + "%");
            }

            TypedQuery<User> query = entityManager.createQuery(jpql.toString(), User.class);
            params.forEach(query::setParameter);
            users = query.getResultList();
        } catch (Exception e) {
            FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Something went wrong when filtering users", e);
        } finally {
            if (entityManager != null) entityManager.close();
        }
        return users;
    }
}
