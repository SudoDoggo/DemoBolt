package com.example.courseprifs.hibernateControl;

import com.example.courseprifs.model.BasicUser;
import com.example.courseprifs.model.Driver;
import com.example.courseprifs.model.Restaurant;
import com.example.courseprifs.model.User;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Testai metodams ir priklausomybėms, kurie atsirado refaktoringo metu
 * (resolveUserType, priceAfterRemoval, UserNotifier įšvirkštimas).
 */
class RefactoredHelpersTest {

    @Test
    void resolveUserType_knownTypes() {
        assertEquals(User.class, CustomHibernate.resolveUserType("User"));
        assertEquals(BasicUser.class, CustomHibernate.resolveUserType("BasicUser"));
        assertEquals(Restaurant.class, CustomHibernate.resolveUserType("Restaurant"));
        assertEquals(Driver.class, CustomHibernate.resolveUserType("Driver"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", CustomHibernate.ALL_USERS, "driver", "Admin"})
    void resolveUserType_returnsNullWhenNoTypeFilter(String userType) {
        assertNull(CustomHibernate.resolveUserType(userType));
    }

    @Test
    void priceAfterRemoval_subtractsAndNeverGoesNegative() {
        assertEquals(6.0, CustomHibernate.priceAfterRemoval(10.0, 4.0), 0.0001);
        assertEquals(0.0, CustomHibernate.priceAfterRemoval(3.0, 4.0), 0.0001);
    }

    @Test
    void priceAfterRemoval_missingPriceKeepsOrderPrice() {
        assertNull(CustomHibernate.priceAfterRemoval(null, 4.0));
        assertEquals(10.0, CustomHibernate.priceAfterRemoval(10.0, null));
    }

    @Test
    void injectedNotifierReceivesErrors_withoutJavaFx() {
        EntityManagerFactory brokenEmf = mock(EntityManagerFactory.class);
        when(brokenEmf.createEntityManager()).thenThrow(new IllegalStateException("DB down"));
        UserNotifier notifier = mock(UserNotifier.class);
        CustomHibernate hibernate = new CustomHibernate(brokenEmf, notifier);

        hibernate.getFilteredUsers("User", null, null, null);
        hibernate.create(new User());
        hibernate.getAllRecords(User.class);

        verify(notifier).error(eq("Something went wrong when filtering users"), any(Exception.class));
        verify(notifier).error(eq("Something went wrong when creating"), any(Exception.class));
        verifyNoMoreInteractions(notifier); // getAllRecords klaidas praryja tyliai
    }
}
