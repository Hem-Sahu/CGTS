package com.nic.transport;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceData {
    private static final String USER_PREFS = "transport";
    private SharedPreferences appSharedPrefs;
    private SharedPreferences.Editor prefsEditor;

    // private String user_name = "user_name_prefs";
    // String user_id = "user_id_prefs";

    public SharedPreferenceData(Context context) {
        this.appSharedPrefs = context.getSharedPreferences(USER_PREFS, Activity.MODE_PRIVATE);
        this.prefsEditor = appSharedPrefs.edit();
    }

    public void setMobile(String stringKeyValue, String _stringValue) {

        prefsEditor.putString(stringKeyValue, _stringValue).commit();

    }

    public String getMobile(String stringKeyValue) {
        return appSharedPrefs.getString(stringKeyValue, "");
    }

    public void clearData() {
        prefsEditor.clear().commit();

    }


}
