package controller;

import application.ScrapperApplication;
import config.ScrapperConfig;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import response.RestResponse;
import service.ScrapperService;

@Path("/scrapper-service")
@Produces(MediaType.APPLICATION_JSON)
public class ScrapperController {

  private static final Logger LOG = LoggerFactory.getLogger(ScrapperController.class);
  private static final int OK = Response.Status.OK.getStatusCode();
  private static final int INTERNAL_SERVER_ERROR = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();

  @Path("/scrap-data")
  @PUT
  public RestResponse scrapData(@QueryParam("recreateDB") boolean recreateDB,
                                 @QueryParam("limit") int limit) {
    RestResponse response = new RestResponse();
    try {
      ScrapperConfig scrapperConfig = ScrapperApplication.loadConfig();
      assert scrapperConfig != null;
      scrapperConfig.setRecreateDB(recreateDB);
      if (limit > 0) {
        scrapperConfig.setMaxNoOfArticlesToScrap(limit);
      }
      new ScrapperService(scrapperConfig).run();
      response.setResponseCode(OK);
    }
    catch (Exception e) {
      response.setData(null);
      response.setResponseCode(INTERNAL_SERVER_ERROR);
      response.setErrorMsg(e.getLocalizedMessage());
      LOG.error("error scrapping data", e);
    }
    return response;
  }

}
