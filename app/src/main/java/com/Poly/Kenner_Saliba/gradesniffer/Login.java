package com.Poly.Kenner_Saliba.gradesniffer;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class Login {

  private List<String> cookies;
  private HttpsURLConnection conn;

  private final String USER_AGENT = "Mozilla/5.0";
  private static Login http = new Login();
  private static String newUrl;

    public static void userLogin(User user) {
      String url = "https://idp.calpoly.edu/idp/profile/cas/login?service=https%3A%2F%2Fpolylearn.calpoly.edu%2Flogin%2Findex.php";
      CookieHandler.setDefault(new CookieManager());

      try {
        String page = http.GetPageContent(url);
        String postParams = http.getFormParams(page, user.getName(), user.getPassword());
        http.sendPost(newUrl, postParams);
        http.GetPageContent("https://polylearn.calpoly.edu/login/index.php");
      } catch (Exception e) {
      }
    }

    private void sendPost(String url, String postParams) throws Exception {

      URL obj = new URL(url);
      conn = (HttpsURLConnection) obj.openConnection();

      // Acts like a browser
      conn.setUseCaches(false);
      conn.setInstanceFollowRedirects(false);
      conn.setRequestMethod("POST");
      conn.setRequestProperty("User-Agent", USER_AGENT);
      conn.setRequestProperty("Accept",
          "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
      conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
      conn.setRequestProperty("Connection", "keep-alive");
      conn.setRequestProperty("Host", "idp.calpoly.edu");
      conn.setRequestProperty("Origin", "https://idp.calpoly.edu");
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      conn.setRequestProperty("Content-Length", Integer.toString(postParams.length()));

      conn.setDoOutput(true);
      conn.setDoInput(true);

      // Send post request
      DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
      wr.writeBytes(postParams);
      wr.flush();
      wr.close();

      conn.getResponseCode();
   }

    private String GetPageContent(String url) throws Exception {
      URL obj = new URL(url);
      conn = (HttpsURLConnection) obj.openConnection();

      // default is GET
      conn.setRequestMethod("GET");

      conn.setUseCaches(false);

      // act like a browser
      conn.setRequestProperty("User-Agent", USER_AGENT);
      conn.setRequestProperty("Accept",
          "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
      conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
      conn.getResponseCode();
      conn.getResponseCode();
      BufferedReader in =
          new BufferedReader(new InputStreamReader(conn.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();

      return response.toString();
    }

    private String getFormParams(String html, String username, String password)
        throws UnsupportedEncodingException {
      Document doc = Jsoup.parse(html);

      Element loginform = doc.getElementById("preExpired");
      Elements inputElements = loginform.getElementsByTag("input");
      Elements url = loginform.getElementsByTag("form");
      newUrl = "https://idp.calpoly.edu" + url.attr("action");
      List<String> paramList = new ArrayList<String>();
      for (Element inputElement : inputElements) {
        String key = inputElement.attr("name");
        String value = inputElement.attr("value");

        if (key.equals("j_username")) {
          value = username;
        } else if (key.equals("j_password"))
          value = password;
        paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
      }

      StringBuilder result = new StringBuilder();
      for (String param : paramList) {
        if (result.length() == 0) {
          result.append(param);
        } else {
          result.append("&" + param);
        }
      }
      result.append("&_eventId_proceed=");
      return result.toString();
    }

   static Boolean successfulLogin() {
    Elements element = null;
    boolean logged;

    try {
      String html = http.GetPageContent("https://polylearn.calpoly.edu/");
      Document doc = Jsoup.parse(html);
      element = doc.getElementsByClass("logininfo");
    } catch (Exception e) {
      Log.e("Login", e.toString());
    }

    try {
      logged = element.select("a").get(1).text().equals("Log out");
    } catch (Exception e) {
      logged = false;
    }
    return logged;
  }

  static String coursePage(){
    try {
      return http.GetPageContent("https://polylearn.calpoly.edu/course/mycourses.php");
    } catch (Exception e) {
      Log.e("Login Courses", e.toString());
    }
    return null;
  }

   static String gradePage(String id, String year) {
    try {
       return http.GetPageContent("https://polylearn.calpoly.edu/" + year +"/grade/report/user/index.php?" + id);
    } catch (Exception e) {
      Log.e("Login Grades", e.toString());
    }
    return null;
  }

  static String gradeOverview(Course c) {
    try {
      return http.GetPageContent("https://polylearn.calpoly.edu/" + c.getYear() +
          "/grade/report/overview/index.php?" + c.getId()+ "&userid=" + Grade.userId);
    } catch (Exception e) {
      Log.e("Login Grades", e.toString());
    }
    return null;
  }

}