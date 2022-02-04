package com.sonya_shum.linkedphotoShSonya.dagger.module;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.sonya_shum.linkedphotoShSonya.act.MainAppClass;
import com.sonya_shum.linkedphotoShSonya.db.DbManager;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
@Module(includes = AppModule.class)
public class MainAppClassModule {
        @Provides
        @NonNull
        public MainAppClass providerMainAppClass() {
            return new MainAppClass();
        }
    @Provides
    @NonNull
    @Named("mainDb")
    public DatabaseReference providerDatabaseReference(){
          return providerMainAppClass().getMainDbRef();
    }
    @Provides
    @NonNull
    @Named("userDb")
    public DatabaseReference providerDatabaseReferenceUSER(){
        return providerMainAppClass().getUserDbRef();
    }
    @Provides
    @NonNull
   public FirebaseAuth providerFireBAseAuth(){
            return providerMainAppClass().getAuth();
   }
    @Provides
    @NonNull
   public FirebaseStorage providerStorage(){
            return providerMainAppClass().getFs();
   }

    }


