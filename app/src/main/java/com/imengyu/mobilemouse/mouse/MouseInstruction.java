package com.imengyu.mobilemouse.mouse;

public class MouseInstruction implements ControlInstruction {

    public static final int MOUSE_EMPTY = 0;
    public static final int MOUSE_DOWN = 1;
    public static final int MOUSE_UP = 2;
    public static final int MOUSE_MOVE = 3;
    public static final int MOUSE_OUT = 4;
    public static final int MOUSE_SCROLL = 5;

    public static final int BUTTON_NONE = 0;
    public static final int BUTTON_LEFT = 0x1;
    public static final int BUTTON_RIGHT = 0x2;
    public static final int BUTTON_MIDDLE = 0x4;

    public MouseInstruction(int type) {
        this.type = type;
    }

    private int type = MOUSE_EMPTY;
    private int button = BUTTON_NONE;
    private int x = 0;
    private int y = 0;

    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public int getButton() {
        return button;
    }
    public void setButton(int button) {
        this.button = button;
    }
    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }
    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }

    /**
     * 打包当前指令为byte格式
     *
     * 第0位  type 0xFF
     * 第1位  button 0xFF
     * 第2位
     * 第3位  X 0xFFFF
     * 第4位
     * 第5位  Y 0xFFFF
     * @param arr byte数组，必须长于16位
     */
    public void pack(byte[] arr) {
        if(arr.length < 16)
            return;

        arr[0] = (byte) type;
        arr[1] = (byte) button;

        arr[2] = (byte)((x >> 24) & 0xFF);
        arr[3] = (byte)((x >> 16) & 0xFF);
        arr[4] = (byte)((x >> 8) & 0xFF);
        arr[5] = (byte)(x & 0xFF);

        arr[6] = (byte)((y >> 24) & 0xFF);
        arr[7] = (byte)((y >> 16) & 0xFF);
        arr[8] = (byte)((y >> 8) & 0xFF);
        arr[9] = (byte)(y & 0xFF);
    }

    @Override
    public String getPrefix() {
        return "mtl";
    }
}
