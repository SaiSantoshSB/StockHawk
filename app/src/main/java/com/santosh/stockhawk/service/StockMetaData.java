package com.santosh.stockhawk.service;

import android.os.Parcel;
import android.os.Parcelable;


public class StockMetaData implements Parcelable {

    public static final Creator<StockMetaData> CREATOR = new Creator<StockMetaData>() {
        @Override
        public StockMetaData createFromParcel(Parcel in) {
            return new StockMetaData(in);
        }

        @Override
        public StockMetaData[] newArray(int size) {
            return new StockMetaData[size];
        }
    };
    public String date;
    public double close;

    private StockMetaData(Parcel in) {
        date = in.readString();
        close = in.readDouble();
    }

    StockMetaData(String date, double close) {
        this.date = date;
        this.close = close;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(date);
        dest.writeDouble(close);
    }
}