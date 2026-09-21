package ch.ergon.arciphant.example.orders.domain;

import ch.ergon.arciphant.example.orders.api.OrderId;

public final class OrderFixtures {

    private OrderFixtures() {
    }

    public static Order anOrder() {
        return new Order(new OrderId("order-1"), "Example Customer");
    }
}
