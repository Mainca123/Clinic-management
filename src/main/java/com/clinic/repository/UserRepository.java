package com.clinic.repository;

import com.clinic.domain.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public Optional<User> findByUsernameOrEmail(String identifier) {
        return find("username = ?1 or email = ?1", identifier)
                .firstResultOptional();
    }
}