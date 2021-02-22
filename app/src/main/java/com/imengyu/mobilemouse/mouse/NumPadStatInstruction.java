package com.imengyu.mobilemouse.mouse;

public class NumPadStatInstruction implements Instruction {

    private boolean numPadOn = false;
    private OnInstructionReturnCallback onInstructionReturnCallback = null;

    public void setOnInstructionReturnCallback(OnInstructionReturnCallback onInstructionReturnCallback) {
        this.onInstructionReturnCallback = onInstructionReturnCallback;
    }
    public boolean isNumPadOn() {
        return numPadOn;
    }

    @Override
    public void pack(byte[] arr) {}
    @Override
    public String getPrefix() {
        return "num";
    }
    @Override
    public boolean isNeedCheckReturn() {
        return true;
    }
    @Override
    public void setReturnData(String data) {
        numPadOn = data.endsWith("on");
        if(onInstructionReturnCallback != null)
            onInstructionReturnCallback.onInstructionReturnCallback(this);
    }
}
