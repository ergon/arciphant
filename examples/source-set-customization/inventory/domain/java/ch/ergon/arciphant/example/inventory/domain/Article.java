package ch.ergon.arciphant.example.inventory.domain;

import ch.ergon.arciphant.example.inventory.api.ArticleId;

public record Article(ArticleId id, String name) {
}
