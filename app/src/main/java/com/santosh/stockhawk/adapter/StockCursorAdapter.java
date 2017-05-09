package com.santosh.stockhawk.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.santosh.stockhawk.R;
import com.santosh.stockhawk.data.StockContract;
import com.santosh.stockhawk.data.StockProvider;
import com.santosh.stockhawk.service.StockTaskService;
import com.santosh.stockhawk.touch_events.ItemTouchHelperAdapter;
import com.santosh.stockhawk.touch_events.ItemTouchHelperViewHolder;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;


public class StockCursorAdapter extends CursorRecyclerViewAdapter<StockCursorAdapter.ViewHolder>
        implements ItemTouchHelperAdapter {

    private final DecimalFormat dollarFormat;
    private final DecimalFormat dollarFormatWithPlus;
    private Context mContext;

    public StockCursorAdapter(Context context, Cursor cursor) {
        super(cursor);
        mContext = context;
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        DecimalFormat percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_recycler_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor) {
        viewHolder.symbol.setText(cursor.getString(cursor.getColumnIndex("symbol")));
        viewHolder.bidPrice.setText(dollarFormat.format(Float.parseFloat(cursor.getString(cursor.getColumnIndex("bid_price")))));
        if (cursor.getInt(cursor.getColumnIndex("is_up")) == 1) {
            viewHolder.change.setBackground(ContextCompat.getDrawable(mContext, R.drawable.percent_background_green));
        } else {
            viewHolder.change.setBackground(ContextCompat.getDrawable(mContext, R.drawable.percent_background_red));
        }
        String change = dollarFormatWithPlus.format(Float.parseFloat(cursor.getString(cursor.getColumnIndex("change"))));
        if (Utils.showPercent) {
            viewHolder.change.setText(cursor.getString(cursor.getColumnIndex("percent_change")));
        } else {
            viewHolder.change.setText(change);
        }
    }

    @Override
    public void onItemDismiss(int position) {
        Cursor c = getCursor();
        c.moveToPosition(position);
        String symbol = c.getString(c.getColumnIndex(StockContract.SYMBOL));
        mContext.getContentResolver().delete(StockProvider.Quotes.withSymbol(symbol), null, null);
        notifyItemRemoved(position);
        if (c.getCount() == 1) {
            if (!Utils.isNetworkAvailable(mContext)) {
                StockTaskService.setStockStatus(mContext, StockTaskService.STATUS_NO_NETWORK);
            } else {
                StockTaskService.setStockStatus(mContext, StockTaskService.STATUS_OK);
            }
        }

    }


    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    class ViewHolder extends RecyclerView.ViewHolder
            implements ItemTouchHelperViewHolder, View.OnClickListener {
        public final TextView symbol;
        public final TextView change;
        final TextView bidPrice;

        ViewHolder(View itemView) {
            super(itemView);
            symbol = (TextView) itemView.findViewById(R.id.stock);
            bidPrice = (TextView) itemView.findViewById(R.id.price);
            change = (TextView) itemView.findViewById(R.id.change);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
