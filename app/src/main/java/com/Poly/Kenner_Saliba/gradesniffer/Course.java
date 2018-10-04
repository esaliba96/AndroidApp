package com.Poly.Kenner_Saliba.gradesniffer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.regex.Pattern;

public class Course implements Serializable {
  private String name;
  private String id;
  private String year;
  private Boolean teaching;

  private Course(String name, String id, String year) {
    this.name = name;
    this.id = id;
    this.year = year;
    this.teaching = false;
  }

   static LinkedList<Course> createCourseList(String html, String quarter) {
    String name, id, url, year;
    LinkedList<Course> courses = new LinkedList<>();
    Document doc = Jsoup.parse(html);
    Elements elements;

    try {
      elements = doc.getElementById(quarter).getElementsByClass("name");
    } catch (NullPointerException e) {
      return null;
    }

    for (Element element : elements) {
      name = element.text().split("-0")[0];
      url = element.select("a").attr("href");
      id = url.split("\\?")[1];
      year = url.split(Pattern.quote("https://polylearn.calpoly.edu/"))[1];
      year = year.split(Pattern.quote("/course"))[0];
      Course course = new Course(name, id, year);
      courses.add(course);
    }
    return courses;
  }

  static Course getCourse(String courseName, LinkedList<Course> courses) {
    for (Course c : courses) {
      if (c.name.equals(courseName)) {
        return c;
      }
    }
    return null;
  }

  static LinkedList<String> courseTeaching(String html) {
    LinkedList<String> courses = new LinkedList<>();
    Document doc = Jsoup.parse(html);

    try {
      Element table = doc.select("table").get(1); //select the first table.
      Elements rows = table.select("tr");

      for (int i = 1; i < rows.size(); i++) { //first row is the col names so skip it.
        Element row = rows.get(i);
        Elements cols = row.select("td");
        courses.add(cols.text().split("-0")[0]);
      }
    } catch (Exception ignored) {
    }
    return courses;
  }
  String getName() {
    return this.name;
  }

  String getId() {
    return this.id;
  }

  String getYear() {
    return this.year;
  }

  Boolean getTeaching() {return  this.teaching;}

  void setTeaching() {
    this.teaching = true;
  }
}
