package ru.job4j.articles.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.articles.model.Article;
import ru.job4j.articles.model.Word;
import ru.job4j.articles.service.generator.ArticleGenerator;
import ru.job4j.articles.store.ArticleStore;
import ru.job4j.articles.store.Store;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SimpleArticleService implements ArticleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleArticleService.class.getSimpleName());

    private final ArticleGenerator articleGenerator;

    public SimpleArticleService(ArticleGenerator articleGenerator) {
        this.articleGenerator = articleGenerator;
    }

    @Override
    public void generate(Store<Word> wordStore, int count, ArticleStore articleStore) throws SQLException {
        LOGGER.info("Геренация статей в количестве {}", count);
        List<Word> words = wordStore.findAll();
            /*List<Article> articles = IntStream.iterate(0, i -> i < count, i -> i + 1)
                    .peek(i -> LOGGER.info("Сгенерирована статья № {}", i + " тысяча"))
                    .mapToObj((x) -> articleGenerator.generate(words))
                    .collect(Collectors.toList());*/
        for (int i = 0; i < count / 1000; i++) {
            List<Article> articles = new ArrayList<>();
            for (int j = 0; j < 1000; j++) {
                articles.add(articleGenerator.generate(words));
            }
            LOGGER.info("Сгенерирована статья № {}", i + " тыща");
            articleStore.getConnection().setAutoCommit(false);
            articleStore.save(articles);
            articleStore.getConnection().commit();
            LOGGER.info("Сохранена № {}", i + " тыща");
        }

    }
}

