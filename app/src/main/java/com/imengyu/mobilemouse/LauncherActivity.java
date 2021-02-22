package com.imengyu.mobilemouse;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.imengyu.mobilemouse.utils.AppUtils;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置语言
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String language = sharedPreferences.getString("language", "");
        AppUtils.setLanguage(this, language);

        setContentView(R.layout.activity_launcher);

        //检查是否同意许可以及请求权限
        AppUtils.testAgreementAllowed(this, (b) -> runContinue());
    }

    private void runContinue() {
        new Thread(() -> {
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //转回UI线程
            runOnUiThread(() -> {
                ((App)getApplication()).setInitFromLauncher(true);
                Intent newIntent = new Intent(LauncherActivity.this, MainActivity.class);
                startActivity(newIntent);
                LauncherActivity.this.finish();
            });
        }).start();
    }

}