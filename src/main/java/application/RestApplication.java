package application;

import config.RestConfig;
import config.ScrapperConfig;
import controller.NewsDataController;
import controller.ScrapperController;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import service.ScrapperService;

public class RestApplication extends Application<RestConfig> {

  public static void main(String[] args) throws Exception {
    new RestApplication().run(args);
  }

  @Override
  public void run(RestConfig restConfig, Environment environment) {
    ScrapperConfig scrapperConfig = ScrapperApplication.loadConfig();
    if (restConfig.isScrappingEnabledOnStartup()) {
      new ScrapperService(scrapperConfig).run();
    }
    environment.jersey().register(new ScrapperController());
    environment.jersey().register(new NewsDataController(scrapperConfig));
    configureCors(environment);
  }

  private void configureCors(Environment environment) {
    Dynamic filter = environment.servlets().addFilter("CORS", CrossOriginFilter.class);
    filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    filter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,PUT,POST,DELETE,OPTIONS");
    filter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
    filter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
    filter.setInitParameter("allowedHeaders", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
    filter.setInitParameter("allowCredentials", "true");
  }

}
