package com.koror.myweather;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.koror.myweather.pojo.ModelPojo;
import com.koror.myweather.pojoday.PojoDayModel;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    final String BASE_URL = "http://api.openweathermap.org";
    final String APP_ID = "b305102fb2366b196ad11f04c5a98eab";
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
    @BindView(R.id.tv_info)
    TextView tv_info;
    @BindView(R.id.main_layout)
    RelativeLayout linearLayout;
    @BindView(R.id.button)
    Button button;

    @BindViews({R.id.day_textView1,R.id.day_textView2,R.id.day_textView3,R.id.day_textView4,R.id.day_textView5})
    List<TextView> textViewDay;

    @BindViews({R.id.day_textView6,R.id.day_textView7,R.id.day_textView8,R.id.day_textView9,R.id.day_textView10})
    List<TextView> textViewDayTemp;

    @BindViews({R.id.day_imageView1,R.id.day_imageView2,R.id.day_imageView3,R.id.day_imageView4,R.id.day_imageView5})
    List<ImageView> imageViewsDay;

    @BindViews({R.id.textView1,R.id.textView2,R.id.textView3,R.id.textView4,R.id.textView5})
    List<TextView> textViewWeek;

    @BindViews({R.id.textView6,R.id.textView7,R.id.textView8,R.id.textView9,R.id.textView10})
    List<TextView> textViewWeekTemp;

    @BindViews({R.id.imageView1,R.id.imageView2,R.id.imageView3,R.id.imageView4,R.id.imageView5})
    List<ImageView> imageViewsWeek;


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

        //инициализвация
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
        //очишаем поле с выводом об ошибке
        tv_info.setText("");
        //запрос на прогноз на текущий день
        weatherAPI.getDataDay(city,APP_ID,UNITS,LANG,6).enqueue(new Callback<PojoDayModel>() {
            @Override
            public void onResponse(Call<PojoDayModel> call, Response<PojoDayModel> response) {
                try{
                    tvTemp.setText(getString(R.string.temp,response.body().getList().get(0).getMain().getTemp().intValue()));
                    tvDesc.setText(response.body().getList().get(0).getWeather().get(0).getDescription());
                    tvWind.setText(getString(R.string.wind,response.body().getList().get(0).getWind().getSpeed().intValue()));
                    int id = getResources().getIdentifier("i"+response.body().getList().get(0).getWeather().get(0).getIcon(),"drawable",getPackageName());
                    imageView.setImageResource(id);
                    //получаем id иконки и берем 3 символ отвеающий за день или ночь
                    String day=response.body().getList().get(0).getWeather().get(0).getIcon();
                    if(day.charAt(2)=='d') {
                        linearLayout.setBackgroundResource(R.drawable.color_day_grad);
                    }
                    else{
                        linearLayout.setBackgroundResource(R.drawable.color_night_grad);
                    }

                    for(int i = 1;i<response.body().getList().size();i++)
                    {
                        textViewDayTemp.get(i-1).setText(String.valueOf(response.body().getList().get(i).getMain().getTemp()));
                        int idD = getResources().getIdentifier("i"+response.body().getList().get(i).getWeather().get(0).getIcon(),"drawable",getPackageName());
                        imageViewsDay.get(i-1).setImageResource(idD);
                        textViewDay.get(i-1).setText(getDate(response.body().getList().get(i).getDt().longValue()));
                    }
                }catch (NullPointerException e)
                {
                    e.printStackTrace();
                    acTextView.setText(lastCity);
                }

            }

            @Override
            public void onFailure(Call<PojoDayModel> call, Throwable t) {
                tv_info.setText(R.string.internetError);
            }
        });

        //запрос на прогноз на неделю
        weatherAPI.getDataWeek(city,APP_ID,UNITS,LANG,6).enqueue(new Callback<ModelPojo>() {
            @Override
            public void onResponse(Call<ModelPojo> call, Response<ModelPojo> response) {
                try {

                     for(int i=1;i<response.body().getList().size();i++)
                     {
                         textViewWeekTemp.get(i-1).setText(String.valueOf(response.body().getList().get(i).getTemp().getDay()));
                         int id = getResources().getIdentifier("i"+response.body().getList().get(i).getWeather().get(0).getIcon(),"drawable",getPackageName());
                         imageViewsWeek.get(i-1).setImageResource(id);
                         textViewWeek.get(i-1).setText(getWeekDay(response.body().getList().get(i).getDt()));
                     }

                    lastCity = acTextView.getText().toString();
                }catch (NullPointerException e)
                {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this,R.string.cityError,Toast.LENGTH_LONG).show();

                    acTextView.setText(lastCity);
                }
            }

            @Override
            public void onFailure(Call<ModelPojo> call, Throwable t) {
                tv_info.setText(R.string.internetError);
            }
        });


    }

    public static String getDate(long time)
    {
        //конвертирует секунды в часы и минуты
        time*=1000L;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date(time));
    }

    public static String getWeekDay(Integer time)
    {
        //конвертирует секунды в дни недели
        time*=1000;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return parseWeekDay( cal.get(Calendar.DAY_OF_WEEK));
    }

    private  static String parseWeekDay(int day)
    {
        //конвертирует номер дня в строку
        switch (day)
        {
            case 1:
                return "пн";
            case 2:
                return "вт";
            case 3:
                return "ср";
            case 4:
                return "чт";
            case 5:
                return "пт";
            case 6:
                return "сб";
            case 7:
                return "вс";
        }
        return "Er";
    }

    @Override
    protected void onPause() {
        super.onPause();
        //сохраняет значение введенного города
        pref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("city",city);
        editor.putString("cityName",acTextView.getText().toString());
        editor.apply();
    }

}
