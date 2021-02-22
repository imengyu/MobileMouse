package com.imengyu.mobilemouse.mouse;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.imengyu.mobilemouse.MouseActivity;
import com.imengyu.mobilemouse.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressLint("ClickableViewAccessibility")
public class KeypadHandler {

    public KeypadHandler(MouseActivity mouseActivity,
                         MouseClient client,
                         ViewGroup layout_keyboard,
                         ViewGroup layout_keyboard_num,
                         Handler handler) {
        this.client = client;
        this.mouseActivity = mouseActivity;
        this.handler = handler;
        initPadBig(layout_keyboard);
        initPadSmall(layout_keyboard_num);
        initKeys();

        capsStatInstruction.setOnInstructionReturnCallback((i) ->
                mouseActivity.runOnUiThread(() -> {
                    setCapsLockStat(capsStatInstruction.isCapsOn());
                })
        );
        numPadStatInstruction.setOnInstructionReturnCallback((i) ->
                mouseActivity.runOnUiThread(() -> {
                    setNumLockStat(numPadStatInstruction.isNumPadOn());
                })
        );
    }

    private final MouseActivity mouseActivity;
    private final Handler handler;
    private final MouseClient client;

    private Button button_key_numlock;
    private Button button_key_caps;

    public void setNumLockStat(boolean on) {
        button_key_numlock.setBackgroundTintList(on ? mouseActivity.getStateToolbarActive() : mouseActivity.getStateToolbarNormal());
    }
    public void setCapsLockStat(boolean on) {
        button_key_caps.setBackgroundTintList(on ? mouseActivity.getStateToolbarActive() : mouseActivity.getStateToolbarNormal());
    }

    private final CapsStatInstruction capsStatInstruction = new CapsStatInstruction();
    private final NumPadStatInstruction numPadStatInstruction = new NumPadStatInstruction();

    private Button button_key_fn;
    private Button button_key_1;
    private Button button_key_2;
    private Button button_key_3;
    private Button button_key_4;
    private Button button_key_5;
    private Button button_key_6;
    private Button button_key_7;
    private Button button_key_8;
    private Button button_key_9;
    private Button button_key_0;
    private Button button_key_oem_minus;
    private Button button_key_oem_plus;

    private void initPadBig(ViewGroup layout_keyboard) {
        List<Button> buttons = searchAllButtons(layout_keyboard);
        buttons.forEach((button) -> button.setOnTouchListener(onKeyTouchListener));

        button_key_caps = layout_keyboard.findViewById(R.id.button_key_caps);
        button_key_fn = layout_keyboard.findViewById(R.id.button_key_fn);
        button_key_1 = layout_keyboard.findViewById(R.id.button_key_1);
        button_key_2 = layout_keyboard.findViewById(R.id.button_key_2);
        button_key_3 = layout_keyboard.findViewById(R.id.button_key_3);
        button_key_4 = layout_keyboard.findViewById(R.id.button_key_4);
        button_key_5 = layout_keyboard.findViewById(R.id.button_key_5);
        button_key_6 = layout_keyboard.findViewById(R.id.button_key_6);
        button_key_7 = layout_keyboard.findViewById(R.id.button_key_7);
        button_key_8 = layout_keyboard.findViewById(R.id.button_key_8);
        button_key_9 = layout_keyboard.findViewById(R.id.button_key_9);
        button_key_0 = layout_keyboard.findViewById(R.id.button_key_0);
        button_key_oem_minus = layout_keyboard.findViewById(R.id.button_key_oem_minus);
        button_key_oem_plus = layout_keyboard.findViewById(R.id.button_key_oem_plus);
    }
    private void initPadSmall(ViewGroup layout_keyboard_num) {
        List<Button> buttons = searchAllButtons(layout_keyboard_num);
        buttons.forEach((button) -> button.setOnTouchListener(onKeyTouchListener));

        button_key_numlock = layout_keyboard_num.findViewById(R.id.button_key_numlock);
    }

    //接收器
    private final View.OnTouchListener onKeyTouchListener = (v, event) -> {
        if((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN)
            handleKey(v, KeyInstruction.KEY_DOWN);
        else if((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP)
            handleKey(v, KeyInstruction.KEY_UP);
        return true;
    };

    //按键id与代码映射表
    private final HashMap<Integer, Integer> singleKeyRefMap = new HashMap<>();

    //初始化键值表
    private void initKeys() {
        singleKeyRefMap.put(R.id.button_key_esc, KeyInstruction.VK_ESCAPE);
        singleKeyRefMap.put(R.id.button_key_oem3, KeyInstruction.VK_OEM_3);
        singleKeyRefMap.put(R.id.button_key_oem4, KeyInstruction.VK_OEM_4);
        singleKeyRefMap.put(R.id.button_key_oem6, KeyInstruction.VK_OEM_6);
        singleKeyRefMap.put(R.id.button_key_oem5, KeyInstruction.VK_OEM_5);
        singleKeyRefMap.put(R.id.button_key_oem7, KeyInstruction.VK_OEM_7);
        singleKeyRefMap.put(R.id.button_key_oem1, KeyInstruction.VK_OEM_1);
        singleKeyRefMap.put(R.id.button_key_oem2, KeyInstruction.VK_OEM_2);
        singleKeyRefMap.put(R.id.button_key_oem_comma, KeyInstruction.VK_OEM_COMMA);
        singleKeyRefMap.put(R.id.button_key_oem_period, KeyInstruction.VK_OEM_PERIOD);
        singleKeyRefMap.put(R.id.button_key_del, KeyInstruction.VK_DELETE);
        singleKeyRefMap.put(R.id.button_key_tab, KeyInstruction.VK_TAB);
        singleKeyRefMap.put(R.id.button_key_caps, KeyInstruction.VK_CAPITAL);
        singleKeyRefMap.put(R.id.button_key_left_shift, KeyInstruction.VK_LSHIFT);
        singleKeyRefMap.put(R.id.button_key_right_shift, KeyInstruction.VK_RSHIFT);
        singleKeyRefMap.put(R.id.button_key_left_ctrl, KeyInstruction.VK_LCONTROL);
        singleKeyRefMap.put(R.id.button_key_right_ctrl, KeyInstruction.VK_RCONTROL);
        singleKeyRefMap.put(R.id.button_key_win, KeyInstruction.VK_LWIN);
        singleKeyRefMap.put(R.id.button_key_left_alt, KeyInstruction.VK_LMENU);
        singleKeyRefMap.put(R.id.button_key_right_alt, KeyInstruction.VK_RMENU);
        singleKeyRefMap.put(R.id.button_key_up, KeyInstruction.VK_UP);
        singleKeyRefMap.put(R.id.button_key_menu, KeyInstruction.VK_MENU);
        singleKeyRefMap.put(R.id.button_key_backspace, KeyInstruction.VK_BACK);
        singleKeyRefMap.put(R.id.button_key_space, KeyInstruction.VK_SPACE);
        singleKeyRefMap.put(R.id.button_key_left, KeyInstruction.VK_LEFT);
        singleKeyRefMap.put(R.id.button_key_down, KeyInstruction.VK_DOWN);
        singleKeyRefMap.put(R.id.button_key_right, KeyInstruction.VK_RIGHT);
        singleKeyRefMap.put(R.id.button_key_enter, KeyInstruction.VK_RETURN);
        singleKeyRefMap.put(R.id.button_key_a, KeyInstruction.VK_A);
        singleKeyRefMap.put(R.id.button_key_b, KeyInstruction.VK_B);
        singleKeyRefMap.put(R.id.button_key_c, KeyInstruction.VK_C);
        singleKeyRefMap.put(R.id.button_key_d, KeyInstruction.VK_D);
        singleKeyRefMap.put(R.id.button_key_e, KeyInstruction.VK_E);
        singleKeyRefMap.put(R.id.button_key_f, KeyInstruction.VK_F);
        singleKeyRefMap.put(R.id.button_key_g, KeyInstruction.VK_G);
        singleKeyRefMap.put(R.id.button_key_h, KeyInstruction.VK_H);
        singleKeyRefMap.put(R.id.button_key_i, KeyInstruction.VK_I);
        singleKeyRefMap.put(R.id.button_key_j, KeyInstruction.VK_J);
        singleKeyRefMap.put(R.id.button_key_k, KeyInstruction.VK_K);
        singleKeyRefMap.put(R.id.button_key_l, KeyInstruction.VK_L);
        singleKeyRefMap.put(R.id.button_key_m, KeyInstruction.VK_M);
        singleKeyRefMap.put(R.id.button_key_n, KeyInstruction.VK_N);
        singleKeyRefMap.put(R.id.button_key_o, KeyInstruction.VK_O);
        singleKeyRefMap.put(R.id.button_key_p, KeyInstruction.VK_P);
        singleKeyRefMap.put(R.id.button_key_q, KeyInstruction.VK_Q);
        singleKeyRefMap.put(R.id.button_key_r, KeyInstruction.VK_R);
        singleKeyRefMap.put(R.id.button_key_s, KeyInstruction.VK_S);
        singleKeyRefMap.put(R.id.button_key_t, KeyInstruction.VK_T);
        singleKeyRefMap.put(R.id.button_key_u, KeyInstruction.VK_U);
        singleKeyRefMap.put(R.id.button_key_v, KeyInstruction.VK_V);
        singleKeyRefMap.put(R.id.button_key_w, KeyInstruction.VK_W);
        singleKeyRefMap.put(R.id.button_key_x, KeyInstruction.VK_X);
        singleKeyRefMap.put(R.id.button_key_y, KeyInstruction.VK_Y);
        singleKeyRefMap.put(R.id.button_key_z, KeyInstruction.VK_Z);


        singleKeyRefMap.put(R.id.button_key_num_0, KeyInstruction.VK_NUMPAD0);
        singleKeyRefMap.put(R.id.button_key_num_1, KeyInstruction.VK_NUMPAD1);
        singleKeyRefMap.put(R.id.button_key_num_2, KeyInstruction.VK_NUMPAD2);
        singleKeyRefMap.put(R.id.button_key_num_3, KeyInstruction.VK_NUMPAD3);
        singleKeyRefMap.put(R.id.button_key_num_4, KeyInstruction.VK_NUMPAD4);
        singleKeyRefMap.put(R.id.button_key_num_5, KeyInstruction.VK_NUMPAD5);
        singleKeyRefMap.put(R.id.button_key_num_6, KeyInstruction.VK_NUMPAD6);
        singleKeyRefMap.put(R.id.button_key_num_7, KeyInstruction.VK_NUMPAD7);
        singleKeyRefMap.put(R.id.button_key_num_8, KeyInstruction.VK_NUMPAD8);
        singleKeyRefMap.put(R.id.button_key_num_9, KeyInstruction.VK_NUMPAD9);
        singleKeyRefMap.put(R.id.button_key_num_decimal, KeyInstruction.VK_DECIMAL);

        singleKeyRefMap.put(R.id.button_key_add, KeyInstruction.VK_ADD);
        singleKeyRefMap.put(R.id.button_key_substract, KeyInstruction.VK_SUBTRACT);
        singleKeyRefMap.put(R.id.button_key_divide, KeyInstruction.VK_DIVIDE);
        singleKeyRefMap.put(R.id.button_key_mutipy, KeyInstruction.VK_MULTIPLY);
        singleKeyRefMap.put(R.id.button_key_home, KeyInstruction.VK_HOME);
        singleKeyRefMap.put(R.id.button_key_end, KeyInstruction.VK_END);
        singleKeyRefMap.put(R.id.button_key_pause, KeyInstruction.VK_PAUSE);
        singleKeyRefMap.put(R.id.button_key_numlock, KeyInstruction.VK_NUMLOCK);
        singleKeyRefMap.put(R.id.button_key_prev_track, KeyInstruction.VK_MEDIA_PREV_TRACK);
        singleKeyRefMap.put(R.id.button_key_play_pause, KeyInstruction.VK_MEDIA_PLAY_PAUSE);
        singleKeyRefMap.put(R.id.button_key_next_track, KeyInstruction.VK_MEDIA_NEXT_TRACK);
        singleKeyRefMap.put(R.id.button_key_vol_mute, KeyInstruction.VK_VOLUME_MUTE);
        singleKeyRefMap.put(R.id.button_key_print_screen, KeyInstruction.VK_PRINT);
        singleKeyRefMap.put(R.id.button_key_media, KeyInstruction.VK_LAUNCH_MEDIA_SELECT);
        singleKeyRefMap.put(R.id.button_key_vol_up, KeyInstruction.VK_VOLUME_UP);
        singleKeyRefMap.put(R.id.button_key_vol_down, KeyInstruction.VK_VOLUME_DOWN);
    }

    //按键id转换
    private int idToVkKey(int id) {
        if (id == R.id.button_key_1)
            return fnStatus ? KeyInstruction.VK_F1 : KeyInstruction.VK_1;
        else if (id == R.id.button_key_2)
            return fnStatus ? KeyInstruction.VK_F2 : KeyInstruction.VK_2;
        else if (id == R.id.button_key_3)
            return fnStatus ? KeyInstruction.VK_F3 : KeyInstruction.VK_3;
        else if (id == R.id.button_key_4)
            return fnStatus ? KeyInstruction.VK_F4 : KeyInstruction.VK_4;
        else if (id == R.id.button_key_5)
            return fnStatus ? KeyInstruction.VK_F5 : KeyInstruction.VK_5;
        else if (id == R.id.button_key_6)
            return fnStatus ? KeyInstruction.VK_F6 : KeyInstruction.VK_6;
        else if (id == R.id.button_key_7)
            return fnStatus ? KeyInstruction.VK_F7 : KeyInstruction.VK_7;
        else if (id == R.id.button_key_8)
            return fnStatus ? KeyInstruction.VK_F8 : KeyInstruction.VK_8;
        else if (id == R.id.button_key_9)
            return fnStatus ? KeyInstruction.VK_F9 : KeyInstruction.VK_9;
        else if (id == R.id.button_key_0)
            return fnStatus ? KeyInstruction.VK_F10 : KeyInstruction.VK_0;
        else if (id == R.id.button_key_oem_minus)
            return fnStatus ? KeyInstruction.VK_F11 : KeyInstruction.VK_OEM_MINUS;
        else if (id == R.id.button_key_oem_plus)
            return fnStatus ? KeyInstruction.VK_F12 : KeyInstruction.VK_OEM_PLUS;
        else {
            Integer integer = singleKeyRefMap.get(id);
            return integer != null ? integer : 0;
        }
    }
    //按键处理
    private void handleKey(View key, int event) {
        int id = key.getId();

        if (id == R.id.button_key_media_key_and_num_key && event == KeyInstruction.KEY_UP)
            mouseActivity.switchNumPadVisibleStatus();
        else if (id == R.id.button_key_fn && event == KeyInstruction.KEY_UP)
            switchFn();
        else {
            int vk = idToVkKey(id);
            if (vk > 0) {
                KeyInstruction keyInstruction = client.requestKeyInstruction();
                keyInstruction.setType(event);
                keyInstruction.setKey(vk);
                client.pushInstruction(keyInstruction);
            }
        }

        if (event == KeyInstruction.KEY_DOWN)
            handler.sendEmptyMessage(MouseActivity.MSG_VIBRATOR);
        if (event == KeyInstruction.KEY_UP) {

            if (id == R.id.button_key_caps)
                handler.sendEmptyMessageDelayed(MouseActivity.MSG_LATE_TEST_CAPS_STATE, 100);
            else if (id == R.id.button_key_numlock)
                handler.sendEmptyMessageDelayed(MouseActivity.MSG_LATE_TEST_NUM_STATE, 100);
        }
    }

    public void sendTestCapsStat() { client.pushInstruction(capsStatInstruction); }
    public void sendTestNumStat() { client.pushInstruction(numPadStatInstruction); }

    private boolean fnStatus = false;
    //切换FN的状态
    @SuppressLint("SetTextI18n")
    private void switchFn() {
        fnStatus = !fnStatus;
        if(fnStatus) {
            button_key_fn.setBackgroundTintList(mouseActivity.getStateToolbarActive());
            button_key_1.setText("F1");
            button_key_2.setText("F2");
            button_key_3.setText("F3");
            button_key_4.setText("F4");
            button_key_5.setText("F5");
            button_key_6.setText("F6");
            button_key_7.setText("F7");
            button_key_8.setText("F8");
            button_key_9.setText("F9");
            button_key_0.setText("F10");
            button_key_oem_minus.setText("F11");
            button_key_oem_plus.setText("F12");
        } else {
            button_key_fn.setBackgroundTintList(mouseActivity.getStateToolbarNormal());
            button_key_1.setText(R.string.text_key_1);
            button_key_2.setText(R.string.text_key_2);
            button_key_3.setText(R.string.text_key_3);
            button_key_4.setText(R.string.text_key_4);
            button_key_5.setText(R.string.text_key_5);
            button_key_6.setText(R.string.text_key_6);
            button_key_7.setText(R.string.text_key_7);
            button_key_8.setText(R.string.text_key_8);
            button_key_9.setText(R.string.text_key_9);
            button_key_0.setText(R.string.text_key_0);
            button_key_oem_minus.setText(R.string.text_key_oem_minus);
            button_key_oem_plus.setText(R.string.text_key_oem_plus);
        }
    }

    //搜索所有按钮控件
    private List<Button> searchAllButtons(ViewGroup view) {
        ArrayList<Button>  result = new ArrayList<>();
        for(int i = 0, c = view.getChildCount(); i < c; i++) {
            View viewChild = view.getChildAt(i);
            if(viewChild instanceof ViewGroup)
                result.addAll(searchAllButtons((ViewGroup) viewChild));
            else if(viewChild instanceof Button)
                result.add((Button)viewChild);
        }
        return result;
    }
}
