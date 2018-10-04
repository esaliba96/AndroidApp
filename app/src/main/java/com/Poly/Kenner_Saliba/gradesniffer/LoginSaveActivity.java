package com.Poly.Kenner_Saliba.gradesniffer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class LoginSaveActivity extends MainActivity {
  Button updateUserCredentialsButton;
  EditText username, password;
  LinearLayout dynamicContent;
  SharedPreferences loginInfo, courses, quarters, currentQuarter;
  HashMap<String, Grade> grades;
  FileOutputStream fileOutputStream;
  ObjectOutputStream objectOutputStream;
  User user;
  LinkedList<String> teaching = new LinkedList<>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    dynamicContent = (LinearLayout) findViewById(R.id.dynamicContent);
    View wizard = getLayoutInflater().inflate(R.layout.login_save, null);
    dynamicContent.addView(wizard);

    RadioButton rb = (RadioButton) findViewById(R.id.user_credentials);
    rb.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.user_selected, 0, 0);
    rb.setTextColor(getResources().getColor(R.color.colormustanggold));

    loginInfo = getSharedPreferences(getString(R.string.myACCESS), Context.MODE_PRIVATE);

    updateUserCredentialsButton = (Button) findViewById(R.id.save_button);
    username = (EditText) findViewById(R.id.username);
    password = (EditText) findViewById(R.id.password);

    username.setText(loginInfo.getString(getString(R.string.uName), null));
    password.setText(loginInfo.getString(getString(R.string.uPass), null));

    updateUserCredentialsButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        courses = getSharedPreferences(getString(R.string.courses), MODE_PRIVATE);
        SharedPreferences.Editor coursesEditor = courses.edit();
        coursesEditor.clear();
        coursesEditor.apply();
        String u = username.getText().toString();
        String p = password.getText().toString();
        user = new User(u, p);
        SharedPreferences.Editor editor = loginInfo.edit();
        editor.putString(getString(R.string.uName), u);
        editor.putString(getString(R.string.uPass), p);
        editor.apply();
        if (loginInfo.getString(getString(R.string.uName), null) != null &&
            u.equals(loginInfo.getString(getString(R.string.uName), null))) {
          deleteFiles();
        }
        Toast.makeText(LoginSaveActivity.this, "Credentials Updated!", Toast.LENGTH_LONG).show();
        new LoginOperation(user).execute("");
      }
    });
  }

  @SuppressLint("StaticFieldLeak")
  public class LoginOperation extends AsyncTask<String, Void, String> {
    private User user;

    LoginOperation(User user) {
      this.user = user;
    }

    @Override
    protected String doInBackground(String... params) {
      boolean logged = false;
      try {
        logged = user.login();
      } catch (Exception e) {
        Log.d("elie", e.toString());
      }
      return String.valueOf(logged);
    }

    @Override
    protected void onPostExecute(String result) {
      if (result.equals("true")) {
        quarters = getSharedPreferences(getString(R.string.quarters), Context.MODE_PRIVATE);
        currentQuarter = getSharedPreferences(getString(R.string.currentQuarter), Context.MODE_PRIVATE);
        loadCurrentQuarterClasses(user.getCurrentQuarter());
        SharedPreferences.Editor editor = quarters.edit();
        Set<String> quartersList = new HashSet<>();
        quartersList.addAll(user.quarters);
        editor.putStringSet(getString(R.string.quarters), quartersList);
        editor.apply();
        editor = currentQuarter.edit();
        editor.putString(getString(R.string.currentQuarter), user.getCurrentQuarter());
        editor.apply();
        Toast.makeText(getApplicationContext(), "Successful Login!", Toast.LENGTH_LONG).show();
      } else {
        Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_LONG).show();
      }
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }

    private void loadCurrentQuarterClasses(String currentQuarter) {
      String filePath = getFilesDir().getPath() + "/Courses" + currentQuarter + ".txt";
      LinkedList<Course> courses;

      try {
        fileOutputStream = new FileOutputStream(filePath);
        objectOutputStream = new ObjectOutputStream(fileOutputStream);
          if (user.courses != null) {
            courses = user.courses;
            new GradeInit(user, courses).execute("");
            objectOutputStream.writeObject(courses);
            objectOutputStream.close();
          }
      } catch (Exception e) {
        Log.e("Loading Courses in Initial Login", e.toString());
      }
    }
  }

  @SuppressLint("StaticFieldLeak")
  class GradeInit extends AsyncTask<String, Void, String> {
    User user;
    LinkedList<Course> courses;

    GradeInit(User user, LinkedList<Course> courses) {
      this.user = user;
      this.courses = courses;
    }

    @Override
    protected String doInBackground(String... params) {
      initGrades();

      return "success";
    }

    @Override
    protected void onPostExecute(String result) {
      flagTeaching();
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }

    private void initGrades() {
      String filePath;
      for (Course c : courses) {
        filePath = getFilesDir().getPath() + "/grades" + c.getName() + ".txt";
        try {
          grades = new HashMap<>();
          user.updateGrades(c, grades);
          writeToFile(filePath);
        } catch (Exception e) {
          Log.e("User init grades", e.toString());
        }
        if (teaching!= null && teaching.size() == 0) {
          teaching = Course.courseTeaching(Login.gradeOverview(c));
        }
      }
    }

    void flagTeaching() {
      String filePath = getFilesDir().getPath() + "/Courses" + user.getCurrentQuarter() + ".txt";

      for (Course c : courses) {
        if (teaching != null) {
          if (teaching.contains(c.getName())) {
            c.setTeaching();
          }
        }
      }
      try {
        fileOutputStream = new FileOutputStream(filePath);
        objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(courses);
        objectOutputStream.close();
      } catch (Exception ignored) {
      }
    }
  }

  void deleteFiles() {
    File file = getFilesDir();
    List<File> files = Arrays.asList(file.listFiles());
    for (File f : files) {
      f.delete();
    }

    quarters = getSharedPreferences(getString(R.string.quarters), Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = quarters.edit();
    editor.clear();
    editor.apply();
  }

  private void writeToFile(String filePath) {
    try {
      fileOutputStream = new FileOutputStream(filePath);
      objectOutputStream = new ObjectOutputStream(fileOutputStream);

      objectOutputStream.writeObject(grades);
      objectOutputStream.close();
    } catch (Exception e) {
      Log.e("Grades", e.toString());
    }
  }
}
