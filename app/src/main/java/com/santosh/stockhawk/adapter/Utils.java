package com.santosh.stockhawk.adapter;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.santosh.stockhawk.data.StockContract;
import com.santosh.stockhawk.data.StockProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class Utils {

    public static boolean showPercent = true;

    public static ArrayList<ContentProviderOperation> quoteJsonToContentVals(String JSON) throws JSONException {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject;
        JSONArray resultsArray;
        ContentProviderOperation cpo;

        jsonObject = new JSONObject(JSON);
        if (jsonObject.length() != 0) {
            jsonObject = jsonObject.getJSONObject("query");
            int count = Integer.parseInt(jsonObject.getString("count"));
            if (count == 1) {
                jsonObject = jsonObject.getJSONObject("results")
                        .getJSONObject("quote");
                cpo = buildBatchOperation(jsonObject);
                if (cpo != null) {
                    batchOperations.add(cpo);
                }

            } else {
                resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                if (resultsArray != null && resultsArray.length() != 0) {
                    for (int i = 0; i < resultsArray.length(); i++) {
                        jsonObject = resultsArray.getJSONObject(i);
                        cpo = buildBatchOperation(jsonObject);
                        if (cpo != null) {
                            batchOperations.add(cpo);
                        }

                    }
                }
            }
        }

        return batchOperations;
    }

    private static String truncateBidPrice(String bidPrice) {
        bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
        return bidPrice;
    }

    private static String truncateChange(String change, boolean isPercentChange) {
        String weight = change.substring(0, 1);
        String ampersand = "";
        if (isPercentChange) {
            ampersand = change.substring(change.length() - 1, change.length());
            change = change.substring(0, change.length() - 1);
        }
        change = change.substring(1, change.length());
        double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
        change = String.format("%.2f", round);
        StringBuilder changeBuffer = new StringBuilder(change);
        changeBuffer.insert(0, weight);
        changeBuffer.append(ampersand);
        change = changeBuffer.toString();
        return change;
    }


    private static ContentProviderOperation buildBatchOperation(JSONObject jsonObject) throws JSONException {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                StockProvider.Quotes.CONTENT_URI);

        if (!jsonObject.getString("Change").equals("null") && !jsonObject.getString("Bid").equals("null")) {
            String change = jsonObject.getString("Change");
            builder.withValue(StockContract.SYMBOL, jsonObject.getString("symbol"));
            builder.withValue(StockContract.BID_PRICE, truncateBidPrice(jsonObject.getString("Bid")));
            builder.withValue(StockContract.PERCENT_CHANGE, truncateChange(jsonObject.getString("ChangeinPercent"), true));
            builder.withValue(StockContract.CHANGE, truncateChange(change, false));
            builder.withValue(StockContract.IS_CURRENT, 1);
            if (change.charAt(0) == '-') {
                builder.withValue(StockContract.IS_UP, 0);
            } else {
                builder.withValue(StockContract.IS_UP, 1);
            }
            builder.withValue(StockContract.NAME, jsonObject.getString("Name"));
            builder.withValue(StockContract.CURRENCY, jsonObject.getString("Currency"));
            builder.withValue(StockContract.LAST_TRADE_DATE, jsonObject.getString("LastTradeDate"));
            builder.withValue(StockContract.DAY_LOW, jsonObject.getString("DaysLow"));
            builder.withValue(StockContract.DAY_HIGH, jsonObject.getString("DaysHigh"));
            builder.withValue(StockContract.YEAR_LOW, jsonObject.getString("YearLow"));
            builder.withValue(StockContract.YEAR_HIGH, jsonObject.getString("YearHigh"));
        } else {
            return null;
        }
        return builder.build();
    }


    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo availableNetwork = cm.getActiveNetworkInfo();
        return availableNetwork != null && availableNetwork.isConnectedOrConnecting();
    }


    public static String convertDate(String inputDate) {
        return inputDate.substring(6) +
                "/" +
                inputDate.substring(4, 6) +
                "/" +
                inputDate.substring(2, 4);
    }

}