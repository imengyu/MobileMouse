package com.imengyu.mengui.menu;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.imengyu.mengui.R;
import com.imengyu.mengui.utils.AlertDialogTool;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PopupMenu extends PopupWindow {

    public PopupMenu(Activity activity, ViewGroup root, int menuResId) {
        super(activity);
        this.activity = activity;
        view = LayoutInflater.from(activity).inflate(R.layout.layout_popup_menu, root, false);
        layout_popup_menu = view.findViewById(R.id.layout_popup_menu);
        setContentView(view);
        setFocusable(true);
        setOutsideTouchable(true);
        setTouchable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setAnimationStyle(R.style.DialogZoomPopup);
        initMenu(menuResId);
    }

    private final View view;
    private final View layout_popup_menu;
    private final Activity activity;

    private OnMenuItemClickListener onMenuItemClickListener = null;

    private static class MenuItem {
        public View item;
        public TextView text;
        public ImageView image;
        public int id;
    }
    private final List<MenuItem> items = new ArrayList<>();
    private final View.OnClickListener onItemClickListener = v -> {
        MenuItem item = (MenuItem)v.getTag();
        if(onMenuItemClickListener != null)
            onMenuItemClickListener.onMenuItemClickListener(item.id);
        dismiss();
    };

    private void initMenu(int menuResId) {
        Resources r = activity.getResources();
        XmlResourceParser xrp = r.getXml(menuResId);

        try {
            while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT) {
                if (xrp.getEventType() == XmlResourceParser.START_TAG) {
                    String name = xrp.getName();
                    if (name.equals("item")) {
                        MenuItem item = new MenuItem();

                        View view = LayoutInflater.from(activity)
                                .inflate(R.layout.layout_popup_menu_item, (ViewGroup) this.view, false);
                        item.text = view.findViewById(R.id.text);
                        item.image = view.findViewById(R.id.image);
                        item.item =  view.findViewById(R.id.layout_popup_menu_item);
                        item.item.setTag(item);

                        for(int i = 0, c = xrp.getAttributeCount(); i < c; i++) {
                            String attrName = xrp.getAttributeName(i);
                            if("title".equals(attrName)) {
                                int id = xrp.getAttributeResourceValue(i, -1);
                                if(id == -1)
                                    item.text.setText(xrp.getAttributeValue(i));
                                else
                                    item.text.setText(id);
                            }
                            else if("id".equals(attrName)) {
                                item.id = xrp.getAttributeResourceValue(i, -1);
                            }
                            else if("icon".equals(attrName)) {
                                int id = xrp.getAttributeResourceValue(i, -1);
                                if(id != -1)
                                    item.image.setImageResource(id);
                            }
                            else if("iconTint".equals(attrName)) {
                                int id = xrp.getAttributeResourceValue(i, -1);
                                if(id != -1)
                                    item.image.setImageTintList(ColorStateList.valueOf(r.getColor(id, null)));
                            }
                            else if("enabled".equals(attrName)) {
                                item.item.setEnabled(xrp.getAttributeBooleanValue(i, true));
                            }
                        }

                        ((ViewGroup) layout_popup_menu).addView(item.item);

                        item.item.setOnClickListener(onItemClickListener);
                    }
                }
                xrp.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener onMenuItemClickListener) {
        this.onMenuItemClickListener = onMenuItemClickListener;
    }
    public interface OnMenuItemClickListener {
        void onMenuItemClickListener(int id);
    }
}
