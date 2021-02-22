package com.imengyu.mobilemouse.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.imengyu.mobilemouse.R;
import com.imengyu.mobilemouse.model.MainConnectDevice;
import com.imengyu.mobilemouse.model.holder.MainListHolder;
import com.imengyu.mobilemouse.utils.DateUtils;

import java.util.Date;
import java.util.List;

public class MainListAdapter extends RecyclerView.Adapter<MainListHolder> {

    public MainListAdapter(Context context, List<MainConnectDevice> list) {
        this.list = list;
        this.context = context;
        text_list_connected_time = context.getString(R.string.text_list_connected_time);
        text_never_connect = context.getString(R.string.text_never_connect);
    }

    public static final int ACTION_CLICK = 1;
    public static final int ACTION_DELETE = 2;
    public static final int ACTION_EDIT = 3;

    public interface OnItemActionListener {
        void onItemActionListener(MainConnectDevice item, int action);
    }

    private final Context context;
    private final List<MainConnectDevice> list;
    private final String text_list_connected_time;
    private final String text_never_connect;

    private boolean isSelectMode = false;
    private OnItemActionListener onItemActionListener = null;

    public boolean isSelectMode() {
        return isSelectMode;
    }
    public void setSelectMode(boolean selectMode) {
        isSelectMode = selectMode;
        notifyDataSetChanged();
    }
    public void setOnItemActionListener(OnItemActionListener onItemActionListener) {
        this.onItemActionListener = onItemActionListener;
    }

    private final View.OnClickListener editButtonOnClickListener = v -> {
        if(onItemActionListener != null) {
            MainConnectDevice item = (MainConnectDevice)v.getTag();
            onItemActionListener.onItemActionListener(item, ACTION_EDIT);
        }
    };
    private final View.OnClickListener deleteButtonOnClickListener = v -> {
        if(onItemActionListener != null) {
            MainConnectDevice item = (MainConnectDevice)v.getTag();
            onItemActionListener.onItemActionListener(item, ACTION_DELETE);
        }
    };
    private final View.OnClickListener onClickListener = v -> {
        if(onItemActionListener != null) {
            MainConnectDevice item = (MainConnectDevice)v.getTag();
            onItemActionListener.onItemActionListener(item, ACTION_CLICK);
        }
    };

    @NonNull
    @Override
    public MainListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_main, parent, false);
        v.setOnClickListener(onClickListener);
        MainListHolder holder = new MainListHolder(v);
        holder.button_delete.setOnClickListener(deleteButtonOnClickListener);
        holder.button_edit.setOnClickListener(editButtonOnClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MainListHolder holder, int position) {
        MainConnectDevice item = list.get(position);

        holder.itemView.setTag(item);
        holder.button_delete.setTag(item);
        holder.button_edit.setTag(item);

        switch (item.getTargetType()) {
            case MainConnectDevice.DEVICE_TYPE_LINUX: holder.image.setImageResource(R.drawable.ic_type_linux); break;
            case MainConnectDevice.DEVICE_TYPE_MAC: holder.image.setImageResource(R.drawable.ic_type_mac); break;
            case MainConnectDevice.DEVICE_TYPE_WIN: holder.image.setImageResource(R.drawable.ic_type_win); break;
            default: holder.image.setImageResource(R.drawable.ic_type_other); break;
        }

        if(item.isOnline()) {
            holder.text_status.setText(R.string.text_online);
            holder.image_status.setImageResource(R.drawable.ic_status_online);
        } else {
            holder.image_status.setImageResource(R.drawable.ic_status_offline);
            holder.text_status.setText(R.string.text_offline);
        }

        holder.text_name.setText(item.getTargetHostname());
        holder.text_name.setText(item.getTargetHostname());
        holder.text_address.setText(item.getTargetAddress());

        Date lastCoon = item.getLastConnectTime();
        if(lastCoon != null)
            holder.text_time.setText(String.format(text_list_connected_time, DateUtils.getTimeLeaveString(context, lastCoon)));
        else
            holder.text_time.setText(text_never_connect);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
