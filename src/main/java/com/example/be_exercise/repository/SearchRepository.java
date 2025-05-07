package com.example.be_exercise.repository;

import com.example.be_exercise.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SearchRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public Page<User> searchUsers(int pageNumber, int pageSize, String username, String firstName, String lastName, String email) {
        StringBuilder sqlSelect = new StringBuilder("SELECT u FROM User u");
        StringBuilder sqlCount = new StringBuilder("SELECT COUNT(u) FROM User u");
        StringBuilder sqlPredicate = new StringBuilder(" WHERE u.isDelete = false");

        Map<String, String> params = new HashMap<>();

        // Check if username exist
        if (StringUtils.hasText(username)) {
            appendCondition(sqlPredicate, "u.username = :username");
            params.put("username", username);
        }

        // Check if firstName exist
        if (StringUtils.hasText(firstName)) {
            appendCondition(sqlPredicate, "u.firstName LIKE :firstName");
            params.put("firstName", "%" + firstName + "%");
        }

        // Check if lastName exist
        if (StringUtils.hasText(lastName)) {
            appendCondition(sqlPredicate, "u.lastName LIKE :lastName");
            params.put("lastName", "%" + lastName + "%");
        }

        // Check if email exist
        if (StringUtils.hasText(email)) {
            appendCondition(sqlPredicate, "u.email = :email");
            params.put("email", email);
        }

        // Add predicate into sql
        sqlSelect.append(sqlPredicate);
        sqlCount.append(sqlPredicate);

        // Create query from sql
        Query querySelect = entityManager.createQuery(sqlSelect.toString());
        Query queryCount = entityManager.createQuery(sqlCount.toString());

        // Set params
        params.forEach((key, value) -> {
            querySelect.setParameter(key, value);
            queryCount.setParameter(key, value);
        });

        // Create pageable
        pageNumber--;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        // Select
        querySelect.setFirstResult(pageNumber);
        querySelect.setMaxResults(pageSize);
        List<User> users = (List<User>) querySelect.getResultList();
        users = users.stream().filter(user -> !user.isDelete()).toList();

        // Count
        Long totalUsers = (Long) queryCount.getSingleResult();

        return new PageImpl<>(users, pageable, totalUsers);
    }

    private void appendCondition(StringBuilder predicate, String condition) {
        if (predicate.isEmpty()) {
            predicate.append(" WHERE ").append(condition);
        } else {
            predicate.append(" AND ").append(condition);
        }
    }
}
