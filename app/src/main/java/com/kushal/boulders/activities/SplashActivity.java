package com.kushal.boulders.activities;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kushal.boulders.App;
import com.kushal.boulders.dependencies.component.AppComponent;
import com.kushal.boulders.utils.storage.ConfigStorage;
import com.kushal.boulders.utils.storage.ImageStorage;
import com.kushal.boulders.utils.storage.SharedPrefStorage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.Map;

import javax.inject.Inject;

public class SplashActivity extends AuthenticatedActivity {

    private static int SPLASH_TIME_OUT = 1000;
    ConfigStorage configStorage;
    JsonParser  mJsonParser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppComponent appComponent = App.getApplicationContext(this).getAppComponent();
        appComponent.inject(this);

        configStorage = new ConfigStorage (this);

        mJsonParser = new JsonParser();

        configStorage.resetStorage();
        saveDBEnvironmentVariables();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if(isAuthenticated()) {
                    Intent i = new Intent(SplashActivity.this, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(i);

                } else {

                    Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(i);

                }

                // close splash activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

    public void saveDBEnvironmentVariables(){

        JsonArray dbConfig = mJsonParser.parse(getAssets("ENV_Database.txt")).getAsJsonArray();

        int i=0;

        for(JsonElement current : dbConfig){

            String dbName = "DB0" + i;

            System.out.println( dbName + " ====== & size :" + dbConfig.size());

            JsonObject dbObject = current.getAsJsonObject();
            JsonArray dbURLs = dbObject.getAsJsonArray(dbName);

            System.out.println(" ====== dbURLs size :" + dbURLs.size());

            for(JsonElement url : dbURLs) {

                JsonObject currItem = url.getAsJsonObject();

                String currItemKey = null;
                String currItemValue;

                for (Map.Entry<String, JsonElement> e :  currItem.entrySet()) {

                    currItemKey = e.getKey();

                }

                currItemValue = currItem.get(currItemKey).getAsString();

                configStorage.saveConfigValue(dbName, currItemKey, currItemValue);

                System.out.println( dbName + " ======" + currItemKey + " == " + currItemValue);

            }

            i++;
        }

    }

    public String getAssets(String fileName) {
        StringBuilder text = new StringBuilder();
        BufferedReader reader = null;

        AssetManager am = getApplicationContext().getAssets();

        try {
            InputStream is = am.open(fileName);

            reader = new BufferedReader(
                    new InputStreamReader(is));
            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                text.append(mLine);
                text.append('\n');
            }
        } catch (IOException e) {

            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }

        return text.toString();

    }


}
