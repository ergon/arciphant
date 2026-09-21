package ch.ergon.arciphant.example.orders.domain;

import ch.ergon.arciphant.example.orders.api.OrderId;

public record Order(OrderId id, String customer) {
}
