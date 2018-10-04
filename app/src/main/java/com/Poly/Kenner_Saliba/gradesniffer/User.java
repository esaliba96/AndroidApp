package com.Poly.Kenner_Saliba.gradesniffer;

import android.content.Context;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class User {
  private String name;
  private String password;
  LinkedList<Course> courses;
  ArrayList<String> quarters;
  private String currentQuarter;

  User(String name, String password) {
    this.name = name;
    this.password = password;
    this.quarters = new ArrayList<>();
    this.currentQuarter = "";
  }

  boolean login() {
    Login.userLogin(this);
    if (Login.successfulLogin()) {
      initQuarters();
      initCourses(this.currentQuarter);
      return true;
    }
    return false;
  }

  boolean login(String quarter) {
    Login.userLogin(this);

    if (Login.successfulLogin()) {
      this.initCourses(quarter);
      return true;
    }
    return false;
  }

  boolean updateGrades(Course course, HashMap<String, Grade> grades) {
    Login.userLogin(this);

    if (Login.successfulLogin()) {
      return getGrades(course, grades);
    }
    return false;
  }

  private void initCourses(String quarter) {
    courses = Course.createCourseList(Login.coursePage(), quarter);
  }

  private void initQuarters() {
    String html = Login.coursePage();
    String quarter;
    Document doc = Jsoup.parse(html);
    int i = 0;
    try {
      Elements ul = doc.select("div.content > ul");
      Elements li = ul.select("li");
      for (Element element : li) {
          quarter = element.select("a").attr("href");
        if(quarter.charAt(0) == '#') {
          if (i == 0) {
            this.currentQuarter = quarter.substring(1);
            i++;
          }
          quarters.add(quarter.substring(1));
        }
      }
    } catch (NullPointerException e) {
      Log.e("User: initQuarters", e.toString());
    }

  }

  boolean getGrades(Course course, HashMap<String, Grade> grades) {
    return Grade.getGrades(Login.gradePage(course.getId(), course.getYear()), grades);
  }

  String getName() {
    return this.name;
  }

  String getPassword() {
    return this.password;
  }

  String getCurrentQuarter() {
    return this.currentQuarter;
  }
 }
