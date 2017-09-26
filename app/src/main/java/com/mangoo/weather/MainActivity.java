package com.mangoo.weather;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;


import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private List<Weather> weatherList = new ArrayList<>();
    private WeatherAdapter weatherArrayAdapter;

    private SharedPreferences sPref;
    final String SAVED_USERS_CITIES = "strKey"; //переменная для сохранения и востановления настроек

    private final int MENU_DELETE = 1; //переменная для удаленя через контекстного меню


    ListView weatherListView;
    Button buttonAdd;

    SwipeRefreshLayout mSwipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Проверка на наличие интернет соединения
        if (isOnline() == false) {
            Toast.makeText(MainActivity.this, getString(R.string.isOnline_fasle_text), Toast.LENGTH_SHORT).show();
            loadUsersCitiesNameIsOffline();
        } else {

            //Установка имени города, при пустом ListView
            sPref = getPreferences(MODE_PRIVATE);
            if (sPref.getStringSet(SAVED_USERS_CITIES, new HashSet<String>()).size() == 0){
                createUrlToJson("kazan"); //здесь может быть город, определенный по GPS
                Toast.makeText(this, R.string.toast_if_listview_isempty_oncreate, Toast.LENGTH_SHORT).show();
            }
        }

        weatherListView = (ListView) findViewById(R.id.listViewMain);
        weatherArrayAdapter = new WeatherAdapter(this, weatherList);
        weatherListView.setAdapter(weatherArrayAdapter);
        registerForContextMenu(weatherListView);

        //Отображение названия города, при нажатии на его пункт
        weatherListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, getString(R.string.list_view_item_toast) + " " +
                        weatherArrayAdapter.getItem(position).cityName, Toast.LENGTH_SHORT).show();
            }
        });

        //Кнопка добавления новых городов
        buttonAdd = (Button) findViewById(R.id.btnAdd);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, GetCityActivity.class);
                startActivityForResult(intent, 1);

            }
        });

        //обновление погоды при свайпе вниз
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (weatherArrayAdapter.getCount() == 0 | isOnline() == false){

                    if (isOnline() == false){
                        mSwipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(MainActivity.this, R.string.toast_swype_refresh_internet_none, Toast.LENGTH_SHORT).show();
                    } else {
                        mSwipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(MainActivity.this, getString(R.string.swype_refresh_negative_message), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    mSwipeRefreshLayout.setRefreshing(true);
                    mSwipeRefreshLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            saveUsersCitiesNames();
                            weatherArrayAdapter.clear();
                            loadShowUsersCitiesNames();
                            mSwipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(MainActivity.this, R.string.swype_refresh_positive_message, Toast.LENGTH_SHORT).show();
                        }
                    }, 300 + weatherArrayAdapter.getCount());
                }
            }
        });
        loadShowUsersCitiesNames(); //восстановление добавленных ранее городов
    }





    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, MENU_DELETE, 0 , R.string.contec_menu_delete_city);
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        if (item.getItemId() == MENU_DELETE){

            Toast.makeText(this, getString(R.string.context_menu_item_delete_toast) + " " +
                    weatherArrayAdapter.getItem(info.position).cityName, Toast.LENGTH_SHORT).show();

            //получение обекта по позиции из адаптера и его удаление
            Object toRemove = weatherArrayAdapter.getItem(info.position);
            weatherArrayAdapter.remove((Weather) toRemove);

            return true;
        }else {
            return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //обработка ошибок пустого интента или отсутсвия интернета
        if (data == null | !isOnline()){
            if (!isOnline()) {
                Toast.makeText(MainActivity.this, R.string.isOnline_fasle_text, Toast.LENGTH_SHORT).show();
            }
            return;
        }

        //получение списка городов
        ArrayList<String> moreCityNames = data.getExtras().getStringArrayList("list");

        //добавление полученных городов из списка
        for (int i = 0; i < moreCityNames.size() ; i++) {

            if (moreCityNames.get(i).length() != 0) //проверка на пустое значение
            createUrlToJson(moreCityNames.get(i));
        }
    }

    private void createUrlToJson(String CityName) {

        URL url = createUrl(CityName); //создание URL с полученным названием города

        //запрос информации по городу
        if (url != null) {
                GetWeatherTask getLocalWeatherTask = new GetWeatherTask();
                getLocalWeatherTask.execute(url);
        } else {
            Toast.makeText(MainActivity.this, getString(R.string.bad_url), Toast.LENGTH_SHORT).show();
        }
    }

    private URL createUrl(String city) {

        //создание url для последующего запроса

        String apiKey = getString(R.string.api_key);
        String baseUrl = getString(R.string.web_service_url);

        try {
            String urlString = baseUrl + URLEncoder.encode(city, "UTF-8") +
                    "&units=metric&APPID=" + apiKey;
            return new URL(urlString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; //если некорректный URL
    }

    private class GetWeatherTask extends AsyncTask<URL, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(URL... params) {
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) params[0].openConnection(); //создание подключения
                int response = connection.getResponseCode();

                if (response == HttpURLConnection.HTTP_OK) {                // проверка подключения
                    StringBuilder builder = new StringBuilder();

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()))) {

                        String line;

                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }
                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this, getString(R.string.read_error), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    return new JSONObject(builder.toString());
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject weather) {
            convertJsonArrayList(weather); //создание нового объекта для списка
            weatherArrayAdapter.notifyDataSetChanged(); //обновление адаптера
        }
    }

    private void convertJsonArrayList(JSONObject forecast) {

        try {
            //получение Json объектов
            JSONObject main = forecast.getJSONObject("main");
            JSONObject wind = forecast.getJSONObject("wind");

            //Добавление объекта Weather в ListView
            weatherList.add(new Weather(
                    forecast.getString("name"), //название города
                    main.getString("temp"),     //температура в городе
                    wind.getString("speed"),    //скорость ветра
                    wind.getDouble("deg")));    //направление ветра

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        saveUsersCitiesNames(); //сохранение данных при внезапном крушении
    }

    @Override
    protected void onStop() {
        super.onStop();

        saveUsersCitiesNames(); //сохранение данных при закрытии или уничтожении Activity
    }

    public void saveUsersCitiesNames() {

        //получение файли настроек
        sPref = getPreferences(MODE_PRIVATE);

        //запись списка добавленных пользователем городов
        Set<String> userCitiesNames = new HashSet<>();

        for (int i = 0; i < weatherArrayAdapter.getCount() ; i++) {
            userCitiesNames.add(weatherArrayAdapter.getItem(i).cityName);
        }

        SharedPreferences.Editor e =sPref.edit();
        e.putStringSet(SAVED_USERS_CITIES, userCitiesNames);
        e.apply();
    }

    public void loadShowUsersCitiesNames(){

        //получение файли настроек
        sPref = getPreferences(MODE_PRIVATE);

        //извлечение названий городов в List
        Set<String> ret = sPref.getStringSet(SAVED_USERS_CITIES, new HashSet<String>());

        //добавление полученных городов в ListView
        if (isOnline() != false) {
            for (String r : ret) {
                createUrlToJson(r);
            }
        }
    }
        public void loadUsersCitiesNameIsOffline() {

        //восстановление добавленных ранее городов без интернета

        //получение файли настроек
        sPref = getPreferences(MODE_PRIVATE);

        //извлечение названий городов в List
        Set<String> userCitiesNames = sPref.getStringSet(SAVED_USERS_CITIES, new HashSet<String>());


        //добавление полученных городов в ListView
            for (String r : userCitiesNames) {
                weatherList.add(new Weather(r, "0", "0", 0));

            }

    }
}
