package com.pm.taskapp.auth.enitity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;


import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_enabled", columnList = "enabled")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {


    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;


    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;


    @Column(name = "avatar_url", length = 1000)
    private String avatarUrl;


    @Column(name = "name", nullable = false, length = 255)
    private String name;


    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;


    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;


    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;


    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
}