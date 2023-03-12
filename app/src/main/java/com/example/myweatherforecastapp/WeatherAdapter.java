package com.example.myweatherforecastapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> {
    Context context;
    ArrayList<WeatherModel> weatherData;

    public WeatherAdapter(Context context, ArrayList<WeatherModel> weatherData) {
        this.context = context;
        this.weatherData = weatherData;
    }

    @NonNull
    @Override
    public WeatherAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.weather_row,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherAdapter.ViewHolder holder, int position) {
        WeatherModel weatherModel = weatherData.get(position);
        SimpleDateFormat input = new SimpleDateFormat("yyyy-mm-dd hh:mm");
        SimpleDateFormat output = new SimpleDateFormat("hh:mm aa");
        try {
            Date time = input.parse(weatherModel.getTime());
            holder.time_tv.setText(output.format(time));
        }catch (Exception e) {
            e.printStackTrace();
        }
        Picasso.get().load("https:".concat(weatherModel.getIcon())).into(holder.condition_iv);
        holder.temp_tv.setText(weatherModel.getTemperature().concat("℃"));
        holder.wind_tv.setText(weatherModel.getWindSpeed().concat("Km/h"));
    }

    @Override
    public int getItemCount() {
        return weatherData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView wind_tv,temp_tv,time_tv;
        ImageView condition_iv;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            wind_tv = itemView.findViewById(R.id.wind_speed_txt);
            temp_tv = itemView.findViewById(R.id.temp_txt);
            time_tv = itemView.findViewById(R.id.time_txt);
            condition_iv = itemView.findViewById(R.id.condition_iv);
        }
    }
}
