package com.android.linkedphotoShSonya.biling;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;

import java.util.ArrayList;
import java.util.List;

public class BillingManager {
    public static final String REMOVE_ADS = "remove_ads";
    private BillingClient bClient;
    private Activity context;

    public BillingManager(Activity context) {//чтобы оплачивать какой лиюо продукт из любого активити
        this.context = context;
        setUpBillingClient();
    }

    private void setUpBillingClient() {
        bClient = BillingClient.newBuilder(context).setListener(getPurchaseListener()).enablePendingPurchases().build();
    }

    public void startConection() {
        bClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {

            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                getItemList();
            }
        });
    }

    private void getItemList() {
        List<String> skuList = new ArrayList<>();
        skuList.add(REMOVE_ADS);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);// что за покупка и передаем список
        bClient.querySkuDetailsAsync(params.build(), (((billingResult, list) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                if (list != null) {
                    if (!list.isEmpty()) {
                        BillingFlowParams bParams = BillingFlowParams.newBuilder().setSkuDetails(list.get(0)).build();
                        bClient.launchBillingFlow(context, bParams);
                    }
                }
            }
        })));
    }

    private PurchasesUpdatedListener getPurchaseListener() {// проверка покупки - прошла она или нет
        return ((billingResult, list) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                if (list.size() > 0) {
                    nonconsumableItem(list.get(0));
                }
            }
        });
    }

    private void nonconsumableItem(Purchase purchase) {  //подтвеждение покупке
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acParam = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
                bClient.acknowledgePurchase(acParam, billingResult -> {

                });
            }
        }
    }
}
