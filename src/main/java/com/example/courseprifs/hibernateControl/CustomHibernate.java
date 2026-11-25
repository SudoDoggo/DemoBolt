package com.example.courseprifs.hibernateControl;

import com.example.courseprifs.model.BasicUser;
import com.example.courseprifs.model.Cuisine;
import com.example.courseprifs.model.Driver;
import com.example.courseprifs.model.FoodOrder;
import com.example.courseprifs.model.OrderStatus;
import com.example.courseprifs.model.Restaurant;
import com.example.courseprifs.model.Review;
import com.example.courseprifs.model.User;
import com.example.courseprifs.utils.PasswordUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Programai specifinės užklausos. EntityManager valdymas ir klaidų apdorojimas paveldimas
 * iš {@link GenericHibernate}, todėl kiekvienas metodas aprašo tik pačią užklausą.
 */
public class CustomHibernate extends GenericHibernate {

    /** Reikšmė iš vartotojų filtro ComboBox'o, reiškianti „nefiltruoti pagal tipą“. */
    public static final String ALL_USERS = "All users";

    private static final String ORDERS_WITH_ITEMS =
            "SELECT DISTINCT o FROM FoodOrder o LEFT JOIN FETCH o.cuisineList";

    private static final Map<String, Class<? extends User>> USER_TYPES = Map.of(
            "User", User.class,
            "BasicUser", BasicUser.class,
            "Restaurant", Restaurant.class,
            "Driver", Driver.class);

    private static final String READ_ERROR = "Something went wrong when getting information";

    public CustomHibernate(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory);
    }

    public CustomHibernate(EntityManagerFactory entityManagerFactory, UserNotifier notifier) {
        super(entityManagerFactory, notifier);
    }

    /**
     * Grąžina vartotoją, jei login ir slaptažodis teisingi, kitaip {@code null}.
     * Bet kokia klaida (vartotojas nerastas, blogas hash'as, DB klaida) tyliai grąžina {@code null}.
     */
    public User getUserByCredentials(String login, String password) {
        return executeQueryIgnoringErrors(em -> {
            User user = em.createQuery("SELECT u FROM User u WHERE u.login = :login", User.class)
                    .setParameter("login", login)
                    .getSingleResult();
            return PasswordUtils.verifyPassword(password, user.getPassword()) ? user : null;
        }, null);
    }

    public List<FoodOrder> getRestaurantOrders(Restaurant restaurant) {
        return executeQuery(em -> em.createQuery(ORDERS_WITH_ITEMS + " WHERE o.restaurant = :restaurant", FoodOrder.class)
                .setParameter("restaurant", restaurant)
                .getResultList(), new ArrayList<>(), READ_ERROR);
    }

    public List<Cuisine> getRestaurantCuisine(Restaurant restaurant) {
        return executeQuery(em -> em.createQuery("SELECT c FROM Cuisine c WHERE c.restaurant = :restaurant", Cuisine.class)
                .setParameter("restaurant", restaurant)
                .getResultList(), new ArrayList<>(), READ_ERROR);
    }

    public List<FoodOrder> getFilteredRestaurantOrders(OrderStatus status, BasicUser client, LocalDate from,
                                                       LocalDate to, Restaurant restaurant) {
        return executeQuery(em -> new JpqlQueryBuilder(ORDERS_WITH_ITEMS)
                .equalsIfPresent("o.orderStatus", "status", status)
                .equalsIfPresent("o.buyer", "client", client)
                .equalsIfPresent("o.restaurant", "restaurant", restaurant)
                .greaterOrEqualIfPresent("o.dateCreated", "from", from)
                .lessOrEqualIfPresent("o.dateCreated", "to", to)
                .build(em, FoodOrder.class)
                .getResultList(), new ArrayList<>(), "Something went wrong when filtering orders");
    }

    public List<Review> getReviewsByChatId(int chatId) {
        return executeQuery(em -> em.createQuery("SELECT r FROM Review r WHERE r.chat.id = :chatId", Review.class)
                .setParameter("chatId", chatId)
                .getResultList(), new ArrayList<>(), READ_ERROR);
    }

    /** Jei chatas neturi užsakymo, parodomas klaidos langas ir grąžinamas {@code null}. */
    public FoodOrder getFoodOrderByChatId(int chatId) {
        return executeQuery(em -> em.createQuery("SELECT o FROM FoodOrder o WHERE o.chat.id = :chatId", FoodOrder.class)
                .setParameter("chatId", chatId)
                .getSingleResult(), null, READ_ERROR);
    }

    /** Jei užsakymo nėra, parodomas klaidos langas ir grąžinamas {@code null}. */
    public FoodOrder getFoodOrderWithItems(int orderId) {
        return executeQuery(em -> orderWithItemsQuery(em, orderId).getSingleResult(),
                null, "Something went wrong when getting order with items");
    }

    public void removeCuisineFromOrder(int orderId, int cuisineId) {
        executeInTransaction(em -> {
            FoodOrder order = orderWithItemsQuery(em, orderId).getResultStream().findFirst().orElse(null);
            if (order == null) {
                notifier().error("Order not found", "Could not find the order with ID: " + orderId);
                return;
            }
            Cuisine cuisine = em.find(Cuisine.class, cuisineId);
            if (cuisine == null) {
                notifier().error("Cuisine not found", "Could not find the cuisine with ID: " + cuisineId);
                return;
            }
            if (order.getCuisineList() == null || !order.getCuisineList().contains(cuisine)) {
                notifier().warning("Item not in order", "The selected cuisine is not part of this order.");
                return;
            }
            // order yra „managed“, todėl pakeitimai išsaugomi per commit (merge nereikalingas)
            order.getCuisineList().remove(cuisine);
            order.setPrice(priceAfterRemoval(order.getPrice(), cuisine.getPrice()));
            order.setDateUpdated(LocalDate.now());
        }, "Something went wrong when removing cuisine from order");
    }

    public List<FoodOrder> getAllFoodOrdersWithItems() {
        return executeQuery(em -> em.createQuery(ORDERS_WITH_ITEMS, FoodOrder.class).getResultList(),
                new ArrayList<>(), "Something went wrong when getting all orders with items");
    }

    /**
     * Filtruoja vartotojus. Tuščias / {@value #ALL_USERS} / nežinomas tipas nefiltruoja pagal tipą,
     * tekstiniai laukai ieško dalinio atitikimo be didžiųjų/mažųjų raidžių skirtumo.
     */
    public List<User> getFilteredUsers(String userType, String login, String name, String surname) {
        return executeQuery(em -> new JpqlQueryBuilder("SELECT u FROM User u")
                .equalsIfPresent("TYPE(u)", "userType", resolveUserType(userType))
                .containsIgnoreCaseIfPresent("u.login", "login", login)
                .containsIgnoreCaseIfPresent("u.name", "name", name)
                .containsIgnoreCaseIfPresent("u.surname", "surname", surname)
                .build(em, User.class)
                .getResultList(), new ArrayList<>(), "Something went wrong when filtering users");
    }

    /** Grąžina esybės klasę pagal pavadinimą arba {@code null}, jei filtruoti pagal tipą nereikia. */
    static Class<? extends User> resolveUserType(String userType) {
        return userType == null ? null : USER_TYPES.get(userType);
    }

    /** Nauja kaina pašalinus patiekalą; niekada neigiama. Jei kurios nors kainos nėra, kaina nekeičiama. */
    static Double priceAfterRemoval(Double orderPrice, Double cuisinePrice) {
        if (orderPrice == null || cuisinePrice == null) {
            return orderPrice;
        }
        return Math.max(0, orderPrice - cuisinePrice);
    }

    private static TypedQuery<FoodOrder> orderWithItemsQuery(EntityManager em, int orderId) {
        return em.createQuery(ORDERS_WITH_ITEMS + " WHERE o.id = :orderId", FoodOrder.class)
                .setParameter("orderId", orderId);
    }
}
