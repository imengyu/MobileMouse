package com.imengyu.mengui.dialog;

public interface OnCommonDialogResult {
    /**
     * 用户点击对话框按钮时返回回调
     * @param button 点击的按钮 CommonDialog.BUTTON_*
     * @param dialog 当前对话框
     * @return 返回true则会自动关闭对话框
     */
    boolean onCommonDialogResult(int button, CommonDialog dialog);
}
