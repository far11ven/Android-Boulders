package com.kushal.boulders.models;

public class User {

    private String mUsername;
    private String mPassword;

    public User(String username, String password) {
        mUsername = username;
        mPassword = password;

    }

    public String getUserName() {
        return mUsername;
    }

    public boolean match(String username, String password) {

        return (mUsername.equals(username) && mPassword.equals(password));
    }

}
