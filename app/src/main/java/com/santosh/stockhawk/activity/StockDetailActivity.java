package com.santosh.stockhawk.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.santosh.stockhawk.R;
import com.santosh.stockhawk.fragment.StockDetailFragment;

public class StockDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        setupAppBar();
        StockDetailFragment detailFragment = new StockDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("data", getIntent().getExtras().getParcelable("data"));
        detailFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.stocks_detail_container, detailFragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void setupAppBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Stock Hawk");
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
