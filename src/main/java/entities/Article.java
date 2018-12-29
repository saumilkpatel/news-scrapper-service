package entities;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public class Article {

  @JsonProperty("articleId")
  private int id;

  @JsonProperty("articleTitle")
  private String title;

  @JsonProperty("articleDescription")
  private String description;

  public Article(int id, String title, String description) {
    this.id = id;
    this.title = title;
    this.description = description;
  }

  public long getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
