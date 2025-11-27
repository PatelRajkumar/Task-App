package com.pm.taskapp.auth.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pm.taskapp.auth.enitity.Permission;
import com.pm.taskapp.auth.enitity.Role;
import com.pm.taskapp.auth.enitity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Custom UserPrincipal class implementing Spring Security's UserDetails.
 * This class is used by Spring Security for authentication and authorization.
 */
public class UserPrincipal implements UserDetails {

    private UUID id;
    private String name;
    private String email;
    @JsonIgnore
    private String password;
    private boolean enabled;
    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(UUID id, String name, String email, String password,
                         boolean enabled, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    /**
     * Create UserPrincipal from User entity.
     */
    public static UserPrincipal create(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Add role authorities
        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                // Add role as authority
                authorities.add(new SimpleGrantedAuthority(role.getName()));

                // Add permissions as authorities
                if (role.getPermissions() != null) {
                    for (Permission permission : role.getPermissions()) {
                        authorities.add(new SimpleGrantedAuthority(permission.getCode()));
                    }
                }
            }
        }

        return new UserPrincipal(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPasswordHash(),
                user.isEnabled(),
                authorities
        );
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email; // Using email as username
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
        return enabled;
    }

    /**
     * Check if user has a specific role.
     */
    public boolean hasRole(String roleName) {
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(roleName));
    }

    /**
     * Check if user has a specific permission.
     */
    public boolean hasPermission(String permissionCode) {
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(permissionCode));
    }

    /**
     * Get all roles.
     */
    public Set<String> getRoles() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .collect(Collectors.toSet());
    }

    /**
     * Get all permissions.
     */
    public Set<String> getPermissions() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> !auth.startsWith("ROLE_"))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}