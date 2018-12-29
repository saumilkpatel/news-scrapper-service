package manager;

import config.ScrapperConfig;
import entities.Article;
import entities.Author;
import java.io.Closeable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;
import utils.Constants;
import utils.DatabaseUtils;

public class DatabaseManager implements Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(DatabaseManager.class);
  private ScrapperConfig scrapperConfig;
  private Connection connection;

  public DatabaseManager(ScrapperConfig scrapperConfig) {
    this.scrapperConfig = scrapperConfig;
    openConnection();
  }

  private void openConnection() {
    try {
      this.connection = DriverManager.getConnection(scrapperConfig.getDbOutPath());
    } catch (SQLException e) {
      LOG.error("error creating connection for DB : {}", scrapperConfig.getDbOutPath(), e);
    }
  }

  public static void createTablesIfNotExist(ScrapperConfig config) {
    String sqlAuthor = "CREATE TABLE IF NOT EXISTS "+ Constants.AUTHOR_TABLE+" (\n"
        + Constants.AUTHOR_ID_COLUMN + " integer PRIMARY KEY,\n"
        + Constants.AUTHOR_NAME_COLUMN + " text NOT NULL\n"
        + ");";
    String sqlArticle = "CREATE TABLE IF NOT EXISTS "+Constants.ARTICLE_TABLE+" (\n"
        + Constants.ARTICLE_ID_COLUMN + " integer PRIMARY KEY,\n"
        + Constants.ARTICLE_TITLE_COLUMN + " text NOT NULL,\n"
        + Constants.ARTICLE_DESC_COLUMN + " text NOT NULL\n"
        + ");";
    String sqlAuthorship = "CREATE TABLE IF NOT EXISTS "+Constants.AUTHORSHIP_TABLE+" (\n"
        + Constants.AUTHOR_ID_COLUMN + " integer NOT NULL,\n"
        + Constants.ARTICLE_ID_COLUMN + " integer NOT NULL,\n"
        + " FOREIGN KEY("+ Constants.AUTHOR_ID_COLUMN +") REFERENCES "+Constants.AUTHOR_TABLE+"("+Constants.AUTHOR_ID_COLUMN+"),\n"
        + " FOREIGN KEY("+ Constants.ARTICLE_ID_COLUMN +") REFERENCES "+Constants.ARTICLE_TABLE+"("+Constants.ARTICLE_ID_COLUMN+"),\n"
        + " CONSTRAINT "+Constants.AUTHORSHIP_ID_COLUMN +" PRIMARY KEY ("+Constants.AUTHOR_ID_COLUMN+","+Constants.ARTICLE_ID_COLUMN+")\n"
        + ");";

    SQLiteConfig sqLiteConfig = new SQLiteConfig();
    sqLiteConfig.enforceForeignKeys(true);
    try (Connection connection = DriverManager.getConnection(config.getDbOutPath(), sqLiteConfig.toProperties())) {
      DatabaseUtils.execute(connection, sqlArticle);
      DatabaseUtils.execute(connection, sqlAuthor);
      DatabaseUtils.execute(connection, sqlAuthorship);
    } catch (SQLException e) {
      LOG.error("error creating connection to database : {}", config.getDbOutPath());
      e.printStackTrace();
    }
  }

  public void insertIntoArticleTable(int articleId, String title, String description) {
    String sql = "INSERT INTO "+Constants.ARTICLE_TABLE+" ("+Constants.ARTICLE_ID_COLUMN+", "+Constants.ARTICLE_TITLE_COLUMN+", "+Constants.ARTICLE_DESC_COLUMN+")\n"
        + "VALUES (" + articleId + ", '" + title + "', '" + description + "')";
    DatabaseUtils.execute(connection, sql);
  }

  public void insertIntoAuthorTable(int authorId, String authorName) {
    String sql = "INSERT INTO "+Constants.AUTHOR_TABLE+" ("+Constants.AUTHOR_ID_COLUMN+", "+Constants.AUTHOR_NAME_COLUMN+")\n"
        + "VALUES (" + authorId + ", '" + authorName + "')";
    DatabaseUtils.execute(connection, sql);
  }

  public void insertIntoAuthorshipTable(int authorId, int articleId) {
    String sql = "INSERT INTO "+Constants.AUTHORSHIP_TABLE+" ("+Constants.AUTHOR_ID_COLUMN+", "+Constants.ARTICLE_ID_COLUMN+")\n"
        + "VALUES (" + authorId + ", " + articleId + ")";
    DatabaseUtils.execute(connection, sql);
  }

  public boolean isArticleAlreadyInDB(String title) {
    String sql = "SELECT * FROM " + Constants.ARTICLE_TABLE
        + " WHERE " + Constants.ARTICLE_TITLE_COLUMN + "='" + title + "'";
    ResultSet resultSet = DatabaseUtils.executeQuery(connection, sql);
    try {
      if (resultSet.next()) {
        return true;
      }
    } catch (SQLException e) {
      LOG.error("error getting articles from db for title : {}", title);
    }
    return false;
  }

  public int getMaxID(String table, String field) {
    String sql = "SELECT max(" + field + ") FROM " + table;
    ResultSet resultSet = DatabaseUtils.executeQuery(connection, sql);
    try {
      if (resultSet.next()) {
        return resultSet.getInt(1);
      }
    } catch (SQLException e) {
      LOG.error("error while getting max id from table : {}, for column : {}", table, field, e);
    }
    return -1;
  }

  public int getAuthorIdFromName(String authorName) {
    if (connection != null) {
      try {
        String sql = "SELECT "+Constants.AUTHOR_ID_COLUMN+" FROM "+Constants.AUTHOR_TABLE+" WHERE "+Constants.AUTHOR_NAME_COLUMN+"='" + authorName + "'";
        ResultSet resultSet = DatabaseUtils.executeQuery(connection, sql);
        if (resultSet.next()) {
          return resultSet.getInt(Constants.AUTHOR_ID_COLUMN);
        }
        return -1;
      } catch (SQLException e) {
       LOG.error("error getting author id from given author name : {} from DB", authorName);
      }
    }
    return -1;
  }

  public List<Author> getAvailableAuthors() throws SQLException {
    String sql = "SELECT * FROM " + Constants.AUTHOR_TABLE;
    ResultSet resultSet = DatabaseUtils.executeQuery(connection, sql);

    List<Author> authors = new ArrayList<>();
    while (resultSet.next()) {
      int authorId = resultSet.getInt(Constants.AUTHOR_ID_COLUMN);
      String authorName = resultSet.getString(Constants.AUTHOR_NAME_COLUMN);
      authors.add(new Author(authorId, authorName));
    }
    return authors;
  }

  public List<Article> getAvailableArticles(int limit) throws SQLException {
    String sql = "SELECT * FROM " + Constants.ARTICLE_TABLE;
    if (limit > 0) {
      sql += " LIMIT " + limit;
    }
    ResultSet resultSet = DatabaseUtils.executeQuery(connection, sql);
    return getArticlesFromResultSet(resultSet);
  }

  public List<Article> getArticlesWith(String authorName, int limit) throws SQLException {
    String sql = "SELECT * FROM " + Constants.ARTICLE_TABLE + " WHERE " + Constants.ARTICLE_ID_COLUMN
        + " IN (SELECT " + Constants.ARTICLE_ID_COLUMN + " FROM " + Constants.AUTHORSHIP_TABLE
        + " NATURAL JOIN " + Constants.AUTHOR_TABLE + " WHERE "
        + Constants.AUTHOR_NAME_COLUMN + "='" + authorName.toLowerCase() + "')";
    if (limit > 0) {
      sql += " LIMIT " + limit;
    }
    ResultSet resultSet = DatabaseUtils.executeQuery(connection, sql);
    return getArticlesFromResultSet(resultSet);
  }

  public List<Article> getArticlesWith(String title, String description, int limit) throws SQLException {
    String sql = "SELECT * from " + Constants.ARTICLE_TABLE + " WHERE instr(" + Constants.ARTICLE_TITLE_COLUMN
        + ", '" + title + "') > 0 AND instr(" + Constants.ARTICLE_DESC_COLUMN
        + ", '" + description + "') > 0";
    if (limit > 0) {
      sql += " LIMIT " + limit;
    }
    ResultSet resultSet = DatabaseUtils.executeQuery(connection, sql);
    return getArticlesFromResultSet(resultSet);
  }

  private List<Article> getArticlesFromResultSet(ResultSet resultSet) throws SQLException {
    List<Article> articles = new ArrayList<>();
    while (resultSet.next()) {
      int articleId = resultSet.getInt(Constants.ARTICLE_ID_COLUMN);
      String title = resultSet.getString(Constants.ARTICLE_TITLE_COLUMN);
      String description = resultSet.getString(Constants.ARTICLE_DESC_COLUMN);
      articles.add(new Article(articleId, title, description));
    }
    return articles;
  }

  @Override
  public void close() {
    try {
      if (this.connection != null && !this.connection.isClosed()) {
        this.connection.close();
      }
    } catch (SQLException e) {
      LOG.error("error closing Connection for DB : {}", scrapperConfig.getDbOutPath(), e);
    }
  }
}
