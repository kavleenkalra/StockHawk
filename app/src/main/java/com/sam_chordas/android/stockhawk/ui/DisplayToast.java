package com.sam_chordas.android.stockhawk.ui;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by kakalra on 5/21/2016.
 */
public class DisplayToast implements Runnable
{
    private final Context mContext;
    private final String mText;

    public DisplayToast(Context mContext,String mText)
    {
        this.mContext=mContext;
        this.mText=mText;
    }

    @Override
    public void run()
    {
        Toast.makeText(mContext,mText,Toast.LENGTH_SHORT);
    }
}
