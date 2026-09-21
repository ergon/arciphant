package ch.ergon.arciphant.example.orders.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderTest {

    @Test
    void orderHasCustomer() {
        assertEquals("Example Customer", OrderFixtures.anOrder().customer());
    }
}
