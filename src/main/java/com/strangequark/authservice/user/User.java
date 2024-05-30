package com.strangequark.authservice.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
* An object to capture all the information related to users to be stored in our "authservice" database in the
* "users" table
*
* @author StrangeQuark
*/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")//Put User objects in the "users" DB table
public class User implements UserDetails {
    /**
     * A unique auto-generated ID for each user
     */
    @Id
    @GeneratedValue
    private Integer id;

    /**
     * A unique username for each user
     */
    private String username;

    /**
     * A unique email address for each user
     */
    private String email;

    /**
     * A password for the user
     */
    private String password;

    /**
     * A {@link Role} for the user
     */
    @Enumerated(EnumType.STRING)
    private Role role;

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
        return true;
    }
}
