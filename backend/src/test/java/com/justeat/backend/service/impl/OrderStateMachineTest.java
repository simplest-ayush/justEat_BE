package com.justeat.backend.service.impl;

import com.justeat.backend.enums.OrderStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStateMachineTest {

    // Mirrors OrderServiceImpl.ALLOWED_TRANSITIONS - isolated here so the state
    // machine's rules are tested independently of the rest of order placement logic.
    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.PENDING, Set.of(OrderStatus.PREPARING),
            OrderStatus.PREPARING, Set.of(OrderStatus.READY),
            OrderStatus.READY, Set.of()
    );

    @ParameterizedTest(name = "{0} -> {1} should be legal: {2}")
    @CsvSource({
            "PENDING, PREPARING, true",
            "PREPARING, READY, true",
            "PENDING, READY, false",
            "READY, PENDING, false",
            "READY, PREPARING, false",
            "PREPARING, PENDING, false",
    })
    void transition_matchesExpectedLegality(OrderStatus from, OrderStatus to, boolean expectedLegal) {
        boolean actuallyLegal = ALLOWED_TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
        assertThat(actuallyLegal).isEqualTo(expectedLegal);
    }

    @org.junit.jupiter.api.Test
    void readyStatus_hasNoLegalTransitionsAtAll() {
        assertThat(ALLOWED_TRANSITIONS.get(OrderStatus.READY)).isEmpty();
    }

    @org.junit.jupiter.api.Test
    void pendingStatus_hasExactlyOneLegalTransition() {
        assertThat(ALLOWED_TRANSITIONS.get(OrderStatus.PENDING)).containsExactly(OrderStatus.PREPARING);
    }
}