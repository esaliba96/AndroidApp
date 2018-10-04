package com.Poly.Kenner_Saliba.gradesniffer;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Collin Kenner and Elie Saliba on 3/26/2018.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class GradeScheduler extends JobService {

  MainActivity gradeMainActivity;
  HashMap<String, Grade> grades;
  FileOutputStream fileOutputStream;
  ObjectOutputStream objectOutputStream;
  FileInputStream fileInputStream;
  ObjectInputStream objectInputStream;
  String coursesFilePath, gradesFilePath;
  LinkedList<Course> courses;

  //This method is called when the service instance is created
  @Override
  public void onCreate() {
    super.onCreate();
    Log.i("GradeScheduler", "gradeService created");
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.i("GradeScheduler", "gradeService destroyed");
  }

  @Override
  public boolean onStartJob( final JobParameters params) {
    Log.i("GradeScheduler", "on start job");
    checkForNewGrade();
    jobFinished(params, true);
    return true;
  }

  @Override
  public boolean onStopJob(JobParameters params) {
    Log.i("GradeScheduler", "on stop job");
    return true;
  }

  public void setUICallback(MainActivity activity) {
    gradeMainActivity = activity;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Messenger callback = intent.getParcelableExtra("messenger");
    Message m = Message.obtain();
    m.what = 2;
    m.obj = this;
    try {
      callback.send(m);
    } catch (RemoteException e) {
      Log.e("GradeScheduler", "Error passing service object back to activity.");
    }
    return START_NOT_STICKY;
  }

  private void checkForNewGrade() {
    SharedPreferences quarter = getSharedPreferences(getString(R.string.currentQuarter), Context.MODE_PRIVATE);
    String currentQuarter = quarter.getString(getString(R.string.currentQuarter), null);
    SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.myACCESS), Context.MODE_PRIVATE);
    String username = sharedPreferences.getString(getString(R.string.uName), null);
    String password = sharedPreferences.getString(getString(R.string.uPass), null);
    User user = new User(username, password);
    coursesFilePath = getFilesDir().getPath() + "/Courses" + currentQuarter + ".txt";

    try {
      fileInputStream = new FileInputStream(coursesFilePath);
      objectInputStream = new ObjectInputStream(fileInputStream);
      courses = (LinkedList<Course>) objectInputStream.readObject();
      new initialLogin(user).execute("");
    } catch (Exception e) {
      Log.e("scheduler", e.toString());
    }
  }

  @SuppressLint("StaticFieldLeak")
  private class checkForGrades extends AsyncTask<String, Void, String> {
    private User user;
    private Course course;

    checkForGrades(User user, Course c) {
      this.user = user;
      this.course = c;
    }
    @Override
    protected String doInBackground(String... params) {
      if (user.getGrades(course, grades)) {
        writeToFile();
        return "new grade";
      }
      return "";
    }

    @Override
    protected void onPostExecute(String result) {
      if(result.equals("new grade")) {
        Intent intent = new Intent(GradeScheduler.this, GradeDisplay.class).putExtra(getString(R.string.selectedCourse), course);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(GradeScheduler.this, 0, intent, 0);

        //send notification here
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.mustang_logo_foreground));
        mBuilder.setSmallIcon(R.mipmap.mustang_logo_foreground);
        mBuilder.setContentText("New Grade has been posted in " + course.getName());
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =  (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
        mNotificationManager.notify(104, mBuilder.build());
      }

    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }
  }

  @SuppressLint("StaticFieldLeak")
  private class initialLogin extends AsyncTask<String, Void, String> {
    private User user;

    initialLogin(User user) {
      this.user = user;
    }
    @Override
    protected String doInBackground(String... params) {
      if (user.login()) {
        return "success";
      }
      return "";
    }

    @Override
    protected void onPostExecute(String result) {
      if(result.equals("success")) {
        for (Course c :courses) {
          try {
            gradesFilePath = getFilesDir().getPath() + "/grades" + c.getName() + ".txt";
            fileInputStream = new FileInputStream(gradesFilePath);
            objectInputStream = new ObjectInputStream(fileInputStream);
            grades = (HashMap) objectInputStream.readObject();
            objectInputStream.close();
            new checkForGrades(user, c).execute("");
          } catch (Exception e) {
            grades = new HashMap<>();
            writeToFile();
          }
        }
      }

    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }
  }

  private void writeToFile() {
    try {
      fileOutputStream = new FileOutputStream(gradesFilePath);
      objectOutputStream = new ObjectOutputStream(fileOutputStream);

      objectOutputStream.writeObject(grades);
      objectOutputStream.close();
    } catch (Exception e) {
      Log.e("Grades", e.toString());
    }
  }

}


