package com.kushal.boulders.utils.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.kushal.boulders.models.User;

public class ImageStorage{

    private static final String PREFS_NAME = "BouldersPrefs_ImageStorage";

    private SharedPreferences mSharedPref;

    public ImageStorage(Context context) {
        this.mSharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getMemberImage(String member_id) {
        String memberImageString = mSharedPref.getString(member_id + "_memberImage", null);

        if (memberImageString != null ) {
            return memberImageString;
        } else {
            return null;
        }
    }

    public void saveMemberImage(String member_id, String memberImageString) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(member_id + "_memberImage", memberImageString);
        editor.apply();
    }

    public void resetMemberImage(String member_id) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.remove(member_id + "_memberImage");
        editor.apply();
    }

}
