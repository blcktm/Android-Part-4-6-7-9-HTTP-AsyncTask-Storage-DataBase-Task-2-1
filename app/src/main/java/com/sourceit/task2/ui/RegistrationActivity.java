package com.sourceit.task2.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sourceit.task2.R;
import com.sourceit.task2.ui.model.Country;
import com.sourceit.task2.utils.L;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.sourceit.task2.ui.Countries.countryList;
import static com.sourceit.task2.ui.DataBaseCreator.CommunicationColumns.TABLE_NAME_COMMUNICATIONS;
import static com.sourceit.task2.ui.DataBaseCreator.CountryCoulumns.NAME;
import static com.sourceit.task2.ui.DataBaseCreator.RegionColumns.REGION;
import static com.sourceit.task2.ui.DataBaseCreator.SubregionColumns.SUBREGION;

public class RegistrationActivity extends AppCompatActivity {

    public static final int NOPRIMITIVE = 6;
    public static final String PASSWORD = "password";
    public static final String COUNTRY = "userCountry";
    private Button register;
    private EditText name;
    private EditText userCountry;
    private EditText password;


    private ArrayList<String> regions;
    private ArrayList<String> subregions;

    public static final String REGISTRATION = "registration";
    public static final String REGISTER = "register";
    public static final String UNREGISTER = "unregister";
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        if (DataBaseMaster.getInstance(this).getAllCountry().isEmpty()) {

            regions = new ArrayList();
            subregions = new ArrayList<>();

            Retrofit.getBanks(new Callback<List<Country>>() {
                @Override
                public void success(List<Country> countries, Response response) {
                    countryList = countries;
                    addType(REGION);
                    addType(SUBREGION);
                    addType(NAME);
                    addType(TABLE_NAME_COMMUNICATIONS);
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(getApplicationContext(), "something went wrong :(", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            countryList = DataBaseMaster.getInstance(this).getAllCountry();
        }

        init();

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                L.d("name: " + name.getText());
                L.d("userCountry: " + userCountry.getText());
                L.d("password: " + password.getText());
                if (name.getText().toString().equals("") || password.getText().toString().equals("") || userCountry.getText().toString().equals("")) {

                    String toastText = new String("Введите ");

                    if (userCountry.getText().toString().equals("")) {
                        toastText += "страну, ";
                    }
                    if (password.getText().toString().equals("")) {
                        toastText += "пароль, ";
                    }
                    if (name.getText().toString().equals("")) {
                        toastText += "имя!";
                    }
                    Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
                } else {

                    String toastText = new String();

                    editor.putString(NAME, name.getText().toString());

                    for (Country country : countryList) {
                        if (country.name.equalsIgnoreCase(userCountry.getText().toString())) {
                            editor.putString(COUNTRY, userCountry.getText().toString());
                            editor.apply();
                        }
                    }
                    if (sp.getString(COUNTRY, "").equals("")) {
                        toastText += "введите корректное название страны, ";
                    }

                    if (password.getText().length() < NOPRIMITIVE) {
                        toastText += "Ваш пароль слишком простой!";
                    } else {
                        editor.putInt(PASSWORD, password.getText().toString().hashCode());
                        editor.apply();
                    }

                    if (!sp.getString(NAME, "").equals("") && !sp.getString(COUNTRY, "").equals("") && sp.getInt(PASSWORD, 0) != 0) {
                        editor.putString(REGISTER, REGISTER);
                        editor.apply();
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void init() {
        sp = getSharedPreferences(REGISTRATION, Context.MODE_PRIVATE);
        editor = sp.edit();
        intent = new Intent(RegistrationActivity.this, MainScreenActivity.class);

        register = (Button) findViewById(R.id.button_registration);
        name = (EditText) findViewById(R.id.registration_name);
        userCountry = (EditText) findViewById(R.id.registration_country);
        password = (EditText) findViewById(R.id.registration_password);
    }

    private void addType(String type) {

        if (!type.equals(NAME) && !type.equals(TABLE_NAME_COMMUNICATIONS)) {
            for (Country country : countryList) {
                if (type.equals(REGION)) {
                    if (!regions.contains(country.region)) {
                        regions.add(country.region);
                    }
                } else if (type.equals(SUBREGION)) {
                    if (!subregions.contains(country.subregion)) {
                        subregions.add(country.subregion);
                    }
                }
            }
        }

        if (type.equals(REGION)) {
            for (String region : regions) {
                DataBaseMaster.getInstance(this).addType(region, REGION);
            }
        } else if (type.equals(SUBREGION)) {
            for (String subregion : subregions) {
                DataBaseMaster.getInstance(this).addType(subregion, SUBREGION);
            }
        } else if (type.equals(NAME)) {
            for (Country country : countryList) {
                DataBaseMaster.getInstance(this).addType(country);
            }
        } else if (type.equals(TABLE_NAME_COMMUNICATIONS)) {
            for (int i = 0; i < countryList.size(); i++) {
                DataBaseMaster.getInstance(this).addType(i + 1, regions.indexOf(countryList.get(i).region) + 1, subregions.indexOf(countryList.get(i).subregion) + 1);
            }
        }
    }
}
