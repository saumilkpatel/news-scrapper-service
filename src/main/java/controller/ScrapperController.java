package controller;

import application.ScrapperApplication;
import config.RestConfig;
import config.ScrapperConfig;
import entities.Author;
import java.util.List;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import manager.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import response.RestResponse;

@Path("/scrapper-service")
@Produces(MediaType.APPLICATION_JSON)
public class ScrapperController {

  private static final Logger LOG = LoggerFactory.getLogger(ScrapperController.class);
  private static final int OK = Response.Status.OK.getStatusCode();
  private static final int INTERNAL_SERVER_ERROR = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
  private final RestConfig restConfig;
  private final ScrapperConfig scrapperConfig;

  public ScrapperController(RestConfig restConfig, ScrapperConfig scrapperConfig) {
    this.restConfig = restConfig;
    this.scrapperConfig = scrapperConfig;
  }

  @Path("/scrap-data")
  @PUT
  private RestResponse scrapData(@FormParam("recreateDB") boolean recreateDB,
                                 @FormParam("limit") int limit) {
    RestResponse response = new RestResponse();
    DatabaseManager databaseManager = new DatabaseManager(scrapperConfig);
    try {
      ScrapperConfig scrapperConfig = ScrapperApplication.loadConfig();
      assert scrapperConfig != null;
      scrapperConfig.setRecreateDB(recreateDB);
      if (limit > 0) {
        scrapperConfig.setMaxNoOfArticlesToScrap(limit);
      }
      response.setResponseCode(OK);
    }
    catch (Exception e) {
      response.setData(null);
      response.setResponseCode(INTERNAL_SERVER_ERROR);
      response.setErrorMsg(e.getLocalizedMessage());
      LOG.error("error scrapping data", e);
    }
    databaseManager.close();
    return response;
  }

}
