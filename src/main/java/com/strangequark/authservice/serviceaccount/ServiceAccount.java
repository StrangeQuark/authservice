package com.strangequark.authservice.serviceaccount;

import com.strangequark.authservice.utility.StringEncryptDecryptConverter;
import jakarta.persistence.*;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "service_accounts")
public class ServiceAccount {
    @Id
    @GeneratedValue
    private UUID id;

    @Convert(converter = StringEncryptDecryptConverter.class)
    private String clientId;

    private String clientPassword;

    @Convert(converter = StringEncryptDecryptConverter.class)
    private Set<String> authorizations;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientPassword() {
        return clientPassword;
    }

    public void setClientPassword(String clientPassword) {
        this.clientPassword = clientPassword;
    }

    public Set<String> getAuthorizations() {
        return authorizations;
    }

    public void setAuthorizations(Set<String> authorizations) {
        this.authorizations = authorizations;
    }
}
