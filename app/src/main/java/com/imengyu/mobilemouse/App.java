package com.imengyu.mobilemouse;

import android.app.Application;

import com.imengyu.mengui.toast.SmallToast;
import com.imengyu.mobilemouse.service.DataService;

public class App extends Application {

    private DataService dataService;
    private boolean initFromLauncher = false;

    public DataService getDataService() {
        return dataService;
    }
    public boolean isNotInitFromLauncher() {
        return !initFromLauncher;
    }
    public void setInitFromLauncher(boolean initFromLauncher) {
        this.initFromLauncher = initFromLauncher;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SmallToast.init();
        dataService = new DataService(getApplicationContext());
        dataService.load();
    }

    @Override
    public void onTerminate() {
        dataService.save();
        dataService.close();
        SmallToast.destroy();
        super.onTerminate();
    }
}
