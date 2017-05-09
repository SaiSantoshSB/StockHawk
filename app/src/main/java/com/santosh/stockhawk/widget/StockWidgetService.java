package com.santosh.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.santosh.stockhawk.R;
import com.santosh.stockhawk.data.StockContract;
import com.santosh.stockhawk.data.StockProvider;


public class StockWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockRVFactory(this.getApplicationContext(), intent);
    }

    public class StockRVFactory implements RemoteViewsFactory {

        private Context context;
        private Cursor cursor;

        StockRVFactory(Context context, Intent intent) {
            this.context = context;
            intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        @Override
        public void onCreate() {
            cursor = getContentResolver().query(
                    StockProvider.Quotes.CONTENT_URI,
                    new String[]{StockContract._ID, //0
                            StockContract.SYMBOL, //1
                            StockContract.BID_PRICE, //2
                            StockContract.CHANGE, //3
                            StockContract.IS_UP,//4
                            StockContract.NAME,//5
                            StockContract.CURRENCY,//6
                            StockContract.LAST_TRADE_DATE,//7
                            StockContract.DAY_LOW,//8
                            StockContract.DAY_HIGH,//9
                            StockContract.YEAR_LOW,//10
                            StockContract.YEAR_HIGH},//11
                    StockContract.IS_CURRENT + " = ?",
                    new String[]{"1"},
                    null
            );
        }

        @Override
        public void onDataSetChanged() {
            cursor = getContentResolver().query(
                    StockProvider.Quotes.CONTENT_URI,
                    new String[]{StockContract._ID, //0
                            StockContract.SYMBOL, //1
                            StockContract.BID_PRICE, //2
                            StockContract.CHANGE, //3
                            StockContract.IS_UP,//4
                            StockContract.NAME,//5
                            StockContract.CURRENCY,//6
                            StockContract.LAST_TRADE_DATE,//7
                            StockContract.DAY_LOW,//8
                            StockContract.DAY_HIGH,//9
                            StockContract.YEAR_LOW,//10
                            StockContract.YEAR_HIGH}, //13
                    StockContract.IS_CURRENT + " = ?",
                    new String[]{"1"},
                    null
            );
        }

        @Override
        public void onDestroy() {
            if (this.cursor != null)
                this.cursor.close();
        }

        @Override
        public int getCount() {
            return (this.cursor != null) ? this.cursor.getCount() : 0;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews remoteViews = new RemoteViews(this.context.getPackageName(), R.layout.stock_recycler_item);
            if (this.cursor.moveToPosition(position)) {
                String symbol = cursor.getString(1);
                remoteViews.setTextViewText(R.id.stock, symbol);
                remoteViews.setTextViewText(R.id.price, cursor.getString(2));
                remoteViews.setTextViewText(R.id.change, cursor.getString(3));
                String name = cursor.getString(5);
                String currency = cursor.getString(6);
                String lasttradedate = cursor.getString(7);
                String daylow = cursor.getString(8);
                String dayhigh = cursor.getString(9);
                String yearlow = cursor.getString(10);
                String yearhigh = cursor.getString(11);
                if (cursor.getInt(4) == 1) {
                    remoteViews.setInt(R.id.change, "setBackgroundResource",
                            R.drawable.percent_background_green);
                } else {
                    remoteViews.setInt(R.id.change, "setBackgroundResource",
                            R.drawable.percent_background_red);
                }
                Bundle extras = new Bundle();
                extras.putString(StockWidgetProvider.EXTRA_SYMBOL, symbol);
                extras.putString(StockWidgetProvider.EXTRA_NAME, name);
                extras.putString(StockWidgetProvider.EXTRA_CURRENCY, currency);
                extras.putString(StockWidgetProvider.EXTRA_LASTTRADEDATE, lasttradedate);
                extras.putString(StockWidgetProvider.EXTRA_DAYLOW, daylow);
                extras.putString(StockWidgetProvider.EXTRA_DAYHIGH, dayhigh);
                extras.putString(StockWidgetProvider.EXTRA_YEARLOW, yearlow);
                extras.putString(StockWidgetProvider.EXTRA_YEARHIGH, yearhigh);
                Intent fillInIntent = new Intent();
                fillInIntent.putExtras(extras);
                remoteViews.setOnClickFillInIntent(R.id.llParent, fillInIntent);
            }
            return remoteViews;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return this.cursor.getInt(0);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
