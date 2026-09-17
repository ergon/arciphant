package ch.ergon.arciphant.example.inventory.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArticleTest {

    @Test
    void articleHasName() {
        assertEquals("Example Article", ArticleFixtures.anArticle().name());
    }
}
