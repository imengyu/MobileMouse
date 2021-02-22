package com.imengyu.mobilemouse;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.imengyu.mengui.utils.StatusBarUtils;
import com.imengyu.mengui.widget.TitleBar;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        StatusBarUtils.setLightMode(this);
        StatusBarUtils.setStatusBarColor(this, getColor(R.color.colorWhite));

        if(((App) getApplication()).isNotInitFromLauncher()) finish();

        findViewById(R.id.button).setOnClickListener((v) -> finish());
        ((TitleBar)findViewById(R.id.toolbar)).setLeftIconOnClickListener((v) -> finish());

        TextView text_version = findViewById(R.id.text_version);
        text_version.setText(String.format(getString(R.string.text_version), BuildConfig.VERSION_NAME));
    }
}