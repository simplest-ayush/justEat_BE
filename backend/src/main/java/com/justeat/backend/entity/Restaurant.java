package com.justeat.backend.entity;

import com.justeat.backend.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "restaurants", uniqueConstraints = {
        @UniqueConstraint(name = "uk_restaurant_owner_name", columnNames = {"owner_id", "name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false,  onlyExplicitlyIncluded = true)
public class Restaurant extends BaseEntity {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "cuisine_type", length = 50)
    private String cuisineType;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 10)
    private String phone;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

}
