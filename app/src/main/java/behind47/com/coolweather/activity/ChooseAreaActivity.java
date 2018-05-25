package behind47.com.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import behind47.com.coolweather.R;
import behind47.com.coolweather.db.CoolWeatherDB;
import behind47.com.coolweather.model.City;
import behind47.com.coolweather.model.County;
import behind47.com.coolweather.model.Province;
import behind47.com.coolweather.utils.HttpCallbackListener;
import behind47.com.coolweather.utils.HttpUtils;
import behind47.com.coolweather.utils.Utility;

/**
 * Created by behind47 on 2018/5/22.
 */
public class ChooseAreaActivity extends Activity {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog mProgressDialog;
    private TextView mTitleText;
    private ListView mListView;
    private ArrayAdapter<String> mAdapter;
    private CoolWeatherDB mCoolWeatherDB;

    // data list buffer
    private List<String> mDataList = new ArrayList<>();

    /**
     * list of province , city, and county separately
     */
    private List<Province> mProvinceList;
    private List<City> mCityList;
    private List<County> mCountyList;

    /**
     * selected province and city separately
     */
    private Province mSelectedProvince;
    private City mSelectedCity;

    /**
     * current selected level
     */
    private int mCurrentLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set view structure
        closeProgressDialog();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        mListView = (ListView) findViewById(R.id.list_view);
        mTitleText = (TextView) findViewById(R.id.title_text);

        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mDataList);
        mListView.setAdapter(mAdapter);

        // obtain and set data in view structure
        mCoolWeatherDB = CoolWeatherDB.getInstance(this);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mCurrentLevel == LEVEL_PROVINCE) {
                    mSelectedProvince = mProvinceList.get(position);
                    queryCities();
                } else if (mCurrentLevel == LEVEL_CITY) {
                    mSelectedCity = mCityList.get(position);
                    queryCounties();
                }
            }
        });
        queryProvinces();
    }

    /**
     * query all provinces, first from database, then from network
     */
    private void queryProvinces() {
        mProvinceList = mCoolWeatherDB.loadProvinces();
        if (mProvinceList.size() > 0) {
            mDataList.clear();
            for (Province province : mProvinceList) {
                mDataList.add(province.getProvinceName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mTitleText.setText("China");
            mCurrentLevel = LEVEL_PROVINCE;
        } else {
            queryFromServer(null, "province");
        }
    }

    /**
     * query all cities, first from database, then from network
     */
    private void queryCities() {
        mCityList = mCoolWeatherDB.loadCities(mSelectedProvince.getId());
        if (mCityList.size() > 0) {
            mDataList.clear();
            for (City city : mCityList) {
                mDataList.add(city.getCityName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mTitleText.setText(mSelectedProvince.getProvinceName());
            mCurrentLevel = LEVEL_CITY;
        } else {
            queryFromServer(mSelectedProvince.getProvinceCode(), "city");
        }
    }

    /**
     * query all counties, first from database, then from network
     */
    private void queryCounties() {
        mCountyList = mCoolWeatherDB.loadCounties(mSelectedCity.getId());
        if (mCountyList.size() > 0) {
            mDataList.clear();
            for (County county : mCountyList) {
                mDataList.add(county.getCountyName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mTitleText.setText(mSelectedCity.getCityName());
            mCurrentLevel = LEVEL_COUNTY;
        } else {
            queryFromServer(mSelectedCity.getCityCode(), "county");
        }
    }

    /**
     * query data from server with type
     * @param code  null, province_code, city_code
     * @param type  province, city, county
     */
    private void queryFromServer(final String code, final String type) {
        String address;
        if (!TextUtils.isEmpty(code)) {
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
        } else {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }

        showProgressDialog();

        HttpUtils.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result = false;
                // resolve data from HttpResponse and save to local database
                if ("province".equals(type)) {
                    result = Utility.handleProvincesResponse(mCoolWeatherDB, response);
                } else if ("city".endsWith(type)) {
                    result = Utility.handleCitiesResponse(mCoolWeatherDB, response, mSelectedProvince.getId());
                } else if ("county".endsWith(type)) {
                    result = Utility.handleCountiesResponse(mCoolWeatherDB, response, mSelectedCity.getId());
                }

                // load data from database and update view with them
                if (result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this, "failed loading", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("loading...");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    private void closeProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if (mCurrentLevel == LEVEL_COUNTY) {
            queryCities();
        } else if (mCurrentLevel == LEVEL_CITY) {
            queryProvinces();
        } else {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeProgressDialog();
    }
}
