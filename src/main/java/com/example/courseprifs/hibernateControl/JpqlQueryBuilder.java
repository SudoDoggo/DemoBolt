package com.example.courseprifs.hibernateControl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Surenka JPQL užklausą iš neprivalomų filtrų. Filtras pridedamas tik tada, kai reikšmė nurodyta,
 * todėl nebereikia {@code WHERE 1=1} triuko ir kartojamų {@code if} blokų.
 */
final class JpqlQueryBuilder {

    private final StringBuilder jpql;
    private final Map<String, Object> parameters = new LinkedHashMap<>();
    private boolean hasCondition;

    JpqlQueryBuilder(String baseQuery) {
        this.jpql = new StringBuilder(baseQuery);
    }

    /** {@code path = :param}, jei value != null. */
    JpqlQueryBuilder equalsIfPresent(String path, String param, Object value) {
        return addIfPresent(value, path + " = :" + param, param, value);
    }

    /** {@code path >= :param}, jei value != null. */
    JpqlQueryBuilder greaterOrEqualIfPresent(String path, String param, Object value) {
        return addIfPresent(value, path + " >= :" + param, param, value);
    }

    /** {@code path <= :param}, jei value != null. */
    JpqlQueryBuilder lessOrEqualIfPresent(String path, String param, Object value) {
        return addIfPresent(value, path + " <= :" + param, param, value);
    }

    /** Dalinė paieška be didžiųjų/mažųjų raidžių skirtumo, jei tekstas netuščias. */
    JpqlQueryBuilder containsIgnoreCaseIfPresent(String path, String param, String text) {
        if (!hasText(text)) {
            return this;
        }
        return add("LOWER(" + path + ") LIKE LOWER(:" + param + ")", param, "%" + text.trim() + "%");
    }

    <T> TypedQuery<T> build(EntityManager em, Class<T> resultType) {
        TypedQuery<T> query = em.createQuery(jpql.toString(), resultType);
        parameters.forEach(query::setParameter);
        return query;
    }

    String toJpql() {
        return jpql.toString();
    }

    Map<String, Object> parameters() {
        return Map.copyOf(parameters);
    }

    static boolean hasText(String text) {
        return text != null && !text.trim().isEmpty();
    }

    private JpqlQueryBuilder addIfPresent(Object value, String condition, String param, Object paramValue) {
        return value == null ? this : add(condition, param, paramValue);
    }

    private JpqlQueryBuilder add(String condition, String param, Object value) {
        jpql.append(hasCondition ? " AND " : " WHERE ").append(condition);
        hasCondition = true;
        parameters.put(param, value);
        return this;
    }
}
