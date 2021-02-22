package com.imengyu.mobilemouse.mouse;

public interface ControlInstruction {
    void pack(byte[] arr);
    String getPrefix();
}
