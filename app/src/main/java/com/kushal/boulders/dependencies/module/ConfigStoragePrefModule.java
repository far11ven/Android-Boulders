package com.kushal.boulders.dependencies.module;

import android.content.Context;

import com.kushal.boulders.utils.storage.ConfigStorage;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ConfigStoragePrefModule {

    private Context mContext;

    public ConfigStoragePrefModule(Context context) {
        mContext = context;
    }

    @Singleton
    @Provides
    ConfigStorage provideConfigStoragePref() {

        return new ConfigStorage(mContext);
    }

}
