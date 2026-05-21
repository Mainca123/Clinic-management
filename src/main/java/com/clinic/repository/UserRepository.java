package com.clinic.repository;

import com.clinic.domain.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public Optional<User> findByUsernameOrEmail(String identifier) {
        return find("username = ?1 or email = ?1", identifier)
                .firstResultOptional();
    }

    public Optional<User> findByEmail(String email) {
        return find("email = ?1", email)
                .firstResultOptional();
    }

    public Optional<User> findByTokenVerified(String token) {
        return find("tokenVerified = ?1", token).firstResultOptional();
    }

    public List<User> searchUserByFullName(
            String keyword,
            Long currentUserId,
            int limit
    ) {

        if (keyword == null || keyword.trim().length() < 2) {
            return Collections.emptyList();
        }

        String search = "%" + keyword.trim().toLowerCase() + "%";

        return find("""
            lower(fullName) like ?1
            and id != ?2
            and isDeleted = false
            """,
                search,
                currentUserId
        )
                .page(Page.ofSize(limit))
                .list();
    }
    public List<User> getUsers(int page, int size) {
        return findAll()
                .page(page - 1, size)
                .list();
    }

    public long countUsers() {
        return count("isDeleted = false");
    }

}