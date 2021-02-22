package com.imengyu.mobilemouse.constant;

public class RemoteConst {

    /**
     * 用于设备搜索的端口
     */
    public static final int DEVICE_SEARCH_PORT = MouseConst.DISCOVER_PORT;
    /**
     * 设备搜索次数
     */
    public static final int SEARCH_DEVICE_TIMES = 1;
    /**
     * 搜索的最大设备数量
     */
    public static final int SEARCH_DEVICE_MAX = 250;
    /**
     * 接收超时时间
     */
    public static final int RECEIVE_TIME_OUT = 1000;

    /**
     * udp数据包前缀
     */
    public static final int PACKET_PREFIX = '$';
    /**
     * udp数据包类型：搜索类型
     */
    public static final int PACKET_TYPE_SEARCH_DEVICE_REQ = 0x10;
    /**
     * udp数据包类型：搜索应答类型
     */
    public static final int PACKET_TYPE_SEARCH_DEVICE_RSP = 0x11;
}
