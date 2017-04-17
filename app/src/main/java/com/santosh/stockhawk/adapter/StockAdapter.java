package com.santosh.stockhawk.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.santosh.stockhawk.R;
import com.santosh.stockhawk.activity.StockDetailActivity;
import com.santosh.stockhawk.activity.StocksActivity;
import com.santosh.stockhawk.data.Contract;
import com.santosh.stockhawk.data.PrefUtils;
import com.santosh.stockhawk.fragment.StockDetailFragment;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {
    private final DecimalFormat dollarFormatWithPlus;
    private final DecimalFormat dollarFormat;
    private final DecimalFormat percentageFormat;
    private Context context;
    private boolean mTwoPane;
    private Cursor cursor;

    public StockAdapter(Context context, boolean mTwoPane) {
        this.mTwoPane = mTwoPane;
        this.context = context;

        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    public String getSymbolAtPosition(int position) {
        cursor.moveToPosition(position);
        return cursor.getString(Contract.Quote.POSITION_SYMBOL);
    }

    @Override
    public StockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new StockViewHolder(LayoutInflater.from(context).inflate(R.layout.stock_recycler_item, parent, false));
    }

    @Override
    public void onBindViewHolder(StockViewHolder holder, int position) {
        cursor.moveToPosition(position);
        holder.stockName.setText(cursor.getString(Contract.Quote.POSITION_SYMBOL));
        holder.price.setText(dollarFormat.format(cursor.getFloat(Contract.Quote.POSITION_PRICE)));


        float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

        if (rawAbsoluteChange > 0) {
            holder.priceChange.setBackgroundResource(R.drawable.percent_background_green);
        } else {
            holder.priceChange.setBackgroundResource(R.drawable.percent_background_red);
        }

        String change = dollarFormatWithPlus.format(rawAbsoluteChange);
        String percentage = percentageFormat.format(percentageChange / 100);

        if (PrefUtils.getDisplayMode(context)
                .equals(context.getString(R.string.pref_display_mode_absolute_key))) {
            holder.priceChange.setText(change);
        } else {
            holder.priceChange.setText(percentage);
        }
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
        }
        return count;
    }

    class StockViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView stockName;
        private TextView price;
        private TextView priceChange;

        StockViewHolder(View itemView) {
            super(itemView);
            stockName = (TextView) itemView.findViewById(R.id.stock);
            price = (TextView) itemView.findViewById(R.id.price);
            priceChange = (TextView) itemView.findViewById(R.id.change);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int itemPosition = getAdapterPosition();
            if (mTwoPane) {
                StockDetailFragment fragment = new StockDetailFragment();
                Bundle bundle = new Bundle();
                bundle.putString("data", getSymbolAtPosition(itemPosition));
                fragment.setArguments(bundle);
                ((StocksActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.stocks_detail_container, fragment).commit();
            } else {
                Intent intent = new Intent(context, StockDetailActivity.class);
                intent.putExtra("data", getSymbolAtPosition(itemPosition));
                context.startActivity(intent);
            }

        }
    }

}
