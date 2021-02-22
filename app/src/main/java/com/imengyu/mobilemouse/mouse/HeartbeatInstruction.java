package com.imengyu.mobilemouse.mouse;

public class HeartbeatInstruction implements Instruction {
    @Override
    public void pack(byte[] arr) {

    }
    @Override
    public String getPrefix() {
        return "het";
    }
    @Override
    public boolean isNeedCheckReturn() {
        return false;
    }
    @Override
    public void setReturnData(String data) {

    }
}
