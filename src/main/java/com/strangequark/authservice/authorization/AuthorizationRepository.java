package com.strangequark.authservice.authorization;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthorizationRepository extends JpaRepository<Authorization, UUID> {
    Optional<Authorization> findByName(String name);
}
