package com.santosh.stockhawk.fragment;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.santosh.stockhawk.R;
import com.santosh.stockhawk.adapter.Utils;
import com.santosh.stockhawk.service.HistoricalData;
import com.santosh.stockhawk.service.StockMetaData;

import java.util.ArrayList;

public class StockDetailFragment extends Fragment implements HistoricalData.HistoricalDataCallback {

    private HistoricalData historicalData;
    private LineChart lineChart;
    private LinearLayout linearLayout;
    private String symbol = "";
    private Context context;
    private Bundle bundle;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.stock_detail_fragment, container, false);
        bundle = getArguments();
        bindData(root);
        historicalData = new HistoricalData(context, this);
        historicalData.getHistoricalData(symbol);
        return root;
    }

    private void bindData(View root) {
        lineChart = (LineChart) root.findViewById(R.id.lineChart_activity_line_graph);
        linearLayout = (LinearLayout) root.findViewById(R.id.ll_activity_line_graph);
        TextView tvCurrency = (TextView) root.findViewById(R.id.currency);
        TextView tvStockSymbol = (TextView) root.findViewById(R.id.stocksymbol);
        TextView tvYearHigh = (TextView) root.findViewById(R.id.yearhigh);
        TextView tvYearLow = (TextView) root.findViewById(R.id.yearlow);
        TextView tvDayLow = (TextView) root.findViewById(R.id.daylow);
        TextView tvDayHigh = (TextView) root.findViewById(R.id.dayhigh);
        TextView tvStockName = (TextView) root.findViewById(R.id.stock_name);
        TextView tvLastTradeDate = (TextView) root.findViewById(R.id.last_trade_date);

        symbol = bundle.getString("symbol_name");

        tvStockSymbol.setText(bundle.getString("symbol_name"));
        tvStockName.setText(bundle.getString("name"));
        tvCurrency.setText(bundle.getString("currency"));
        tvLastTradeDate.setText(bundle.getString("lasttradedate"));
        tvDayLow.setText(bundle.getString("daylow"));
        tvDayHigh.setText(bundle.getString("dayhigh"));
        tvYearLow.setText(bundle.getString("yearlow"));
        tvYearHigh.setText(bundle.getString("yearhigh"));
    }

    @Override
    public void onSuccess(ArrayList<StockMetaData> sp) {

        ArrayList<StockMetaData> stockListData = sp;

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> xvalues = new ArrayList<>();

        for (int i = 0; i < stockListData.size(); i++) {

            StockMetaData stockMetaData = stockListData.get(i);
            double yValue = stockMetaData.close;

            xvalues.add(Utils.convertDate(stockMetaData.date));
            entries.add(new Entry((float) yValue, i));
        }

        XAxis xAxis = lineChart.getXAxis();
        // xAxis.setLabelsToSkip(5);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(12f);
        xAxis.setTextColor(Color.rgb(182, 182, 182));

        YAxis left = lineChart.getAxisLeft();
        left.setEnabled(true);
        left.setLabelCount(10, true);
        left.setTextColor(Color.rgb(182, 182, 182));

        lineChart.getAxisRight().setEnabled(false);
        lineChart.getLegend().setTextSize(16f);
        lineChart.setDrawGridBackground(true);
        lineChart.setGridBackgroundColor(Color.rgb(25, 118, 210));
        lineChart.setDescriptionColor(Color.WHITE);
        lineChart.setDescription("Last 12 Months Stock Comparison");

        String name = getResources().getString(R.string.stock);
        LineDataSet dataSet = new LineDataSet(entries, name);
        LineData lineData = new LineData(xvalues, dataSet);

        lineChart.animateX(2500);
        lineChart.setData(lineData);

    }

    @Override
    public void onFailure() {
        String errorMessage = "";

        @HistoricalData.HistoricalDataStatuses
        int status = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(getString(R.string.historicalDataStatus), -1);

        switch (status) {
            case HistoricalData.STATUS_ERROR_JSON:
                errorMessage += getString(R.string.data_error_json);
                break;
            case HistoricalData.STATUS_ERROR_NO_NETWORK:
                errorMessage += getString(R.string.data_no_internet);
                break;
            case HistoricalData.STATUS_ERROR_PARSE:
                errorMessage += getString(R.string.data_error_parse);
                break;
            case HistoricalData.STATUS_ERROR_UNKNOWN:
                errorMessage += getString(R.string.data_unknown_error);
                break;
            case HistoricalData.STATUS_ERROR_SERVER:
                errorMessage += getString(R.string.data_server_down);
                break;
            case HistoricalData.STATUS_OK:
                errorMessage += getString(R.string.data_no_error);
                break;
            default:
                break;
        }

        final Snackbar snackbar = Snackbar
                .make(linearLayout, getString(R.string.no_data_show) + errorMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        historicalData.getHistoricalData(symbol);
                    }
                })
                .setActionTextColor(Color.GREEN);

        View subview = snackbar.getView();
        TextView tv = (TextView) subview.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.RED);
        snackbar.show();
    }
}
