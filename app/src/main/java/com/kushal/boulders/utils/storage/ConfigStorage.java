package com.kushal.boulders.utils.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class ConfigStorage {

    private static final String PREFS_NAME = "BouldersPrefs_ConfigStorage";

    private SharedPreferences mSharedPref;

    public ConfigStorage(Context context) {
        this.mSharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getConfigValue(String dbName, String keyName) {
        String memberImageString = mSharedPref.getString(dbName + ":" + keyName, null);

        if (memberImageString != null ) {
            return memberImageString;
        } else {
            return null;
        }
    }

    public void saveConfigValue(String dbName, String keyName, String keyValue) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(dbName + ":" + keyName, keyValue);
        editor.apply();
    }

    public void resetConfigValue(String dbName, String keyName) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.remove(dbName + ":" + keyName);
        editor.apply();
    }

    public void resetStorage() {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.clear();
        editor.apply();
    }

}
