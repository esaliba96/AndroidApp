package com.Poly.Kenner_Saliba.gradesniffer;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.HashMap;

public class Grade implements Serializable {
  private String gradeIdentifier;
  private Double score;
  private Double range;
  private Boolean isAdded;
  private int weight;
  static public String userId;

  private Grade(Double score, Double range, String gradeIdentifier) {
    this.score = score;
    this.range = range;
    this.gradeIdentifier = gradeIdentifier;
    this.weight = 0;
    this.isAdded = false;
  }

  public Grade(Double score, Double range, String gradeIdentifier, int weight) {
    this.score = score;
    this.range = range;
    this.gradeIdentifier = gradeIdentifier;
    this.weight = weight;
    this.isAdded = true;
  }

  public static boolean getGrades(String html, HashMap<String, Grade> grades) {
    Boolean added = false;

    Document doc = Jsoup.parse(html);
    Element table = doc.select("table").get(0);
    Elements rows = table.select("tr");
    for (Element row : rows) {
      added = parseGrade(row, grades);
    }
    return added;
  }

  String getGradePercentage() {
    String percentage;
    Double percent;

    if (score == 0. || range == 0.) {
      percentage = "N/A";
    } else {
      percent = this.score / this.range * 100;
      percentage = String.valueOf(percent.intValue());
    }

    return percentage;
  }

  public String getGradeIdentifier() {
    return gradeIdentifier;
  }

  private static boolean parseGrade(Element row, HashMap<String, Grade> grades) {
    String name, gradeValue;
    Double score, range;
    Boolean added = false;

    name = row.select("span").attr("title");
    try {
      userId = row.select("th").get(0).id().split("_")[2];
    } catch (Exception ignored) {
    }
    if (name.length() == 0) {
      name = row.select("a").text();
    }
    if (name.length() > 0) {
      gradeValue = row.select("td").get(0).text();
      if (!gradeValue.equals("-")) {
        try {
          score = Double.parseDouble(row.select("td").get(0).text().split(" ")[0]);
        } catch (NumberFormatException e) {
          score = 0.;
        }
        range = Double.parseDouble(row.select("td").get(1).text().split("–")[1]);

        if (score != 0) {
          Grade grade = new Grade(score, range, name);
          if (!grades.containsKey(name)) {
            added = true;
            grades.put(name, grade);
          }
        }
      }
    }
   return added;
  }

  boolean setWeight(String weight,HashMap<String, Grade> grades) {
    int userInput;

    if (StringUtil.isNumeric(weight)) {
      userInput = Integer.valueOf(weight);
      if (userInput > 0 && userInput < 100 ) {
        if(checkGradeWeights(userInput,grades)) {
          this.weight = userInput;

          return true;
        }
      }
      return false;
    }
    return false;
  }

  private boolean checkGradeWeights(int weight,HashMap<String, Grade> grades){
    int totalSum=0;
    for (HashMap.Entry<String, Grade> grade : grades.entrySet()) {
      totalSum= totalSum + grade.getValue().getWeight();
    }
    totalSum += weight;
    if(totalSum > 100){
      return false;
    }
    return true;
  }

  void setAddedFlag() {
    this.isAdded = true;
  }

  boolean getAddedFlag() {
    return this.isAdded;
  }

  int getWeight() {
    return this.weight;
  }
}
