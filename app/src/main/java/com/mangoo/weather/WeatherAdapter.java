package com.mangoo.weather;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import java.util.List;

 class WeatherAdapter extends ArrayAdapter<Weather> {


    private static class ViewHolder {
        TextView tvCityName;
        TextView tvTemp;
        TextView tvWindSpeed;
        TextView tvWindDeg;
    }

    WeatherAdapter(@NonNull Context context, List<Weather> forecast) {
        super(context, -1, forecast);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Weather day = getItem(position);

        ViewHolder viewHolder;

        //проверка повторного использования ViewHolder
        if (convertView == null) {

            viewHolder = new ViewHolder();

            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item, parent,false);

            viewHolder.tvCityName = (TextView) convertView.findViewById(R.id.tvCityName);
            viewHolder.tvTemp = (TextView) convertView.findViewById(R.id.tvTemp);
            viewHolder.tvWindSpeed = (TextView) convertView.findViewById(R.id.tvWindSpeed);
            viewHolder.tvWindDeg = (TextView) convertView.findViewById(R.id.tvWindDeg);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //Получаем данные из объекта Weather

        try {
            assert day != null;
            viewHolder.tvCityName.setText(day.cityName);
            viewHolder.tvTemp.setText(day.temp);
            viewHolder.tvWindSpeed.setText(day.windSpeed);
            viewHolder.tvWindDeg.setText(day.getWindDeg());
        }catch (Exception e){
            e.printStackTrace();
        }

        return convertView;

    }
}
