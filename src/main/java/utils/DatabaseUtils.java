package utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseUtils {

  private static final Logger LOG = LoggerFactory.getLogger(DatabaseUtils.class);

  public static void execute(Connection connection, String sql) {
    if (connection != null) {
      try {
        Statement statement = connection.createStatement();
        statement.execute(sql);
      } catch (SQLException e) {
        LOG.error("error while running SQLite query : {}", sql, e);
      }
    }
  }

  public static ResultSet executeQuery(Connection connection, String sql) {
    if (connection != null) {
      try {
        Statement statement = connection.createStatement();
        return statement.executeQuery(sql);
      } catch (SQLException e) {
        LOG.error("error while running SQLite query : {}", sql, e);
      }
    }
    return null;
  }
}
