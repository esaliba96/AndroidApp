package com.Poly.Kenner_Saliba.gradesniffer;


import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.gcm.TaskParams;

import android.app.NotificationManager;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class GradeScheduleService extends GcmTaskService {

  @Override
  public int onRunTask(TaskParams taskParams) {
    switch (taskParams.getTag()) {
      case "periodic":
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.mustang_logo_foreground));
        mBuilder.setSmallIcon(R.mipmap.mustang_logo_foreground);
        mBuilder.setContentText("New Grade has been posted");
        NotificationManager mNotificationManager =  (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
        mNotificationManager.notify(104, mBuilder.build());
        return GcmNetworkManager.RESULT_SUCCESS;
      case "Oneoff":
        //run this periodically
        return GcmNetworkManager.RESULT_SUCCESS;
      default:
        return GcmNetworkManager.RESULT_FAILURE;
    }
  }
}
