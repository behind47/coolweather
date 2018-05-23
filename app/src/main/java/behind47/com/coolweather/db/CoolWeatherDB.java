package behind47.com.coolweather.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import behind47.com.coolweather.model.City;
import behind47.com.coolweather.model.County;
import behind47.com.coolweather.model.Province;

/**
 * Created by behind47 on 2018/5/22.
 */
public class CoolWeatherDB {

    /**
     * database name
     */
    public static final String sDB_NAME = "cool_weather";

    /**
     * database version
     */
    public static final int sVERSION = 1;

    private static volatile CoolWeatherDB sCoolWeatherDB;

    private SQLiteDatabase db;

    public CoolWeatherDB(Context context) {
        CoolWeatherOpenHelper dbHelper = new CoolWeatherOpenHelper(context, sDB_NAME, null, sVERSION);
        db = dbHelper.getWritableDatabase();
    }

    /**
     * get CoolWeatherDB Instance
     *
     * @param context
     * @return
     */
    public static CoolWeatherDB getInstance(Context context) {
        if (sCoolWeatherDB == null) {
            synchronized (CoolWeatherDB.class) {
                if (sCoolWeatherDB == null) {
                    sCoolWeatherDB = new CoolWeatherDB(context);
                }
            }
        }
        return sCoolWeatherDB;
    }

    /**
     * save Province Instance into database
     *
     * @param province
     */
    public void saveProvince(Province province) {
        if (province != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("province_name", province.getProvinceName());
            contentValues.put("province_code", province.getProvinceCode());
            db.insert("Province", null, contentValues);
        }
    }

    /**
     * read info of Provinces from database
     * @return
     */
    public List<Province> loadProvinces() {
        List<Province> list = new ArrayList<>();
        Cursor cursor = db.query("Province", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Province province = new Province();
                province.setId(cursor.getInt(cursor.getColumnIndex("id")));
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
                list.add(province);
            } while (cursor.moveToNext());
        }
        return list;
    }

    /**
     * save City Instance into database
     * @param city
     */
    public void saveCity(City city) {
        if (city != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("city_name", city.getCityName());
            contentValues.put("city_code", city.getCityCode());
            contentValues.put("province_id", city.getProvinceId());
            db.insert("City", null, contentValues);
        }
    }

    /**
     * read list of Cities from database
     * @param provinceId
     * @return
     */
    public List<City> loadCities(int provinceId) {
        List<City> list = new ArrayList<>();
        Cursor cursor = db.query("City", null, "province_id = ?", new String[] {String.valueOf(provinceId)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                City city = new City();
                city.setId(cursor.getInt(cursor.getInt(cursor.getColumnIndex("id"))));
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
                city.setProvinceId(provinceId);
                list.add(city);
            } while (cursor.moveToNext());
        }
        return list;
    }

    /**
     * save County Instance into database
     * @param county
     */
    public void saveCounty(County county) {
        if (county != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("county_name", county.getCountyName());
            contentValues.put("county_code", county.getCountyCode());
            contentValues.put("city_id", county.getCityId());
            db.insert("County", null, contentValues);
        }
    }

    public List<County> loadCounties(int cityId) {
        List<County> list = new ArrayList<>();
        Cursor cursor = db.query("County", null, "city_id = ?", new String[]{String.valueOf(cityId)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                County county = new County();
                county.setId(cursor.getInt(cursor.getColumnIndex("id")));
                county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
                county.setCityId(cityId);
                list.add(county);
            } while (cursor.moveToNext());
        }
        return list;
    }


}
