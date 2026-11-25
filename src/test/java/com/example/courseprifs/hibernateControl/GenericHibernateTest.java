package com.example.courseprifs.hibernateControl;

import com.example.courseprifs.model.BasicUser;
import com.example.courseprifs.model.Cuisine;
import com.example.courseprifs.model.User;
import com.example.courseprifs.utils.FxUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import javafx.scene.control.Alert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Charakterizaciniai (characterization) testai GenericHibernate klasei.
 * Parašyti PRIEŠ refaktoringą: jie fiksuoja dabartinę elgseną (įskaitant klaidų langus),
 * ir turi praeiti tiek prieš, tiek po refaktoringo.
 * <p>
 * FxUtils yra užmockintas (mockStatic), nes paprastame JUnit teste JavaFX toolkit nepaleistas
 * ir tikras Alert langas mestų IllegalStateException.
 */
class GenericHibernateTest {

    private EntityManagerFactory emf;
    private GenericHibernate hibernate;
    private MockedStatic<FxUtils> fxUtils;

    @BeforeEach
    void setUp() {
        emf = H2TestSupport.newEntityManagerFactory();
        hibernate = new GenericHibernate(emf);
        fxUtils = mockStatic(FxUtils.class);
    }

    @AfterEach
    void tearDown() {
        fxUtils.close();
        emf.close();
    }

    private static User newUser(String login) {
        return new User(login, "pass", "Vardas", "Pavarde", "+370600");
    }

    private void verifyNoAlerts() {
        fxUtils.verifyNoInteractions();
    }

    private void verifyErrorDialog(String header) {
        fxUtils.verify(() -> FxUtils.generateDialogAlert(eq(Alert.AlertType.ERROR), eq(header), any(Exception.class)));
    }

    // ---------- create ----------

    @Test
    void create_persistsEntity() {
        User user = newUser("jonas");

        hibernate.create(user);

        assertTrue(user.getId() > 0);
        User fromDb = hibernate.getEntityById(User.class, user.getId());
        assertEquals("jonas", fromDb.getLogin());
        verifyNoAlerts();
    }

    @Test
    void create_whenPersistFails_showsErrorDialog() {
        hibernate.create(newUser("dup"));
        User duplicateLogin = newUser("dup"); // login stulpelis yra unique

        hibernate.create(duplicateLogin);

        verifyErrorDialog("Something went wrong when creating");
        assertEquals(1, hibernate.getAllRecords(User.class).size());
    }

    // ---------- update ----------

    @Test
    void update_mergesChanges() {
        User user = newUser("petras");
        hibernate.create(user);

        user.setName("Naujas");
        hibernate.update(user);

        assertEquals("Naujas", hibernate.getEntityById(User.class, user.getId()).getName());
        verifyNoAlerts();
    }

    @Test
    void update_whenMergeFails_showsErrorDialog() {
        hibernate.update(null);

        verifyErrorDialog("Something went wrong when updating");
    }

    // ---------- getEntityById ----------

    @Test
    void getEntityById_returnsNullWhenNotFound() {
        assertNull(hibernate.getEntityById(User.class, 999));
        verifyNoAlerts();
    }

    @Test
    void getEntityById_returnsSubclassInstance() {
        BasicUser basicUser = new BasicUser("bu", "p", "N", "S", "1", "Adresas");
        hibernate.create(basicUser);

        User found = hibernate.getEntityById(User.class, basicUser.getId());

        assertInstanceOf(BasicUser.class, found);
    }

    @Test
    void getEntityById_whenDatabaseFails_showsErrorDialogAndReturnsNull() {
        EntityManagerFactory brokenEmf = mock(EntityManagerFactory.class);
        when(brokenEmf.createEntityManager()).thenThrow(new IllegalStateException("DB down"));

        User result = new GenericHibernate(brokenEmf).getEntityById(User.class, 1);

        assertNull(result);
        verifyErrorDialog("Failed to get information from data base");
    }

    // ---------- delete ----------

    @Test
    void delete_removesEntity() {
        User user = newUser("trinamas");
        hibernate.create(user);

        hibernate.delete(User.class, user.getId());

        assertNull(hibernate.getEntityById(User.class, user.getId()));
        verifyNoAlerts();
    }

    @Test
    void delete_whenEntityDoesNotExist_showsErrorDialog() {
        // Dabartinė elgsena: remove(null) meta IllegalArgumentException -> rodomas klaidos langas
        hibernate.delete(Cuisine.class, 12345);

        verifyErrorDialog("Something went wrong when deleting");
    }

    // ---------- getAllRecords ----------

    @Test
    void getAllRecords_returnsAllRowsIncludingSubclasses() {
        hibernate.create(newUser("a"));
        hibernate.create(new BasicUser("b", "p", "N", "S", "1", "Adr"));

        List<User> users = hibernate.getAllRecords(User.class);
        List<BasicUser> basicUsers = hibernate.getAllRecords(BasicUser.class);

        assertEquals(2, users.size());
        assertEquals(1, basicUsers.size());
    }

    @Test
    void getAllRecords_returnsEmptyListWhenTableEmpty() {
        assertTrue(hibernate.getAllRecords(Cuisine.class).isEmpty());
    }

    @Test
    void getAllRecords_whenDatabaseFails_returnsEmptyListWithoutDialog() {
        EntityManagerFactory brokenEmf = mock(EntityManagerFactory.class);
        when(brokenEmf.createEntityManager()).thenThrow(new IllegalStateException("DB down"));

        List<User> result = new GenericHibernate(brokenEmf).getAllRecords(User.class);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoAlerts();
    }

    // ---------- resursų tvarkymas ----------

    @Test
    void entityManagerIsClosedAfterSuccessfulAndFailedCalls() {
        EntityManagerFactory mockEmf = mock(EntityManagerFactory.class);
        EntityManager em = mock(EntityManager.class);
        EntityTransaction tx = mock(EntityTransaction.class);
        when(mockEmf.createEntityManager()).thenReturn(em);
        when(em.getTransaction()).thenReturn(tx);
        doThrow(new RuntimeException("persist failed")).when(em).persist(any());

        GenericHibernate generic = new GenericHibernate(mockEmf);
        generic.create(newUser("x"));
        generic.update(newUser("y"));

        verify(em, times(2)).close();
        verifyErrorDialog("Something went wrong when creating");
    }

    @Test
    void createManagerFailure_showsDialogForEveryWriteOperation() {
        EntityManagerFactory brokenEmf = mock(EntityManagerFactory.class);
        when(brokenEmf.createEntityManager()).thenThrow(new IllegalStateException("DB down"));
        GenericHibernate generic = new GenericHibernate(brokenEmf);

        generic.create(newUser("x"));
        generic.update(newUser("x"));
        generic.delete(User.class, 1);

        verifyErrorDialog("Something went wrong when creating");
        verifyErrorDialog("Something went wrong when updating");
        verifyErrorDialog("Something went wrong when deleting");
    }
}
