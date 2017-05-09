package com.santosh.stockhawk.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.santosh.stockhawk.StockApplication;
import com.santosh.stockhawk.R;
import com.santosh.stockhawk.fragment.StockDetailFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

public class HistoricalData {

    public static final int STATUS_OK = 0;
    public static final int STATUS_ERROR_JSON = 1;
    public static final int STATUS_ERROR_SERVER = 2;
    public static final int STATUS_ERROR_PARSE = 3;
    public static final int STATUS_ERROR_NO_NETWORK = 4;
    public static final int STATUS_ERROR_UNKNOWN = 5;
    private static final String TAG = HistoricalData.class.getSimpleName();
    private static final String JSON_SERIES = "series";
    private static final String JSON_DATE = "Date";
    private static final String JSON_CLOSE = "close";
    private HistoricalDataCallback callback;
    private ArrayList<StockMetaData> stockListData;
    private Context context;

    public HistoricalData(Context context, StockDetailFragment object) {
        this.context = context;
        this.callback = object;
    }

    public void getHistoricalData(String symbol) {

        String BASE_URL = "http://chartapi.finance.yahoo.com/instrument/1.0/";
        String END_URL = "/chartdata;type=quote;range=1y/json";
        String URL = BASE_URL + symbol + END_URL;

        final StringRequest request = new StringRequest(
                Request.Method.GET,
                URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null) {
                            stockListData = new ArrayList<>();
                            try {
                                String json = response.substring(response.indexOf("(") + 1, response.lastIndexOf(")"));
                                JSONObject mainObject = new JSONObject(json);
                                JSONArray series_data = mainObject.getJSONArray(JSON_SERIES);
                                for (int i = 0; i < series_data.length(); i += 10) {
                                    JSONObject singleObject = series_data.getJSONObject(i);
                                    String date = singleObject.getString(JSON_DATE);
                                    double close = singleObject.getDouble(JSON_CLOSE);
                                    stockListData.add(new StockMetaData(date, close));
                                }
                                if (callback != null) {
                                    setHistoricalDataStatus(STATUS_OK);
                                    callback.onSuccess(stockListData);
                                }
                            } catch (JSONException e) {
                                setHistoricalDataStatus(STATUS_ERROR_JSON);
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            setHistoricalDataStatus(STATUS_ERROR_NO_NETWORK);
                        } else if (error instanceof ServerError) {
                            setHistoricalDataStatus(STATUS_ERROR_SERVER);
                        } else if (error instanceof NetworkError) {
                            setHistoricalDataStatus(STATUS_ERROR_UNKNOWN);
                        } else if (error instanceof ParseError) {
                            setHistoricalDataStatus(STATUS_ERROR_PARSE);
                        }

                        if (callback != null) {
                            callback.onFailure();
                        }
                    }
                }
        );
        StockApplication.getInstance().addToRequestQueue(request, TAG);
    }

    private void setHistoricalDataStatus(@HistoricalDataStatuses int status) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(context.getString(R.string.historicalDataStatus), status);
        editor.apply();
    }


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATUS_OK, STATUS_ERROR_JSON, STATUS_ERROR_NO_NETWORK, STATUS_ERROR_PARSE
            , STATUS_ERROR_SERVER, STATUS_ERROR_UNKNOWN})
    public @interface HistoricalDataStatuses {
    }

    public interface HistoricalDataCallback {
        void onSuccess(ArrayList<StockMetaData> list);
        void onFailure();
    }
}