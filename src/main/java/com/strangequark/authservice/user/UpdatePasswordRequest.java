package com.strangequark.authservice.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object for updating the user's password
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePasswordRequest {

    /**
     * Password of authenticating user
     */
    private String password;

    /**
     * New password for update request
     */
    private String newPassword;
}