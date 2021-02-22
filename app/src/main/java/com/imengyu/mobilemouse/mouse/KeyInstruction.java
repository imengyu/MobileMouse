package com.imengyu.mobilemouse.mouse;

public class KeyInstruction implements ControlInstruction {

    public static final int KEY_EMPTY = 0;
    public static final int KEY_DOWN = 1;
    public static final int KEY_UP = 2;

    public static final int VK_VOLUME_UP = 0xAF;
    public static final int VK_VOLUME_DOWN = 0xAE;
    public static final int VK_VOLUME_MUTE = 0xAD;
    public static final int VK_LAUNCH_MEDIA_SELECT= 0xB5;
    public static final int VK_MEDIA_PLAY_PAUSE = 0xB3;
    public static final int VK_MEDIA_NEXT_TRACK = 0xB0;
    public static final int VK_MEDIA_PREV_TRACK = 0xB1;

    public static final int VK_BACK = 0x08;
    public static final int VK_TAB = 0x09;
    public static final int VK_CLEAR = 0x0C;
    public static final int VK_RETURN = 0x0D;
    public static final int VK_SHIFT = 0x10;
    public static final int VK_CONTROL = 0x11;
    public static final int VK_MENU = 0x12;
    public static final int VK_PAUSE = 0x13;
    public static final int VK_CAPITAL = 0x14;
    public static final int VK_NUMLOCK = 0x90;

    public static final int VK_ESCAPE = 0x1B;

    public static final int VK_CONVERT = 0x1C;
    public static final int VK_NONCONVERT = 0x1D;
    public static final int VK_ACCEPT = 0x1E;
    public static final int VK_MODECHANGE = 0x1F;

    public static final int VK_SPACE = 0x20;
    public static final int VK_PRIOR = 0x21;
    public static final int VK_NEXT = 0x22;
    public static final int VK_END = 0x23;
    public static final int VK_HOME = 0x24;
    public static final int VK_LEFT = 0x25;
    public static final int VK_UP = 0x26;
    public static final int VK_RIGHT = 0x27;
    public static final int VK_DOWN = 0x28;
    public static final int VK_SELECT = 0x29;
    public static final int VK_PRINT = 0x2A;
    public static final int VK_EXECUTE = 0x2B;
    public static final int VK_SNAPSHOT = 0x2C;
    public static final int VK_INSERT = 0x2D;
    public static final int VK_DELETE = 0x2E;
    public static final int VK_HELP = 0x2F;
    public static final int VK_LWIN = 0x5B;
    public static final int VK_RWIN = 0x5C;
    public static final int VK_APPS = 0x5D;
    public static final int VK_SLEEP = 0x5F;
    public static final int VK_NUMPAD0 = 0x60;
    public static final int VK_NUMPAD1 = 0x61;
    public static final int VK_NUMPAD2 = 0x62;
    public static final int VK_NUMPAD3 = 0x63;
    public static final int VK_NUMPAD4 = 0x64;
    public static final int VK_NUMPAD5 = 0x65;
    public static final int VK_NUMPAD6 = 0x66;
    public static final int VK_NUMPAD7 = 0x67;
    public static final int VK_NUMPAD8 = 0x68;
    public static final int VK_NUMPAD9 = 0x69;
    public static final int VK_MULTIPLY = 0x6A;
    public static final int VK_ADD = 0x6B;
    public static final int VK_SEPARATOR = 0x6C;
    public static final int VK_SUBTRACT = 0x6D;
    public static final int VK_DECIMAL = 0x6E;
    public static final int VK_DIVIDE = 0x6F;
    public static final int VK_F1 = 0x70;
    public static final int VK_F2 = 0x71;
    public static final int VK_F3 = 0x72;
    public static final int VK_F4 = 0x73;
    public static final int VK_F5 = 0x74;
    public static final int VK_F6 = 0x75;
    public static final int VK_F7 = 0x76;
    public static final int VK_F8 = 0x77;
    public static final int VK_F9 = 0x78;
    public static final int VK_F10 = 0x79;
    public static final int VK_F11 = 0x7A;
    public static final int VK_F12 = 0x7B;
    public static final int VK_0 = 0x30;
    public static final int VK_1 = 0x31;
    public static final int VK_2 = 0x32;
    public static final int VK_3 = 0x33;
    public static final int VK_4 = 0x34;
    public static final int VK_5 = 0x35;
    public static final int VK_6 = 0x36;
    public static final int VK_7 = 0x37;
    public static final int VK_8 = 0x38;
    public static final int VK_9 = 0x39;
    public static final int VK_A = 0x41;
    public static final int VK_B = 0x42;
    public static final int VK_C = 0x43;
    public static final int VK_D = 0x44;
    public static final int VK_E = 0x45;
    public static final int VK_F = 0x46;
    public static final int VK_G = 0x47;
    public static final int VK_H = 0x48;
    public static final int VK_I = 0x49;
    public static final int VK_J = 0x4A;
    public static final int VK_K = 0x4B;
    public static final int VK_L = 0x4C;
    public static final int VK_M = 0x4D;
    public static final int VK_N = 0x4E;
    public static final int VK_O = 0x4F;
    public static final int VK_P = 0x50;
    public static final int VK_Q = 0x51;
    public static final int VK_R = 0x52;
    public static final int VK_S = 0x53;
    public static final int VK_T = 0x54;
    public static final int VK_U = 0x55;
    public static final int VK_V = 0x56;
    public static final int VK_W = 0x57;
    public static final int VK_X = 0x58;
    public static final int VK_Y = 0x59;
    public static final int VK_Z = 0x5A;
    public static final int VK_OEM_1 = 0xBA;
    public static final int VK_OEM_PLUS = 0xBB;
    public static final int VK_OEM_COMMA = 0xBC;
    public static final int VK_OEM_MINUS = 0xBD;
    public static final int VK_OEM_PERIOD = 0xBE;
    public static final int VK_OEM_2 = 0xBF;
    public static final int VK_OEM_3 = 0xC0;
    public static final int VK_OEM_4 = 0xDB;
    public static final int VK_OEM_5 = 0xDC;
    public static final int VK_OEM_6 = 0xDD;
    public static final int VK_OEM_7 = 0xDE;
    public static final int VK_OEM_8 = 0xDF;
    public static final int VK_LSHIFT = 0xA0;
    public static final int VK_RSHIFT = 0xA1;
    public static final int VK_LCONTROL = 0xA2;
    public static final int VK_RCONTROL = 0xA3;
    public static final int VK_LMENU = 0xA4;
    public static final int VK_RMENU = 0xA5;

    public KeyInstruction(int type) {
        this.type = type;
    }

    private int type = KEY_EMPTY;
    private int key = KEY_EMPTY;

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    /**
     * 打包当前指令为byte格式
     * <p>
     * 第0位  type 0xFF
     * 第1位  key 0xFF
     *
     * @param arr byte数组，必须长于16位
     */
    public void pack(byte[] arr) {
        if (arr.length < 16)
            return;

        arr[0] = (byte) type;
        arr[1] = (byte) key;
    }

    @Override
    public String getPrefix() {
        return "ktl";
    }
}
