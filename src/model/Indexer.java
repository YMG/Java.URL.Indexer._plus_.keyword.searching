package model;

import java.sql.SQLException;
import java.util.List;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import de.l3s.boilerpipe.util.UnicodeTokenizer;

/**
 * Database utility class, saves and searches the database data
 * 
 */
public class Indexer {

  private ConnectionSource cSource;
  private BasicDataSource ds;

  /**
   * Handles initiating a connection to the database with the database address only
   * assuming no user is required to connect to the database
   * 
   * @param address
   */
  public void ConnectionPoolInit(String address) {

    BasicDataSource ds;

    try {

      ds = new BasicDataSource();

      ds.setDriverClassName("com.mysql.jdbc.Driver");
      ds.setUrl(address);
      ds.setUsername("root");
      ds.setMaxActive(20);
      ds.setMaxIdle(5);
      ds.setInitialSize(5);
      ds.setValidationQuery("SELECT 1");

      this.ds = ds;
      cSource = new DataSourceConnectionSource(this.ds, this.ds.getUrl());

    } catch (Exception ignored) {
      ignored.printStackTrace();
    }
  }

  /**
   * Handles initiating a connection to the database with the given paramteres
   * 
   * @param address
   * @param user
   * @param password
   */
  public void ConnectionPoolInit(String address, String user, String pwd) {

    BasicDataSource ds;

    try {

      ds = new BasicDataSource();

      ds.setDriverClassName("com.mysql.jdbc.Driver");
      ds.setUsername(user);
      ds.setPassword(pwd);
      ds.setUrl(address);
      ds.setMaxActive(20);
      ds.setMaxIdle(5);
      ds.setInitialSize(5);
      ds.setValidationQuery("SELECT 1");

      this.ds = ds;

    } catch (Exception ignored) {}
  }

  /**
   * Handles dumping fetched pages into database in a concurrent way
   * 
   * @param Webpage
   */
  public void DBSaveResults(List<Webpage> wp) {
    synchronized (wp) {
      try {
        Dao<Webpage, Integer> webpageDAO = DaoManager.createDao(cSource, Webpage.class);
        for (Webpage p : wp) {
          webpageDAO.create(p);
        }
      } catch (Exception ignored) {}
    }
  }

  /**
   * Handles searching in the database given a keyword(s) and returns results depending on matched
   * token
   * 
   * @param keyword
   * @return Webpage
   * @throws SQLException
   */
  public Webpage FindMatch(String keyword) throws SQLException {
    Dao<Webpage, Integer> webpageDAO = DaoManager.createDao(cSource, Webpage.class);
    Dao<Keywords, String> keywordDAO = DaoManager.createDao(cSource, Keywords.class);


    boolean foundInKeywords = false;

    Keywords keywords_result =
        keywordDAO.queryBuilder().where().eq("keyword", keyword).queryForFirst();

    if (keywords_result != null) {
      foundInKeywords = true;
      Webpage wpReference =
          webpageDAO.queryBuilder().where().eq("index", keywords_result.getIndex()).queryForFirst();
      Webpage storedResult = new Webpage();
      String[] tokenizedText = UnicodeTokenizer.tokenize(wpReference.getText());

      for (int i = 0; i < tokenizedText.length; ++i) {

        if (tokenizedText[i].toLowerCase().equals(keyword)) {


          String firstToken =
              (i - 2) >= 0 && i - 2 < tokenizedText.length ? tokenizedText[i - 2] : "";
          String secondToken =
              (i - 1) >= 0 && i - 1 < tokenizedText.length ? tokenizedText[i - 1] : "";
          String thirdToken =
              (i + 1) >= 0 && i + 1 < tokenizedText.length ? tokenizedText[i + 1] : "";
          String fourthToken =
              (i + 2) >= 0 && i + 2 < tokenizedText.length ? tokenizedText[i + 2] : "";

          storedResult.setText(String.format("\"%s %s\" \"%s\" \"%s %s\"", firstToken, secondToken,
              tokenizedText[i], thirdToken, fourthToken));
          storedResult.setUrl(wpReference.getUrl());
          storedResult.setTitle(wpReference.getTitle());


          return storedResult;
        }
      }

    }

    if (!foundInKeywords) {
      List<Webpage> current = webpageDAO.queryForAll();
      Webpage filtered = new Webpage();

      for (Webpage p : current) {

        String[] tokens = UnicodeTokenizer.tokenize(p.getText());

        for (int i = 0; i < tokens.length; ++i) {

          if (tokens[i].toLowerCase().equals(keyword)) {


            String firstToken = (i - 2) >= 0 && i - 2 < tokens.length ? tokens[i - 2] : "";
            String secondToken = (i - 1) >= 0 && i - 1 < tokens.length ? tokens[i - 1] : "";
            String thirdToken = (i + 1) >= 0 && i + 1 < tokens.length ? tokens[i + 1] : "";
            String fourthToken = (i + 2) >= 0 && i + 2 < tokens.length ? tokens[i + 2] : "";

            filtered.setText(String.format("\"%s %s\" \"%s\" \"%s %s\"", firstToken, secondToken,
                tokens[i], thirdToken, fourthToken));
            filtered.setUrl(p.getUrl());
            filtered.setTitle(p.getTitle());

            Keywords k = new Keywords();
            k.setKeyword(tokens[i]);
            k.setIndex(p.getIndex());
            keywordDAO.create(k);

            return filtered;
          }

        }

      }
    }
    return null;

  }

}
