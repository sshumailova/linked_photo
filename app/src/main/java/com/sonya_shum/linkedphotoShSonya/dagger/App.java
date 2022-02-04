package com.sonya_shum.linkedphotoShSonya.dagger;

import android.app.Application;

import com.sonya_shum.linkedphotoShSonya.dagger.module.AppModule;

public class App  extends Application {
  // DaggerAppComponent appComponent;

AppComponent appComponent;
    public  AppComponent getComponent() {
        return appComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
appComponent=DaggerAppComponent.builder().appModule(new AppModule(this)).build();
    }

//    protected AppComponent buildComponent(){
//        return DaggerAppComponent.create();
//                builder()
//                .appModule(new AppModule(this))
//                .build();

}


