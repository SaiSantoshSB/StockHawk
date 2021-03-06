package com.santosh.stockhawk.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

public class StockContract {
    @DataType(DataType.Type.INTEGER)
    @PrimaryKey
    @AutoIncrement
    public static final String _ID = "_id";
    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String SYMBOL = "symbol";
    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String PERCENT_CHANGE = "percent_change";
    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String CHANGE = "change";
    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String BID_PRICE = "bid_price";
    @DataType(DataType.Type.TEXT)
    public static final String CREATED = "created";
    @DataType(DataType.Type.INTEGER)
    @NotNull
    public static final String IS_UP = "is_up";
    @DataType(DataType.Type.INTEGER)
    @NotNull
    public static final String IS_CURRENT = "is_current";
    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String NAME = "name";
    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String CURRENCY = "currency";
    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String LAST_TRADE_DATE = "lasttradedate";
    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String DAY_LOW = "daylow";
    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String DAY_HIGH = "dayhigh";
    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String YEAR_LOW = "yearlow";
    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String YEAR_HIGH = "yearhigh";
}
