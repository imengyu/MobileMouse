package com.imengyu.mengui.toast;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.imengyu.mengui.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 一个简单的Toast
 */
public class SmallToast {

    /**
     * 显示 Toast
     * @param context 上下文
     * @param strResId 字符串资源ID
     * @param duration 显示时长（毫秒），可以是 SmallToast.LENGTH_SHORT 或 SmallToast.LENGTH_LONG
     * @return 返回 Toast 实例
     */
    public static SmallToast makeText(Context context, int strResId, int duration) {
        return makeText(context, context.getString(strResId), null, duration);
    }
    /**
     * 显示 Toast
     * @param context 上下文
     * @param str 字符串
     * @param duration 显示时长（毫秒），可以是 SmallToast.LENGTH_SHORT 或 SmallToast.LENGTH_LONG
     * @return 返回 Toast 实例
     */
    public static SmallToast makeText(Context context, CharSequence str, int duration) {
        return makeText(context, str, null, duration);
    }
    /**
     * 显示 Toast
     * @param context 上下文
     * @param strResId 字符串资源ID
     * @param imageResId 图标资源ID
     * @param duration 显示时长（毫秒），可以是 SmallToast.LENGTH_SHORT 或 SmallToast.LENGTH_LONG
     * @return 返回 Toast 实例
     */
    public static SmallToast makeText(Context context, int strResId, int imageResId, int duration) {
        SmallToast result = new SmallToast(context);
        result.text = context.getString(strResId);
        result.duration = duration;
        result.imageResId = imageResId;
        return result;
    }
    /**
     * 显示 Toast
     * @param context 上下文
     * @param str 字符串
     * @param duration 显示时长（毫秒），可以是 SmallToast.LENGTH_SHORT 或 SmallToast.LENGTH_LONG
     * @return 返回 Toast 实例
     */
    public static SmallToast makeText(Context context, CharSequence str, Drawable imageDrawable, int duration) {
        SmallToast result = new SmallToast(context);
        result.text = str.toString();
        result.duration = duration;
        result.imageDrawable = imageDrawable;
        return result;
    }

    public static final int LENGTH_SHORT = 1600;
    public static final int LENGTH_LONG = 3000;

    private SmallToast(Context context) {
        this.context = context;
        initPopup();
    }

    private static final int MESSAGE_HIDE_TOAST = 2;
    private static class SmallToastPopupWindow extends PopupWindow {

        @SuppressLint("InflateParams")
        public SmallToastPopupWindow(Context context) {
            super(context);

            layout_toast = LayoutInflater.from(context)
                    .inflate(R.layout.layout_toast, null);
            text = layout_toast.findViewById(R.id.text);
            image = layout_toast.findViewById(R.id.image);
            setContentView(layout_toast);
            setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            setAnimationStyle(R.style.DialogZoomPopup);
        }

        private final TextView text;
        private final ImageView image;
        private SmallToast usingToast;
        private final View layout_toast;

        public View getLayoutToast() {
            return layout_toast;
        }
        public SmallToast getUsingToast() {
            return usingToast;
        }
        public void setUsingToast(SmallToast usingToast) {
            this.usingToast = usingToast;
        }
        public void setText(String text) {
            this.text.setText(text);
        }
        public void setImageResource(int id) {
            if(id == 0)
                image.setVisibility(View.GONE);
            else {
                image.setVisibility(View.VISIBLE);
                image.setImageResource(id);
            }
        }
        public void setImageDrawable(Drawable drawable) {
            if(drawable == null)
                image.setVisibility(View.GONE);
            else {
                image.setVisibility(View.VISIBLE);
                image.setImageDrawable(drawable);
            }
        }
    }
    private static class SmallToastHandler extends Handler {
        public SmallToastHandler() {
            super(Looper.myLooper());
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MESSAGE_HIDE_TOAST) {
                SmallToast toast = (SmallToast)msg.obj;
                if(toast.isShow) {
                    toast.isShow = false;
                    toast.popup.dismiss();
                    listToastItems.remove(toast);
                }
            }
            super.handleMessage(msg);
        }
    }

    private static HashMap<Context, SmallToastPopupWindow> listToastPopups = null;
    private static List<SmallToast> listToastItems = null;
    private static Handler toastHandler = null;

    public static void init() {
        listToastPopups = new HashMap<>();
        listToastItems = new ArrayList<>();
        toastHandler = new SmallToastHandler();
    }
    public static void destroy() {
        listToastPopups.clear();
        for(SmallToast toast : listToastItems) {
            if(toast.isShow())
                toast.dismiss();
        }
        listToastItems.clear();
        toastHandler = null;
    }

    /**
     * 获取是否正在显示
     */
    public boolean isShow() {
        return isShow;
    }

    private boolean isShow = false;
    private final Context context;
    private String text;
    private int duration = LENGTH_LONG;
    private int imageResId = -1;
    private Drawable imageDrawable = null;
    private SmallToastPopupWindow popup;

    private void initPopup()  {

        if(duration <= 0)
            throw new RuntimeException("duration must be greater than 0");

        popup = listToastPopups.get(context);
        if (popup == null) {
            popup = new SmallToastPopupWindow(context);
            listToastPopups.put(context, popup);
        }

        listToastItems.add(this);
    }

    /**
     * 关闭提示
     */
    public void dismiss() {
        if(popup.getUsingToast() == this && isShow) {
            isShow = false;
            popup.setUsingToast(null);
            popup.dismiss();
            listToastItems.remove(this);
        }
    }
    /**
     * 显示提示
     */
    public void show() {

        if(!listToastItems.contains(this))
            throw new RuntimeException("This toast already dismissed, it can not be use anymore");
        if(isShow)
            return;

        isShow = true;
        popup.setText(text);
        if (imageDrawable != null)
            popup.setImageDrawable(imageDrawable);
        else if (imageResId != -1)
            popup.setImageResource(imageResId);
        else
            popup.setImageDrawable(null);

        SmallToast last = popup.getUsingToast();
        if(last != null)
            last.isShow = false;

        popup.setUsingToast(this);

        if(context instanceof Activity) {
            popup.showAtLocation(((Activity)context).getWindow().getDecorView(),
                    Gravity.CENTER, 0, 200);
        } else {
            popup.showAtLocation(null, Gravity.CENTER, 0, 200);
        }

        if(toastHandler != null) {

            Message message = new Message();
            message.what = MESSAGE_HIDE_TOAST;
            message.obj = this;
            toastHandler.sendMessageDelayed(message, duration);
        }
    }
}
