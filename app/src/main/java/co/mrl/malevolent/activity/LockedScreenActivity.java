package co.mrl.malevolent.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.jungly.gridpasswordview.GridPasswordView;

import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.mrl.malevolent.R;
import co.mrl.malevolent.Utils.SharedPrefs;

/**
 * Created by MrL on 5/3/16.
 */
public class LockedScreenActivity extends AppCompatActivity {

    public static String TAG = "Locked Screen Activity";
    SharedPrefs sharedPrefs;
    @Bind(R.id.password_et) GridPasswordView password_et;
    @Bind(R.id.time_tv) TextView time_tv;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_unlock);
        ButterKnife.bind(this);

        sharedPrefs = new SharedPrefs(getApplicationContext());

        Calendar cal = Calendar.getInstance();
        String display_date = cal.get(Calendar.HOUR_OF_DAY) + "";
        display_date += " : " + cal.get(Calendar.MINUTE);
        time_tv.setText(display_date);

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
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                } else {
                    password_et.clearPassword();
                    sharedPrefs.increment_wrong_count();
                    if (sharedPrefs.get_wrong_count() >= 3) {
                        Intent startMain = new Intent(Intent.ACTION_MAIN);
                        startMain.addCategory(Intent.CATEGORY_HOME);
                        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(startMain);
                        finish();
                    }
                }
            }
        });
    }

}
