package co.mrl.malevolent.service;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.jungly.gridpasswordview.GridPasswordView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.mrl.malevolent.R;
import co.mrl.malevolent.Utils.SharedPrefs;

/**
 * Created by MrL on 5/3/16.
 */
public class LockCheckService extends Service {

    public static final String TAG = "Lock Checking Service";
    public static String currentApp = "";
    public static String previousApp = "";
    public List<String> apps_list;
    ImageView imageView;
    GridPasswordView password_et;
    SharedPrefs sharedPref;
    List<String> packageName;

    private Context context = null;
    private Timer timer;
    private WindowManager windowManager;
    private Dialog dialog;
    private TimerTask updateTask = new TimerTask() {
        @Override
        public void run() {
            if (sharedPref != null) {
                packageName = sharedPref.getLocked();
            }
            if (isConcernedAppIsInForeground()) {
                if (imageView != null) {
                    imageView.post(new Runnable() {
                        public void run() {
                            if (!currentApp.matches(previousApp)) {
                                showUnlockDialog();
                                previousApp = currentApp;
                            }
                        }
                    });
                }
            } else {
                if (imageView != null) {
                    imageView.post(new Runnable() {
                        public void run() {
                            hideUnlockDialog();
                        }
                    });
                }
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "Created");
        apps_list = new ArrayList<>();
        context = getApplicationContext();
        sharedPref = new SharedPrefs(context);
        if (sharedPref != null) {
            packageName = sharedPref.getLocked();
        }
        timer = new Timer("AppCheckServices");
        timer.schedule(updateTask, 1000L, 1000L);

        // Google Analytics Tracker
        //final Tracker t = ((AppLockApplication) getApplication()).getTracker(AppLockApplication.TrackerName.APP_TRACKER);
        //t.setScreenName(AppLockConstants.APP_LOCK);
        //t.send(new HitBuilders.AppViewBuilder().build());

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        imageView = new ImageView(this);
        imageView.setVisibility(View.GONE);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.CENTER;
        params.x = ((getApplicationContext().getResources().getDisplayMetrics().widthPixels) / 2);
        params.y = ((getApplicationContext().getResources().getDisplayMetrics().heightPixels) / 2);
        windowManager.addView(imageView, params);

    }

    void showUnlockDialog() {
        showDialog();
    }

    void hideUnlockDialog() {
        previousApp = "";
        try {
            if (dialog != null) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void showDialog() {
        if (context == null)
            context = getApplicationContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View promptsView = layoutInflater.inflate(R.layout.popup_unlock, null);

        Calendar cal = Calendar.getInstance();
        String display_date = cal.get(Calendar.HOUR_OF_DAY) + "";
        display_date += " : " + cal.get(Calendar.MINUTE);

        TextView time_tv = (TextView)
                promptsView.findViewById(R.id.time_tv);
        time_tv.setText(display_date);
        final SharedPrefs sharedPrefs = new SharedPrefs(context);

        password_et = (GridPasswordView) promptsView.
                findViewById(R.id.password_et);
        password_et.setOnPasswordChangedListener(new GridPasswordView.OnPasswordChangedListener() {
            @Override public void onTextChanged(String psw) {
                // Do nothin
            }

            @Override public void onInputFinish(String psw) {

                Log.i(TAG, "Checking if correct or not");
                String usr_password = sharedPrefs.get_password();
                Log.i(TAG, "req password : " + usr_password + " pass : " + psw);
                if (psw.equals(usr_password)) {
                    sharedPrefs.reset_count();
                    dialog.dismiss();
                } else {
                    password_et.clearPassword();
                    sharedPrefs.increment_wrong_count();
                }
                if (sharedPrefs.get_wrong_count() >= 3) {
                    Intent startMain = new Intent(Intent.ACTION_MAIN);
                    startMain.addCategory(Intent.CATEGORY_HOME);
                    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(startMain);
                    dialog.dismiss();
                }

            }
        });

        dialog = new Dialog(context, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.setContentView(promptsView);
        dialog.getWindow().setGravity(Gravity.CENTER);

        dialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == KeyEvent.ACTION_UP) {
                    Intent startMain = new Intent(Intent.ACTION_MAIN);
                    startMain.addCategory(Intent.CATEGORY_HOME);
                    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(startMain);
                }
                return true;
            }
        });

        dialog.show();
        password_et.requestFocus();

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "started");
        /*
         * We want this service to continue running until it is explicitly
         * stopped, so return sticky.
         */

        return START_STICKY;
    }

    public boolean isConcernedAppIsInForeground() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> task = manager.getRunningTasks(5);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
            if (task.size() > 0) {
                String mPackageName = task.get(0).topActivity.getPackageName();
                for (int i = 0; packageName != null && i < packageName.size(); i++) {
                    if (mPackageName.equals(packageName.get(i))) {
                        currentApp = packageName.get(i);
                        return true;
                    }
                }
            }
        } else {
            String mPackageName = manager.getRunningAppProcesses().get(0).processName;
            for (int i = 0; packageName != null && i < packageName.size(); i++) {
                if (mPackageName.equals(packageName.get(i))) {
                    currentApp = packageName.get(i);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timer = null;
        if (imageView != null) {
            windowManager.removeView(imageView);
        }
        /**** added to fix the bug of view not attached to window manager ****/
        try {
            if (dialog != null) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
