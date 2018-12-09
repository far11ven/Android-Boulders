package com.kushal.boulders;

import android.app.Application;
import android.content.Context;

import com.kushal.boulders.dependencies.component.AppComponent;
import com.kushal.boulders.dependencies.component.DaggerAppComponent;
import com.kushal.boulders.dependencies.module.NetworkModule;
import com.kushal.boulders.dependencies.module.SharedPrefStorageModule;

public class App extends Application {

    private AppComponent mAppComponent;

    public static App getApplicationContext(Context context) {
        return (App) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAppComponent = DaggerAppComponent.builder()
                .networkModule(new NetworkModule(getApplicationContext()))
                .sharedPrefStorageModule(new SharedPrefStorageModule(getApplicationContext()))
                .build();
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }

}
