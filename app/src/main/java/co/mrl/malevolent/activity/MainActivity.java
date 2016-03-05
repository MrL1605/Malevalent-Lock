package co.mrl.malevolent.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.jungly.gridpasswordview.GridPasswordView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.mrl.malevolent.R;
import co.mrl.malevolent.Utils.SharedPrefs;
import co.mrl.malevolent.service.AlarmReceiver;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "Main Activity";
    @Bind(R.id.password_et) GridPasswordView password_et;
    @Bind(R.id.try_it_bt) Button try_it_bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        List<String> apps_list = new ArrayList<>();
        PackageManager packageManager = getPackageManager();
        Intent main_intent = new Intent(Intent.ACTION_MAIN, null);
        main_intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> appList = packageManager.queryIntentActivities(main_intent, 0);
        List<PackageInfo> pack_list = packageManager.getInstalledPackages(0);
        for (PackageInfo pack_info : pack_list) {
            ApplicationInfo app_info = pack_info.applicationInfo;
            if ((app_info.flags & ApplicationInfo.FLAG_SYSTEM) != 1) {
                apps_list.add(app_info.packageName);
            }
        }

        try_it_bt.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

                startActivity(new Intent(MainActivity.this, LockedScreenActivity.class));
                finish();
            }
        });

        password_et.setOnPasswordChangedListener(new GridPasswordView.OnPasswordChangedListener() {
            @Override public void onTextChanged(String psw) {
                // Do nothin
            }

            @Override public void onInputFinish(String psw) {

                String pass_str = password_et.getPassWord();
                SharedPrefs sharedPrefs = new SharedPrefs(getApplicationContext());
                sharedPrefs.set_password(pass_str);
                start_background_service();
                Toast.makeText(MainActivity.this, "Password Changed", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void start_background_service() {

        startService(new Intent(MainActivity.this, LockedScreenActivity.class));
        Context context = getApplicationContext();

        try {
            Intent alarmIntent = new Intent(context, AlarmReceiver.class);
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1996, alarmIntent, 0);
            int interval = (86400 * 1000) / 4;
            if (manager != null) {
                manager.cancel(pendingIntent);
                manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, interval, pendingIntent);
                Log.i(TAG, "alarm set accordingly");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
