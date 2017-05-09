package com.santosh.stockhawk.fragment;


import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.santosh.stockhawk.R;
import com.santosh.stockhawk.activity.StockDetailActivity;
import com.santosh.stockhawk.activity.StocksActivity;
import com.santosh.stockhawk.adapter.RecyclerViewItemClickListener;
import com.santosh.stockhawk.adapter.StockCursorAdapter;
import com.santosh.stockhawk.adapter.Utils;
import com.santosh.stockhawk.data.StockContract;
import com.santosh.stockhawk.data.StockProvider;
import com.santosh.stockhawk.service.StockIntentService;
import com.santosh.stockhawk.service.StockTaskService;
import com.santosh.stockhawk.touch_events.SimpleItemTouchHelperCallback;
import com.santosh.stockhawk.widget.StockWidgetProvider;

public class StocksFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener, View.OnClickListener, android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {

    int symbolColumnIndex, nameColoumIndex, currencyColoumIndex, lasttradedateColoumIndex,
            daylowColoumIndex, dayhighColoumIndex, yearlowColoumIndex, yearhighColoumIndex;
    private View errorLayout;
    private FloatingActionButton addStock;
    private Context context;
    private SwipeRefreshLayout swipeRefreshLayout;
    private StockCursorAdapter stockAdapter;
    private Cursor mCursor;
    private RecyclerView stockRecyclerView;
    private Intent mServiceIntent;
    private int SYMBOL_SEARCH_QUERY_TAG;
    private boolean mTwoPane;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stocks_fragment, container, false);
        mTwoPane = getArguments().getBoolean("mTwoPane");
        initView(view);
        onRefresh();
        setHasOptionsMenu(true);
        boolean isConnected = Utils.isNetworkAvailable(context);
        mServiceIntent = new Intent(context, StockIntentService.class);
        if (savedInstanceState == null) {
            mServiceIntent.putExtra("tag", "init");
            if (isConnected) {
                context.startService(mServiceIntent);
            } else {
                setLayout();
            }
        }
        int CURSOR_LOADER_ID = 2546;
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
        startPeriodicTask();
        return view;
    }

    private void startPeriodicTask() {
        if (Utils.isNetworkAvailable(context)) {
            long period = 3600L;
            long flex = 10L;
            String periodicTag = "periodic";

            PeriodicTask periodicTask = new PeriodicTask.Builder()
                    .setService(StockTaskService.class)
                    .setPeriod(period)
                    .setFlex(flex)
                    .setTag(periodicTag)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setRequiresCharging(false)
                    .build();
            GcmNetworkManager.getInstance(context).schedule(periodicTask);

        }
    }

    private void initView(View view) {
        stockRecyclerView = (RecyclerView) view.findViewById(R.id.stocks_recycler_view);
        stockRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);
        errorLayout = view.findViewById(R.id.errorLayout);
        addStock = (FloatingActionButton) view.findViewById(R.id.add_stock);
        addStock.setOnClickListener(this);
        stockAdapter = new StockCursorAdapter(context, null);
        stockRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(context,
                new RecyclerViewItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {
                        if (mCursor.moveToPosition(position)) {
                            Intent intent = new Intent(getActivity(), StockDetailActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("symbol_name", mCursor.getString(symbolColumnIndex));
                            bundle.putString("name", mCursor.getString(nameColoumIndex));
                            bundle.putString("currency", mCursor.getString(currencyColoumIndex));
                            bundle.putString("lasttradedate", mCursor.getString(lasttradedateColoumIndex));
                            bundle.putString("daylow", mCursor.getString(daylowColoumIndex));
                            bundle.putString("dayhigh", mCursor.getString(dayhighColoumIndex));
                            bundle.putString("yearlow", mCursor.getString(yearlowColoumIndex));
                            bundle.putString("yearhigh", mCursor.getString(yearhighColoumIndex));
                            if (mTwoPane) {
                                StockDetailFragment fragment = new StockDetailFragment();
                                fragment.setArguments(bundle);
                                ((StocksActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.stocks_detail_container, fragment).commit();
                            } else {
                                intent.putExtra("data", bundle);
                                context.startActivity(intent);
                            }
                        }
                    }
                }));

        stockRecyclerView.setAdapter(stockAdapter);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(stockAdapter);
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(stockRecyclerView);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.context = null;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        AppCompatActivity parentActivity = (AppCompatActivity) context;
        if (parentActivity != null && parentActivity.getSupportActionBar() != null) {
            parentActivity.getSupportActionBar().setHomeButtonEnabled(false);
            parentActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_stock_change:
                Utils.showPercent = !Utils.showPercent;
                context.getContentResolver().notifyChange(StockProvider.Quotes.CONTENT_URI, null);
                setDisplayModeMenuItemIcon(item);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setDisplayModeMenuItemIcon(MenuItem item) {
        if (Utils.showPercent) {
            item.setIcon(R.drawable.ic_percentage);
        } else {
            item.setIcon(R.drawable.ic_dollar);
        }
    }


    private void setLayout() {
        if (!networkUp() && stockAdapter.getItemCount() == 0) {
            swipeRefreshLayout.setRefreshing(false);
            errorLayout.setVisibility(View.VISIBLE);
            ((TextView) errorLayout.findViewById(R.id.errorText)).setText(R.string.no_network);
            ((ImageView) errorLayout.findViewById(R.id.errorImage)).setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_wifi_off_black_48dp));
        } else if (!networkUp()) {
            swipeRefreshLayout.setRefreshing(false);
            errorLayout.setVisibility(View.VISIBLE);
            ((TextView) errorLayout.findViewById(R.id.errorText)).setText(R.string.no_network);
            ((ImageView) errorLayout.findViewById(R.id.errorImage)).setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_wifi_off_black_48dp));
        } else if (stockAdapter.getItemCount() == 0) {
            swipeRefreshLayout.setRefreshing(false);
            errorLayout.setVisibility(View.VISIBLE);
            addStock.setVisibility(View.VISIBLE);
            stockRecyclerView.setVisibility(View.GONE);
            ((TextView) errorLayout.findViewById(R.id.errorText)).setText(R.string.no_stocks);
            ((ImageView) errorLayout.findViewById(R.id.errorImage)).setImageDrawable(ContextCompat.getDrawable(context, R.drawable.no_stocks));
        } else {
            stockRecyclerView.setVisibility(View.VISIBLE);
            addStock.setVisibility(View.VISIBLE);
            errorLayout.setVisibility(View.GONE);
        }
    }

    void addStock(String symbol) {
        if (symbol != null && !symbol.isEmpty()) {
            if (networkUp()) {
                swipeRefreshLayout.setRefreshing(true);
                SymbolQuery symbolQuery = new SymbolQuery(context.getContentResolver(), symbol);
                symbolQuery.startQuery(SYMBOL_SEARCH_QUERY_TAG,
                        null,
                        StockProvider.Quotes.CONTENT_URI,
                        new String[]{StockContract.SYMBOL},
                        StockContract.SYMBOL + "=?",
                        new String[]{symbol},
                        null);
            } else {
                swipeRefreshLayout.setRefreshing(false);
                String message = getString(R.string.toast_stock_added_no_connectivity, symbol);
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(context,
                StockProvider.Quotes.CONTENT_URI,
                new String[]{StockContract._ID, StockContract.SYMBOL, StockContract.BID_PRICE,
                        StockContract.PERCENT_CHANGE, StockContract.CHANGE, StockContract.IS_UP, StockContract.NAME, StockContract.CURRENCY,
                        StockContract.LAST_TRADE_DATE, StockContract.DAY_LOW, StockContract.DAY_HIGH, StockContract.YEAR_LOW, StockContract.YEAR_HIGH,
                },
                StockContract.IS_CURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        swipeRefreshLayout.setRefreshing(false);
        stockAdapter.swapCursor(data);
        mCursor = data;
        symbolColumnIndex = mCursor.getColumnIndex(StockContract.SYMBOL);
        nameColoumIndex = mCursor.getColumnIndex(StockContract.NAME);
        currencyColoumIndex = mCursor.getColumnIndex(StockContract.CURRENCY);
        lasttradedateColoumIndex = mCursor.getColumnIndex(StockContract.LAST_TRADE_DATE);
        daylowColoumIndex = mCursor.getColumnIndex(StockContract.DAY_LOW);
        dayhighColoumIndex = mCursor.getColumnIndex(StockContract.DAY_HIGH);
        yearlowColoumIndex = mCursor.getColumnIndex(StockContract.YEAR_LOW);
        yearhighColoumIndex = mCursor.getColumnIndex(StockContract.YEAR_HIGH);
        setLayout();
        updateStocksWidget();
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        stockAdapter.swapCursor(null);
        swipeRefreshLayout.setRefreshing(false);
        setLayout();
    }

    private void updateStocksWidget() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
        int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, StockWidgetProvider.class));
        if (ids.length > 0) {
            appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.lv_stock_widget_layout);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.add_stock) {
            final Dialog dialog = new Dialog(context);
            View view = LayoutInflater.from(context).inflate(R.layout.add_stock_layout, null);
            final EditText stockName = (EditText) view.findViewById(R.id.stock_name);
            Button cancel = (Button) view.findViewById(R.id.cancel_button);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            Button add = (Button) view.findViewById(R.id.add_button);
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    addStock(stockName.getText().toString());
                }
            });
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            if (dialog.getWindow() != null && dialog.getWindow().getAttributes() != null) {
                lp.copyFrom(dialog.getWindow().getAttributes());
            }
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.setContentView(view);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            dialog.getWindow().setAttributes(lp);
        }
    }

    @Override
    public void onRefresh() {

    }

    private class SymbolQuery extends AsyncQueryHandler {

        String symbol;

        SymbolQuery(ContentResolver cr, String symbol) {
            super(cr);
            this.symbol = symbol;
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (token == SYMBOL_SEARCH_QUERY_TAG) {
                if (cursor != null && cursor.getCount() != 0) {
                    setLayout();
                } else {
                    mServiceIntent.putExtra("tag", "add");
                    mServiceIntent.putExtra("symbol", this.symbol);
                    context.startService(mServiceIntent);
                    updateStocksWidget();
                }
            }
        }
    }
}
