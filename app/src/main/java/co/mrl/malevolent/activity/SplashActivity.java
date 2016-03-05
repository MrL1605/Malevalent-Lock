package co.mrl.malevolent.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import co.mrl.malevolent.AppLockConstants;
import co.mrl.malevolent.R;
import co.mrl.malevolent.Utils.SharedPrefs;
import co.mrl.malevolent.service.AlarmReceiver;

/**
 * Created by lalit on 5/3/16.
 */
public class SplashActivity extends Activity {

    private static final int SPLASH_TIME_OUT = 1000 * 3;
    public static final String TAG = "Splash Activity";
    SharedPrefs sharedPrefs;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        context = getApplicationContext();
        sharedPrefs = new SharedPrefs(context);

        // too much important ****************************************************************
        startService(new Intent(SplashActivity.this, LockedScreenActivity.class));

        try {
            Intent alarmIntent = new Intent(context, AlarmReceiver.class);
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1996, alarmIntent, 0);
            int interval = (86400 * 1000) / 4;
            if (manager != null) {
                manager.cancel(pendingIntent);
                manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 2000, interval, pendingIntent);
            Log.i(TAG, "alarm set accordingly");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //**************************************************************************************

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                boolean isPasswordSet = sharedPrefs.is_password_set();
                if (isPasswordSet) {
                    Intent i = new Intent(SplashActivity.this, LockedScreenActivity.class);
                    startActivity(i);
                } else {
                    Intent i = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(i);
                }
                finish();
            }
        }, SPLASH_TIME_OUT);

    }
}
