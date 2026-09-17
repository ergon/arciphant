package ch.ergon.arciphant.example.inventory.web;

import ch.ergon.arciphant.example.inventory.domain.Article;

public class ArticleResource {

    public String render(Article article) {
        return ApiVersion.VALUE + "/" + article.id().value();
    }
}
