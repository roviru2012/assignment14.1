package com.example.weather.fragment;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weather.GlobalActivity;
import com.example.weather.R;
import com.example.weather.activity.DetailActivity;
import com.example.weather.launch.FirstLaunch;
import com.example.weather.retriever.RemoteFetch;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WeatherFragment extends Fragment {
    Typeface weatherFont;
    Button button;
    TextView detailsField[] = new TextView[10] , weatherIcon[] = new TextView[11];
    TextView windView , humidityView , directionView, dailyView, updatedField, cityField;
    double tc;
    Handler handler;
    JSONObject json0 , json1;
    SwipeRefreshLayout swipeView;
    int Clicks = 0;
    JSONObject[] jsonz;
    MaterialDialog pd;
    View rootView;

    private void updateWeatherData(final String city, final String lat, final String lon) {
        new Thread(){
            public void run(){
                if (lat == null && lon == null)
                    jsonz = RemoteFetch.getJSON(getActivity(), city);
                else if (city == null)
                    jsonz = RemoteFetch.getJSONLocation(getActivity(), lat , lon);
                if(jsonz == null) {
                    GlobalActivity.cp.setCity(GlobalActivity.cp.getLastCity());
                    handler.post(new Runnable(){
                        public void run(){
                            Toast.makeText(getActivity(),
                                    getActivity().getString(R.string.place_not_found),
                                    Toast.LENGTH_LONG).show();
                            GlobalActivity.i = 1;
                            if (GlobalActivity.cp.getLaunched()) {
                                pd.dismiss();
                                Intent intent = new Intent(getActivity(), FirstLaunch.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                Log.i("Loaded" , "Weather");
                                startActivity(intent);
                            }
                            else {
                                pd.dismiss();
                                showInputDialog();
                            }
                        }
                    });
                } else {
                    handler.post(new Runnable(){
                        public void run(){
                            GlobalActivity.cp.setLaunched();
                            renderWeather(jsonz);
                            Snackbar.make(rootView , "Loaded Weather Data" , 500).show();
                            pd.dismiss();
                            GlobalActivity.cp.setLastCity(city);
                        }
                    });
                }
            }
        }.start();
    }

    public void Units(JSONObject json1)
    {
        try {
            int bool = Clicks % 2;
            switch (bool) {
                case 0 :
                    double Fah = json1.getJSONObject("main").getDouble("temp") * 1.8 + 32;
                    int F = (int) Fah;
                    String result = Integer.toString(F) + "°F";
                    button.setText(result);
                    ++Clicks;
                    break;
                case 1:
                    result = (int) Math.round(json1.getJSONObject("main").getDouble("temp")) + "°C";
                    button.setText(result);
                    ++Clicks;
                    break;
            }
        }
        catch (Exception ex)
        {
            Log.e("Unlikely" , "Why?");
        }
    }

    public void changeCity(String city)
    {
        if (!swipeView.isRefreshing())
            pd.show();
        updateWeatherData(city, null, null);
        GlobalActivity.cp.setCity(city);
    }

    public void changeCity(String lat , String lon)
    {
        pd.show();
        updateWeatherData(null, lat, lon);
    }

    private void renderWeather(JSONObject[] jsonObj){
        try {
            Clicks = 0;
            Log.i("Showed" , "Done");
            json1 = jsonObj[0];
            json0 = jsonObj[1];
            tc = json1.getJSONObject("main").getDouble("temp");
            GlobalActivity.cp.setCity(json0.getJSONObject("city").getString("name"));
            int a = (int) Math.round(json1.getJSONObject("main").getDouble("temp"));                        //℃
            cityField.setText(json0.getJSONObject("city").getString("name").toUpperCase(Locale.US) +
                    ", " +
                    json0.getJSONObject("city").getString("country"));
            final String city = json0.getJSONObject("city").getString("name").toUpperCase(Locale.US) +
                    ", " +
                    json0.getJSONObject("city").getString("country");
            cityField.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v) {
                    Snackbar.make(v, city, Snackbar.LENGTH_SHORT)
                            .show();
                }
            });
            Log.i("Location" , "Location Received");
            JSONObject details[] = new JSONObject[10];
            for (int i = 0; i < 10; ++i)
            {
                details[i] = json0.getJSONArray("list").getJSONObject(i);
            }
            Log.i("Objects" , "JSON Objects Created");
            for (int i = 0; i < 10; ++i)
            {
                final JSONObject J = details[i];
                String date1 = details[i].getString("dt");
                Date expiry = new Date(Long.parseLong(date1) * 1000);
                String date = new SimpleDateFormat("EE, dd" , Locale.US).format(expiry);
                SpannableString ss1=  new SpannableString(date + "\n"
                + details[i].getJSONObject("temp").getLong("max") + "°" + "      "
                + details[i].getJSONObject("temp").getLong("min") + "°" + "\n");
                ss1.setSpan(new RelativeSizeSpan(1.1f), 0,7, 0); // set size
                ss1.setSpan(new RelativeSizeSpan(1.4f) , 8 , 11 , 0);
                detailsField[i].setText(ss1);
                Log.i("Details[" + Integer.toString(i) + "]", "Information String " + Integer.toString(i + 1) + " loaded");
                setWeatherIcon(details[i].getJSONArray("weather").getJSONObject(0).getInt("id") , i);
                final Intent intent = new Intent(getContext() , DetailActivity.class);
                intent.putExtra("jsonStr" , J.toString());
                try {
                    intent.putExtra("city", json0.getJSONObject("city").getString("name").toUpperCase(Locale.US) +
                            ", " +
                            json0.getJSONObject("city").getString("country"));
                }
                catch (JSONException jx) {
                    Log.e("JSONEX" , "Caught a JSON Exception");
                }
                intent.putExtra("sunrise" , json1.getJSONObject("sys").getLong("sunrise"));
                intent.putExtra("sunset" , json1.getJSONObject("sys").getLong("sunset"));
                detailsField[i].setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        startActivity(intent);
                    }
                });
                weatherIcon[i].setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v)
                    {
                        startActivity(intent);
                    }
                });
            }
            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = "Last update: " + df.format(new Date(json1.getLong("dt")*1000));
            updatedField.setText(updatedOn);
            int deg = json1.getJSONObject("wind").getInt("deg");
            setDeg(deg);
            setWeatherIcon(json1.getJSONArray("weather").getJSONObject(0).getInt("id"),10);
            humidityView.setText("HUMIDITY:\n" + json1.getJSONObject("main").getInt("humidity") + "%");
            Log.i("Humidity Loaded" , "Done");
            windView.setText("WIND:\n" + json1.getJSONObject("wind").getDouble("speed") + "km/h");
            Log.i("Wind Loaded" , "Done");
            Log.i("10" , "Weather Icon 11 Set");
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick (View v)
                {
                    Units(json1);
                }
            });
            weatherIcon[10].setOnClickListener(new View.OnClickListener()
            {
                public void onClick (View v)
                {
                    try {
                        String rs = json1.getJSONArray("weather").getJSONObject(0).getString("description");
                        String[] strArray = rs.split(" ");
                        StringBuilder builder = new StringBuilder();
                        for (String s : strArray) {
                            String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                            builder.append(cap.concat(" "));
                        }
                        Snackbar.make(v , "Hey there, " + builder.toString() + "here right now!", Snackbar.LENGTH_SHORT)
                                .show();
                    }
                    catch (Exception e) {
                        Log.e("Error", "Main Weather Icon OnClick Details could not be loaded");
                    }
                }
            });
            String r1 = Integer.toString(a) + "°C";
            button.setText(r1);
            button.setClickable(false);
        }catch(Exception e){
            Log.e("SimpleWeather", "One or more fields not found in the JSON data");
        }
    }

    private void setDeg(int deg) {
        if (deg == 0) {
            directionView.setText(getActivity().getString(R.string.top));
            directionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view , "Wind blowing towards North" , Snackbar.LENGTH_SHORT).show();
                }
            });
        }
        else if (deg < 90) {
            directionView.setText(getActivity().getString(R.string.top_right));
            directionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view , "Wind blowing in the North-East direction" , Snackbar.LENGTH_SHORT).show();
                }
            });
        }
        else if (deg == 90) {
            directionView.setText(getActivity().getString(R.string.right));
            directionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view , "Wind blowing towards East" , Snackbar.LENGTH_SHORT).show();
                }
            });
        }
        else if (deg < 180) {
            directionView.setText(getActivity().getString(R.string.bottom_right));
            directionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view , "Wind blowing in the South-East direction" , Snackbar.LENGTH_SHORT).show();
                }
            });
        }
        else if (deg == 180) {
            directionView.setText(getActivity().getString(R.string.down));
            directionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view , "Wind blowing towards South" , Snackbar.LENGTH_SHORT).show();
                }
            });
        }
        else if (deg < 270) {
            directionView.setText(getActivity().getString(R.string.bottom_left));
            directionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view , "Wind blowing in the South-West direction" , Snackbar.LENGTH_SHORT).show();
                }
            });
        }
        else if (deg == 270) {
            directionView.setText(getActivity().getString(R.string.left));
            directionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view , "Wind blowing towards West" , Snackbar.LENGTH_SHORT).show();
                }
            });
        }
        else {
            directionView.setText(getActivity().getString(R.string.top_left));
            directionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view , "Wind blowing in the North-West direction" , Snackbar.LENGTH_SHORT).show();
                }
            });
        }
    }

    public WeatherFragment() {
        handler = new Handler();
    }

    private void setWeatherIcon(int id , int i) {
        String icon = "";
        switch(id) {
            case 501 : icon = getActivity().getString(R.string.weather_drizzle);
                break;
            case 500 : icon = getActivity().getString(R.string.weather_drizzle);
                break;
            case 502 : icon = getActivity().getString(R.string.weather_rainy);
                break;
            case 503 : icon = getActivity().getString(R.string.weather_rainy);
                break;
            case 504 : icon = getActivity().getString(R.string.weather_rainy);
                break;
            case 511 : icon = getActivity().getString(R.string.weather_rain_wind);
                break;
            case 520 : icon = getActivity().getString(R.string.weather_shower_rain);
                break;
            case 521 : icon = getActivity().getString(R.string.weather_drizzle);
                break;
            case 522 : icon = getActivity().getString(R.string.weather_thunder);
                break;
            case 531 : icon = getActivity().getString(R.string.weather_thunder);
                break;
            case 200 : icon = getActivity().getString(R.string.weather_thunder);
                break;
            case 201 : icon = getActivity().getString(R.string.weather_thunder);
                break;
            case 202 : icon = getActivity().getString(R.string.weather_thunder);
                break;
            case 210 : icon = getActivity().getString(R.string.weather_thunder);
                break;
            case 211 : icon = getActivity().getString(R.string.weather_thunder);
                break;
            case 212 : icon = getActivity().getString(R.string.weather_thunder);
                break;
            case 221 : icon = getActivity().getString(R.string.weather_thunder);
                break;
            case 230 : icon = getActivity().getString(R.string.weather_thunder);
                break;
            case 231 : icon = getActivity().getString(R.string.weather_thunder);
                break;
            case 232 : icon = getActivity().getString(R.string.weather_thunder);
                break;
            case 300 : icon = getActivity().getString(R.string.weather_shower_rain);
                break;
            case 301 : icon = getActivity().getString(R.string.weather_shower_rain);
                break;
            case 302 : icon = getActivity().getString(R.string.weather_heavy_drizzle);
                break;
            case 310 : icon = getActivity().getString(R.string.weather_shower_rain);
                break;
            case 311 : icon = getActivity().getString(R.string.weather_shower_rain);
                break;
            case 312 : icon = getActivity().getString(R.string.weather_heavy_drizzle);
                break;
            case 313 : icon = getActivity().getString(R.string.weather_rain_drizzle);
                break;
            case 314 : icon = getActivity().getString(R.string.weather_heavy_drizzle);
                break;
            case 321 : icon = getActivity().getString(R.string.weather_heavy_drizzle);
                break;
            case 600 : icon = getActivity().getString(R.string.weather_snowy);
                break;
            case 601 : icon = getActivity().getString(R.string.weather_snowy);
                break;
            case 602 : icon = getActivity().getString(R.string.weather_heavy_snow);
                break;
            case 611 : icon = getActivity().getString(R.string.weather_sleet);
                break;
            case 612 : icon = getActivity().getString(R.string.weather_heavy_snow);
                break;
            case 903 :
            case 615 : icon = getActivity().getString(R.string.weather_snowy);
                break;
            case 616 : icon = getActivity().getString(R.string.weather_snowy);
                break;
            case 620 : icon = getActivity().getString(R.string.weather_snowy);
                break;
            case 621 : icon = getActivity().getString(R.string.weather_snowy);
                break;
            case 622 : icon = getActivity().getString(R.string.weather_snowy);
                break;
            case 701 :
            case 702 :
            case 721 : icon = getActivity().getString(R.string.weather_smoke);
                break;
            case 751 :
            case 761 :
            case 731 : icon = getActivity().getString(R.string.weather_dust);
                break;
            case 741 : icon = getActivity().getString(R.string.weather_foggy);
                break;
            case 762 : icon = getActivity().getString(R.string.weather_volcano);
                break;
            case 771 :
            case 900 :
            case 781 : icon = getActivity().getString(R.string.weather_tornado);
                break;
            case 904 : icon = getActivity().getString(R.string.weather_sunny);
                break;
            case 800 : icon = getActivity().getString(R.string.weather_sunny);
                break;
            case 801 : icon = getActivity().getString(R.string.weather_cloudy);
                break;
            case 802 : icon = getActivity().getString(R.string.weather_cloudy);
                break;
            case 803 : icon = getActivity().getString(R.string.weather_cloudy);
                break;
            case 804 : icon = getActivity().getString(R.string.weather_cloudy);
                break;
            case 901 : icon = getActivity().getString(R.string.weather_storm);
                break;
            case 902 : icon = getActivity().getString(R.string.weather_hurricane);
                break;
        }
        Log.i(Integer.toString(id) , Integer.toString(i));
        weatherIcon[i].setText(icon);
    }

    private void showInputDialog() {
        new MaterialDialog.Builder(this.getActivity())
                .title("Change City")
                .content("Hey there, could not find the city you wanted. Please enter a new one:")
                .negativeText("CANCEL")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog , @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .input(null, null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, @NonNull CharSequence input) {
                        changeCity(input.toString());
                    }
                }).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_weather, container, false);
        cityField = (TextView)rootView.findViewById(R.id.city_field);
        updatedField = (TextView)rootView.findViewById(R.id.updated_field);
        humidityView = (TextView) rootView.findViewById(R.id.humidity_view);
        windView = (TextView) rootView.findViewById(R.id.wind_view);
        directionView = (TextView)rootView.findViewById(R.id.direction_view);
        swipeView = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe);
        swipeView.setColorSchemeResources(R.color.colorAccent, R.color.green);
        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeView.setRefreshing(true);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        changeCity(GlobalActivity.cp.getCity());
                    }
                });
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeView.setRefreshing(false);
                    }
                }, 1500);
            }
        });
        directionView.setTypeface(weatherFont);
        dailyView = (TextView)rootView.findViewById(R.id.daily_view);
        dailyView.setText(getString(R.string.daily));
        button = (Button)rootView.findViewById(R.id.button1);
        button.setText("°C");
        pd.show();
        for (int i = 0; i < 11; ++i)
        {
            String f = "details_view" + (i + 1) , g = "weather_icon" + (i + 1);
            if (i != 10) {
                int resID = getResources().getIdentifier(f, "id", getContext().getPackageName());
                detailsField[i] = (TextView) rootView.findViewById(resID);
            }
            int resIDI = getResources().getIdentifier(g, "id" , getContext().getPackageName());
            weatherIcon[i] = (TextView)rootView.findViewById(resIDI);
            weatherIcon[i].setTypeface(weatherFont);
        }
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this.getActivity())
                .title("Please Wait")
                .content("Loading")
                .cancelable(false)
                .progress(true , 0);
        pd = builder.build();
        super.onCreate(savedInstanceState);
        weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weather.ttf");
        updateWeatherData(GlobalActivity.cp.getCity(), null, null);
    }
}
