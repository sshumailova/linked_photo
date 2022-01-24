package com.sonya_shum.linkedphotoShSonya.act;

import static com.google.android.gms.ads.MobileAds.initialize;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class AdsViewActivity extends AppCompatActivity {
    private AdView adView; //для рекламы

    @Override
    protected void onResume() {
        super.onResume();
        if(adView!=null) {
            adView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(adView!=null) {
            adView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(adView!=null) {
            adView.destroy();
        }
    }
    public void addAds(AdView adView) {
        initialize(this);
        AdRequest adRequest = new AdRequest.Builder().build();
     adView.loadAd(adRequest);
     this.adView=adView;
    }
}
