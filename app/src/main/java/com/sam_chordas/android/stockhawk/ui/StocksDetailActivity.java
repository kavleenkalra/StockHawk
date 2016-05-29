package com.sam_chordas.android.stockhawk.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by kakalra on 5/21/2016.
 */
public class StocksDetailActivity extends AppCompatActivity
{
    private OkHttpClient client = new OkHttpClient();
    private final String LOG_TAG=StocksDetailActivity.class.getSimpleName();
    private LineChartView lineChartView;

    String fetchData(String url) throws IOException{
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);
        Intent intent=getIntent();
        if(intent!=null && intent.hasExtra("SYMBOL"))
        {
            String symbol=intent.getStringExtra("SYMBOL");
            TextView stockTextView=(TextView)findViewById(R.id.stock_symbol_textview);
            stockTextView.setText(symbol.toUpperCase()+" "+getApplicationContext().getString(R.string.one_month_data));
            stockTextView.setContentDescription(getApplicationContext().getString(R.string.talkback_stock_name)+symbol);

            lineChartView=(LineChartView)findViewById(R.id.linechart);

            FetchStockDataTask myStockTask=new FetchStockDataTask();
            myStockTask.execute(symbol);

        }

    }

    public class FetchStockDataTask extends AsyncTask<String ,Void,double[]>
    {
        double closeQuoteArray[];
        LineSet dataSet=new LineSet();

        @Override
        protected double[] doInBackground(String... params)
        {
            String stockSymbol=params[0];

            StringBuilder urlStringBuilder=new StringBuilder();
            try
            {
                //getting the current date and the date 6 months before.

                Calendar startDate=Calendar.getInstance();
                startDate.add(Calendar.MONTH,-1);

                Calendar endDate=Calendar.getInstance();

                DateFormat myFormat=new SimpleDateFormat("yyyy-MM-dd");
                String formattedStartDate=myFormat.format(startDate.getTime());
                String formattedEndDate=myFormat.format(endDate.getTime());

                Log.v("Date",formattedStartDate);
                Log.v("Date",formattedEndDate);

                // Base URL for the Yahoo query
                urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
                urlStringBuilder.append(URLEncoder.encode("select Date, Close from yahoo.finance.historicaldata where symbol "
                + "in (", "UTF-8"));
                urlStringBuilder.append(URLEncoder.encode("\""+stockSymbol+"\")", "UTF-8"));
                urlStringBuilder.append(URLEncoder.encode("and startDate=\"" + formattedStartDate + "\" and endDate=\"" + formattedEndDate + "\"", "UTF-8"));
                urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                + "org%2Falltableswithkeys&callback=");

            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }

            String urlString;
            String getResponse;

            if(urlStringBuilder!=null)
            {
                urlString=urlStringBuilder.toString();
                Log.v("URL",urlString);
                try
                {
                    getResponse=fetchData(urlString);
                    JSONObject stockObject=new JSONObject(getResponse);
                    JSONObject resultObject=stockObject.getJSONObject("query").getJSONObject("results");
                    JSONArray quoteArray=resultObject.getJSONArray("quote");
                    closeQuoteArray=new double[stockObject.getJSONObject("query").getInt("count")];
                    for(int i=closeQuoteArray.length-1;i>=0;i--)
                    {
                        closeQuoteArray[i]=quoteArray.getJSONObject(i).getDouble("Close");
                    }

                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                catch (JSONException e)
                {
                    Log.v(LOG_TAG,"Could not convert to Json...");
                    e.printStackTrace();
                }
            }
            return closeQuoteArray;
        }

        @Override
        protected void onPostExecute(double[] quoteArray)
        {
            super.onPostExecute(quoteArray);
            if(quoteArray!=null)
            {
                double min=10000;
                double max= -10000;

                for(int i=0;i<quoteArray.length;i++)
                {
                    if(closeQuoteArray[i]<min)
                        min=closeQuoteArray[i];
                    if(closeQuoteArray[i]>max)
                        max=closeQuoteArray[i];

                    dataSet.addPoint(new Point(""+(i+1),(float)closeQuoteArray[i]));
                }
                int minVal=(int)min;
                int maxVal=(int)max;
                int difference=maxVal-minVal;
                int step=2;

                if(difference%step!=0)
                    maxVal=maxVal+step-(difference%step);

                dataSet.setColor(getResources().getColor(R.color.material_blue_500));
                dataSet.setDotsColor(getResources().getColor(R.color.dot_color_green));

                lineChartView.addData(dataSet);
                lineChartView.setAxisBorderValues(minVal,maxVal,step);
                setLineChartProperties();
            }
        }
    }

    private void setLineChartProperties()
    {
        Paint chartGridPaint=new Paint();
        chartGridPaint.setColor(getResources().getColor(R.color.grid_color_white));
        chartGridPaint.setStrokeWidth(1.0f);

        lineChartView.show();
        lineChartView.setStep(10);
        lineChartView.setLabelsColor(getResources().getColor(R.color.material_blue_500));
        lineChartView.setXLabels(AxisController.LabelPosition.OUTSIDE);
        lineChartView.setYLabels(AxisController.LabelPosition.OUTSIDE);
        lineChartView.setGrid(ChartView.GridType.FULL,chartGridPaint);
    }
}
