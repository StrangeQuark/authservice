package com.strangequark.authservice.user;

import com.strangequark.authservice.utility.EncryptDecryptConverter;
import com.strangequark.authservice.utility.RoleEncryptDecryptConverter;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

/**
* An object to capture all the information related to users to be stored in our "authservice" database in the
* "users" table
*/

@Entity
@Table(name = "users")//Put User objects in the "users" DB table
public class User implements UserDetails {

    public User() {
    }
    public User(String username, String email, Role role, boolean isEnabled, HashSet<String> authorizations, String password) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.isEnabled = isEnabled;
        this.authorizations = authorizations;
        this.password = password;
    }

    /**
     * A unique auto-generated ID for each user
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * A unique username for each user
     */
    @Convert(converter = EncryptDecryptConverter.class)
    private String username;

    /**
     * A unique email address for each user
     */
    @Convert(converter = EncryptDecryptConverter.class)
    private String email;

    /**
     * A password for the user
     */
    private String password;

    /**
     * A {@link Role} for the user
     */
    @Convert(converter = RoleEncryptDecryptConverter.class)
    private Role role;

    /**
     * Refresh JWT token
     */
    @Convert(converter = EncryptDecryptConverter.class)
    @Column(length = 2048)
    private String refreshToken;

    /**
     * Boolean whether the account is enabled or disabled
     */
    private boolean isEnabled;

    /**
     * A set of authorizations for the user
     */
    @Convert(converter = EncryptDecryptConverter.class)
    private Set<String> authorizations;

    /**
     * Returns all the authorities granted to this user
     * @return The list of authorities granted to this user, depending on which role the user is assigned
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    /**
     * Returns whether the account is expired or not
     * @return True: Account is active, False: Account is expired
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Returns whether the account is locked or not
     * @return True: Account is open, False: Accoutn is locked
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Returns whether the credentials are expired or not
     * @return True: Credentials are active, False: Credentials are expired
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Returns whether the account is enabled or not
     * @return True: Account is enabled, False: Account is disabled
     */
    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Set the enabled boolean for the account
     */
    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    /**
     * Append to the list of authorizations
     */
    public void appendAuthorizations(Set<String> auths) {
        authorizations.addAll(auths);
    }

    /**
     * Remove from the list of authorizations
     */
    public void removeAuthorizations(Set<String> auths) {
        authorizations.removeAll(auths);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Set<String> getAuthorizations() {
        return authorizations;
    }

    public void setAuthorizations(Set<String> authorizations) {
        this.authorizations = authorizations;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
