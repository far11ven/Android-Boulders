package com.kushal.boulders.dependencies.module;

import android.content.Context;

import com.kushal.boulders.utils.storage.ImageStorage;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ImageStoragePrefModule {

    private Context mContext;

    public ImageStoragePrefModule(Context context) {
        mContext = context;
    }

    @Singleton
    @Provides
    ImageStorage provideImageStoragePref() {

        return new ImageStorage(mContext);
    }

}
