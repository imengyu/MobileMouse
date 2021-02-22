package com.imengyu.mengui.dialog.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.imengyu.mengui.R;
import com.imengyu.mengui.dialog.CommonDialogInternal;
import com.imengyu.mengui.utils.AlertDialogTool;
import com.imengyu.mengui.utils.KeyBoardUtil;
import com.imengyu.mengui.utils.NavigationBarUtils;
import com.imengyu.mengui.utils.PixelTool;
import com.imengyu.mengui.utils.ScreenUtils;
import com.imengyu.mengui.utils.StatusBarUtils;


public class CommonDialogFragment extends DialogFragment {

    public CommonDialogFragment() {
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
    }

    private Context context;
    private CommonDialogInternal commonDialogInternal;

    public CommonDialogInternal getCommonDialogInternal() { return commonDialogInternal; }
    public CommonDialogInternal createCommonDialogInternal(Context context) {
        commonDialogInternal = new CommonDialogInternal(context);
        return commonDialogInternal;
    }

    private int keyBoardHeight = 0;
    private boolean isKeyBoardVisible = false;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        context = requireContext();
        StatusBarUtils.setDarkMode(commonDialogInternal.getWindow());

        View decorView = commonDialogInternal.getWindow().getDecorView();
        int navHeight = NavigationBarUtils.getNavigationBarHeight(context);

        decorView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();

            decorView.getWindowVisibleDisplayFrame(r);

            int screenHeight = decorView.getRootView().getHeight();
            int newKeyboardHeight = screenHeight - (r.bottom) - navHeight;

            if (keyBoardHeight != newKeyboardHeight) {
                keyBoardHeight = newKeyboardHeight;
                isKeyBoardVisible = (newKeyboardHeight > 100);
                onConfigurationChanged(context.getResources().getConfiguration());
            }
        });

        return commonDialogInternal;
    }

    @Override
    public void onResume() {

        commonDialogInternal.refreshViews();

        Window window = commonDialogInternal.getWindow();
        window.setWindowAnimations(R.style.DialogBottomPopup);

        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(params);

        onConfigurationChanged(getResources().getConfiguration());
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        AlertDialogTool.bottomDialogSizeAutoSet(commonDialogInternal,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        int navHeight = NavigationBarUtils.getNavigationBarHeight(context);
        commonDialogInternal.getWindow().getDecorView().setPadding(0,0,0, navHeight +
                (isKeyBoardVisible ? keyBoardHeight : 0));

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            commonDialogInternal.layout_dialog.setPadding(commonDialogInternal.layout_dialog.getPaddingLeft(),
                    PixelTool.dp2px(context, 10),
                    commonDialogInternal.layout_dialog.getPaddingRight(),
                    PixelTool.dp2px(context, 10));

        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

            commonDialogInternal.layout_dialog.setPadding(commonDialogInternal.layout_dialog.getPaddingLeft(),
                    PixelTool.dp2px(context, 35),
                    commonDialogInternal.layout_dialog.getPaddingRight(),
                    PixelTool.dp2px(context, 30));

        }



        super.onConfigurationChanged(newConfig);
    }
}
