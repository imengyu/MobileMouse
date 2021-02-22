package com.imengyu.mengui.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.Size;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.imengyu.mengui.R;

/**
 * 弹出框工具类
 */
public class AlertDialogTool {

    /**
     * 设置底部弹出对话框
     * @param dialog 对话框
     * @param width 对话框宽度
     * @param height 对话框高度
     */
    public static void bottomDialogSizeAutoSet(Dialog dialog, int width, int height) {
        Context context = dialog.getContext();
        ViewGroup viewGroup = dialog.findViewById(R.id.layout_dialog);

        if (viewGroup != null) {
            ViewGroup.LayoutParams layoutParams = viewGroup.getLayoutParams();
            layoutParams.width = width;

            //设置对话框在宽屏模式下的最宽宽度
            if (width == ViewGroup.LayoutParams.MATCH_PARENT) {
                Point point = new Point();
                context.getDisplay().getRealSize(point);
                Size screenSize = new Size(point.x, point.y);

                Configuration mConfiguration = context.getResources().getConfiguration(); //获取设置的配置信息
                if (mConfiguration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    layoutParams.width = screenSize.getWidth() > PixelTool.dp2px(context, 500) ?
                            PixelTool.dp2px(context, 500) : width;
                } else if (mConfiguration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    layoutParams.width = width;
                }
            }

            layoutParams.height = height;
            viewGroup.setLayoutParams(layoutParams);
        }
    }

    /**
     * 设置Activity遮罩透明度
     * @param activity Activity
     * @param alpha 遮罩透明度(0-1)
     */
    public static void backgroundAlpha(Activity activity, float alpha) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = alpha;
        activity.getWindow().setAttributes(lp);
    }
}
