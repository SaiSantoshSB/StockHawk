package com.santosh.stockhawk.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import com.santosh.stockhawk.R;
import com.santosh.stockhawk.fragment.StockDetailFragment;
import com.santosh.stockhawk.fragment.StocksFragment;

public class StocksActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean mTwoPane;
        if (findViewById(R.id.stocks_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.stocks_detail_container, new StockDetailFragment(), "Detail Fragment")
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
        setupAppBar();
        Fragment fragment = new StocksFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("mTwoPane", mTwoPane);
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.stocks_fragment, fragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void setupAppBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Stock Hawk");
        }
    }
}
