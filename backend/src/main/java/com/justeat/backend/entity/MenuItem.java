package com.justeat.backend.entity;

import com.justeat.backend.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "menu_items", uniqueConstraints = {
        @UniqueConstraint(name = "uk_menuitem_restaurant_name", columnNames = {"restaurant_id", "name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class MenuItem extends BaseEntity {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 50)
    private String category;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active=true;

    @Column(name = "is_special", nullable = false)
    @Builder.Default
    private boolean special=false;

    @Column(name = "special_until")
    private Instant specialUntil;

    @Column(name = "order_count", nullable = false)
    @Builder.Default
    private int orderCount = 0;

    @Column(name = "is_mostly_ordered", nullable = false)
    @Builder.Default
    private boolean mostlyOrdered = false;

}
