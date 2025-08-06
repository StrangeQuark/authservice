package com.strangequark.authservice.serviceaccount;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceAccountRepository extends JpaRepository<ServiceAccount, Integer> {

    /**
     * Find a service account in the database by their clientId
     * @param clientId The clientId to be retrieved
     */
    Optional<ServiceAccount> findByClientId(String clientId);
}
