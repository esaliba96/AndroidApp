package com.Poly.Kenner_Saliba.gradesniffer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MainMenu extends MainActivity {
  LinearLayout dynamicContent;
  Button usernameButton;
  private ProgressBar loadingClassDonut;
  static int quarterIndex;
  static boolean set = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    SharedPreferences values = getSharedPreferences(getString(R.string.myACCESS), Context.MODE_PRIVATE);
    SharedPreferences quarters = getSharedPreferences(getString(R.string.quarters), Context.MODE_PRIVATE);
    String username = values.getString(getString(R.string.uName), null);

    dynamicContent = (LinearLayout) findViewById(R.id.dynamicContent);
    View wizard = getLayoutInflater().inflate(R.layout.activity_main_menu, null);
    dynamicContent.addView(wizard);

    RadioButton rb = (RadioButton) findViewById(R.id.courses);
    rb.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.courses_option_selected, 0, 0);
    rb.setTextColor(getResources().getColor(R.color.colormustanggold));

    Spinner spinner = findViewById(R.id.quarterSpinner);
    loadingClassDonut = findViewById(R.id.loadingDonut);

    usernameButton = findViewById(R.id.toUserCredentialPage);
    if (username == null) {
      usernameButton.setText(R.string.noUser);
    } else {
      usernameButton.setText(username);
    }
    usernameButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(MainMenu.this, LoginSaveActivity.class));
      }
    });

    Set<String> quartersList = quarters.getStringSet(getString(R.string.quarters), null);
    ArrayList<String> spinnerItems = new ArrayList<>();
    if (quartersList != null) {
      spinnerItems = new ArrayList<>(quartersList);
    } else {
      spinnerItems.add("No Data Loaded Yet");
    }

    final SharedPreferences currentQuarter = getSharedPreferences(getString(R.string.currentQuarter), Context.MODE_PRIVATE);
    final String current = currentQuarter.getString(getString(R.string.currentQuarter), null);

    CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(this, R.layout.spinner_design, spinnerItems);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);

    if (spinnerItems.indexOf(current) != -1 && !set) {
      set = true;
      spinner.setSelection(spinnerItems.indexOf(current));
    } else {
      spinner.setSelection(quarterIndex);
    }
    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      SharedPreferences values = getSharedPreferences(getString(R.string.myACCESS), Context.MODE_PRIVATE);
      String username = values.getString(getString(R.string.uName), null);
      String password = values.getString(getString(R.string.uPass), null);
      User user = new User(username, password);
      String quarter;
      final ListView lv = findViewById(R.id.classListView);

      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedItem = parent.getItemAtPosition(position).toString();

        quarter = selectedItem.intern();
        quarterIndex = position;
        if (username == null && password == null) {
          Toast.makeText(getBaseContext(), "No user registered yet", Toast.LENGTH_LONG).show();
        } else {
          loadingClassDonut.setVisibility(View.VISIBLE);
          new PopulateCourses(user, lv, quarter).execute("");
        }
      }

      public void onNothingSelected(AdapterView<?> parent) {
        if (username == null && password == null) {
          Toast.makeText(getBaseContext(), "No user registered yet", Toast.LENGTH_LONG).show();
        } else {
          new PopulateCourses(user, lv, quarter).execute("");
        }
      }

    });
  }

  @SuppressLint("StaticFieldLeak")
  private class PopulateCourses extends AsyncTask<String, Void, String> {
    private User user;
    private LinkedList<Course> courses;
    private String quarter;
    private ListView lv; // I need this in here so I can declare the OnClick
    private ArrayList<String> courseNames;

    PopulateCourses(User user, ListView lv, String quarter) {
      this.user = user;
      this.lv = lv;
      this.quarter = quarter;
    }

    @Override
    protected String doInBackground(String... params) {
      FileOutputStream fileOutputStream;
      ObjectOutputStream objectOutputStream;
      FileInputStream fileInputStream;
      ObjectInputStream objectInputStream;
      String filePath = getFilesDir().getPath() + "/Courses" + quarter + ".txt";

      try {
        try {
          fileInputStream = new FileInputStream(filePath);
          objectInputStream = new ObjectInputStream(fileInputStream);
          courses = (LinkedList<Course>) objectInputStream.readObject();
        } catch (Exception e) {
          courses = new LinkedList<>();
          fileOutputStream = new FileOutputStream(filePath);
          objectOutputStream = new ObjectOutputStream(fileOutputStream);

          if (user.login(quarter)) {
            if (user.courses != null) {
              courses = user.courses;
              objectOutputStream.writeObject(courses);
              objectOutputStream.close();
              return "success";
            }
            return "failure";
          }
        }
        return "success";
      } catch (Exception e) {
        Log.e("Getting Grades", e.toString());
      }
      return "failure";
    }

    @Override
    protected void onPostExecute(String result) {
      loadingClassDonut.setVisibility(View.GONE);
      if (result.equals("failure")) {
        lv.setAdapter(null);
        Toast.makeText(getApplicationContext(), "No courses Available", Toast.LENGTH_SHORT).show();
      } else {
        courseNames = getCourseNames(courses);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(),
            R.layout.classes_list,
            courseNames);

        lv.setAdapter(arrayAdapter);

        lv.setOnItemClickListener(
            new AdapterView.OnItemClickListener() {
              @Override
              public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                cancel(true);
                String courseName = lv.getItemAtPosition(position).toString();
                Course course = Course.getCourse(courseName, courses);
                Intent intent = new Intent(MainMenu.this, GradeDisplay.class)
                    .putExtra(getString(R.string.selectedCourse), course);
                startActivity(intent);
              }
            });
      }
    }

    @Override
    protected void onPreExecute() {
      lv.setAdapter(null);
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }

    private ArrayList<String> getCourseNames(List<Course> courses) {
      ArrayList<String> courseNames = new ArrayList<>(); //

      for (Course c : courses) {
        if (!c.getTeaching()) {
          courseNames.add(c.getName());
        }
      }

      return courseNames;
    }
  }

}
