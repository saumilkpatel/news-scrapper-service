package config;

@SuppressWarnings("unused")
public class ScrapperConfig {

  private String baseUrl;
  private String dateOfDataToScrap;
  private String dbOutPath;
  private String dbName;
  private boolean recreateDB;
  private int maxNoOfArticlesToScrap;

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getDateOfDataToScrap() {
    return dateOfDataToScrap;
  }

  public void setDateOfDataToScrap(String dateOfDataToScrap) {
    this.dateOfDataToScrap = dateOfDataToScrap;
  }

  public String getDbName() {
    return dbName;
  }

  public void setDbName(String dbName) {
    this.dbName = dbName;
  }

  public String getDbOutPath() {
    return dbOutPath;
  }

  public void setDbOutPath(String dbOutPath) {
    this.dbOutPath = dbOutPath;
  }

  public boolean isRecreateDB() {
    return recreateDB;
  }

  public void setRecreateDB(boolean recreateDB) {
    this.recreateDB = recreateDB;
  }

  public int getMaxNoOfArticlesToScrap() {
    return maxNoOfArticlesToScrap;
  }

  public void setMaxNoOfArticlesToScrap(int maxNoOfArticlesToScrap) {
    this.maxNoOfArticlesToScrap = maxNoOfArticlesToScrap;
  }
}
