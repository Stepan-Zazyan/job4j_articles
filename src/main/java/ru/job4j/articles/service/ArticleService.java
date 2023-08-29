package ru.job4j.articles.service;

import ru.job4j.articles.model.Word;
import ru.job4j.articles.store.ArticleStore;
import ru.job4j.articles.store.Store;

import java.sql.SQLException;

public interface ArticleService {
    void generate(Store<Word> wordStore, int count, ArticleStore articleStore) throws SQLException;
}
