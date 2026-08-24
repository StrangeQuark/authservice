package com.strangequark.authservice.authorization;

import com.strangequark.authservice.user.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"role", "authorization_id"}))
public class RoleAuthorization {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne
    private Authorization authorization;

    public RoleAuthorization() {
    }

    public RoleAuthorization(Role role, Authorization authorization) {
        this.role = role;
        this.authorization = authorization;
    }

    public UUID getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public Authorization getAuthorization() {
        return authorization;
    }
}
