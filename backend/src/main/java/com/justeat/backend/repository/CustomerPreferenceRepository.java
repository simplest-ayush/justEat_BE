package com.justeat.backend.repository;

import com.justeat.backend.entity.CustomerPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerPreferenceRepository extends JpaRepository<CustomerPreference, UUID> {

    Optional<CustomerPreference> findByCustomerId(UUID customerId);
}
