package com.imengyu.mobilemouse;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.imengyu.mengui.utils.StatusBarUtils;
import com.imengyu.mengui.widget.TitleBar;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        StatusBarUtils.setLightMode(this);
        StatusBarUtils.setStatusBarColor(this, getColor(R.color.colorWhite));

        if(((App) getApplication()).isNotInitFromLauncher()) finish();

        ((TitleBar)findViewById(R.id.toolbar)).setLeftIconOnClickListener((v) -> finish());
    }
}