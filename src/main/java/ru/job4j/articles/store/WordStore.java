package ru.job4j.articles.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.articles.model.Word;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class WordStore implements Store<Word>, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(WordStore.class.getSimpleName());

    private final Properties properties;

    private Connection connection;

    public WordStore(Properties properties) throws ClassNotFoundException {
        this.properties = properties;
        initConnection();
        initScheme();
        initWords();
    }

    private void initConnection() throws ClassNotFoundException {
        LOGGER.info("Подключение к базе данных слов");
        Class.forName("org.postgresql.Driver");
        try {
            connection = DriverManager.getConnection(
                    properties.getProperty("url"),
                    properties.getProperty("username"),
                    properties.getProperty("password")
            );
        } catch (SQLException e) {
            LOGGER.error("Не удалось выполнить операцию: { }", e.getCause());
            throw new IllegalStateException();
        }
    }

    private void initScheme() {
        LOGGER.info("Создание схемы таблицы слов");
        try (var statement = connection.createStatement()) {
            var sql = Files.readString(Path.of("db/scripts", "dictionary.sql"));
            statement.execute(sql);
        } catch (Exception e) {
            LOGGER.error("Не удалось выполнить операцию: { }", e.getCause());
            throw new IllegalStateException();
        }
    }

    private void initWords() {
        LOGGER.info("Заполнение таблицы слов");
        try (Statement statement = connection.createStatement()) {
            String sql = Files.readString(Path.of("db/scripts", "words.sql"));
            statement.executeLargeUpdate(sql);
        } catch (Exception e) {
            LOGGER.error("Не удалось выполнить операцию: { }", e.getCause());
            throw new IllegalStateException();
        }
    }

    @Override
    public List<Word> save(List<Word> model) {
        LOGGER.info("Добавление слова в базу данных");
        String sql = "insert into dictionary(word) values(?);";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (Word x: model) {
                statement.setString(1, x.getValue());
                statement.executeUpdate();
                ResultSet key = statement.getGeneratedKeys();
                if (key.next()) {
                    x.setId(key.getInt(1));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Не удалось выполнить операцию: { }", e.getCause());
            throw new IllegalStateException();
        }
        return model;
    }

    @Override
    public List<Word> findAll() {
        LOGGER.info("Загрузка всех слов");
        String sql = "select * from dictionary";
        ArrayList<Word> words = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet selection = statement.executeQuery();
            while (selection.next()) {
                words.add(new Word(
                        selection.getInt("id"),
                        selection.getString("word")
                ));
            }
        } catch (Exception e) {
            LOGGER.error("Не удалось выполнить операцию: { }", e.getCause());
            throw new IllegalStateException();
        }
        return words;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

}
