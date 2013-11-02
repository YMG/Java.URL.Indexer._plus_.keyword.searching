package model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Class representing table object at the database
 * 
 * 
 */
@DatabaseTable(tableName = "keywords")
public class Keywords {

  @DatabaseField(columnName = "index")
  private int index;

  @DatabaseField(columnName = "keyword")
  private String keyword;


  public int getIndex() {
    return index;
  }

  public String getKeyword() {
    return keyword;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public void setKeyword(String keyword) {
    this.keyword = keyword;
  }

}
