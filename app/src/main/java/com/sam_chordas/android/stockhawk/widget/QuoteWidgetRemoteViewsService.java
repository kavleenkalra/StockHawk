package com.sam_chordas.android.stockhawk.widget;

import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by kakalra on 5/28/2016.
 */
public class QuoteWidgetRemoteViewsService extends RemoteViewsService
{
    public final String LOG_TAG=QuoteWidgetRemoteViewsService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent)
    {
        return new RemoteViewsFactory()
        {
            private Cursor data=null;
            @Override
            public void onCreate()
            {
                //Nothing to do.
            }

            @Override
            public void onDataSetChanged()
            {
                if (data!=null)
                    data.close();
                final long identityToken= Binder.clearCallingIdentity();
                data=getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                        new String[]{
                            QuoteColumns._ID,
                            QuoteColumns.SYMBOL,
                            QuoteColumns.BIDPRICE,
                            QuoteColumns.PERCENT_CHANGE,
                            QuoteColumns.CHANGE,
                            QuoteColumns.ISUP
                        },
                        QuoteColumns.ISCURRENT + " = ?",
                        new String[]{"1"},
                        null);

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy()
            {
                if(data!=null)
                {
                    data.close();
                    data=null;
                }
            }

            @Override
            public int getCount()
            {
                return data==null?0:data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position)
            {
                if(position== AdapterView.INVALID_POSITION || data==null || !data.moveToPosition(position))
                {
                    return null;
                }
                RemoteViews views=new RemoteViews(getPackageName(),R.layout.widget_quote_list_item_detail);
                String symbol;
                String change;

                symbol=data.getString(1);
                change=data.getString(4);

                if(data.getInt(data.getColumnIndex("is_up"))==1)
                {
                    views.setInt(R.id.widget_price_change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                }
                else
                {
                    views.setInt(R.id.widget_price_change,"setBackgroundResource",R.drawable.percent_change_pill_red);
                }

                views.setTextViewText(R.id.widget_stock_symbol,symbol);
                views.setContentDescription(R.id.widget_stock_symbol,getApplicationContext().getString(R.string.talkback_stock_name)+symbol);

                views.setTextViewText(R.id.widget_price_change,change);
                views.setContentDescription(R.id.widget_price_change,getApplicationContext().getString(R.string.talkback_change)+change);

                Bundle extras=new Bundle();
                extras.putString("SYMBOL",symbol);

                final Intent fillInIntent=new Intent();
                fillInIntent.putExtras(extras);
                views.setOnClickFillInIntent(R.id.widget_list_item,fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView()
            {
                return new RemoteViews(getPackageName(), R.layout.widget_quote_list_item_detail);
            }

            @Override
            public int getViewTypeCount()
            {
                return 1;
            }

            @Override
            public long getItemId(int position)
            {
                if(data!=null && data.moveToPosition(position))
                {
                    return data.getLong(0);
                }
                return position;
            }

            @Override
            public boolean hasStableIds()
            {
                return true;
            }
        };
    }
}
