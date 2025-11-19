package com.pm.taskapp.auth.enitity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {


    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;


    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;


    @Column(name = "description", columnDefinition = "text")
    private String description;


    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles = new HashSet<>();
}
