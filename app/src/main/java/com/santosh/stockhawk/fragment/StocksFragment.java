package com.santosh.stockhawk.fragment;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
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

import com.santosh.stockhawk.R;
import com.santosh.stockhawk.adapter.StockAdapter;
import com.santosh.stockhawk.data.Contract;
import com.santosh.stockhawk.data.PrefUtils;
import com.santosh.stockhawk.sync.QuoteSyncJob;

import static com.santosh.stockhawk.activity.StocksActivity.NO_STOCK;

public class StocksFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private static final int STOCK_LOADER = 0;
    private View errorLayout;
    private FloatingActionButton addStock;
    private Context context;
    private SwipeRefreshLayout swipeRefreshLayout;
    private StockAdapter stockAdapter;
    private BroadcastReceiver noStockReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String symbol = intent.getExtras().getString("symbol");
            PrefUtils.removeStock(context, symbol);
            stockAdapter.notifyDataSetChanged();
            Toast.makeText(StocksFragment.this.context, "No Stock found, Please a enter a valid stock name", Toast.LENGTH_SHORT).show();
        }
    };
    private RecyclerView stockRecyclerView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stocks_fragment, container, false);
        boolean mTwoPane = getArguments().getBoolean("mTwoPane");
        initView(view, mTwoPane);
        onRefresh();
        QuoteSyncJob.initialize(context);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(context).registerReceiver(noStockReceiver, new IntentFilter(NO_STOCK));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(noStockReceiver);
    }

    private void initView(View view, boolean mTwoPane) {
        stockAdapter = new StockAdapter(context, mTwoPane);
        stockRecyclerView = (RecyclerView) view.findViewById(R.id.stocks_recycler_view);
        stockRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        stockRecyclerView.setAdapter(stockAdapter);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);
        errorLayout = view.findViewById(R.id.errorLayout);
        addStock = (FloatingActionButton) view.findViewById(R.id.add_stock);
        addStock.setOnClickListener(this);
        getActivity().getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String symbol = stockAdapter.getSymbolAtPosition(viewHolder.getAdapterPosition());
                PrefUtils.removeStock(context, symbol);
                int i = getActivity().getContentResolver().delete(Contract.Quote.makeUriForStock(symbol), null, null);
                if (PrefUtils.getStocksCount(context) == 0) {
                    setLayout();
                }
            }
        }).attachToRecyclerView(stockRecyclerView);
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
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_stock_change:
                PrefUtils.toggleDisplayMode(context);
                setDisplayModeMenuItemIcon(item);
                stockAdapter.notifyDataSetChanged();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setDisplayModeMenuItemIcon(MenuItem item) {
        if (PrefUtils.getDisplayMode(context)
                .equals(getString(R.string.pref_display_mode_absolute_key))) {
            item.setIcon(R.drawable.ic_percentage);
        } else {
            item.setIcon(R.drawable.ic_dollar);
        }
    }

    @Override
    public void onRefresh() {
        QuoteSyncJob.syncImmediately(context);
        setLayout();
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
        } else if (PrefUtils.getStocks(context).size() == 0) {
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
            } else {
                String message = getString(R.string.toast_stock_added_no_connectivity, symbol);
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
            PrefUtils.addStock(context, symbol);
            QuoteSyncJob.syncImmediately(context);
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(context,
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS,
                null, null, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        swipeRefreshLayout.setRefreshing(false);
        stockAdapter.setCursor(data);
        if (errorLayout.getVisibility() == View.VISIBLE) {
            setLayout();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        swipeRefreshLayout.setRefreshing(false);
        stockAdapter.setCursor(null);
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
}
