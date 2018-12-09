package com.kushal.boulders.dependencies.module;

import android.content.Context;

import com.kushal.boulders.utils.storage.SharedPrefStorage;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class SharedPrefStorageModule {

    private Context mContext;

    public SharedPrefStorageModule(Context context) {
        mContext = context;
    }

    @Singleton
    @Provides
    SharedPrefStorage provideSharedPrefStorage() {

        return new SharedPrefStorage(mContext);
    }

}
