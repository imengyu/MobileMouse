package com.imengyu.mobilemouse;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.InputType;
import android.util.Size;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.imengyu.mengui.dialog.CommonDialog;
import com.imengyu.mengui.utils.ScreenUtils;
import com.imengyu.mengui.utils.StatusBarUtils;
import com.imengyu.mengui.widget.TitleBar;
import com.imengyu.mobilemouse.model.MainConnectDevice;
import com.imengyu.mobilemouse.mouse.ComputerInstruction;
import com.imengyu.mobilemouse.mouse.KeyInstruction;
import com.imengyu.mobilemouse.mouse.KeypadHandler;
import com.imengyu.mobilemouse.mouse.MouseClient;
import com.imengyu.mobilemouse.mouse.MouseInstruction;
import com.imengyu.mobilemouse.mouse.MouseView;
import com.imengyu.mobilemouse.sensor.ImprovedOrientationSensor1Provider;
import com.imengyu.mobilemouse.service.DataService;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import me.kareluo.ui.OptionMenu;
import me.kareluo.ui.PopupMenuView;

@SuppressLint("ClickableViewAccessibility")
public class MouseActivity extends AppCompatActivity {

    private static final int REQUEST_SETTINGS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mouse);

        if(((App) getApplication()).isNotInitFromLauncher()) finish();

        StatusBarUtils.setLightMode(this);
        StatusBarUtils.setStatusBarColor(this, getColor(R.color.colorWhite));

        App app = ((App)getApplication());

        dataService = app.getDataService();
        Size screenSize = ScreenUtils.getScreenSize(this);
        scrollTickHeight = screenSize.getHeight() / 8;

        initResources();
        loadSettings();
        initView();
        initMenu();
        initClient();
        initKeyPad();
        initSensor();
        initVibrator();
        applySettings();
    }
    @Override
    protected void onDestroy() {
        if (client != null)
            client.stop();
        client = null;
        super.onDestroy();
    }

    private Vibrator vibrator;
    private VibrationEffect vibrationEffectShort;
    private int scrollTickHeight = 0;

    private DataService dataService = null;
    private MainConnectDevice currentDev = null;
    private MouseClient client = null;
    private KeypadHandler keypadHandler = null;

    private MouseView mouse_pad;
    private TextView text_connect_status;
    private TitleBar titleBar;
    private ImageButton button_touch_sensor;
    private ImageButton button_touch_hand;
    private ImageView image_touch_pad_mode;
    private TextView text_touch_pad_mode;
    private ImageView scroll_pad;
    private ImageButton button_lock_sensor;
    private ImageView image_touch_pad_lock;
    private View layout_key_controls;
    private Button button_key_media_key_and_num_key;
    private ViewGroup layout_keyboard_num;

    private Drawable ic_bad;
    private Drawable ic_ok;
    private ColorStateList state_toolbar_normal;
    private ColorStateList state_toolbar_active;
    private Animation bottom_up_show;
    private Animation bottom_down_hide;
    private Animation fade_show;
    private Animation fade_hide;

    public ColorStateList getStateToolbarNormal() {
        return state_toolbar_normal;
    }
    public ColorStateList getStateToolbarActive() {
        return state_toolbar_active;
    }

    private boolean buttonMiddleDown = false;
    private boolean buttonMiddleMoved = false;
    private boolean buttonMiddleDownSended = false;
    private float buttonMiddleTouchLastY = 0;
    private int buttonMiddleTouchCount = 0;

    private PopupMenuView menuPowerControl = null;

    private void initResources() {
        Resources resources = getResources();
        ic_bad = ContextCompat.getDrawable(this, R.drawable.ic_bad);
        if (ic_bad != null)
            ic_bad.setBounds(0, 0, ic_bad.getIntrinsicWidth(), ic_bad.getIntrinsicHeight());
        ic_ok = ContextCompat.getDrawable(this, R.drawable.ic_ok);
        if (ic_ok != null)
            ic_ok.setBounds(0, 0, ic_ok.getIntrinsicWidth(), ic_ok.getIntrinsicHeight());

        int color_normal = resources.getColor(R.color.colorTouchPadControlColor, null);
        int color_active = resources.getColor(R.color.colorTouchPadControlActiveColor, null);
        state_toolbar_normal = ColorStateList.valueOf(color_normal);
        state_toolbar_active = ColorStateList.valueOf(color_active);

        bottom_up_show = AnimationUtils.loadAnimation(this, R.anim.bottom_up_show);
        bottom_down_hide = AnimationUtils.loadAnimation(this, R.anim.bottom_down_hide);
        fade_show = AnimationUtils.loadAnimation(this, R.anim.fade_show);
        fade_hide = AnimationUtils.loadAnimation(this, R.anim.fade_hide);
    }
    private void initKeyPad() {
        layout_keyboard_num = findViewById(R.id.layout_keyboard_num);
        layout_keyboard_num.setVisibility(View.GONE);

        ViewGroup layout_keyboard = findViewById(R.id.layout_keyboard);

        keypadHandler = new KeypadHandler(
                this, client, layout_keyboard, layout_keyboard_num, handler);

        button_key_media_key_and_num_key = findViewById(R.id.button_key_media_key_and_num_key);
    }
    private void initView() {
        image_touch_pad_lock = findViewById(R.id.image_touch_pad_lock);
        image_touch_pad_mode = findViewById(R.id.image_touch_pad_mode);
        text_touch_pad_mode = findViewById(R.id.text_touch_pad_mode);
        layout_key_controls = findViewById(R.id.layout_key_controls);

        layout_key_controls.setVisibility(View.GONE);
        image_touch_pad_lock.setVisibility(View.GONE);

        View image_key_controls_bg = findViewById(R.id.image_key_controls_bg);
        image_key_controls_bg.setVisibility(View.GONE);
        ImageButton button_touch_keyboard = findViewById(R.id.button_touch_keyboard);
        button_touch_keyboard.setOnClickListener((v) -> {
            if(layout_key_controls.getVisibility() == View.VISIBLE) {
                layout_key_controls.startAnimation(bottom_down_hide);
                layout_key_controls.setVisibility(View.GONE);
                button_touch_keyboard.setImageTintList(state_toolbar_normal);
                image_key_controls_bg.startAnimation(fade_hide);
                image_key_controls_bg.setVisibility(View.GONE);
            } else {
                layout_key_controls.startAnimation(bottom_up_show);
                layout_key_controls.setVisibility(View.VISIBLE);
                button_touch_keyboard.setImageTintList(state_toolbar_active);
                image_key_controls_bg.startAnimation(fade_show);
                image_key_controls_bg.setVisibility(View.VISIBLE);
            }
        });

        text_connect_status = findViewById(R.id.text_connect_status);
        text_connect_status.setOnClickListener((v) -> {
            if(!client.isConnectSuccess())
                client.start();
        });

        Button button_left = findViewById(R.id.button_left);
        ImageButton button_middle = findViewById(R.id.button_middle);
        Button button_right = findViewById(R.id.button_right);
        mouse_pad = findViewById(R.id.mouse_pad);
        scroll_pad = findViewById(R.id.scroll_pad);
        button_touch_sensor = findViewById(R.id.button_touch_sensor);
        button_touch_hand = findViewById(R.id.button_touch_hand);

        titleBar = findViewById(R.id.titlebar);
        titleBar.setLeftIconOnClickListener((v) -> onNeedFinish());
        titleBar.setRightIconOnClickListener((v) ->
                startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_SETTINGS));

        button_left.setOnTouchListener((v, event) -> {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN: {
                    handleButtonDown(MouseInstruction.BUTTON_LEFT);
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    handleButtonUp(MouseInstruction.BUTTON_LEFT);
                    v.performClick();
                    break;
                }
            }
            return true;
        });
        button_right.setOnTouchListener((v, event) -> {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN: {
                    handleButtonDown(MouseInstruction.BUTTON_RIGHT);
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    handleButtonUp(MouseInstruction.BUTTON_RIGHT);
                    v.performClick();
                    break;
                }
            }
            return true;
        });
        button_middle.setOnTouchListener((v, event) -> {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN: {
                    buttonMiddleDown = true;
                    buttonMiddleMoved = false;
                    buttonMiddleDownSended = false;
                    buttonMiddleTouchLastY = 0;
                    buttonMiddleTouchCount = 1;
                    handleScrollReset();
                    if(event.getPointerCount() == 1)
                        handler.sendEmptyMessageDelayed(MSG_LATE_MIDDLE_DOWN, 400);
                    break;
                }
                case MotionEvent.ACTION_POINTER_DOWN:
                    buttonMiddleTouchCount = event.getPointerCount();
                    break;
                case MotionEvent.ACTION_UP: {
                    buttonMiddleDown = false;
                    if (buttonMiddleDownSended)
                        lateMiddleUp();
                    v.performClick();
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    if(buttonMiddleDown) {
                        buttonMiddleMoved = true;
                        handleScroll(event, false);
                    }
                    break;
                }
            }
            return true;
        });
        scroll_pad.setOnTouchListener((v, event) -> {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN: {
                    buttonMiddleDown = true;
                    buttonMiddleTouchLastY = 0;
                    handleScrollReset();
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    buttonMiddleDown = false;
                    v.performClick();
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    if(buttonMiddleDown)
                        handleScroll(event, true);
                    return false;
                }
            }
            return true;
        });

        button_touch_hand.setOnClickListener((v) -> setTouchPadMode(false));
        button_touch_sensor.setOnClickListener((v) -> setTouchPadMode(true));

        button_lock_sensor = findViewById(R.id.button_lock_sensor);
        button_lock_sensor.setOnTouchListener((v, event) -> {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN: {
                    sensorLocked = true;
                    image_touch_pad_lock.setVisibility(View.VISIBLE);
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    sensorLocked = false;
                    image_touch_pad_lock.setVisibility(View.GONE);
                    v.performClick();
                    break;
                }
            }
            return false;
        });

        ImageButton button_touch_power = findViewById(R.id.button_touch_power);
        button_touch_power.setOnClickListener((View v) -> menuPowerControl.show(button_touch_power));


    }
    private void initClient() {
        int index = getIntent().getIntExtra("devIndex", -1);
        if (index < 0) {
            finish();
            return;
        }

        currentDev = dataService.getConnectDevices().get(index);

        client = new MouseClient();
        client.setPassword(currentDev.getLastPass());
        client.setTargetDevice(currentDev);
        client.setOnMouseClientStatusChangedListener((status) -> runOnUiThread(() -> {
            switch (status) {
                case MouseClient.STATUS_NEED_PASSWORD:
                    onNeedPassAndReConnect();
                    break;
                case MouseClient.STATUS_END:
                    onNeedFinish();
                    break;
                case MouseClient.STATUS_CONNECTING: {
                    text_connect_status.setText(R.string.text_connecting);
                    text_connect_status.setCompoundDrawables(null,null,null,null);
                    break;
                }
                case MouseClient.STATUS_COON_LOST:
                    onConnectLost();
                    break;
                case MouseClient.STATUS_FAILED:
                    onConnectFailed();
                    break;
                case MouseClient.STATUS_OK:
                    onConnectSuccess();
                    break;
                default: {
                    break;
                }
            }
        }));

        mouse_pad.setMouseClient(client);
        titleBar.setTitle(currentDev.getTargetHostname());
    }
    private void initSensor() {
        orientationSensor1Provider = new ImprovedOrientationSensor1Provider(
                (SensorManager) getSystemService(Activity.SENSOR_SERVICE));
    }
    private void initVibrator() {
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrationEffectShort = VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE);
    }
    private void initMenu() {
        menuPowerControl= new PopupMenuView(this);
        menuPowerControl.setMenuItems(Arrays.asList(
                new OptionMenu(getString(R.string.text_touchpad_pc_power_shutdown)),
                new OptionMenu(getString(R.string.text_touchpad_pc_power_reboot)),
                new OptionMenu(getString(R.string.text_touchpad_pc_power_logoff)),
                new OptionMenu(getString(R.string.text_touchpad_pc_power_sleep))
        ));
        menuPowerControl.setOrientation(LinearLayout.VERTICAL);
        menuPowerControl.setOnMenuClickListener((position, menu) -> {
            if(position == 3) {
                //Sleep key
                KeyInstruction key = client.requestKeyInstruction();
                key.setKey(KeyInstruction.VK_SLEEP);
                key.setType(KeyInstruction.KEY_DOWN);
                client.pushInstruction(key);
                key = client.requestKeyInstruction();
                key.setKey(KeyInstruction.VK_SLEEP);
                key.setType(KeyInstruction.KEY_UP);
                client.pushInstruction(key);
            }
            else if(position == 0) {

                new CommonDialog(this)
                        .setTitle(String.format(getString(R.string.text_common_do_you_really_want_to),
                                getString(R.string.text_touchpad_pc_power_shutdown)))
                        .setPositive(R.string.action_yes)
                        .setNegative(R.string.action_no)
                        .setOnResult((r, dialog) -> {
                            if(r == CommonDialog.BUTTON_POSITIVE) {
                                ComputerInstruction instruction = new ComputerInstruction();
                                instruction.setType(ComputerInstruction.ACTION_SHUTDOWN);
                                client.pushInstruction(instruction);
                            }
                            return true;
                        }).show();

            }
            else if(position == 1) {

                new CommonDialog(this)
                        .setTitle(String.format(getString(R.string.text_common_do_you_really_want_to),
                                getString(R.string.text_touchpad_pc_power_reboot)))
                        .setPositive(R.string.action_yes)
                        .setNegative(R.string.action_no)
                        .setOnResult((r, dialog) -> {
                            if(r == CommonDialog.BUTTON_POSITIVE) {
                                ComputerInstruction instruction = new ComputerInstruction();
                                instruction.setType(ComputerInstruction.ACTION_REBOOT);
                                client.pushInstruction(instruction);
                            }
                            return true;
                        }).show();

            }
            else if(position == 2) {

                new CommonDialog(this)
                        .setTitle(String.format(getString(R.string.text_common_do_you_really_want_to),
                                getString(R.string.text_touchpad_pc_power_logoff)))
                        .setPositive(R.string.action_yes)
                        .setNegative(R.string.action_no)
                        .setOnResult((r, dialog) -> {
                            if(r == CommonDialog.BUTTON_POSITIVE) {
                                ComputerInstruction instruction = new ComputerInstruction();
                                instruction.setType(ComputerInstruction.ACTION_LOGOFF);
                                client.pushInstruction(instruction);
                            }
                            return true;
                        }).show();
            }
            return true;
        });
    }

    public void switchNumPadVisibleStatus() {
        if(layout_keyboard_num.getVisibility() == View.VISIBLE) {
            layout_keyboard_num.setVisibility(View.GONE);
            button_key_media_key_and_num_key.setBackgroundTintList(state_toolbar_normal);
        } else {
            layout_keyboard_num.setVisibility(View.VISIBLE);
            button_key_media_key_and_num_key.setBackgroundTintList(state_toolbar_active);
        }
    }
    private void setTouchPadMode(boolean isSensor) {
        useSensorMouse = isSensor;
        mouse_pad.setEnableMouse(!isSensor);
        scroll_pad.setVisibility((!isSensor && showScrollBar) ? View.VISIBLE : View.GONE);
        if(isSensor) {
            button_touch_sensor.setImageTintList(state_toolbar_active);
            button_touch_hand.setImageTintList(state_toolbar_normal);

            image_touch_pad_mode.setImageResource(R.drawable.ic_touchpad_sensor);
            text_touch_pad_mode.setText(R.string.text_touchpad_mode_sensor);

            button_lock_sensor.setVisibility(View.VISIBLE);
        } else {
            button_touch_sensor.setImageTintList(state_toolbar_normal);
            button_touch_hand.setImageTintList(state_toolbar_active);

            image_touch_pad_mode.setImageResource(R.drawable.ic_touchpad_hand);
            text_touch_pad_mode.setText(R.string.text_touchpad_mode_touch);

            button_lock_sensor.setVisibility(View.GONE);
        }
    }

    private void lateMiddleDown() {
        if(buttonMiddleDown && !buttonMiddleMoved && buttonMiddleTouchCount == 1) {
            buttonMiddleDownSended = true;
            handleButtonDown(MouseInstruction.BUTTON_MIDDLE);
        }
    }
    private void lateMiddleUp() {
        handleButtonUp(MouseInstruction.BUTTON_MIDDLE);
    }
    private void handleButtonDown(int button) {
        MouseInstruction instruction = client.requestMouseInstruction();
        instruction.setType(MouseInstruction.MOUSE_DOWN);
        instruction.setButton(button);
        client.pushInstruction(instruction);

        if(touchVibrator)
            vibrator.vibrate(vibrationEffectShort);
    }
    private void handleButtonUp(int button) {
        MouseInstruction instruction = client.requestMouseInstruction();
        instruction.setType(MouseInstruction.MOUSE_UP);
        instruction.setButton(button);
        client.pushInstruction(instruction);
    }

    private int scrollYAccumulativeValue = 0;
    private boolean scrollYInterruptLateSend = false;

    private void handleScrollReset() {
        scrollYAccumulativeValue = 0;
        scrollYInterruptLateSend = false;
    }
    private void handleScrollLateSend() {
        if(!scrollYInterruptLateSend && scrollYAccumulativeValue != 0) {
            handleScrollSend(scrollYAccumulativeValue);
            scrollYAccumulativeValue = 0;
        }
    }
    private void handleScrollSend(int y) {
        MouseInstruction mouseInstruction = client.requestMouseInstruction();

        mouseInstruction.setType(MouseInstruction.MOUSE_SCROLL);
        //noinspection SuspiciousNameCombination
        mouseInstruction.setX(scrollTickHeight);
        mouseInstruction.setY(y);

        client.pushInstruction(mouseInstruction);
    }
    private void handleScroll(MotionEvent event, boolean isLong) {
        if(buttonMiddleTouchLastY != 0) {

            scrollYInterruptLateSend = true;
            float deltaY = buttonMiddleTouchLastY - event.getY();
            int finalY = (int)(deltaY * scrollSensitivity / (isLong ? 3 : 1));
            if(Math.abs(finalY) < scrollTickHeight) {
                scrollYAccumulativeValue += finalY;
                scrollYInterruptLateSend = false;
                handler.sendEmptyMessageDelayed(MSG_LATE_SEND_SCROLL, 200);
            } else {
                handleScrollSend(scrollYAccumulativeValue + finalY);
                scrollYAccumulativeValue = 0;
            }
        }

        buttonMiddleTouchLastY = event.getY();
    }
    private void handleKeyDown(int key) {
        KeyInstruction instruction = client.requestKeyInstruction();
        instruction.setType(KeyInstruction.KEY_DOWN);
        instruction.setKey(key);
        client.pushInstruction(instruction);
    }
    private void handleKeyUp(int key) {
        KeyInstruction instruction = client.requestKeyInstruction();
        instruction.setType(KeyInstruction.KEY_UP);
        instruction.setKey(key);
        client.pushInstruction(instruction);
    }

    //Settings
    //======================================

    private static final int VOL_KEY_NO_USAGE = 0;
    private static final int VOL_KEY_CTL_COMPUTER = 1;
    private static final int VOL_KEY_AS_LEFT_AND_RIGHT = 2;

    private float scrollSensitivity = 1.0f;
    private float touchSensitivity = 1.0f;
    private float sensorSensitivity = 1.0f;
    private boolean touchVibrator = true;
    private boolean useSensorMouse = false;
    private boolean keepScreenOn = true;
    private boolean showScrollBar = true;
    private int volKeyUsage = VOL_KEY_NO_USAGE;

    private void loadSettings() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        volKeyUsage = sharedPreferences.getInt("volume_key_use_to", volKeyUsage);
        touchSensitivity = sharedPreferences.getInt("touch_sensitivity", 100) / 100.0f + 1.0f;
        scrollSensitivity = sharedPreferences.getInt("scroll_sensitivity", 100) / 100.0f - 0.1f;
        sensorSensitivity = sharedPreferences.getInt("move_sensitivity", 100) / 100.0f + 4.0f;
        touchVibrator = sharedPreferences.getBoolean("vibrate_touch", touchVibrator);
        useSensorMouse = sharedPreferences.getBoolean("sensor_mouse", useSensorMouse);
        showScrollBar = sharedPreferences.getBoolean("show_scroll_bar", showScrollBar);
        keepScreenOn = sharedPreferences.getBoolean("show_scroll_bar", keepScreenOn);
    }
    private void applySettings() {
        mouse_pad.setScrollSensitivity(scrollSensitivity);
        mouse_pad.setTouchSensitivity(touchSensitivity);
        setTouchPadMode(useSensorMouse);
        if(keepScreenOn)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    private void saveSettings() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit()
                .putBoolean("sensor_mouse", useSensorMouse)
                .apply();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN: {
                if (volKeyUsage == VOL_KEY_CTL_COMPUTER) {
                    handleKeyUp(KeyInstruction.VK_VOLUME_DOWN);
                    return true;
                }
                else if (volKeyUsage == VOL_KEY_AS_LEFT_AND_RIGHT) {
                    handleButtonUp(MouseInstruction.BUTTON_RIGHT);
                    return true;
                }
                break;
            }
            case KeyEvent.KEYCODE_VOLUME_UP: {
                if(volKeyUsage == VOL_KEY_CTL_COMPUTER){
                    handleKeyUp(KeyInstruction.VK_VOLUME_UP);
                    return true;
                }
                else if(volKeyUsage == VOL_KEY_AS_LEFT_AND_RIGHT) {
                    handleButtonUp(MouseInstruction.BUTTON_LEFT);
                    return true;
                }
                break;
            }
        }
        return super.onKeyUp (keyCode, event);
    }
    @Override
    public boolean onKeyDown (int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN: {
               if(volKeyUsage == VOL_KEY_CTL_COMPUTER){
                   handleKeyDown(KeyInstruction.VK_VOLUME_DOWN);
                   return true;
               }
               else if(volKeyUsage == VOL_KEY_AS_LEFT_AND_RIGHT) {
                   handleButtonDown(MouseInstruction.BUTTON_RIGHT);
                   return true;
               }
               break;
            }
            case KeyEvent.KEYCODE_VOLUME_UP: {
                if(volKeyUsage == VOL_KEY_CTL_COMPUTER){
                    handleKeyDown(KeyInstruction.VK_VOLUME_UP);
                    return true;
                }
                else if(volKeyUsage == VOL_KEY_AS_LEFT_AND_RIGHT) {
                    handleButtonDown(MouseInstruction.BUTTON_LEFT);
                    return true;
                }
                break;
            }
        }
        return super.onKeyDown (keyCode, event);
    }
    @Override
    protected void onPause() {
        if (client != null)
            client.pause();
        if(sensorMoveTimer != null) {
            sensorMoveTimer.cancel();
            sensorMoveTimer = null;
        }
        orientationSensor1Provider.stop();
        saveSettings();
        super.onPause();
    }
    @Override
    protected void onResume() {
        if (client != null) {
            handler.sendEmptyMessageDelayed(MSG_LATE_START, 500);
            client.start();
        }
        if(sensorMoveTimer == null) {
            sensorMoveTimer = new Timer();
            sensorMoveTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(useSensorMouse)
                        onUpdateMouseSensor();
                }
            }, 2000, MOUSE_SENSOR_UPDATE_TIME);
        }
        orientationSensor1Provider.start();
        super.onResume();
    }
    @Override
    public void onBackPressed() {
        onNeedFinish();
    }

    private void onConnectLost() {

        text_connect_status.setText(R.string.text_connection_lost);
        text_connect_status.setCompoundDrawables(ic_bad, null,null,null);

        new CommonDialog(this)
                .setCancelable(false)
                .setTitle(R.string.text_error_connect)
                .setPositive(R.string.action_reconnect)
                .setNegative(R.string.action_cancel)
                .setOnResult((button, dialog) -> {
                    if(button == CommonDialog.BUTTON_POSITIVE)
                        client.start();
                    else if(button == CommonDialog.BUTTON_NEGATIVE)
                        onNeedFinish();
                    return true;
                }).show();
    }
    private void onConnectSuccess() {


        text_connect_status.setText(R.string.text_connect_success);
        text_connect_status.setCompoundDrawables(ic_ok, null,null,null);

        currentDev.setConnectSuccessCount(currentDev.getConnectSuccessCount() + 1);
        currentDev.setLastConnectTime(new Date());
    }
    private void onConnectFailed() {

        if(client == null)
            return;

        text_connect_status.setText(R.string.text_connect_failed);
        text_connect_status.setCompoundDrawables(ic_bad, null,null,null);

        String err;
        int errCode = client.getLastErr();
        switch (errCode) {
            case MouseClient.ERROR_PASS_ERROR:
                err = getString(R.string.text_error_pass_err);
                currentDev.setLastPass("");
                client.setPassword("");
                break;
            case MouseClient.ERROR_TIME_OUT:
                err = getString(R.string.text_error_connect_time_out);
                break;
            case MouseClient.ERROR_RET_ERROR:
                err = getString(R.string.text_error_server_err);
                break;
            case MouseClient.ERROR_EXCEPTION:
                err = client.getLastErrString();
                break;
            default:
                err = getString(R.string.text_error_unknown_err);
                break;
        }

        new CommonDialog(this)
                .setCancelable(false)
                .setTitle(R.string.text_error_connect)
                .setMessage(err)
                .setPositive(R.string.action_reconnect)
                .setNegative(R.string.action_cancel)
                .setOnResult((button, dialog) -> {
                    if(button == CommonDialog.BUTTON_POSITIVE) {
                        if(errCode ==  MouseClient.ERROR_PASS_ERROR) {
                            onNeedPassAndReConnect();
                        } else {
                            currentDev.setLastPass(dialog.getEditTextValue().toString());
                            client.setPassword(currentDev.getLastPass());
                            client.start();
                        }
                        return true;
                    } else if(button == CommonDialog.BUTTON_NEGATIVE) {
                        onNeedFinish();
                        return true;
                    }
                    return false;
                }).show();
    }
    private void onNeedPassAndReConnect() {
        new CommonDialog(this)
                .setCancelable(false)
                .setTitle(R.string.text_enter_password)
                .setEditTextHint(R.string.text_password)
                .setEditInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .setOnEditTextChangedListener((editable, dialog) ->
                    dialog.setPositiveEnable(editable.length() > 0))
                .setPositive(R.string.action_ok)
                .setNegative(R.string.action_cancel)
                .setOnResult((button, dialog) -> {
                    if(button == CommonDialog.BUTTON_POSITIVE) {
                        currentDev.setLastPass(dialog.getEditTextValue().toString());
                        client.setPassword(currentDev.getLastPass());
                        client.start();
                        return true;
                    }
                    else if(button == CommonDialog.BUTTON_NEGATIVE) {
                        onNeedFinish();
                        return true;
                    }
                    return false;
                })
                .show();
    }
    private void onNeedFinish() {
        Intent bundle = new Intent();
        bundle.putExtra("devIndex", dataService.getConnectDevices().indexOf(currentDev));
        setResult(Activity.RESULT_OK, bundle);
        finish();
    }

    private static final int MSG_LATE_MIDDLE_DOWN = 6;
    private static final int MSG_LATE_SEND_SCROLL = 7;
    private static final int MSG_LATE_START = 8;
    public static final int MSG_VIBRATOR = 9;
    public static final int MSG_LATE_TEST_CAPS_STATE = 10;
    public static final int MSG_LATE_TEST_NUM_STATE = 11;

    private final MyHandler handler = new MyHandler(this);

    private ImprovedOrientationSensor1Provider orientationSensor1Provider = null;

    private static final int MOUSE_SENSOR_UPDATE_TIME = 100;

    private float xSensorSpeedLast = 0, ySensorSpeedLast = 0;
    private Timer sensorMoveTimer = null;
    private boolean sensorLocked = false;

    private void onUpdateMouseSensor() {

        float [] eulerAngles = new float[3];
        orientationSensor1Provider.getEulerAngles(eulerAngles);

        float xSensorSpeed = eulerAngles[0];
        float ySensorSpeed = eulerAngles[1];

        float xSensorSpeedDelta = (xSensorSpeed - xSensorSpeedLast) * 100,
                ySensorSpeedDelta = (ySensorSpeed - ySensorSpeedLast) * 100;

        xSensorSpeedLast = xSensorSpeed;
        ySensorSpeedLast = ySensorSpeed;

        if(sensorLocked)
            return;

        float xabs = Math.abs(xSensorSpeedDelta), yabs = Math.abs(ySensorSpeedDelta);
        float xspc = 0, yspc = 0;
        if (xabs < 0 || xabs > 180)
            xSensorSpeedDelta = 0;
        else if(xabs < 10)
            xspc = 1+ (10 - xabs) / 10.0f;
        else if(xabs >= 10)
            xspc = 1;

        if (yabs < 0 || yabs > 180)
            ySensorSpeedDelta = 0;
        else if(yabs < 10)
            yspc = 1+ (10 - xabs) / 10.0f;
        else if(yabs >= 10)
            yspc = 1;

        if (xSensorSpeedDelta != 0 || ySensorSpeedDelta != 0) {

            MouseInstruction mouseInstruction = client.requestMouseInstruction();
            mouseInstruction.setType(MouseInstruction.MOUSE_MOVE);
            mouseInstruction.setX(-(int) ((xSensorSpeedDelta) * sensorSensitivity * xspc));
            mouseInstruction.setY(-(int) ((ySensorSpeedDelta) * sensorSensitivity * yspc));
            client.pushInstruction(mouseInstruction);
        }
    }

    private static class MyHandler extends Handler {

        private final WeakReference<MouseActivity> mouseViewWeakReference;

        public MyHandler(MouseActivity view) {
            super(Looper.myLooper());
            mouseViewWeakReference = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_LATE_MIDDLE_DOWN:
                    mouseViewWeakReference.get().lateMiddleDown();
                    break;
                case MSG_LATE_SEND_SCROLL:
                    mouseViewWeakReference.get().handleScrollLateSend();
                    break;
                case MSG_LATE_START:
                    mouseViewWeakReference.get().client.start();
                    break;
                case MSG_VIBRATOR:
                    mouseViewWeakReference.get().vibrator.vibrate(mouseViewWeakReference.get().vibrationEffectShort);
                    break;
                case MSG_LATE_TEST_CAPS_STATE:
                    mouseViewWeakReference.get().keypadHandler.sendTestCapsStat();
                    break;
                case MSG_LATE_TEST_NUM_STATE:
                    mouseViewWeakReference.get().keypadHandler.sendTestNumStat();
                    break;
            }
            super.handleMessage(msg);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_SETTINGS) {
            if(data !=  null && data.getBooleanExtra("needRestart", false)) {
                Intent intent = new Intent();
                intent.putExtra("needRestart", true);
                intent.putExtra("restartDevAddress", currentDev.getTargetAddress());
                setResult(Activity.RESULT_OK, intent);
                finish();
            } else {
                loadSettings();
                applySettings();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}