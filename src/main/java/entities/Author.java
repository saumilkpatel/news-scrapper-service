package entities;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public class Author {

  @JsonProperty("authorId")
  private int id;

  @JsonProperty("authorName")
  private String name;

  public Author(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public long getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
