package com.deskmint.dashboard;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.deskmint.dashboard.fragments.AppsFragment;
import com.deskmint.dashboard.fragments.GamesFragment;
import com.deskmint.dashboard.fragments.HomeFragment;
import com.deskmint.dashboard.fragments.MediaFragment;
import com.deskmint.dashboard.fragments.SettingsFragment;
import com.deskmint.dashboard.fragments.TasksFragment;

public class MainActivity extends Activity {
    private static final String[] TAB_TITLES = {"HOME","TASKS","MEDIA","APPS","GAMES","SETTINGS"};
    private static final int[] TAB_COLORS = {
            Color.rgb(0,229,199), Color.rgb(70,150,255), Color.rgb(190,90,255),
            Color.rgb(255,170,55), Color.rgb(255,85,110), Color.rgb(120,220,120)
    };
    private LinearLayout tabStrip;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(7,9,12));
        buildUi();
        show(0);
    }

    private GradientDrawable rounded(int color, float radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(8,10,14));
        root.setPadding(10,8,10,8);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(16,4,16,4);
        header.setBackground(rounded(Color.rgb(18,22,28), 18));

        TextView logo = new TextView(this);
        logo.setText("DESKMINT");
        logo.setTextColor(Color.rgb(0,229,199));
        logo.setTextSize(15);
        logo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        header.addView(logo, new LinearLayout.LayoutParams(0,48,1));

        TextView mode = new TextView(this);
        mode.setText("A3000  •  READY");
        mode.setTextColor(Color.rgb(125,135,150));
        mode.setTextSize(9);
        mode.setGravity(Gravity.CENTER);
        header.addView(mode, new LinearLayout.LayoutParams(-2,48));
        root.addView(header, new LinearLayout.LayoutParams(-1,58));

        tabStrip = new LinearLayout(this);
        tabStrip.setOrientation(LinearLayout.HORIZONTAL);
        tabStrip.setGravity(Gravity.CENTER_VERTICAL);
        tabStrip.setPadding(4,8,4,8);
        root.addView(tabStrip, new LinearLayout.LayoutParams(-1,64));

        for (int i=0;i<TAB_TITLES.length;i++) {
            final int index=i;
            TextView t=new TextView(this);
            t.setText(TAB_TITLES[i]);
            t.setTextSize(9);
            t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            t.setGravity(Gravity.CENTER);
            t.setTextColor(Color.rgb(120,128,140));
            t.setPadding(2,0,2,0);
            t.setOnClickListener(new View.OnClickListener(){ public void onClick(View v){ show(index); }});
            tabStrip.addView(t,new LinearLayout.LayoutParams(0,46,1));
        }

        FrameLayout frame = new FrameLayout(this);
        frame.setId(2001);
        frame.setBackground(rounded(Color.rgb(12,15,20), 20));
        root.addView(frame,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
    }

    private void show(int index) {
        Fragment f;
        switch(index){
            case 1:f=new TasksFragment();break;
            case 2:f=new MediaFragment();break;
            case 3:f=new AppsFragment();break;
            case 4:f=new GamesFragment();break;
            case 5:f=new SettingsFragment();break;
            default:f=new HomeFragment();
        }
        FragmentTransaction tx=getFragmentManager().beginTransaction();
        tx.replace(2001,f).commit();
        for(int i=0;i<tabStrip.getChildCount();i++){
            TextView t=(TextView)tabStrip.getChildAt(i);
            t.setTextColor(i==index?TAB_COLORS[i]:Color.rgb(120,128,140));
            t.setTextSize(i==index?10:9);
        }
    }
}
