package com.example.myweatherforecastapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.myweatherforecastapp.databinding.ActivityMainBinding;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    ArrayList<WeatherModel> weatherData;
    WeatherAdapter weatherAdapter;
    String TAG = "MainActivity";
    LocationManager locationManager;
    int PERMISSION_CODE = 1;
    String cityName;
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("ServiceCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(binding.getRoot());
        getSupportActionBar().hide();
        weatherData = new ArrayList<>();
        weatherAdapter = new WeatherAdapter(this,weatherData);
        binding.weatherRv.setAdapter(weatherAdapter);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location==null) {
            cityName = "Bihar Sharif";
        } else {
            cityName = getCityName(location.getLongitude(),location.getLatitude());
        }
        getWeatherInfo(cityName);
        binding.searchIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence city = binding.editTextTiet.getText();
                if (city==null || city.toString().isEmpty()) {
                    Toast.makeText(MainActivity.this,"Please Enter City Name",Toast.LENGTH_LONG).show();
                } else {
                    binding.cityNameTxt.setText(city);
                    getWeatherInfo(city.toString());
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==PERMISSION_CODE) {
            if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
               Toast.makeText(MainActivity.this,"Permission Granted",Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this,"Please Provide the Permission",Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude) {
        String cityName = null;
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(latitude,longitude,10);
            for (Address address:addressList) {
                if (address!=null) {
                    String city = address.getLocality();
                    if (city!=null && !city.isEmpty()) {
                        cityName = city;
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return cityName;
    }
    private void getWeatherInfo(String cityName) {
      String url = "https://api.weatherapi.com/v1/forecast.json?key=15c800b0d26446498ab91433231103&q="+cityName+"&days=1&aqi=yes&alerts=yes";
      binding.cityNameTxt.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(JSONObject response) {
               binding.loadingProgressBar.setVisibility(View.GONE);
               binding.homeLayout.setVisibility(View.VISIBLE);
               weatherData.clear();
                try {
                    String temp = response.getJSONObject("current").getString("temp_c");
                    binding.temperatureTxt.setText(temp+"℃");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("http:".concat(conditionIcon)).into(binding.tempIv);
                    binding.conditionTxt.setText(condition);
                    if (isDay==1) {
                        Picasso.get().load("https://images.unsplash.com/photo-1600262912274-28f333fa17bd?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8NXx8bW9ybmluZyUyMHNreXxlbnwwfHwwfHw%3D&auto=format&fit=crop&w=500&q=60").into(binding.bgImg);
                    } else {
                        Picasso.get().load("https://media.istockphoto.com/id/809971888/photo/night-sky-landscape.jpg?b=1&s=170667a&w=0&k=20&c=-1smAP9--twhHyqopksRe3lIORQvO9JaaNWbT1Ra-Is=").into(binding.bgImg);
                    }
                    JSONObject forecastJsonObject = response.getJSONObject("forecast");
                    JSONObject foreCast0 = forecastJsonObject.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = foreCast0.getJSONArray("hour");
                    for (int i=0;i<hourArray.length();i++) {
                      JSONObject hourObj = hourArray.getJSONObject(i);
                      String time = hourObj.getString("time");
                      String temperature = hourObj.getString("temp_c");
                      String img = hourObj.getJSONObject("condition").getString("icon");
                      String wind = hourObj.getString("wind_kph");
                      weatherData.add(new WeatherModel(time,temperature,img,wind));
                    }
                    weatherAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               Toast.makeText(MainActivity.this,"Please Enter Valid City Name",Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }
}