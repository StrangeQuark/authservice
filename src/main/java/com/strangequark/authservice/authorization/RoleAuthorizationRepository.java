package com.strangequark.authservice.authorization;

import com.strangequark.authservice.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleAuthorizationRepository extends JpaRepository<RoleAuthorization, UUID> {
    List<RoleAuthorization> findByRole(Role role);

    Optional<RoleAuthorization> findByRoleAndAuthorization(Role role, Authorization authorization);

    boolean existsByAuthorization(Authorization authorization);
}
