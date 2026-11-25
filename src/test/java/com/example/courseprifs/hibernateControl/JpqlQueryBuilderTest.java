package com.example.courseprifs.hibernateControl;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Naujos pagalbinės klasės testai (pridėti refaktoringo metu). */
class JpqlQueryBuilderTest {

    @Test
    void noConditions_keepsBaseQuery() {
        JpqlQueryBuilder builder = new JpqlQueryBuilder("SELECT u FROM User u")
                .equalsIfPresent("u.id", "id", null)
                .containsIgnoreCaseIfPresent("u.name", "name", "  ");

        assertEquals("SELECT u FROM User u", builder.toJpql());
        assertTrue(builder.parameters().isEmpty());
    }

    @Test
    void firstConditionUsesWhere_nextUseAnd() {
        JpqlQueryBuilder builder = new JpqlQueryBuilder("SELECT o FROM FoodOrder o")
                .equalsIfPresent("o.id", "id", 5)
                .greaterOrEqualIfPresent("o.price", "min", 1.0)
                .lessOrEqualIfPresent("o.price", "max", 9.0);

        assertEquals("SELECT o FROM FoodOrder o WHERE o.id = :id AND o.price >= :min AND o.price <= :max",
                builder.toJpql());
        assertEquals(Map.of("id", 5, "min", 1.0, "max", 9.0), builder.parameters());
    }

    @Test
    void containsIgnoreCase_trimsAndWrapsWithWildcards() {
        JpqlQueryBuilder builder = new JpqlQueryBuilder("SELECT u FROM User u")
                .containsIgnoreCaseIfPresent("u.login", "login", "  Jon ");

        assertEquals("SELECT u FROM User u WHERE LOWER(u.login) LIKE LOWER(:login)", builder.toJpql());
        assertEquals("%Jon%", builder.parameters().get("login"));
    }

    @Test
    void hasText() {
        assertFalse(JpqlQueryBuilder.hasText(null));
        assertFalse(JpqlQueryBuilder.hasText(""));
        assertFalse(JpqlQueryBuilder.hasText(" \t "));
        assertTrue(JpqlQueryBuilder.hasText(" a "));
    }
}
