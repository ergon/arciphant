package ch.ergon.arciphant.example.inventory.domain;

import ch.ergon.arciphant.example.inventory.api.ArticleId;

public final class ArticleFixtures {

    private ArticleFixtures() {
    }

    public static Article anArticle() {
        return new Article(new ArticleId("article-1"), "Example Article");
    }
}
