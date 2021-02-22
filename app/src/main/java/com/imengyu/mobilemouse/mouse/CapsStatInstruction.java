package com.imengyu.mobilemouse.mouse;

public class CapsStatInstruction implements Instruction {

    private boolean capsOn = false;
    private OnInstructionReturnCallback onInstructionReturnCallback = null;

    public boolean isCapsOn() {
        return capsOn;
    }
    public void setOnInstructionReturnCallback(OnInstructionReturnCallback onInstructionReturnCallback) {
        this.onInstructionReturnCallback = onInstructionReturnCallback;
    }

    @Override
    public void pack(byte[] arr) {}
    @Override
    public String getPrefix() {
        return "cap";
    }
    @Override
    public boolean isNeedCheckReturn() {
        return true;
    }
    @Override
    public void setReturnData(String data) {
        capsOn = data.endsWith("on");
        if(onInstructionReturnCallback != null)
            onInstructionReturnCallback.onInstructionReturnCallback(this);
    }
}
