package application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import config.ScrapperConfig;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import service.ScrapperService;

public class ScrapperApplication {

  public static void main(String[] args) throws IOException, URISyntaxException {

    ScrapperConfig config = loadConfig();
    config.setDbOutPath(String.format(config.getDbOutPath(), config.getDbName()));
    ScrapperService scrapperService = new ScrapperService(config);

    scrapperService.run();

  }

  private static ScrapperConfig loadConfig() throws URISyntaxException, IOException {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    URL resource = ScrapperApplication.class.getClassLoader().getResource("scrapper-config.yml");
    File configFile = new File(Objects.requireNonNull(resource).toURI());
    return mapper.readValue(configFile, ScrapperConfig.class);
  }


}
