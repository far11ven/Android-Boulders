package com.kushal.boulders.utils.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.kushal.boulders.models.User;

public class SharedPrefStorage implements Storage {

    private static final String PREFS_NAME = "BouldersPrefs";

    private SharedPreferences mSharedPref;

    public SharedPrefStorage(Context context) {
        this.mSharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public User getUser() {
        String username = mSharedPref.getString("username", null);
        String userFirstName = mSharedPref.getString("firstName", null);

        if (username != null ) {
            return new User(username, "",userFirstName);
        } else {
            return null;
        }
    }

    public void saveUserName(User user) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString("username", user.getUserName());
        editor.apply();
    }


    public void resetUserName() {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.remove("username");
        editor.apply();
    }

    @Override
    public void resetStorage() {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.clear();
        editor.apply();
    }

    public void saveUserId(String id) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString("user_id", id);
        editor.apply();
    }


    public String getUserId() {
        String userId = mSharedPref.getString("user_id", null);

        if (userId != null ) {
            return userId;
        } else {
            return null;
        }
    }

    public void resetUserId() {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.remove("user_id");
        editor.apply();
    }

    public String getUserOrg() {
        String userOrg = mSharedPref.getString("org", null);

        if (userOrg != null ) {
            return userOrg;
        } else {
            return null;
        }
    }

    public void saveUserOrg(String org) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString("org", org);
        editor.apply();
    }

    public void resetUserOrg() {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.remove("org");
        editor.apply();
    }


    public String getUserFirstName() {
        String userFirstName = mSharedPref.getString("firstName", null);

        if (userFirstName != null ) {
            return userFirstName;
        } else {
            return null;
        }
    }

    public void saveUserFirstName(String org) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString("firstName", org);
        editor.apply();
    }

    public void resetUserFirstName() {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.remove("firstName");
        editor.apply();
    }


    public String getUserLastName() {
        String userlastName = mSharedPref.getString("lastName", null);

        if (userlastName != null ) {
            return userlastName;
        } else {
            return null;
        }
    }

    public void saveUserLastName(String org) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString("lastName", org);
        editor.apply();
    }

    public void resetUserLastName() {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.remove("lastName");
        editor.apply();
    }

    public String getUserSecurityQuestion() {
        String userSecurityQuestion = mSharedPref.getString("securityQuestion", null);

        if (userSecurityQuestion != null ) {
            return userSecurityQuestion;
        } else {
            return null;
        }
    }

    public void saveUserSecurityQuestion(String securityQuestion) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString("securityQuestion", securityQuestion);
        editor.apply();
    }


    public String getUserSecurityAnswer() {
        String userSecurityAnswer = mSharedPref.getString("securityAnswer", null);

        if (userSecurityAnswer != null ) {
            return userSecurityAnswer;
        } else {
            return null;
        }
    }

    public void saveUserSecurityAnswer(String securityAnswer) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString("securityAnswer", securityAnswer);
        editor.apply();
    }

    public String getUserDBDetails() {
        String userOrg = mSharedPref.getString("dbDetails", null);

        if (userOrg != null ) {
            return userOrg;
        } else {
            return null;
        }
    }

    public void saveUserDBDetails(String org) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString("dbDetails", org);
        editor.apply();
    }

    public void resetUserDBDetails() {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.remove("dbDetails");
        editor.apply();
    }

}
