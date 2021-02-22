package com.imengyu.mobilemouse.model;

import java.util.Date;

public class MainConnectDevice {

    public static final int DEVICE_TYPE_WIN = 1;
    public static final int DEVICE_TYPE_LINUX = 2;
    public static final int DEVICE_TYPE_MAC = 3;

    private String targetAddress;
    private String targetHostname;
    private int targetType;
    private Date lastConnectTime;
    private int connectSuccessCount;
    private String lastPass;
    private boolean hostNameWriteAddByUser = false;
    private boolean isAddByUser = false;
    private boolean online = false;

    public boolean isHostNameWriteAddByUser() {
        return hostNameWriteAddByUser;
    }
    public void setHostNameWriteAddByUser(boolean hostNameWriteAddByUser) {
        this.hostNameWriteAddByUser = hostNameWriteAddByUser;
    }
    public boolean isOnline() {
        return online;
    }
    public void setOnline(boolean online) {
        this.online = online;
    }
    public boolean isAddByUser() {
        return isAddByUser;
    }
    public void setAddByUser(boolean addByUser) {
        isAddByUser = addByUser;
    }
    public String getLastPass() {
        return lastPass;
    }
    public void setLastPass(String lastPass) {
        this.lastPass = lastPass;
    }
    public String getTargetAddress() {
        return targetAddress;
    }
    public void setTargetAddress(String targetAddress) {
        this.targetAddress = targetAddress;
    }
    public String getTargetHostname() {
        return targetHostname;
    }
    public void setTargetHostname(String targetHostname) {
        this.targetHostname = targetHostname;
    }
    public int getTargetType() {
        return targetType;
    }
    public void setTargetType(int targetType) {
        this.targetType = targetType;
    }
    public Date getLastConnectTime() {
        return lastConnectTime;
    }
    public void setLastConnectTime(Date lastConnectTime) {
        this.lastConnectTime = lastConnectTime;
    }
    public int getConnectSuccessCount() {
        return connectSuccessCount;
    }
    public void setConnectSuccessCount(int connectSuccessCount) {
        this.connectSuccessCount = connectSuccessCount;
    }
}
