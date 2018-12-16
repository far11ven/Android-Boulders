package com.kushal.boulders.models;

public class User {

    private String mUsername;
    private String mPassword;
    private String mUserFirstName;

    public User(String username, String password, String userFirstName) {
        mUsername = username;
        mPassword = password;
        mUserFirstName = userFirstName;

    }

    public String getUserName() {
        return mUsername;
    }

    public String getUserFirstName() {
        return mUserFirstName;
    }

    public boolean match(String username, String password) {

        return (mUsername.equals(username) && mPassword.equals(password));
    }

}
