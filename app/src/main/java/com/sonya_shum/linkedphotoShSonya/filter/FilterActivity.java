package com.sonya_shum.linkedphotoShSonya.filter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sonya_shum.linkedphotoShSonya.R;
import com.sonya_shum.linkedphotoShSonya.databinding.ActivityFilterBinding;
import com.sonya_shum.linkedphotoShSonya.utils.CountryManager;
import com.sonya_shum.linkedphotoShSonya.utils.DialogHelper;
import com.sonya_shum.linkedphotoShSonya.utils.MyConstants;

public class FilterActivity extends AppCompatActivity {
    private ActivityFilterBinding rootElement;
    private SharedPreferences preferences;
    public static final int Country_index = 0;
    public static final int City_index = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootElement = ActivityFilterBinding.inflate(getLayoutInflater());
        setContentView(rootElement.getRoot());
        init();
        fillFilter();
    }

    public void onClickAddFilter(View view) {
        String filter = preferences.getString(MyConstants.TEXT_FILTER, "empty");
        if (!filter.equals("empty")) {
            FilterManager.clearFilter(preferences);
            rootElement.bAddFilter.setText(R.string.use_filter);
        } else {
            checkEmptyField();

        }
    }

    private void init() {
        ActionBar ab = getSupportActionBar();//делаем стрелку вменю
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        preferences = getSharedPreferences(MyConstants.MAIN_PREF, MODE_PRIVATE);
        String filter = preferences.getString(MyConstants.TEXT_FILTER, "empty");
        if (!filter.equals("empty")) {
            rootElement.bAddFilter.setText(R.string.cancel_use_filter);
        }
    }

    private void fillFilter() {
        String filter = preferences.getString(MyConstants.TEXT_FILTER, "empty");
        if(filter.equals("empty")){
            return;
        }
        String[] arrayFilter = filter.split("\\|");
        if (!arrayFilter[Country_index].equals("empty")) {
            rootElement.tvCountry.setText(arrayFilter[Country_index]);
        }
        if (!arrayFilter[City_index].equals("empty")) {
            rootElement.tvCity.setText(arrayFilter[City_index]);
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {//стрелка в меню- возвращает назад
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClickCountry(View view) {
        String city = rootElement.tvCity.getText().toString();
        if (!city.equals(getString(R.string.select_city_f_title))) {
            rootElement.tvCity.setText(getString(R.string.select_city_f_title));
        }
        DialogHelper.INSTANCE.showDialog(this, CountryManager.INSTANCE.getAllCountries(this), (TextView) view);
    }

    public void onClickCity(View view) {
        String country = rootElement.tvCountry.getText().toString();
        if (!country.equals(getString(R.string.select_country_f_title))) {
            DialogHelper.INSTANCE.showDialog(this, CountryManager.INSTANCE.getAllCites(this, country), (TextView) view);
        } else {
            Toast.makeText(this, R.string.country_notSelected, Toast.LENGTH_SHORT).show();
        }
    }

    private void checkEmptyField() {
        String country = rootElement.tvCountry.getText().toString();
        if (country.equals(getString(R.string.select_country_f_title))) {
            Toast.makeText(this, "Country not selected!", Toast.LENGTH_SHORT).show();
        } else if (!country.equals(getString(R.string.select_country_f_title))) {
            String textFilter = FilterManager.createTextFilter(rootElement,this);
           String orderBy= FilterManager.createOrderByFilter(textFilter);
           String filter= FilterManager.createFilter(textFilter);
           FilterManager.saveFilter(textFilter,preferences,MyConstants.TEXT_FILTER);
           FilterManager.saveFilter(orderBy,preferences,MyConstants.ORDER_BY_FILTER);
           FilterManager.saveFilter(filter,preferences,MyConstants.FILTER);
            rootElement.bAddFilter.setText(R.string.cancel_use_filter);
        } else {
            Toast.makeText(this, "Wrong filter!", Toast.LENGTH_SHORT).show();
        }
finish();
    }


}