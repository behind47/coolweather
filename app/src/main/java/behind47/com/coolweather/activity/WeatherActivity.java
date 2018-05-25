package behind47.com.coolweather.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import behind47.com.coolweather.R;
import behind47.com.coolweather.utils.HttpCallbackListener;
import behind47.com.coolweather.utils.HttpUtils;
import behind47.com.coolweather.utils.Utility;

/**
 * Created by behind47 on 2018/5/25.
 */
public class WeatherActivity extends Activity implements View.OnClickListener{


    private TextView mCurrentDateText;
    private TextView mMaxTempText;
    private TextView mMinTempText;
    private TextView mWeatherDespText;
    private TextView mPublishText;
    private TextView mCityNameText;
    private LinearLayout mWeatherInfoLayout;

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
    }


    @Override
    public void onClick(View v) {

    }
}
