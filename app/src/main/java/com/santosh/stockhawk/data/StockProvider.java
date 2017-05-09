package com.santosh.stockhawk.data;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

@ContentProvider(authority = StockProvider.AUTHORITY, database = StockDatabase.class)
public class StockProvider {
    public static final String AUTHORITY = "com.santosh.stockhawk.data.StockProvider";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    private static Uri buildUri(String... paths) {
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }

    interface Path {
        String STOCKS = "stocks";
    }

    @TableEndpoint(table = StockDatabase.STOCKS)
    public static class Quotes {
        @ContentUri(
                path = Path.STOCKS,
                type = "vnd.android.cursor.dir/quote"
        )
        public static final Uri CONTENT_URI = buildUri(Path.STOCKS);

        @InexactContentUri(
                name = "STOCk_ID",
                path = Path.STOCKS + "/*",
                type = "vnd.android.cursor.item/quote",
                whereColumn = StockContract.SYMBOL,
                pathSegment = 1
        )
        public static Uri withSymbol(String symbol) {
            return buildUri(Path.STOCKS, symbol);
        }
    }
}
