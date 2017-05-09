package com.santosh.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.santosh.stockhawk.R;
import com.santosh.stockhawk.activity.StockDetailActivity;


public class StockWidgetProvider extends AppWidgetProvider {

    public static final String INTENT_ACTION = "com.santosh.stockhawk.widget.StockWidgetProvider.INTENT_ACTION";
    public static final String EXTRA_SYMBOL = "com.santosh.stockhawk.widget.StockWidgetProvider.EXTRA_SYMBOL";
    public static final String EXTRA_NAME = "com.santosh.stockhawk.widget.StockWidgetProvider.EXTRA_NAME";
    public static final String EXTRA_CURRENCY = "com.santosh.stockhawk.widget.StockWidgetProvider.EXTRA_CURRENCY";
    public static final String EXTRA_LASTTRADEDATE = "com.santosh.stockhawk.widget.StockWidgetProvider.EXTRA_LASTTRADEDATE";
    public static final String EXTRA_DAYLOW = "com.santosh.stockhawk.widget.StockWidgetProvider.EXTRA_DAYLOW";
    public static final String EXTRA_DAYHIGH = "com.santosh.stockhawk.widget.StockWidgetProvider.EXTRA_DAYHIGH";
    public static final String EXTRA_YEARLOW = "com.santosh.stockhawk.widget.StockWidgetProvider.EXTRA_YEARLOW";
    public static final String EXTRA_YEARHIGH = "com.santosh.stockhawk.widget.StockWidgetProvider.EXTRA_YEARHIGH";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(INTENT_ACTION)) {
            Intent showHistoricalData = new Intent(context, StockDetailActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("symbol_name", intent.getStringExtra(EXTRA_SYMBOL));
            bundle.putString("name", intent.getStringExtra(EXTRA_NAME));
            bundle.putString("currency", intent.getStringExtra(EXTRA_CURRENCY));
            bundle.putString("lasttradedate", intent.getStringExtra(EXTRA_LASTTRADEDATE));
            bundle.putString("daylow", intent.getStringExtra(EXTRA_DAYLOW));
            bundle.putString("dayhigh", intent.getStringExtra(EXTRA_DAYHIGH));
            bundle.putString("yearlow", intent.getStringExtra(EXTRA_YEARLOW));
            bundle.putString("yearhigh", intent.getStringExtra(EXTRA_YEARHIGH));
            showHistoricalData.putExtra("data", bundle);
            showHistoricalData.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(showHistoricalData);
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            Intent intent = new Intent(context, StockWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.stock_widget_layout);
            remoteViews.setRemoteAdapter(appWidgetId, R.id.lv_stock_widget_layout, intent);
            remoteViews.setEmptyView(R.id.lv_stock_widget_layout, R.id.tv_empty_stocks_widget_layout);
            Intent openSymbol = new Intent(context, StockWidgetProvider.class);
            openSymbol.setAction(StockWidgetProvider.INTENT_ACTION);
            openSymbol.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, openSymbol,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setPendingIntentTemplate(R.id.lv_stock_widget_layout, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
