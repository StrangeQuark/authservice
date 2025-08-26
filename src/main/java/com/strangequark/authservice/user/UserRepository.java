package com.strangequark.authservice.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * An interface to manage the users' information in the database via Spring's JPA
 */
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Find a user in the database by their username
     * @param username The username to be retrieved
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user in the database by their email
     * @param email The email to be retrieved
     */
    Optional<User> findByEmail(String email);

    /**
     * Determine if at least 1 user exists with a given role
     * @param role {@link Role}
     */
    boolean existsByRole(Role role);

    /**
     * Return a list of user objects when passed a list of user IDs
     * @param ids to be retrieved
     */
    List<User> findByIdIn(List<UUID> ids);
}
