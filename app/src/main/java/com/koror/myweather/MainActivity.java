package com.koror.myweather;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.koror.myweather.pojo.Model;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    final String BASE_URL = "http://api.openweathermap.org";
    final String APP_ID = "yourAPPID";
    final String UNITS = "metric";
    final String LANG = "ru";
    @BindView(R.id.tv_city)
    AutoCompleteTextView acTextView;
    @BindView(R.id.icon)
    ImageView imageView;
    @BindView(R.id.tv_temp)
    TextView tvTemp;
    @BindView(R.id.tv_desc)
    TextView tvDesc;
    @BindView(R.id.tv_wind)
    TextView tvWind;
    @BindView(R.id.tv_sunrise)
    TextView tvSunrise;
    @BindView(R.id.tv_sunset)
    TextView tvSunset;
    @BindView(R.id.tv_info)
    TextView tv_info;
    @BindView(R.id.main_layout)
    RelativeLayout linearLayout;
    @BindView(R.id.button)
    Button button;
    int city;
    String lastCity;
    HashMap<String,Integer> hashMap;
    SharedPreferences pref;

    WeatherAPI weatherAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        acTextView.setText(R.string.defaultCity);
        Gson gson = new GsonBuilder()
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        weatherAPI = retrofit.create(WeatherAPI.class);

        //нициализвация
        pref  = getPreferences(MODE_PRIVATE);
        acTextView.setText(pref.getString("cityName","Moscow RU"));
        city=pref.getInt("city",524901);
        lastCity=acTextView.getText().toString();
        response(weatherAPI);

        //заполняем arrayList городами из json
        hashMap = new HashMap<>();
        final Type REVIEW_TYPE = new TypeToken<List<Review>>() {}.getType();
        JsonReader reader = new JsonReader(new InputStreamReader(getResources().openRawResource(R.raw.ru)));
        List<Review> data = gson.fromJson(reader, REVIEW_TYPE);
        for(Review r: data)
        {
            hashMap.put(r.getName(),r.getId());
        }
        if(!hashMap.isEmpty()) {
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.addAll(hashMap.keySet());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item,R.id.tv_item,arrayList );
            acTextView.setThreshold(3);
            acTextView.setAdapter(adapter);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    city = hashMap.get(acTextView.getText().toString());
                    response(weatherAPI);
                }catch (NullPointerException ne)
                {
                    Toast.makeText(MainActivity.this,R.string.cityError,Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void response(WeatherAPI weatherAPI)
    {
        weatherAPI.getData(city,APP_ID,UNITS,LANG).enqueue(new Callback<Model>() {
            @Override
            public void onResponse(Call<Model> call, Response<Model> response) {
                try {
                    tvTemp.setText(String.valueOf(response.body().getMain().getTemp()));
                    tvDesc.setText(response.body().getWeather().get(0).getDescription());
                    tvWind.setText(getString(R.string.wind , response.body().getWind().getSpeed().intValue()));
                    tvSunrise.setText(getString(R.string.sunrise, getDate(response.body().getSys().getSunrise())));
                    tvSunset.setText(getString(R.string.sunset , getDate(response.body().getSys().getSunset())));
                    lastCity = acTextView.getText().toString();
                    int id = getResources().getIdentifier("i"+response.body().getWeather().get(0).getIcon(),"drawable",getPackageName());
                    imageView.setImageResource(id);
                    //очишаем поле с выводом об ошибке
                    tv_info.setText("");

                    //получаем id иконки и берем 3 символ отвеающий за день или ночь
                    String day=response.body().getWeather().get(0).getIcon();
                    if(day.charAt(2)=='d') {
                        linearLayout.setBackgroundResource(R.drawable.color_day_grad);
                    }
                    else{
                        linearLayout.setBackgroundResource(R.drawable.color_night_grad);
                    }
                }catch (NullPointerException e)
                {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this,R.string.cityError,Toast.LENGTH_LONG).show();

                    acTextView.setText(lastCity);
                }
            }

            @Override
            public void onFailure(Call<Model> call, Throwable t) {
                tv_info.setText(R.string.internetError);
            }
        });
    }

    public static String getDate(long time)
    {
        //конвертирует секунды в часы и минуты
        time*=1000;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return DateFormat.format("HH:mm", cal).toString();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("city",city);
        editor.putString("cityName",acTextView.getText().toString());
        editor.apply();
    }

}
