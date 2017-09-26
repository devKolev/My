package com.mangoo.weather;

class Weather {

    final String cityName;
    final String temp;
    final String windSpeed;

    private String windDeg;

    Weather(String cityName, String temp, String windSpeed, double jsonWindDeg) {
        this.cityName = cityName;
        this.temp = temp + "\u00B0C";
        this.windSpeed = windSpeed + "м/с";
        this.windDeg = getWindDeg();

        if (jsonWindDeg > 337 & jsonWindDeg <= 22){
            windDeg = "Северный";
        } else if (jsonWindDeg > 22 & jsonWindDeg <= 67) {
            windDeg = "Северо-Восточный";
        } else if (jsonWindDeg > 67 & jsonWindDeg <= 112) {
            windDeg = "Восточный";
        } else if (jsonWindDeg > 112 & jsonWindDeg <= 157) {
            windDeg = "Юго-Восточный";
        } else if (jsonWindDeg > 157 & jsonWindDeg <= 202) {
            windDeg = "Южный";
        } else if (jsonWindDeg > 202 & jsonWindDeg <= 247) {
            windDeg = "Юго-Западный";
        } else if (jsonWindDeg > 247 & jsonWindDeg <= 292) {
            windDeg = "западный";
        } else if (jsonWindDeg > 292 & jsonWindDeg <= 337) {
            windDeg = "западный";
        }
    }


    String getWindDeg() {
        return windDeg;
    }

}
