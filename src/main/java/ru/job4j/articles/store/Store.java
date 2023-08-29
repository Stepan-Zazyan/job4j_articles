package ru.job4j.articles.store;

import java.sql.SQLException;
import java.util.List;

public interface Store<T> {
    List<T> save(List<T> model) throws SQLException;
    List<T> findAll();
}
