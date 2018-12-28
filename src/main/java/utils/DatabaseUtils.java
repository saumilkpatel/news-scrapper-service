package utils;

import config.ScrapperConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;

public class DatabaseUtils {

  private static final Logger LOG = LoggerFactory.getLogger(DatabaseUtils.class);

  public static void createTablesIfNotExist(ScrapperConfig config) {
    String sqlAuthor = "CREATE TABLE IF NOT EXISTS "+Constants.AUTHOR_TABLE+" (\n"
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
      execute(connection, sqlArticle);
      execute(connection, sqlAuthor);
      execute(connection, sqlAuthorship);
    } catch (SQLException e) {
      LOG.error("error creating connection to database : {}", config.getDbOutPath());
      e.printStackTrace();
    }
  }

  public static void insertIntoArticleTable(Connection connection, int articleId, String title, String description) {
    String sql = "INSERT into "+Constants.ARTICLE_TABLE+" ("+Constants.ARTICLE_ID_COLUMN+", "+Constants.ARTICLE_TITLE_COLUMN+", "+Constants.ARTICLE_DESC_COLUMN+")\n"
        + "VALUES (" + articleId + ", '" + title + "', '" + description + "')";
    execute(connection, sql);
  }

  public static void insertIntoAuthorTable(Connection connection, int authorId, String authorName) {
    String sql = "INSERT into "+Constants.AUTHOR_TABLE+" ("+Constants.AUTHOR_ID_COLUMN+", "+Constants.AUTHOR_NAME_COLUMN+")\n"
        + "VALUES (" + authorId + ", '" + authorName + "')";
    execute(connection, sql);
  }

  public static void insertIntoAuthorshipTable(Connection connection, int authorId, int articleId) {
    String sql = "INSERT into "+Constants.AUTHORSHIP_TABLE+" ("+Constants.AUTHOR_ID_COLUMN+", "+Constants.ARTICLE_ID_COLUMN+")\n"
        + "VALUES (" + authorId + ", " + articleId + ")";
    execute(connection, sql);
  }

  public static int getAuthorIdFromName(Connection connection, String authorName) {
    if (connection != null) {
      try {
        String sql = "SELECT "+Constants.AUTHOR_ID_COLUMN+" from "+Constants.AUTHOR_TABLE+" WHERE "+Constants.AUTHOR_NAME_COLUMN+"='" + authorName + "'";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        if (resultSet.next()) {
          return resultSet.getInt(Constants.AUTHOR_ID_COLUMN);
        }
        return -1;
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    return -1;
  }

  private static void execute(Connection connection, String sql) {
    if (connection != null) {
      try {
        Statement statement = connection.createStatement();
        statement.execute(sql);
      } catch (SQLException e) {
        LOG.error("error while running SQLite query : {}", sql, e);
      }
    }
  }
}
