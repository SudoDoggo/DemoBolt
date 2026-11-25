package com.example.courseprifs.hibernateControl;

import com.example.courseprifs.model.*;
import com.example.courseprifs.utils.FxUtils;
import com.example.courseprifs.utils.PasswordUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import javafx.scene.control.Alert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Charakterizaciniai testai CustomHibernate klasei (parašyti PRIEŠ refaktoringą).
 * Sėkmės scenarijai tikrinami su tikru Hibernate + H2 DB, klaidų scenarijai su užmockintu
 * EntityManagerFactory. FxUtils užmockintas, kad būtų galima patikrinti, kokie langai rodomi.
 */
class CustomHibernateTest {

    private EntityManagerFactory emf;
    private CustomHibernate hibernate;
    private MockedStatic<FxUtils> fxUtils;

    private Restaurant restaurant;
    private Restaurant otherRestaurant;
    private BasicUser client;
    private BasicUser otherClient;
    private Driver driver;
    private User plainUser;
    private Cuisine pizza;
    private Cuisine soup;
    private Cuisine sushi;

    @BeforeEach
    void setUp() {
        emf = H2TestSupport.newEntityManagerFactory();
        hibernate = new CustomHibernate(emf);
        fxUtils = mockStatic(FxUtils.class);
        seedUsersAndMenu();
    }

    @AfterEach
    void tearDown() {
        fxUtils.close();
        emf.close();
    }

    private void seedUsersAndMenu() {
        plainUser = new User("admin", PasswordUtils.hashPassword("slaptas"), "Admin", "Adminas", "1");
        client = new BasicUser("jonas", "x", "Jonas", "Jonaitis", "2", "Vilnius");
        otherClient = new BasicUser("ona", "x", "Ona", "Onaite", "3", "Kaunas");
        restaurant = new Restaurant("picerija", "x", "Petras", "Picus", "4", "Vilnius");
        otherRestaurant = new Restaurant("sushi", "x", "Rita", "Sushiene", "5", "Kaunas");
        driver = new Driver("vairuotojas", "x", "Vytas", "Vairuotojas", "6", "Vilnius",
                "LT123", LocalDate.of(1990, 1, 1), VehicleType.values()[0], "Toyota");
        pizza = new Cuisine("Pica", "suris", 10.0, false, false, restaurant);
        soup = new Cuisine("Sriuba", "burokai", 4.5, false, true, restaurant);
        sushi = new Cuisine("Sushi", "zuvis", 12.0, false, false, otherRestaurant);

        H2TestSupport.inTransaction(emf, em -> {
            em.persist(plainUser);
            em.persist(client);
            em.persist(otherClient);
            em.persist(restaurant);
            em.persist(otherRestaurant);
            em.persist(driver);
            em.persist(pizza);
            em.persist(soup);
            em.persist(sushi);
        });
    }

    private FoodOrder persistOrder(String name, Double price, BasicUser buyer, Restaurant rest,
                                   OrderStatus status, LocalDate created, Cuisine... items) {
        FoodOrder order = new FoodOrder(name, price, buyer, new ArrayList<>(List.of(items)), rest);
        order.setOrderStatus(status);
        order.setDateCreated(created);
        H2TestSupport.inTransaction(emf, em -> em.persist(order));
        return order;
    }

    private static Set<String> names(List<FoodOrder> orders) {
        return orders.stream().map(FoodOrder::getName).collect(Collectors.toSet());
    }

    private static Set<String> logins(List<User> users) {
        return users.stream().map(User::getLogin).collect(Collectors.toSet());
    }

    private void verifyErrorDialog(String header) {
        fxUtils.verify(() -> FxUtils.generateDialogAlert(eq(Alert.AlertType.ERROR), eq(header), any(Exception.class)));
    }

    private static CustomHibernate brokenHibernate() {
        EntityManagerFactory brokenEmf = mock(EntityManagerFactory.class);
        when(brokenEmf.createEntityManager()).thenThrow(new IllegalStateException("DB down"));
        return new CustomHibernate(brokenEmf);
    }

    // =====================================================================
    @Nested
    class GetUserByCredentials {

        @Test
        void returnsUserWhenPasswordMatches() {
            User user = hibernate.getUserByCredentials("admin", "slaptas");
            assertNotNull(user);
            assertEquals(plainUser.getId(), user.getId());
        }

        @Test
        void returnsNullWhenPasswordWrong() {
            assertNull(hibernate.getUserByCredentials("admin", "blogas"));
        }

        @Test
        void returnsNullWhenLoginUnknown() {
            assertNull(hibernate.getUserByCredentials("nera", "slaptas"));
        }

        @Test
        void returnsNullWhenStoredPasswordIsNotHashed() {
            // "x" nėra BCrypt hash'as -> BCrypt meta IllegalArgumentException, kuris praryjamas
            assertNull(hibernate.getUserByCredentials("jonas", "x"));
        }

        @Test
        void returnsNullAndShowsNoDialogWhenDatabaseFails() {
            assertNull(brokenHibernate().getUserByCredentials("admin", "slaptas"));
            fxUtils.verifyNoInteractions();
        }
    }

    // =====================================================================
    @Nested
    class RestaurantQueries {

        @Test
        void getRestaurantOrders_returnsOnlyRestaurantOrdersWithItemsLoaded() {
            persistOrder("o1", 14.5, client, restaurant, OrderStatus.PENDING, LocalDate.now(), pizza, soup);
            persistOrder("o2", 12.0, client, otherRestaurant, OrderStatus.PENDING, LocalDate.now(), sushi);

            List<FoodOrder> orders = hibernate.getRestaurantOrders(restaurant);

            assertEquals(Set.of("o1"), names(orders));
            assertEquals(2, orders.get(0).getCuisineList().size()); // be LazyInitializationException
        }

        @Test
        void getRestaurantOrders_whenDatabaseFails_showsDialog() {
            assertTrue(brokenHibernate().getRestaurantOrders(restaurant).isEmpty());
            verifyErrorDialog("Something went wrong when getting information");
        }

        @Test
        void getRestaurantCuisine_returnsOnlyRestaurantMenu() {
            List<Cuisine> menu = hibernate.getRestaurantCuisine(restaurant);

            assertEquals(Set.of("Pica", "Sriuba"),
                    menu.stream().map(Cuisine::getName).collect(Collectors.toSet()));
        }

        @Test
        void getRestaurantCuisine_whenDatabaseFails_showsDialog() {
            assertTrue(brokenHibernate().getRestaurantCuisine(restaurant).isEmpty());
            verifyErrorDialog("Something went wrong when getting information");
        }

        @Test
        void getAllFoodOrdersWithItems_returnsAllOrdersWithoutDuplicates() {
            persistOrder("o1", 14.5, client, restaurant, OrderStatus.PENDING, LocalDate.now(), pizza, soup);
            persistOrder("o2", 12.0, client, otherRestaurant, OrderStatus.PENDING, LocalDate.now(), sushi);

            List<FoodOrder> orders = hibernate.getAllFoodOrdersWithItems();

            assertEquals(2, orders.size());
            assertEquals(Set.of("o1", "o2"), names(orders));
        }

        @Test
        void getAllFoodOrdersWithItems_whenDatabaseFails_showsDialog() {
            assertTrue(brokenHibernate().getAllFoodOrdersWithItems().isEmpty());
            verifyErrorDialog("Something went wrong when getting all orders with items");
        }
    }

    // =====================================================================
    @Nested
    class GetFilteredRestaurantOrders {

        private final LocalDate d1 = LocalDate.of(2025, 1, 10);
        private final LocalDate d2 = LocalDate.of(2025, 2, 10);
        private final LocalDate d3 = LocalDate.of(2025, 3, 10);

        @BeforeEach
        void seedOrders() {
            persistOrder("a", 10.0, client, restaurant, OrderStatus.PENDING, d1, pizza);
            persistOrder("b", 4.5, otherClient, restaurant, OrderStatus.DELIVERED, d2, soup);
            persistOrder("c", 12.0, client, otherRestaurant, OrderStatus.PENDING, d3, sushi);
        }

        @Test
        void noFilters_returnsAll() {
            assertEquals(Set.of("a", "b", "c"),
                    names(hibernate.getFilteredRestaurantOrders(null, null, null, null, null)));
        }

        @Test
        void byStatus() {
            assertEquals(Set.of("a", "c"),
                    names(hibernate.getFilteredRestaurantOrders(OrderStatus.PENDING, null, null, null, null)));
        }

        @Test
        void byClient() {
            assertEquals(Set.of("b"),
                    names(hibernate.getFilteredRestaurantOrders(null, otherClient, null, null, null)));
        }

        @Test
        void byRestaurant() {
            assertEquals(Set.of("a", "b"),
                    names(hibernate.getFilteredRestaurantOrders(null, null, null, null, restaurant)));
        }

        @Test
        void byDateRange_isInclusive() {
            assertEquals(Set.of("a", "b"),
                    names(hibernate.getFilteredRestaurantOrders(null, null, d1, d2, null)));
            assertEquals(Set.of("b", "c"),
                    names(hibernate.getFilteredRestaurantOrders(null, null, d2, null, null)));
            assertEquals(Set.of("a"),
                    names(hibernate.getFilteredRestaurantOrders(null, null, null, d1, null)));
        }

        @Test
        void allFiltersCombined() {
            assertEquals(Set.of("a"), names(hibernate.getFilteredRestaurantOrders(
                    OrderStatus.PENDING, client, d1, d3, restaurant)));
            assertTrue(hibernate.getFilteredRestaurantOrders(
                    OrderStatus.DELIVERED, client, d1, d3, restaurant).isEmpty());
        }

        @Test
        void resultHasItemsLoaded() {
            List<FoodOrder> orders = hibernate.getFilteredRestaurantOrders(null, otherClient, null, null, null);
            assertEquals("Sriuba", orders.get(0).getCuisineList().get(0).getName());
        }

        @Test
        void whenDatabaseFails_showsDialog() {
            assertTrue(brokenHibernate().getFilteredRestaurantOrders(null, null, null, null, null).isEmpty());
            verifyErrorDialog("Something went wrong when filtering orders");
        }
    }

    // =====================================================================
    @Nested
    class ChatQueries {

        private Chat chat;
        private FoodOrder order;

        @BeforeEach
        void seedChat() {
            order = persistOrder("su-chatu", 10.0, client, restaurant, OrderStatus.PENDING, LocalDate.now(), pizza);
            H2TestSupport.inTransaction(emf, em -> {
                FoodOrder managed = em.find(FoodOrder.class, order.getId());
                chat = new Chat("Pokalbis", managed);
                em.persist(chat);
                managed.setChat(chat);
                em.persist(new Review("Labas", client, chat));
                em.persist(new Review("Sveiki", restaurant, chat));
            });
        }

        @Test
        void getReviewsByChatId_returnsChatMessages() {
            List<Review> reviews = hibernate.getReviewsByChatId(chat.getId());
            assertEquals(Set.of("Labas", "Sveiki"),
                    reviews.stream().map(Review::getReviewText).collect(Collectors.toSet()));
        }

        @Test
        void getReviewsByChatId_unknownChat_returnsEmpty() {
            assertTrue(hibernate.getReviewsByChatId(999).isEmpty());
            fxUtils.verifyNoInteractions();
        }

        @Test
        void getReviewsByChatId_whenDatabaseFails_showsDialog() {
            assertTrue(brokenHibernate().getReviewsByChatId(1).isEmpty());
            verifyErrorDialog("Something went wrong when getting information");
        }

        @Test
        void getFoodOrderByChatId_returnsOrder() {
            FoodOrder found = hibernate.getFoodOrderByChatId(chat.getId());
            assertEquals(order.getId(), found.getId());
        }

        @Test
        void getFoodOrderByChatId_unknownChat_returnsNullAndShowsDialog() {
            // Dabartinė elgsena: NoResultException -> klaidos langas ir null
            assertNull(hibernate.getFoodOrderByChatId(999));
            verifyErrorDialog("Something went wrong when getting information");
        }
    }

    // =====================================================================
    @Nested
    class GetFoodOrderWithItems {

        @Test
        void returnsOrderWithItemsLoaded() {
            FoodOrder order = persistOrder("o", 14.5, client, restaurant, OrderStatus.PENDING, LocalDate.now(), pizza, soup);

            FoodOrder found = hibernate.getFoodOrderWithItems(order.getId());

            assertEquals(2, found.getCuisineList().size());
        }

        @Test
        void unknownOrder_returnsNullAndShowsDialog() {
            assertNull(hibernate.getFoodOrderWithItems(999));
            verifyErrorDialog("Something went wrong when getting order with items");
        }
    }

    // =====================================================================
    @Nested
    class RemoveCuisineFromOrder {

        @Test
        void removesItemRecalculatesPriceAndUpdatesDate() {
            FoodOrder order = persistOrder("o", 14.5, client, restaurant, OrderStatus.PENDING,
                    LocalDate.of(2025, 1, 1), pizza, soup);

            hibernate.removeCuisineFromOrder(order.getId(), pizza.getId());

            FoodOrder updated = hibernate.getFoodOrderWithItems(order.getId());
            assertEquals(1, updated.getCuisineList().size());
            assertEquals("Sriuba", updated.getCuisineList().get(0).getName());
            assertEquals(4.5, updated.getPrice(), 0.0001);
            assertEquals(LocalDate.now(), updated.getDateUpdated());
            fxUtils.verifyNoInteractions();
        }

        @Test
        void priceNeverGoesBelowZero() {
            FoodOrder order = persistOrder("o", 3.0, client, restaurant, OrderStatus.PENDING, LocalDate.now(), pizza);

            hibernate.removeCuisineFromOrder(order.getId(), pizza.getId());

            assertEquals(0.0, hibernate.getFoodOrderWithItems(order.getId()).getPrice(), 0.0001);
        }

        @Test
        void nullOrderPriceStaysNull() {
            FoodOrder order = persistOrder("o", null, client, restaurant, OrderStatus.PENDING, LocalDate.now(), pizza, soup);

            hibernate.removeCuisineFromOrder(order.getId(), pizza.getId());

            FoodOrder updated = hibernate.getFoodOrderWithItems(order.getId());
            assertNull(updated.getPrice());
            assertEquals(1, updated.getCuisineList().size());
        }

        @Test
        void nullCuisinePriceKeepsOrderPrice() {
            Cuisine free = new Cuisine("Vanduo", "", null, false, true, restaurant);
            H2TestSupport.inTransaction(emf, em -> em.persist(free));
            FoodOrder order = persistOrder("o", 10.0, client, restaurant, OrderStatus.PENDING, LocalDate.now(), pizza, free);

            hibernate.removeCuisineFromOrder(order.getId(), free.getId());

            FoodOrder updated = hibernate.getFoodOrderWithItems(order.getId());
            assertEquals(10.0, updated.getPrice(), 0.0001);
            assertEquals(1, updated.getCuisineList().size());
        }

        @Test
        void unknownOrder_showsErrorAlert() {
            hibernate.removeCuisineFromOrder(999, pizza.getId());

            fxUtils.verify(() -> FxUtils.generateAlert(Alert.AlertType.ERROR, "Error", "Order not found",
                    "Could not find the order with ID: 999"));
        }

        @Test
        void unknownCuisine_showsErrorAlertAndKeepsOrder() {
            FoodOrder order = persistOrder("o", 10.0, client, restaurant, OrderStatus.PENDING, LocalDate.now(), pizza);

            hibernate.removeCuisineFromOrder(order.getId(), 999);

            fxUtils.verify(() -> FxUtils.generateAlert(Alert.AlertType.ERROR, "Error", "Cuisine not found",
                    "Could not find the cuisine with ID: 999"));
            assertEquals(1, hibernate.getFoodOrderWithItems(order.getId()).getCuisineList().size());
        }

        @Test
        void cuisineNotInOrder_showsWarningAndChangesNothing() {
            FoodOrder order = persistOrder("o", 10.0, client, restaurant, OrderStatus.PENDING,
                    LocalDate.of(2025, 1, 1), pizza);

            hibernate.removeCuisineFromOrder(order.getId(), soup.getId());

            fxUtils.verify(() -> FxUtils.generateAlert(Alert.AlertType.WARNING, "Warning", "Item not in order",
                    "The selected cuisine is not part of this order."));
            FoodOrder unchanged = hibernate.getFoodOrderWithItems(order.getId());
            assertEquals(10.0, unchanged.getPrice(), 0.0001);
            assertNull(unchanged.getDateUpdated());
        }

        @Test
        void orderWithoutItems_showsWarning() {
            FoodOrder order = persistOrder("o", 10.0, client, restaurant, OrderStatus.PENDING, LocalDate.now());

            hibernate.removeCuisineFromOrder(order.getId(), pizza.getId());

            fxUtils.verify(() -> FxUtils.generateAlert(eq(Alert.AlertType.WARNING), anyString(), eq("Item not in order"), anyString()));
        }

        @Test
        void unexpectedError_showsDialogAndRollsBack() {
            EntityManagerFactory mockEmf = mock(EntityManagerFactory.class);
            EntityManager em = mock(EntityManager.class);
            EntityTransaction tx = mock(EntityTransaction.class);
            when(mockEmf.createEntityManager()).thenReturn(em);
            when(em.getTransaction()).thenReturn(tx);
            when(tx.isActive()).thenReturn(true);
            when(em.createQuery(anyString(), eq(FoodOrder.class))).thenThrow(new RuntimeException("boom"));

            new CustomHibernate(mockEmf).removeCuisineFromOrder(1, 1);

            verifyErrorDialog("Something went wrong when removing cuisine from order");
            verify(tx).rollback();
            verify(em).close();
        }

        @Test
        void unexpectedError_withInactiveTransaction_doesNotRollBack() {
            EntityManagerFactory mockEmf = mock(EntityManagerFactory.class);
            EntityManager em = mock(EntityManager.class);
            EntityTransaction tx = mock(EntityTransaction.class);
            when(mockEmf.createEntityManager()).thenReturn(em);
            when(em.getTransaction()).thenReturn(tx);
            when(tx.isActive()).thenReturn(false);
            when(em.createQuery(anyString(), eq(FoodOrder.class))).thenThrow(new RuntimeException("boom"));

            new CustomHibernate(mockEmf).removeCuisineFromOrder(1, 1);

            verify(tx, never()).rollback();
            verify(em).close();
        }

        @Test
        void entityManagerCreationFails_showsDialog() {
            brokenHibernate().removeCuisineFromOrder(1, 1);
            verifyErrorDialog("Something went wrong when removing cuisine from order");
        }
    }

    // =====================================================================
    @Nested
    class GetFilteredUsers {

        private final Set<String> everyone =
                Set.of("admin", "jonas", "ona", "picerija", "sushi", "vairuotojas");

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "All users", "NeraTokioTipo", "user", " User "})
        void typeThatDoesNotFilter_returnsEveryone(String userType) {
            assertEquals(everyone, logins(hibernate.getFilteredUsers(userType, null, null, null)));
        }

        @Test
        void typeUser_returnsOnlyPlainUsers() {
            assertEquals(Set.of("admin"), logins(hibernate.getFilteredUsers("User", null, null, null)));
        }

        @Test
        void typeBasicUser_returnsOnlyExactBasicUsers() {
            assertEquals(Set.of("jonas", "ona"), logins(hibernate.getFilteredUsers("BasicUser", null, null, null)));
        }

        @Test
        void typeRestaurant() {
            assertEquals(Set.of("picerija", "sushi"), logins(hibernate.getFilteredUsers("Restaurant", null, null, null)));
        }

        @Test
        void typeDriver() {
            assertEquals(Set.of("vairuotojas"), logins(hibernate.getFilteredUsers("Driver", null, null, null)));
        }

        @Test
        void loginFilter_isCaseInsensitivePartialAndTrimmed() {
            assertEquals(Set.of("picerija"), logins(hibernate.getFilteredUsers(null, "  PICE ", null, null)));
        }

        @Test
        void nameFilter() {
            assertEquals(Set.of("ona", "jonas"), logins(hibernate.getFilteredUsers(null, null, "on", null)));
        }

        @Test
        void surnameFilter() {
            assertEquals(Set.of("sushi"), logins(hibernate.getFilteredUsers(null, null, null, "sushi")));
        }

        @Test
        void blankTextFilters_areIgnored() {
            assertEquals(everyone, logins(hibernate.getFilteredUsers("All users", " ", "", "   ")));
        }

        @Test
        void allFiltersCombined() {
            assertEquals(Set.of("jonas"), logins(hibernate.getFilteredUsers("BasicUser", "jo", "JONAS", "jonait")));
            assertTrue(hibernate.getFilteredUsers("Driver", "jo", null, null).isEmpty());
        }

        @Test
        void percentSignMatchesEverything() {
            // Dabartinė elgsena: % neišvalomas ir veikia kaip LIKE wildcard
            assertEquals(everyone, logins(hibernate.getFilteredUsers(null, "%", null, null)));
        }

        @Test
        void whenDatabaseFails_showsDialog() {
            assertTrue(brokenHibernate().getFilteredUsers("User", "a", "b", "c").isEmpty());
            verifyErrorDialog("Something went wrong when filtering users");
        }
    }
}
