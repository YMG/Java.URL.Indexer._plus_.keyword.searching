package model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Class representing table object at database
 * 
 * 
 */
@DatabaseTable(tableName = "webpage")
public class Webpage {

  @DatabaseField(columnName = "author")
  private String author;
  @DatabaseField(id = true, columnName = "index")
  private int index;

  @DatabaseField(columnName = "text")
  private String text;

  @DatabaseField(columnName = "title")
  private String title;

  @DatabaseField(columnName = "url")
  private String url;
  public String getAuthor() {
    return author;
  }
  public int getIndex() {
    return index;
  }

  public String getText() {
    return text;
  }

  public String getTitle() {
    return title;
  }

  public String getUrl() {
    return url;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public void setText(String text) {
    this.text = text;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
