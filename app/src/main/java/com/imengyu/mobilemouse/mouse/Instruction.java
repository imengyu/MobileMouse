package com.imengyu.mobilemouse.mouse;

public interface Instruction {
    void pack(byte[] arr);
    String getPrefix();
    boolean isNeedCheckReturn();
    void setReturnData(String data);
}
