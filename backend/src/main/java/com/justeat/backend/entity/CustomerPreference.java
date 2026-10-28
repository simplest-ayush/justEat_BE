package com.justeat.backend.entity;

import com.justeat.backend.entity.base.BaseEntity;
import com.justeat.backend.enums.DietaryRestriction;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "customer_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class CustomerPreference extends BaseEntity {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private User customer;

    @ElementCollection
    @CollectionTable(name = "preference_cuisines", joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "cuisine")
    @Builder.Default
    private Set<String> preferredCuisines = new HashSet<>();

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "preference_dietary_restrictions", joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "restriction")
    @Builder.Default
    private Set<DietaryRestriction> dietaryRestrictions = new HashSet<>();
}