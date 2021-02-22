package com.imengyu.mobilemouse.service;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.alibaba.fastjson.JSON;
import com.imengyu.mobilemouse.model.MainConnectDevice;
import com.imengyu.mobilemouse.utils.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class DataService {

    private DatabaseHelper databaseHelper;

    public DataService(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    private final List<MainConnectDevice> connectDevices = new ArrayList<>();
    private final List<String> excludeDevices = new ArrayList<>();

    public boolean isInExcludeDevices(String address) {
        return excludeDevices.contains(address);
    }
    public void removeFromExcludeDevices(String address) {
        excludeDevices.remove(address);
    }
    public void addToExcludeDevices(String address) {
        excludeDevices.add(address);
    }
    public List<String> getExcludeDevices() {
        return excludeDevices;
    }
    public List<MainConnectDevice> getConnectDevices() {
        return connectDevices;
    }
    public MainConnectDevice findConnectDevice(String address) {
        for (MainConnectDevice dev : connectDevices) {
            if(dev.getTargetAddress().equals(address))
                return dev;
        }
        return null;
    }

    public void load() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        connectDevices.clear();

        //Load image list
        Cursor cursor = db.rawQuery("select * from device_list", null);
        while (cursor.moveToNext()) {
            connectDevices.add(JSON.parseObject(cursor.getString(1), MainConnectDevice.class));
        }
        cursor.close();

        //Load image list
        cursor = db.rawQuery("select * from exclude_device_list", null);
        while (cursor.moveToNext()) {
            excludeDevices.add(cursor.getString(1));
        }
        cursor.close();

        db.close();
    }
    public void save() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        db.execSQL("delete from 'device_list'; ");

        for(MainConnectDevice i : connectDevices) {
            if(i.isAddByUser()) {
                String sql = "insert into device_list(obj) values('" + JSON.toJSONString(i) + "')";
                db.execSQL(sql);
            }
        }
        for(String path : excludeDevices) {
            String sql = "insert into exclude_device_list(address) values('" + path + "')";
            db.execSQL(sql);
        }


        db.close();
    }
    public void close() {
        databaseHelper.close();
    }
}
