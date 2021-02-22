package com.imengyu.mobilemouse.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetUtils {
    /**
     * 获取wifi是否已经连接
     * @param context 上下文
     * @return wifi是否已经连接
     */
    public static boolean isWifiConnected(Context context) {
        WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        int ipAddress = wifiInfo == null ? 0 : wifiInfo.getIpAddress();
        return mWifiManager.isWifiEnabled() && ipAddress != 0;
    }

    /**
     * 字符串IP转换为raw ip
     * @param ip 字符串IP
     * @return raw ip，对于IPV4地址，为 4 bytes 的数组，对于IPV6地址，为 16 bytes 的数组。
     */
    public static byte[] stringIpToRawIp(String ip) {

        if(ip.contains(".")) {
            byte[] ipBuf = new byte[4];
            String[] ipStr = ip.split("\\.");
            for(int i = 0; i < 4; i++){
                ipBuf[i] = (byte)(Integer.parseInt(ipStr[i]) & 0xff);
            }
            return ipBuf;
        } else if(ip.contains(":")) {
            int spCount = 0;
            for(int i = 0;i < ip.length(); i++) {
                if(ip.charAt(i) == ':')
                    spCount++;
            }

            int current = 0, lastIndex = 0;
            byte [] rawIp = new byte[16];
            for(int i = 0, c = ip.length(); i < c; i++) {
                if(ip.charAt(i) == ':' || i == c - 1) {
                    if(i > 0 && ip.charAt(i - 1) == ':') { //连续的冒号
                        int lessCount = 7 - spCount + 1;//省略的组数
                        for(; lessCount > 0; lessCount--)
                            rawIp[current++] = 0;
                    } else {
                        String v = ip.substring(lastIndex, i);
                        long vl = Long.parseLong(v);
                        rawIp[current++] = (byte) ((vl & 0xff00) >> 8);
                        rawIp[current++] = (byte) (vl & 0x00ff);
                    }
                }
            }
            return rawIp;
        }

        return null;
    }
    /**
     * 获取IP主机名
     * @param ip IP
     * @return 主机名
     */
    public static String getIpHostName(String ip) {
        try {
            InetAddress inetAddress = InetAddress.getByAddress(stringIpToRawIp(ip));
            return inetAddress.getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "";
    }
}
