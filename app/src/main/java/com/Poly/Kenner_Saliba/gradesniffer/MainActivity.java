/*
 * Copyright (c) 2016. Truiton (http://www.truiton.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 * Mohit Gupt (https://github.com/mohitgupt)
 *
 */

package com.Poly.Kenner_Saliba.gradesniffer;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioGroup;

public class MainActivity extends AppCompatActivity {
  Intent in;
  JobScheduler jobScheduler;

  @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      //gradeGcmNetworkManager = gradeGcmNetworkManager.getInstance(this);
      RadioGroup radioGroup = (RadioGroup) findViewById(R.id.options);
      radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
          switch (checkedId) {
            case R.id.user_credentials:
              in = new Intent(getBaseContext(), LoginSaveActivity.class);
              startActivity(in);
              overridePendingTransition(0, 0);
              break;
            case R.id.courses:
              in = new Intent(getBaseContext(), MainMenu.class);
              startActivity(in);
              overridePendingTransition(0, 0);
              break;
          }
        }
      });
  }

  private static int sessionDepth = 0;

  @Override
  protected void onStart() {
    super.onStart();
    sessionDepth++;
    if(sessionDepth == 1){
      jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
      jobScheduler.cancelAll();
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (sessionDepth > 0)
      sessionDepth--;
    if (sessionDepth == 0) {
      jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
      JobInfo.Builder gradeInfo = new JobInfo.Builder(1, new ComponentName(this, GradeScheduler.class));
      gradeInfo.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
      gradeInfo.setMinimumLatency(30000);
      jobScheduler.schedule(gradeInfo.build());
    }
  }

}