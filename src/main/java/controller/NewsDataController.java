package controller;

import config.RestConfig;
import config.ScrapperConfig;
import entities.Article;
import entities.Author;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import manager.DatabaseManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import response.RestResponse;
import utils.Constants;

@Path("/news-service")
@Produces(MediaType.APPLICATION_JSON)
public class NewsDataController {

  private static final Logger LOG = LoggerFactory.getLogger(NewsDataController.class);
  private static final int OK = Response.Status.OK.getStatusCode();
  private static final int INTERNAL_SERVER_ERROR = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
  private final RestConfig restConfig;
  private final ScrapperConfig scrapperConfig;

  public NewsDataController(RestConfig restConfig, ScrapperConfig scrapperConfig) {
    this.restConfig = restConfig;
    this.scrapperConfig = scrapperConfig;
  }

  @Path("/authors")
  @GET
  public RestResponse getAvailableAuthors() {
    RestResponse response = new RestResponse();
    DatabaseManager databaseManager = new DatabaseManager(scrapperConfig);
    try {
      List<Author> availableAuthors = databaseManager.getAvailableAuthors();
      response.setData(availableAuthors);
      response.setResponseCode(OK);
    }
    catch (Exception e) {
      prepareFailureResponse(response, e);
    }
    databaseManager.close();
    return response;
  }

  @Path("/articles-by-author")
  @GET
  public RestResponse getArticlesBasedOnAuthorName(@QueryParam("authorName") String authorName,
                                                   @QueryParam("limit") int limit) {
    RestResponse response = new RestResponse();
    DatabaseManager databaseManager = new DatabaseManager(scrapperConfig);
    try {
      List<Article> articles = StringUtils.isBlank(authorName) ?
          databaseManager.getAvailableArticles(limit) : databaseManager.getArticlesWith(authorName, limit);
      response.setData(articles);
      response.setResponseCode(OK);
    }
    catch (Exception e) {
      prepareFailureResponse(response, e);
    }
    databaseManager.close();
    return response;
  }

  @Path("/articles-by-content")
  @GET
  public RestResponse getArticlesBasedOnContent(@QueryParam("title") String title,
                                                @QueryParam("description") String description,
                                                @QueryParam("limit") int limit) {
    RestResponse response = new RestResponse();
    DatabaseManager databaseManager = new DatabaseManager(scrapperConfig);
    title = title == null ? Constants.EMPTY_STRING : title;
    description = description == null ? Constants.EMPTY_STRING : description;
    try {
      List<Article> availableAuthors = databaseManager.getArticlesWith(title, description, limit);
      response.setData(availableAuthors);
      response.setResponseCode(OK);
    }
    catch (Exception e) {
      prepareFailureResponse(response, e);
    }
    databaseManager.close();
    return response;
  }

  private void prepareFailureResponse(RestResponse response, Exception e) {
    response.setData(null);
    response.setResponseCode(INTERNAL_SERVER_ERROR);
    response.setErrorMsg(e.getLocalizedMessage());
    LOG.error("error returning valid response", e);
  }

}
