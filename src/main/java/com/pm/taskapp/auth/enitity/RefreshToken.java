package com.pm.taskapp.auth.enitity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;


import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_tokens_token", columnList = "token"),
        @Index(name = "idx_refresh_tokens_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {


    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;


    @Column(name = "token", nullable = false, unique = true, length = 500)
    private String token;


    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}