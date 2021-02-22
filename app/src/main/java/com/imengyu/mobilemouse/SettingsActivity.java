package com.imengyu.mobilemouse;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.imengyu.mengui.utils.StatusBarUtils;
import com.imengyu.mengui.widget.TitleBar;
import com.imengyu.mobilemouse.fragment.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        StatusBarUtils.setLightMode(this);
        StatusBarUtils.setStatusBarColor(this, getColor(R.color.colorWhite));

        TitleBar titleBar = findViewById(R.id.titlebar);
        titleBar.setLeftIconOnClickListener((v) -> finish());

        SettingsFragment settingsFragment = new SettingsFragment();
        switchFragment(settingsFragment);
    }
    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() <= 1)
            finish();
        else
            getSupportFragmentManager().popBackStack();
    }

    public void switchFragment(Fragment targetFragment) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        transaction
                .replace(R.id.settings, targetFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }
}