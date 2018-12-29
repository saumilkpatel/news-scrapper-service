package application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import config.ScrapperConfig;
import java.io.File;
import java.net.URL;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.ScrapperService;

public class ScrapperApplication {

  private static final Logger LOG = LoggerFactory.getLogger(ScrapperApplication.class);

  public static void main(String[] args) {
    ScrapperConfig config = loadConfig();
    ScrapperService scrapperService = new ScrapperService(config);
    scrapperService.run();
  }

  public static ScrapperConfig loadConfig() {
    try {
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      URL resource = ScrapperApplication.class.getClassLoader().getResource("scrapper-config.yml");
      File configFile = new File(Objects.requireNonNull(resource).toURI());
      ScrapperConfig config = mapper.readValue(configFile, ScrapperConfig.class);
      config.setDbOutPath(String.format(config.getDbOutPath(), config.getDbName()));
      return config;
    }
    catch (Exception e) {
      LOG.error("error loading scrapper config", e);
    }
    return null;
  }
}
