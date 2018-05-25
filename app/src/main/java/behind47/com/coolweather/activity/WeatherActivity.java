package behind47.com.coolweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import behind47.com.coolweather.R;
import behind47.com.coolweather.service.AutoUpdateService;
import behind47.com.coolweather.utils.HttpCallbackListener;
import behind47.com.coolweather.utils.HttpUtils;
import behind47.com.coolweather.utils.Utility;

/**
 * Created by behind47 on 2018/5/25.
 */
public class WeatherActivity extends Activity implements View.OnClickListener{

    private LinearLayout mWeatherInfoLayout;
    /**
     * city name of weather info
     */
    private TextView mCityNameText;
    /**
     * publish time of weather info by api
     */
    private TextView mPublishText;
    /**
     * weather description
     */
    private TextView mWeatherDespText;
    /**
     * temperature range
     */
    private TextView mMaxTempText;
    private TextView mMinTempText;
    /**
     * current date
     */
    private TextView mCurrentDateText;

    /**
     * buttons used to refresh weather_info and turn to city-selecting page to switch city.
     */
    private Button mRefreshWeather;
    private Button mSwitchCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        mWeatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        mCityNameText = (TextView) findViewById(R.id.city_name);
        mPublishText = (TextView) findViewById(R.id.publish_text);
        mWeatherDespText = (TextView) findViewById(R.id.weather_desp);
        mMinTempText = (TextView) findViewById(R.id.min_temp);
        mMaxTempText = (TextView) findViewById(R.id.max_temp);
        mCurrentDateText = (TextView) findViewById(R.id.current_date);

        mSwitchCity = (Button) findViewById(R.id.switch_city);
        mRefreshWeather = (Button) findViewById(R.id.refresh_weather);

        mSwitchCity.setOnClickListener(this);
        mRefreshWeather.setOnClickListener(this);

        // should be started by intent with county_code from ChooseAreaActivity
        String countyCode = getIntent().getStringExtra("county_code");

        if (!TextUtils.isEmpty(countyCode)) {
            mPublishText.setText("synchronizing...");
            mWeatherInfoLayout.setVisibility(View.INVISIBLE);
            mCityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        } else {
            showWeather();
        }
    }

    /**
     * query weatherCode by countyCode
     * @param countyCode
     */
    private void queryWeatherCode(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        queryFromServer(address, "countyCode");
    }

    /**
     * query weatherInfo by weatherCode
     * @param weatherCode
     */
    private void queryWeatherInfo(String weatherCode) {
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        queryFromServer(address, "weatherCode");
    }

    /**
     * query weatherCode or weatherInfo according to input address and type
     * @param address
     * @param type
     */
    private void queryFromServer(final String address, final String type) {
        HttpUtils.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if ("countyCode".equals(type)) {
                    if (!TextUtils.isEmpty(response)) {
                        // resolve weatherCode from response
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2) {
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                } else if ("weatherCode".equals(type)) {
                    // resolve weatherInfo response
                    Utility.handleWeatherResponse(WeatherActivity.this, response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPublishText.setText("failed synchronize");
                    }
                });
            }
        });
    }

    /**
     * read weatherInfo from SharedPreferences and show
     */
    private void showWeather() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mCityNameText.setText(sharedPreferences.getString("city_name", ""));
        mMinTempText.setText(sharedPreferences.getString("min_temp", ""));
        mMaxTempText.setText(sharedPreferences.getString("max_temp", ""));
        mWeatherDespText.setText(sharedPreferences.getString("weather_desp", ""));
        mPublishText.setText("today " + sharedPreferences.getString("publish_time", "") + " releases");
        mCurrentDateText.setText(sharedPreferences.getString("current_date", ""));
        mWeatherInfoLayout.setVisibility(View.VISIBLE);
        mCityNameText.setVisibility(View.VISIBLE);

        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_city:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather:
                mPublishText.setText("synchronizing...");
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCode = sharedPreferences.getString("weather_code", "");
                if (!TextUtils.isEmpty(weatherCode)) {
                    queryWeatherInfo(weatherCode);
                }
                break;
            default:
                break;
        }
    }
}
