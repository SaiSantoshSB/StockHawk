package com.santosh.stockhawk.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.santosh.stockhawk.R;


public class NoStockFoundBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, context.getString(R.string.not_found), Toast.LENGTH_SHORT).show();
    }
}
