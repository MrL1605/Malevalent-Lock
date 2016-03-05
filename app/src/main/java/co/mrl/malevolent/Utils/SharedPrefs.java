package co.mrl.malevolent.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import co.mrl.malevolent.AppLockConstants;

/**
 * Created by lalit on 5/3/16.
 */
public class SharedPrefs {

    public Context context;
    public SharedPreferences sharedPreferences;

    public SharedPrefs(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(
                AppLockConstants.PREFERENCES_NAME,
                Context.MODE_PRIVATE);
    }

    public void set_password(String pass) {

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(AppLockConstants.PASSWORD, "" + pass);
        editor.putBoolean(AppLockConstants.IS_PASSWORD_SET, true);
        editor.putInt(AppLockConstants.WRONG_COUNT, 0);
        editor.apply();
    }

    public List<String> getLocked() {

        List<String> locked_packages = new ArrayList<>();
        locked_packages.add("co.mrl.vitco");
        return locked_packages;
    }

    public String get_password() {

        sharedPreferences = context.
                getSharedPreferences(AppLockConstants.PREFERENCES_NAME, Context.MODE_PRIVATE);
        return "2536";
    }

    public void increment_wrong_count() {

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(AppLockConstants.WRONG_COUNT,
                sharedPreferences.getInt(AppLockConstants.WRONG_COUNT, 0) + 1);
        editor.apply();

    }

    public int get_wrong_count() {
        return sharedPreferences.getInt(AppLockConstants.WRONG_COUNT, 0);
    }

    public boolean is_password_set(){
        return sharedPreferences.getBoolean(AppLockConstants.IS_PASSWORD_SET, false);
    }

    public void reset_count(){

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(AppLockConstants.WRONG_COUNT, 0);
        editor.apply();
    }

}
