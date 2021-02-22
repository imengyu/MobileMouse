package com.imengyu.mobilemouse.mouse;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.imengyu.mengui.utils.ScreenUtils;

import java.lang.ref.WeakReference;

public class MouseView extends View {

    public MouseView(Context context) {
        super(context);
        init(context);
    }
    public MouseView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public MouseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    public MouseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        if(!isInEditMode()) {
            Size screenSize = ScreenUtils.getScreenSize(context);
            scrollTickHeight = screenSize.getHeight() / 6;
            handler = new MyHandler(this);
        }
    }

    private int scrollTickHeight = 0;

    private MouseClient mouseClient = null;

    public void setScrollSensitivity(float scrollSensitivity) {
        this.scrollSensitivity = scrollSensitivity;
    }
    public MouseClient getMouseClient() {
        return mouseClient;
    }
    public void setMouseClient(MouseClient mouseClient) {
        this.mouseClient = mouseClient;
    }
    public float getTouchSensitivity() {
        return touchSensitivity;
    }
    public void setTouchSensitivity(float touchSensitivity) {
        this.touchSensitivity = touchSensitivity;
    }

    private boolean enableMouse = true;

    public boolean isEnableMouse() {
        return enableMouse;
    }
    public void setEnableMouse(boolean enableMouse) {
        this.enableMouse = enableMouse;
    }

    //Handler
    //========================

    private static final int MSG_LATE_DOWN_LOCK = 3;
    private static final int MSG_LATE_UP_LOCK = 4;
    private static final int MSG_LATE_SEND_SCROLL = 7;
    private static final int MSG_LATE_UP = 8;

    private MyHandler handler = null;
    private static class MyHandler extends Handler {

        private final WeakReference<MouseView> mouseViewWeakReference;

        public MyHandler(MouseView view) {
            super(Looper.myLooper());
            mouseViewWeakReference = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_LATE_DOWN_LOCK:
                    mouseViewWeakReference.get().lateDown();
                    break;
                case MSG_LATE_SEND_SCROLL:
                    mouseViewWeakReference.get().handleScrollLateSend();
                    break;
                case MSG_LATE_UP_LOCK:
                    mouseViewWeakReference.get().touchUpLock = false;
                    break;
                case MSG_LATE_UP:
                    mouseViewWeakReference.get().handleUp();
                    break;

            }
            super.handleMessage(msg);
        }
    }

    //Touch settings
    //========================

    private float touchSensitivity = 1.0f;
    private float scrollSensitivity = 1.0f;

    //Touch event
    //========================

    private boolean touchDown = false;
    private boolean touchMoved = false;
    private int touchPointCountDown = 0;
    private boolean touchHastMultPoint = false;
    private float touchLastX = 0;
    private float touchLastY = 0;

    private boolean touchDownLock = false;
    private boolean touchUpLock = false;

    private void lateDown() {
        if(touchDownLock) {
            touchDownLock = false;

            if (!touchHastMultPoint)
                touchPointCountDown = 1;
            if (touchMoved)
                touchDown = false;
            else {

                touchDown = true;
                MouseInstruction mouseInstruction = mouseClient.requestMouseInstruction();
                mouseInstruction.setType(MouseInstruction.MOUSE_DOWN);

                if (touchPointCountDown == 1)
                    mouseInstruction.setButton(MouseInstruction.BUTTON_LEFT);
                else if (touchPointCountDown == 2)
                    mouseInstruction.setButton(MouseInstruction.BUTTON_RIGHT);
                else if (touchPointCountDown == 3)
                    mouseInstruction.setButton(MouseInstruction.BUTTON_MIDDLE);

                mouseClient.pushInstruction(mouseInstruction);
            }
        }
    }
    private void handleUp() {
        if(touchUpLock)
            return;

        //touchUpLock = true;
        //handler.sendEmptyMessageDelayed(MSG_LATE_UP_LOCK, 100);

        if(touchDownLock) {
            touchDownLock = false;

            MouseInstruction mouseInstruction = mouseClient.requestMouseInstruction();
            mouseInstruction.setType(MouseInstruction.MOUSE_DOWN);

            if (touchPointCountDown == 1)
                mouseInstruction.setButton(MouseInstruction.BUTTON_LEFT);
            else if (touchPointCountDown == 2)
                mouseInstruction.setButton(MouseInstruction.BUTTON_RIGHT);
            else if (touchPointCountDown == 3)
                mouseInstruction.setButton(MouseInstruction.BUTTON_MIDDLE);

            mouseClient.pushInstruction(mouseInstruction);
            touchDown = true;
        }
        if(touchDown) {
            touchDown = false;

            MouseInstruction mouseInstruction = mouseClient.requestMouseInstruction();
            mouseInstruction.setType(MouseInstruction.MOUSE_UP);

            if (touchPointCountDown == 1)
                mouseInstruction.setButton(MouseInstruction.BUTTON_LEFT);
            else if (touchPointCountDown == 2)
                mouseInstruction.setButton(MouseInstruction.BUTTON_RIGHT);
            else if (touchPointCountDown == 3)
                mouseInstruction.setButton(MouseInstruction.BUTTON_MIDDLE);

            mouseClient.pushInstruction(mouseInstruction);
        }
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
        MouseInstruction mouseInstruction = mouseClient.requestMouseInstruction();

        mouseInstruction.setType(MouseInstruction.MOUSE_SCROLL);
        //noinspection SuspiciousNameCombination
        mouseInstruction.setX(scrollTickHeight);
        mouseInstruction.setY(y);

        mouseClient.pushInstruction(mouseInstruction);
    }
    private void handleScroll(float deltaY) {

        scrollYInterruptLateSend = true;
        int finalY = (int)(deltaY * scrollSensitivity);
        if(Math.abs(finalY) < scrollTickHeight) {
            scrollYAccumulativeValue += finalY;
            scrollYInterruptLateSend = false;
            handler.sendEmptyMessageDelayed(MSG_LATE_SEND_SCROLL, 200);
        } else {
            handleScrollSend(scrollYAccumulativeValue + finalY);
            scrollYAccumulativeValue = 0;
        }

    }

    private static final float TOUCH_MOVE_MIN_DELTA = 20.0f;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mouseClient == null || !enableMouse)
            return super.onTouchEvent(event);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                if(!touchDownLock) {
                    handleScrollReset();
                    touchDownLock = true;
                    touchLastX = touchLastY = 0;
                    touchMoved = touchHastMultPoint = false;
                    handler.sendEmptyMessageDelayed(MSG_LATE_DOWN_LOCK, 220);
                }
                return true;
            }
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP: {
                touchPointCountDown = event.getPointerCount();
                touchHastMultPoint = true;
                return true;
            }
            case MotionEvent.ACTION_UP: {
                if (event.getPointerCount() == 1) {
                    handler.sendEmptyMessageDelayed(MSG_LATE_UP, 30);
                    performClick();
                }
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                if (touchLastX != 0 && touchLastY != 0) {
                    float deltaX = touchLastX - event.getX();
                    float deltaY = touchLastY - event.getY();

                    float xabs = Math.abs(deltaX), yabs = Math.abs(deltaY);
                    if(Math.abs(deltaX) >= 2 || Math.abs(deltaY) >= 2)
                        touchMoved = true;

                    float xspc = 0, yspc = 0;
                    if (xabs == 0 || xabs > 300)
                        deltaX = 0;
                    else
                        xspc = Math.min(0.5f + xabs / TOUCH_MOVE_MIN_DELTA, 1.0f);

                    if (yabs == 0 || yabs > 300)
                        deltaY = 0;
                    else
                        yspc = Math.min(0.5f + yabs / TOUCH_MOVE_MIN_DELTA, 1.0f);

                    if (!touchDownLock) {

                        if (event.getPointerCount() == 1) {
                            MouseInstruction mouseInstruction = mouseClient.requestMouseInstruction();
                            mouseInstruction.setType(MouseInstruction.MOUSE_MOVE);
                            mouseInstruction.setX((int) (deltaX * touchSensitivity * xspc));
                            mouseInstruction.setY((int) (deltaY * touchSensitivity * yspc));
                            mouseClient.pushInstruction(mouseInstruction);
                        }
                        else if (event.getPointerCount() >= 2) {
                            handleScroll((int) (deltaY * (scrollSensitivity)));
                        }
                    }
                }

                touchLastX = event.getX();
                touchLastY = event.getY();

                return true;
            }
        }
        return super.onTouchEvent(event);
    }
    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
