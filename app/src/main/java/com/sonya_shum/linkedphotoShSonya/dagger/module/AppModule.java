package com.sonya_shum.linkedphotoShSonya.dagger.module;

import android.content.Context;

import androidx.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
@Module
public class AppModule {
    private Context appContext;

    public AppModule(@NonNull Context context) {
        appContext = context;
    }

    @Provides
    Context provideContext() {
        return appContext;
    }
}
