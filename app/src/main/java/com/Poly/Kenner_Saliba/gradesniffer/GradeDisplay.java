package com.Poly.Kenner_Saliba.gradesniffer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.baoyz.swipemenulistview.SwipeMenuListView;

import org.jsoup.helper.StringUtil;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class GradeDisplay extends MainActivity {
  Button addGrade;
  SharedPreferences sharedPreferences;
  private ProgressBar loadingClassDonut;
  LinearLayout dynamicContent;
  HashMap<String, Grade> grades;
  FileOutputStream fileOutputStream;
  ObjectOutputStream objectOutputStream;
  FileInputStream fileInputStream;
  ObjectInputStream objectInputStream;
  String filePath;
  SwipeMenuListView gradesList;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    dynamicContent = (LinearLayout) findViewById(R.id.dynamicContent);
    View wizard = getLayoutInflater().inflate(R.layout.activity_grade_display, null);
    dynamicContent.addView(wizard);
    addGrade = findViewById(R.id.addGrade);
    Course course = (Course) getIntent().getSerializableExtra(getString(R.string.selectedCourse));
    loadingClassDonut = findViewById(R.id.loadingDonut);
    gradesList = (SwipeMenuListView)findViewById(R.id.gradeListView);
    sharedPreferences = getSharedPreferences(getString(R.string.myACCESS), Context.MODE_PRIVATE);
    String username = sharedPreferences.getString(getString(R.string.uName), null);
    String password = sharedPreferences.getString(getString(R.string.uPass), null);
    User user = new User(username, password);
    loadingClassDonut.setVisibility(View.VISIBLE);
    new PopulateGrades(user, course, gradesList).execute("");

    addGrade.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        popupAddGrade(gradesList);
      }
    });
  }

  private void popupAddGrade(final ListView gradesList) {
    LayoutInflater li = LayoutInflater.from(GradeDisplay.this);
    View promptsView = li.inflate(R.layout.popup_for_add_grade, null);

    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GradeDisplay.this);
    alertDialogBuilder.setView(promptsView);
    final EditText gradeIdentifier = promptsView.findViewById(R.id.gradeIdentifier);
    final EditText gradeScore = promptsView.findViewById(R.id.gradeScore);
    final EditText gradeRange = promptsView.findViewById(R.id.gradeRange);
    final EditText gradeWeight = promptsView.findViewById(R.id.gradeWght);

    alertDialogBuilder
        .setCancelable(false)
        .setPositiveButton("Save",
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                Double gradeScoreInput, gradeRangeInput;
                int gradeWeightInput;
                String gradeCheck = checkGrade(gradeScore.getText().toString(), gradeRange.
                    getText().toString(), gradeWeight.getText().toString());
                if (gradeCheck.equals("success")) {
                  gradeRangeInput = Double.valueOf(gradeRange.getText().toString());
                  gradeScoreInput = Double.valueOf(gradeScore.getText().toString());
                  gradeWeightInput = Integer.valueOf(gradeWeight.getText().toString());
                  Grade gradeToAdd = new Grade(gradeScoreInput, gradeRangeInput, gradeIdentifier.getText().toString(), gradeWeightInput);
                  if (!grades.containsKey(gradeIdentifier.getText().toString()) && checkGradeWeights(gradeWeightInput,grades)) {
                    grades.put(gradeIdentifier.getText().toString(), gradeToAdd);
                    writeToFile();
                    GradesAdapter arrayAdapter = new GradesAdapter(getApplicationContext(), getGrades());
                    gradesList.setAdapter(arrayAdapter);
                    Toast.makeText(getApplicationContext(), "Grade successfully added",
                        Toast.LENGTH_SHORT).show();
                  } else {
                    Toast.makeText(getApplicationContext(), "Grade already exists or invalid weight provided",
                        Toast.LENGTH_SHORT).show();
                  }
                } else {
                  Toast.makeText(getApplicationContext(), gradeCheck, Toast.LENGTH_SHORT).show();
                }
              }
            })
        .setNegativeButton("Cancel",
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
              }
            });

    final AlertDialog alertDialog = alertDialogBuilder.create();
    alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
      @Override
      public void onShow(DialogInterface arg0) {
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colormustanggold));
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(20);
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(20);
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colormustanggold));
      }
    });
    alertDialog.show();
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

  @SuppressLint("StaticFieldLeak")
  private class PopulateGrades extends AsyncTask<String, Void, String> {
    private User user;
    private Course course;
    private ListView gradesList;
    TextView total;

    PopulateGrades(User user, Course course, SwipeMenuListView gradesList) {
      this.user = user;
      this.course = course;
      this.gradesList = gradesList;
      filePath = getFilesDir().getPath() + "/grades" + course.getName() + ".txt";
    }

    @Override
    protected String doInBackground(String... params) {
      try {
        try {
          fileInputStream = new FileInputStream(filePath);
          objectInputStream = new ObjectInputStream(fileInputStream);
          grades = (HashMap) objectInputStream.readObject();
          objectInputStream.close();
          //user.updateGrades(course, grades);
          //writeToFile();
        } catch (Exception e) {
          grades = new HashMap<>();
          user.updateGrades(course, grades);
          writeToFile();
        }
        return "success";
      } catch (Exception e) {
        Log.e("Getting Grades", e.toString());
      }
      return "failure";
    }

    @Override
    protected void onPostExecute(String result) {
      total = findViewById(R.id.weightedTotalValue);
      loadingClassDonut.setVisibility(View.GONE);
      if (result.equals("failure")) {
        Toast.makeText(getApplicationContext(), "No grades available", Toast.LENGTH_SHORT).show();
      } else {
        GradesAdapter arrayAdapter = new GradesAdapter(getApplicationContext(), getGrades());
        gradesList.setAdapter(arrayAdapter);
        total.setText(String.valueOf(calculateTotalGrade()));

        gradesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            final Grade grade = (Grade) adapterView.getItemAtPosition(i);
            final Grade gradeToBeModified = grades.get(grade.getGradeIdentifier());
            LayoutInflater li = LayoutInflater.from(GradeDisplay.this);
            View promptsView = li.inflate(R.layout.popup_for_grade_weight, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GradeDisplay.this);
            alertDialogBuilder.setView(promptsView);
            final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

            alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Save",
                    new DialogInterface.OnClickListener() {
                      public void onClick(DialogInterface dialog, int id) {
                        if (gradeToBeModified.setWeight(userInput.getText().toString(),grades)) {
                            writeToFile();
                            Toast.makeText(getApplicationContext(), "Weight successfully added",
                                Toast.LENGTH_SHORT).show();
                            total.setText(String.valueOf(calculateTotalGrade()));
                        } else {
                          Toast.makeText(getApplicationContext(), "Invalid Weight, Weight must be a number between 0 and 100 and total weight must be 100",
                              Toast.LENGTH_SHORT).show();
                        }
                      }
                    })
                .setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                      public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                      }
                    });

            final AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
              @Override
              public void onShow(DialogInterface arg0) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colormustanggold));
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(20);
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(20);
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colormustanggold));
              }
            });
            alertDialog.show();
          }
        });
      }

    }

    @Override
    protected void onPreExecute() {
      gradesList.setAdapter(null);
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }
  }

  private ArrayList<Grade> getGrades() {
    ArrayList<Grade> gradesToDisplay = new ArrayList<>();

    for (HashMap.Entry<String, Grade> grade : grades.entrySet()) {
      gradesToDisplay.add(grade.getValue());
    }

    return gradesToDisplay;
  }

  private int calculateTotalGrade() {
    ArrayList<Grade> grades= getGrades();
    int gradeTotal = 0;

    for (Grade g : grades) {
      if (g.getGradePercentage() != null && g.getWeight() != 0) {
        gradeTotal += (Integer.valueOf(g.getGradePercentage()) * g.getWeight())/100.0;
      }
    }
    return gradeTotal;
  }

  private void writeToFile() {
    try {
      fileOutputStream = new FileOutputStream(filePath);
      objectOutputStream = new ObjectOutputStream(fileOutputStream);

      objectOutputStream.writeObject(grades);
      objectOutputStream.close();
    } catch (Exception e) {
      Log.e("Grades", e.toString());
    }
  }


  class GradesAdapter extends BaseAdapter {

    ArrayList<Grade> data;
    Context context;
    private LayoutInflater inflater = null;

    public GradesAdapter(Context context, ArrayList<Grade> data) {
      // TODO Auto-generated constructor stub
      this.data = data;
      this.context = context;
      inflater = (LayoutInflater) context
          .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
      // TODO Auto-generated method stub
      return data.size();
    }

    @Override
    public Object getItem(int position) {
      // TODO Auto-generated method stub
      return data.get(position);
    }

    @Override
    public long getItemId(int position) {
      // TODO Auto-generated method stub
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      // TODO Auto-generated method stub
      View vi = convertView;
      if (vi == null) {
        vi = inflater.inflate(R.layout.grade_row, null);
      }
      Grade grade;
      TextView identifier = vi.findViewById(R.id.identifer_cell_text);
      TextView weight = vi.findViewById(R.id.weight_cell_text);
      TextView percentage = vi.findViewById(R.id.percentage_cell_text);

      grade = data.get(position);
      if (grade.getAddedFlag()) {
        identifier.setTextColor(getResources().getColor(R.color.white));
        percentage.setTextColor(getResources().getColor(R.color.white));
        weight.setTextColor(getResources().getColor(R.color.white));
      } else {
        identifier.setTextColor(getResources().getColor(R.color.colormustanggold));
        percentage.setTextColor(getResources().getColor(R.color.colormustanggold));
        weight.setTextColor(getResources().getColor(R.color.colormustanggold));
      }
      identifier.setText(grade.getGradeIdentifier());
      String gradeToDisplay = grade.getGradePercentage();
      percentage.setText(gradeToDisplay);
      if (grade.getWeight() != 0) {
        weight.setText(String.valueOf(grade.getWeight()));
      } else {
        weight.setText("N/A");
      }
      return vi;
    }
  }

  String checkGrade(String inScore, String inRange, String inWeight) {
    double score, range;

    if (!StringUtil.isNumeric(inScore) || !StringUtil.isNumeric(inRange) || !StringUtil.isNumeric(inWeight)) {
      return "Score, Range, and Weight must be numbers between 0 and 100";
    }
    score = Double.valueOf(inScore);
    range = Double.valueOf(inRange);

    if (score > range) {
      return "Score has to be smaller than range";
    }
    return "success";
  }
}



