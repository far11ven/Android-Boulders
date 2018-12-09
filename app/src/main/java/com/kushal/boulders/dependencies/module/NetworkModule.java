package com.kushal.boulders.dependencies.module;

import android.content.Context;

import com.kushal.boulders.utils.network.HttpClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class NetworkModule {

    private Context mContext;

    public NetworkModule(Context context) {
        mContext = context;
    }

    @Singleton
    @Provides
    HttpClient provideHttpClient() {

        return new HttpClient(mContext);
    }


}
