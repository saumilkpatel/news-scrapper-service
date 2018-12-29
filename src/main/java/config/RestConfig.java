package config;

import io.dropwizard.Configuration;

@SuppressWarnings("unused")
public class RestConfig extends Configuration {

  private boolean scrappingEnabledOnStartup;

  public boolean isScrappingEnabledOnStartup() {
    return scrappingEnabledOnStartup;
  }

  public void setScrappingEnabledOnStartup(boolean scrappingEnabledOnStartup) {
    this.scrappingEnabledOnStartup = scrappingEnabledOnStartup;
  }
}
