package com.santosh.stockhawk.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

@Database(version = StockDatabase.VERSION)
class StockDatabase {
    static final int VERSION = 1;
    @Table(StockContract.class)
    static final String STOCKS = "stocks";

    private StockDatabase() {
    }
}
