package service;

import config.ScrapperConfig;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Constants;
import utils.DatabaseUtils;

public class ScrapperService {

  private final static Logger LOG = LoggerFactory.getLogger(ScrapperService.class);

  private final ScrapperConfig config;

  public ScrapperService(ScrapperConfig config) {
    this.config = config;
    initialize();
  }

  private void initialize() {
    if (config.isRecreateDB()) {
      String[] split = config.getDbOutPath().split(":");
      File dbFile = new File(split[2]);
      boolean delete = dbFile.exists() && dbFile.delete();
      LOG.error("Database deleted : {}", delete);
    }
  }

  public void run() {
    DatabaseUtils.createTablesIfNotExist(config);
    scrapAndStoreDataIntoDB(config);

  }

  private void scrapAndStoreDataIntoDB(ScrapperConfig config) {
    String webUrl = config.getBaseUrl() + "/" + config.getDateOfDataToScrap();
    try(Connection connection = DriverManager.getConnection(config.getDbOutPath())) {
      Document document = Jsoup.connect(webUrl).get();
      Elements archiveElements = document.getElementsByClass("archive-list");

      AtomicInteger articleId = getSequenceNo(connection, Constants.ARTICLE_TABLE, Constants.ARTICLE_ID_COLUMN);
      AtomicInteger authorId = getSequenceNo(connection, Constants.AUTHOR_TABLE, Constants.AUTHOR_ID_COLUMN);

      for (int i = 0; i < archiveElements.size() && i < config.getMaxNoOfArticlesToScrap(); i++) {
        Element element = archiveElements.get(i);
        String articleLink = element.select("a").first().attr("href");
        Document articleDoc = Jsoup.connect(articleLink).get();
        populateDataIntoDB(connection, articleDoc, articleId, authorId);
      }
    } catch (IOException e) {
      LOG.error("error getting web document from url : {}", webUrl, e);
    } catch (SQLException e) {
      LOG.error("error while creating SQLite Connection : {}", config.getDbOutPath(), e);
    }
  }

  private void populateDataIntoDB(Connection connection, Document articleDoc, AtomicInteger articleId, AtomicInteger authorId) {
    try {

      String title = articleDoc.getElementsByClass("title").first().text();
      String description = articleDoc.getElementsByTag("p").first().text();
      DatabaseUtils.insertIntoArticleTable(connection, articleId.get(), title, description);

      String authorName = articleDoc.getElementsByClass("mobile-author").first()
          .getElementsByTag("a").first().text().toLowerCase();

      int authorIdinDb = DatabaseUtils.getAuthorIdFromName(connection, authorName);
      if (authorIdinDb == -1) {
        authorIdinDb = authorId.getAndIncrement();
        DatabaseUtils.insertIntoAuthorTable(connection, authorIdinDb, authorName);
      }
      DatabaseUtils.insertIntoAuthorshipTable(connection, authorIdinDb, articleId.getAndIncrement());
    }
    catch (Exception e) {
      LOG.error("error while populating data into db", e);
    }
  }

  private AtomicInteger getSequenceNo(Connection connection, String table, String column) {
    int maxID = DatabaseUtils.getMaxID(connection, table, column);
    if (maxID != -1) {
      return new AtomicInteger(maxID+1);
    }
    else {
      LOG.info("max id not found in table : {}, for column : {}, Starting the sequence from 0", table, column);
      return new AtomicInteger(0);
    }
  }
}
