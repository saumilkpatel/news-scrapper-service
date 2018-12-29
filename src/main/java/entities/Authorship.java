package entities;

public class Authorship {

  private long authorId;
  private long articleId;

  public Authorship(long authorId, long articleId) {
    this.authorId = authorId;
    this.articleId = articleId;
  }

  public long getAuthorId() {
    return authorId;
  }

  public void setAuthorId(long authorId) {
    this.authorId = authorId;
  }

  public long getArticleId() {
    return articleId;
  }

  public void setArticleId(long articleId) {
    this.articleId = articleId;
  }
}
