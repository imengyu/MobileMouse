package com.imengyu.mobilemouse.model.holder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.imengyu.mobilemouse.R;

public class MainListHolder extends RecyclerView.ViewHolder {

    public MainListHolder(@NonNull View itemView) {
        super(itemView);

        image = itemView.findViewById(R.id.image);
        text_name = itemView.findViewById(R.id.text_name);
        text_address = itemView.findViewById(R.id.text_address);
        text_time = itemView.findViewById(R.id.text_time);
        layout_control = itemView.findViewById(R.id.layout_control);
        button_delete = itemView.findViewById(R.id.button_delete);
        button_edit = itemView.findViewById(R.id.button_edit);
        text_status = itemView.findViewById(R.id.text_status);
        image_status = itemView.findViewById(R.id.image_status);
    }

    public ImageButton button_delete;
    public ImageButton button_edit;
    public ImageView image_status;
    public ViewGroup layout_control;
    public ImageView image;
    public TextView text_name;
    public TextView text_address;
    public TextView text_time;
    public TextView text_status;
}
