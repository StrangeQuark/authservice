package com.strangequark.authservice.serviceaccount;

import com.strangequark.authservice.authorization.Authorization;
import com.strangequark.authservice.utility.StringEncryptDecryptConverter;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "service_accounts")
public class ServiceAccount implements UserDetails {
    @Id
    @GeneratedValue
    private UUID id;

    @Convert(converter = StringEncryptDecryptConverter.class)
    private String clientId;

    private String clientPassword;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Authorization> authorizations = new HashSet<>();

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

    public Set<Authorization> getAuthorizations() {
        return authorizations;
    }

    public void setAuthorizations(Set<Authorization> authorizations) {
        this.authorizations = authorizations;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("SERVICE_ACCOUNT"),
                new SimpleGrantedAuthority(clientId.toUpperCase() + "_SERVICE")
        );
    }

    @Override
    public String getPassword() {
        return clientPassword;
    }

    @Override
    public String getUsername() {
        return clientId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
