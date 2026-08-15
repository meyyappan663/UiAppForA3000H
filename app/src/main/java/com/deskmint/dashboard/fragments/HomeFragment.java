package com.deskmint.dashboard.fragments;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.deskmint.dashboard.weather.WeatherHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private TextView clockView,dateView,weatherView;
    private Handler tickHandler=new Handler();

    private GradientDrawable card(int a,int b,float radius){
        GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{a,b});
        d.setCornerRadius(radius); d.setStroke(1,Color.rgb(42,48,58)); return d;
    }
    private TextView label(String s,int size,int color){
        TextView t=new TextView(getActivity()); t.setText(s); t.setTextSize(size); t.setTextColor(color); return t;
    }

    @Override public View onCreateView(android.view.LayoutInflater inflater,ViewGroup container,Bundle saved){
        LinearLayout root=new LinearLayout(getActivity());
        root.setOrientation(LinearLayout.VERTICAL); root.setPadding(18,18,18,18);
        root.setBackgroundColor(Color.rgb(12,15,20));

        TextView over=label("PERSONAL COMMAND CENTER",10,Color.rgb(120,130,145));
        over.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        root.addView(over,new LinearLayout.LayoutParams(-1,28));

        LinearLayout clockCard=new LinearLayout(getActivity());
        clockCard.setOrientation(LinearLayout.VERTICAL); clockCard.setGravity(Gravity.CENTER);
        clockCard.setPadding(12,14,12,12); clockCard.setBackground(card(Color.rgb(24,29,36),Color.rgb(14,18,24),22));

        TextView live=label("●  LIVE CLOCK",9,Color.rgb(0,229,199));
        live.setTypeface(Typeface.DEFAULT,Typeface.BOLD); live.setGravity(Gravity.CENTER);
        clockCard.addView(live,new LinearLayout.LayoutParams(-1,22));

        clockView=new TextView(getActivity());
        clockView.setTextSize(56); clockView.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);
        clockView.setGravity(Gravity.CENTER); clockView.setShadowLayer(7,0,0,Color.rgb(0,229,199));
        clockCard.addView(clockView,new LinearLayout.LayoutParams(-1,76));

        dateView=new TextView(getActivity()); dateView.setTextSize(14); dateView.setGravity(Gravity.CENTER);
        dateView.setTextColor(Color.rgb(155,165,180));
        clockCard.addView(dateView,new LinearLayout.LayoutParams(-1,34));
        root.addView(clockCard,new LinearLayout.LayoutParams(-1,158));

        LinearLayout row=new LinearLayout(getActivity()); row.setOrientation(LinearLayout.HORIZONTAL); row.setPadding(0,12,0,0);
        row.addView(makeMiniCard("WEATHER","--°C","LOCAL"),new LinearLayout.LayoutParams(0,86,1));
        row.addView(makeMiniCard("SYSTEM","READY","A3000"),new LinearLayout.LayoutParams(0,86,1));
        root.addView(row);

        weatherView=new TextView(getActivity()); weatherView.setTextSize(15); weatherView.setGravity(Gravity.CENTER_VERTICAL);
        weatherView.setTextColor(Color.rgb(225,232,240)); weatherView.setPadding(18,0,18,0);
        weatherView.setBackground(card(Color.rgb(20,25,32),Color.rgb(15,19,25),18));
        LinearLayout.LayoutParams wp=new LinearLayout.LayoutParams(-1,62); wp.topMargin=12; root.addView(weatherView,wp);

        TextView hint=label("FAST • CLEAN • LOW-MEMORY MODE",10,Color.rgb(95,105,120));
        hint.setGravity(Gravity.CENTER); hint.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,42); hp.topMargin=4; root.addView(hint,hp);
        return root;
    }

    private View makeMiniCard(String title,String value,String sub){
        LinearLayout box=new LinearLayout(getActivity()); box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER);
        box.setPadding(8,5,8,5); box.setBackground(card(Color.rgb(21,26,34),Color.rgb(15,19,25),17));
        TextView a=label(title,8,Color.rgb(115,128,145)); a.setGravity(Gravity.CENTER);
        TextView b=label(value,17,Color.rgb(0,229,199)); b.setGravity(Gravity.CENTER); b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        TextView c=label(sub,8,Color.rgb(95,105,120)); c.setGravity(Gravity.CENTER);
        box.addView(a,new LinearLayout.LayoutParams(-1,18)); box.addView(b,new LinearLayout.LayoutParams(-1,30)); box.addView(c,new LinearLayout.LayoutParams(-1,16));
        return box;
    }

    @Override public void onResume(){super.onResume(); applyClockColor(); tickClock(); loadWeather();}
    private void applyClockColor(){
        int c=getActivity().getSharedPreferences("deskmint",Context.MODE_PRIVATE).getInt("clock_color",Color.BLACK);
        clockView.setTextColor(c);
    }
    private void tickClock(){
        if(clockView==null)return; Date now=new Date();
        clockView.setText(new SimpleDateFormat("HH:mm:ss",Locale.getDefault()).format(now));
        dateView.setText(new SimpleDateFormat("EEEE, d MMMM yyyy",Locale.getDefault()).format(now));
        tickHandler.postDelayed(new Runnable(){public void run(){if(isAdded())tickClock();}},1000);
    }
    private void loadWeather(){
        WeatherHelper.fetch(getActivity(),0.0,0.0,new WeatherHelper.Callback(){
            public void onWeather(double tempC,String condition,boolean fromCache){
                if(!isAdded())return;
                weatherView.setText(String.format(Locale.getDefault(),"☁   %.0f°C   •   %s%s",tempC,condition,fromCache?"   • cached":""));
            }
        });
    }
    @Override public void onPause(){super.onPause();tickHandler.removeCallbacksAndMessages(null);}
}
