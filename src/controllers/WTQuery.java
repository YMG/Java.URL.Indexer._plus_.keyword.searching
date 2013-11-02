package controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.Indexer;
import model.Webpage;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import de.l3s.boilerpipe.extractors.DefaultExtractor;

/**
 * Default and single handler for both db requests and queries
 * 
 */
public class WTQuery extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private final Indexer dbIndex = new Indexer();
  private final ExecutorService pool = Executors.newFixedThreadPool(20);
  private Map<String, Future<List<Webpage>>> webPageTaskList =
      new HashMap<String, Future<List<Webpage>>>();


  /**
   * Handles crawling pages and constructing required details and returns them as Future object
   * (deferred result)
   * 
   * @param url
   * @return Constructed List of Webpages
   * @throws IOException
   * @throws InterruptedException
   */
  private Future<List<Webpage>> CrawlPages(final String url) throws IOException,
      InterruptedException {
    return pool.submit(new Callable<List<Webpage>>() {
      @Override
      public List<Webpage> call() throws Exception {

        List<Webpage> constructedPages = new ArrayList<Webpage>();
        List<String> listOfUrls = new ArrayList<String>();

        // adding url from the request first
        listOfUrls.add(url);

        // constructing based uri for matching
        URL u = new URL(url);
        String path = u.getFile().substring(0, u.getFile().lastIndexOf('/'));
        String baseURL = u.getProtocol() + "://" + u.getHost() + path;

        Document doc = Jsoup.connect(url).get();
        Elements link = doc.select("a");


        for (Element s : link) {
          if (s.absUrl("href").contains(baseURL)) {
            listOfUrls.add(s.attr("abs:href"));
          }
        }

        for (String s : listOfUrls) {
          Webpage wp = new Webpage();

          String extracted = DefaultExtractor.INSTANCE.getText(new URL(s));

          wp.setAuthor("");
          wp.setText(extracted);
          wp.setTitle(Jsoup.connect(s).get().title());
          wp.setUrl(s);

          constructedPages.add(wp);
        }

        dbIndex.DBSaveResults(constructedPages);

        return constructedPages;
      }
    });
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    resp.setCharacterEncoding("UTF-8");
    int taskCounter = 0;
    Gson gson = new Gson();
    PrintWriter out = resp.getWriter();
    HashMap<String, String> keyword_task_results = new HashMap<String, String>();

    if (req.getParameter("keywords") != null) {
      String keyword = req.getParameter("keywords").toLowerCase();

      try {
        Webpage match = dbIndex.FindMatch(keyword);
        keyword_task_results.put("match", gson.toJson(match));
      } catch (Exception e) {
        keyword_task_results.put("failed", "Database wasnt configured correctly");
      }
    }

    if (req.getParameter("jobs") != null) {
      JsonElement jelement = new JsonParser().parse(req.getParameter("jobs"));
      JsonArray jarray = jelement.getAsJsonArray();

      for (java.util.Map.Entry<String, Future<List<Webpage>>> nightmare : webPageTaskList
          .entrySet()) {
        for (JsonElement id : jarray) {
          if (nightmare.getKey().equals(id.toString().replaceAll("\"", ""))
              && !nightmare.getValue().isDone()) {
            taskCounter++;
          }
        }
      }

      keyword_task_results.put("countedTasks", Integer.toString(taskCounter));
    }

    out.write(gson.toJson(keyword_task_results));

  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    resp.setCharacterEncoding("UTF-8");
    PrintWriter out = resp.getWriter();

    Gson jsonSerializer = new Gson();
    HashMap<String, String> message = new HashMap<String, String>();

    String user = req.getParameter("dbuser");
    String pass = req.getParameter("dbpassword");
    String dburl = req.getParameter("dbaddress");
    String crawlURI = req.getParameter("url");

    String jobID = UUID.randomUUID().toString();

    if (user == null || pass == null) {
      dbIndex.ConnectionPoolInit(dburl);
    } else {
      dbIndex.ConnectionPoolInit(dburl, user, pass);
    }

    try {
      webPageTaskList.put(jobID, this.CrawlPages(crawlURI));
    } catch (InterruptedException e) {
      message.put("error", "failed to process your request");
    }

    message.put("success", jobID);


    out.write(jsonSerializer.toJson(message));

  }
}
