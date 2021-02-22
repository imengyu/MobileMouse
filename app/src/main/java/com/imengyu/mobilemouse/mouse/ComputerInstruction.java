package com.imengyu.mobilemouse.mouse;

public class ComputerInstruction implements Instruction {

    public static final int ACTION_EMPTY = 0;
    public static final int ACTION_SHUTDOWN = 4;
    public static final int ACTION_REBOOT = 5;
    public static final int ACTION_LOGOFF = 6;

    private int type = ACTION_EMPTY;

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public void pack(byte[] arr) {
        if(arr.length < 16)
            return;

        arr[0] = (byte) type;
    }
    @Override
    public String getPrefix() {
        return "act";
    }
    @Override
    public boolean isNeedCheckReturn() {
        return false;
    }
    @Override
    public void setReturnData(String data) {}
}
