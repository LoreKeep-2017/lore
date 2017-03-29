package com.example.ilya.lorekeep.LinkFragment;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ilya.lorekeep.InfoActivity;
import com.example.ilya.lorekeep.LinksActivity;

public class LinkFragment {

    private LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
    );

    private LinearLayout layout;

    @Nullable
    public View CreateView(ViewGroup parent) {

        layout = new LinearLayout(parent.getContext());
        layout.setBackgroundColor(Color.GRAY);
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(parent.getContext());
        title.setLayoutParams(params);

        TextView content = new TextView(parent.getContext());
        content.setLayoutParams(params);

        params.setMargins(20,20,20,20);
        layout.setLayoutParams(params);

        title.setText("OLOLOLOL Title OLololol0");
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        content.setText("OLOLOLLOLO Content OLOLOLOLOL");
        content.setGravity(Gravity.CENTER);
        content.setTextColor(Color.WHITE);
        layout.addView(content);

        return layout;
    }

    private int getChildNumber(){
        return ((ViewGroup)layout.getParent()).getChildCount();
    }
}
