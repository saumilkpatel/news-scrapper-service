package service;

import config.ScrapperConfig;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import manager.DatabaseManager;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Constants;

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
      LOG.info("Database deleted : {}", delete);
    }
  }

  public void run() {
    DatabaseManager.createTablesIfNotExist(config);
    scrapAndStoreDataIntoDB(config);
  }

  private void scrapAndStoreDataIntoDB(ScrapperConfig config) {
    String webUrl = config.getBaseUrl() + "/" + config.getDateOfDataToScrap();
    DatabaseManager databaseManager = new DatabaseManager(config);
    try {
      Document document = Jsoup.connect(webUrl).get();
      Elements archiveElements = document.getElementsByClass("archive-list");

      AtomicInteger articleId = getSequenceNo(databaseManager, Constants.ARTICLE_TABLE, Constants.ARTICLE_ID_COLUMN);
      AtomicInteger authorId = getSequenceNo(databaseManager, Constants.AUTHOR_TABLE, Constants.AUTHOR_ID_COLUMN);

      for (int i = 0; i < archiveElements.size() && articleId.get() <= config.getMaxNoOfArticlesToScrap(); i++) {
        Element element = archiveElements.get(i);
        String articleLink = element.select("a").first().attr("href");
        Document articleDoc = Jsoup.connect(articleLink).get();
        populateDataIntoDB(databaseManager, articleDoc, articleId, authorId);
      }
      databaseManager.close();
    } catch (IOException e) {
      LOG.error("error getting web document from url : {}", webUrl, e);
      databaseManager.close();
    }
  }

  private void populateDataIntoDB(DatabaseManager databaseManager, Document articleDoc, AtomicInteger articleId, AtomicInteger authorId) {
    try {
      String title = articleDoc.getElementsByClass("title").first().text();
      String description = articleDoc.getElementsByTag("p").first().text();
      String authorName = articleDoc.getElementsByClass("mobile-author").first()
          .getElementsByTag("a").first().text().toLowerCase();

      if (StringUtils.isBlank(title) || StringUtils.isBlank(description) || StringUtils.isBlank(authorName)
          || databaseManager.isArticleAlreadyInDB(title)) {
        return;
      }
      int authorIdInDb = databaseManager.getAuthorIdFromName(authorName);
      if (authorIdInDb == -1) {
        authorIdInDb = authorId.getAndIncrement();
        databaseManager.insertIntoAuthorTable(authorIdInDb, authorName);
      }
      databaseManager.insertIntoArticleTable(articleId.get(), title, description);
      databaseManager.insertIntoAuthorshipTable(authorIdInDb, articleId.getAndIncrement());
      LOG.info("populated data in db for articleId : {}, authorId : {}", articleId.get()-1, authorIdInDb);
    }
    catch (Exception e) {
      LOG.error("error while populating data into db");
    }
  }

  private AtomicInteger getSequenceNo(DatabaseManager manager, String table, String column) {
    int maxID = manager.getMaxID(table, column);
    if (maxID != -1) {
      return new AtomicInteger(maxID+1);
    }
    else {
      LOG.info("max id not found in table : {}, for column : {}, Starting the sequence from 0", table, column);
      return new AtomicInteger(0);
    }
  }
}
