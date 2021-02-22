package com.imengyu.mobilemouse;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.imengyu.mengui.dialog.CommonDialog;
import com.imengyu.mengui.toast.SmallToast;
import com.imengyu.mengui.utils.StatusBarUtils;
import com.imengyu.mengui.menu.PopupMenu;
import com.imengyu.mengui.widget.TitleBar;
import com.imengyu.mobilemouse.adapter.MainListAdapter;
import com.imengyu.mobilemouse.constant.Constants;
import com.imengyu.mobilemouse.constant.MouseConst;
import com.imengyu.mobilemouse.model.MainConnectDevice;
import com.imengyu.mobilemouse.net.Device;
import com.imengyu.mobilemouse.net.DeviceSearcher;
import com.imengyu.mobilemouse.service.DataService;
import com.imengyu.mobilemouse.utils.NetUtils;
import com.imengyu.mobilemouse.utils.StringUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;

import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_CODE_MOUSE = 2;
    private final int REQUEST_CODE_SETTINGS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(((App) getApplication()).isNotInitFromLauncher()) finish();

        StatusBarUtils.setLightMode(this);
        StatusBarUtils.setStatusBarColor(this, getColor(R.color.colorWhite));

        App app = ((App)getApplication());

        dataService = app.getDataService();

        initView();
        initMenu();
        initList();
        scanNetDevices();

        restartDevAddress = getIntent().getStringExtra("restartDevAddress");
        if(!StringUtils.isNullOrEmpty(restartDevAddress))
            mainHandler.sendEmptyMessageDelayed(MSG_LOAD_RESTART_DEV, 1300);
    }

    private String restartDevAddress;

    private DataService dataService = null;

    @Override
    protected void onPause() {
        dataService.save();
        super.onPause();
    }

    //Init
    //=====================

    private View content_main;

    private void initView() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> onAddDev());

        content_main = findViewById(R.id.content_main);
        list_main = findViewById(R.id.list_main);
        titleBar = findViewById(R.id.titlebar);
        titleBar.setRightIconOnClickListener((v) -> showMenu());
    }

    //Menu
    //=====================

    private TitleBar titleBar = null;
    private PopupMenu mainMenu = null;

    private void initMenu() {
        mainMenu = new PopupMenu(this, (ViewGroup) getWindow().getDecorView(), R.menu.menu_main);
        mainMenu.setOnMenuItemClickListener((id) -> {
            if(id == R.id.action_settings) {
                startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), REQUEST_CODE_SETTINGS);
            } else if(id == R.id.action_help) {
                startActivity( new Intent(this, HelpActivity.class));
            } else if(id == R.id.action_scan_devices) {
                scanNetDevices();
            }
        });
    }
    private void showMenu() {
        if(!mainMenu.isShowing())
            mainMenu.showAsDropDown(titleBar.getRightButton());
    }

    //再按一次退出程序
    //=====================

    private long mExitTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                SmallToast.makeText(this, "再按一次退出程序", SmallToast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                SmallToast.destroy();
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //Handler
    //=====================

    private static final int MSG_FLUSH_ALL = 102;
    private static final int MSG_FLUSH_ONE = 103;
    private static final int MSG_LOAD_RESTART_DEV = 104;

    private final MainHandler mainHandler = new MainHandler(this);
    private static class MainHandler extends Handler {

        private final WeakReference<MainActivity> mainActivityWeakReference;

        public MainHandler(MainActivity activity) {
            super(Looper.myLooper());
            mainActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_FLUSH_ALL:
                    mainActivityWeakReference.get().mainListAdapter.notifyDataSetChanged();
                    break;
                case MSG_FLUSH_ONE:
                    mainActivityWeakReference.get().mainListAdapter.notifyItemChanged((int)msg.obj);
                    break;
                case MSG_LOAD_RESTART_DEV:
                    mainActivityWeakReference.get().connectDev(
                            mainActivityWeakReference.get().restartDevAddress
                    );
                    break;
            }
            super.handleMessage(msg);
        }
    }

    //List
    //=====================

    private MainListAdapter mainListAdapter = null;
    private RecyclerView list_main = null;
    private MainConnectDevice lastRemoveDev = null;
    private int lastRemoveDevIndex = 0;

    private void initList() {
        mainListAdapter = new MainListAdapter(this, dataService.getConnectDevices());

        list_main.setAdapter(mainListAdapter);
        list_main.setLayoutManager(new StaggeredGridLayoutManager(2, OrientationHelper.VERTICAL));

        mainListAdapter.setOnItemActionListener((i, act) -> {
            switch (act) {
                case MainListAdapter.ACTION_CLICK:
                    i.setAddByUser(true);
                    Intent intent = new Intent(MainActivity.this, MouseActivity.class);
                    intent.putExtra("devIndex", dataService.getConnectDevices().indexOf(i));
                    startActivityForResult(intent, REQUEST_CODE_MOUSE);
                    break;
                case MainListAdapter.ACTION_DELETE:
                    deleteDev(i);
                    break;
                case MainListAdapter.ACTION_EDIT:
                    editDev(i);
                    break;
            }
        });
        mainListAdapter.notifyDataSetChanged();
    }
    private void connectDev(String address) {

        MainConnectDevice dev = dataService.findConnectDevice(address);
        if(dev == null) {
            dev = new MainConnectDevice();
            dev.setTargetAddress(address);
            dev.setAddByUser(true);
            dataService.getConnectDevices().add(dev);
            flushDeviceInfoBackground(dev);
            mainListAdapter.notifyItemInserted(dataService.getConnectDevices().size() - 1);
        }

        Intent intent = new Intent(MainActivity.this, MouseActivity.class);
        intent.putExtra("devIndex", dataService.getConnectDevices().indexOf(dev));
        startActivityForResult(intent, REQUEST_CODE_MOUSE);
    }
    private void deleteDev(MainConnectDevice dev) {
        final List<MainConnectDevice> list = dataService.getConnectDevices();
        lastRemoveDev = dev;
        lastRemoveDevIndex = list.indexOf(lastRemoveDev);
        list.remove(dev);
        mainListAdapter.notifyItemRemoved(lastRemoveDevIndex);
        //添加移除项
        dataService.addToExcludeDevices(dev.getTargetAddress());

        //通知
        Snackbar.make(content_main,
                String.format(getString(R.string.text_device_removed), lastRemoveDev.getTargetHostname()),
                Snackbar.LENGTH_LONG)
                .setAction(R.string.action_cancel, (v) -> {
                    list.add(lastRemoveDev);
                    mainListAdapter.notifyItemInserted(list.size() - 1);
                    dataService.removeFromExcludeDevices(lastRemoveDev.getTargetAddress());
                    lastRemoveDevIndex = 0;
                    lastRemoveDev = null;
                }).show();
    }
    private void editDev(MainConnectDevice dev) {

        View dialogView = getLayoutInflater().inflate(R.layout.content_dialog_edit_dev, (ViewGroup) getWindow().getDecorView(), false);

        TextView text_err = dialogView.findViewById(R.id.text_err);
        EditText edit_dev_name = dialogView.findViewById(R.id.edit_dev_name);
        EditText edit_dev_ip = dialogView.findViewById(R.id.edit_dev_ip);
        EditText edit_dev_pass = dialogView.findViewById(R.id.edit_dev_pass);

        edit_dev_name.setText(dev.getTargetHostname());
        edit_dev_ip.setText(dev.getTargetAddress());
        edit_dev_pass.setText(dev.getLastPass());

        new CommonDialog(this)
                .setTitle(R.string.action_edit)
                .setCustomView(dialogView)
                .setPositive(R.string.action_ok)
                .setNegative(R.string.action_cancel)
                .setOnResult((button, dialog) -> {
                    if(button == CommonDialog.BUTTON_POSITIVE) {

                        String dev_name = edit_dev_name.getText().toString();
                        String address = edit_dev_ip.getText().toString();

                        if(StringUtils.isNullOrEmpty(dev_name)) {
                            text_err.setText(R.string.text_please_type_a_name);
                            return false;
                        } else if(!StringUtils.isValidIPV4Address(address) && !StringUtils.isValidIPV6Address(address)) {
                            text_err.setText(R.string.text_please_type_a_invalid_ip);
                            return false;
                        } else {
                            text_err.setText("");
                        }

                        dev.setAddByUser(true);
                        dev.setTargetHostname(dev_name);
                        dev.setTargetAddress(address);
                        dev.setLastPass(edit_dev_pass.getText().toString());
                        mainListAdapter.notifyItemChanged(dataService.getConnectDevices().indexOf(dev));
                    }
                    return true;
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        if(mainListAdapter.isSelectMode()) {
            mainListAdapter.setSelectMode(false);
            return;
        }
        super.onBackPressed();
    }
    private void onAddDev() {
        new CommonDialog(this)
                .setTitle(R.string.action_add_computer)
                .setEditTextHint(R.string.text_enter_ip_address)
                .setOnEditTextChangedListener((editable, dialog) ->
                        dialog.setPositiveEnable(editable.length() > 0))
                .setPositive(R.string.action_ok)
                .setNegative(R.string.action_cancel)
                .setOnResult((button, dialog) -> {
                    String address = dialog.getEditTextValue().toString();
                    if(button == CommonDialog.BUTTON_POSITIVE
                            && !address.isEmpty()) {
                        if(!StringUtils.isValidIPV4Address(address)
                                && !StringUtils.isValidIPV6Address(address)) {
                            dialog.setEditTextErrorText(R.string.text_please_type_a_invalid_ip);
                            return false;
                        } else {
                            dialog.setEditTextErrorText("");
                        }
                        if(dataService.isInExcludeDevices(address))
                            dataService.removeFromExcludeDevices(address);
                        connectDev(address);
                    }
                    return true;
                })
                .show();
    }

    private void flushDeviceInfoBackground(MainConnectDevice device) {
        new Thread(() -> flushDeviceInfo(device)).start();
    }
    private void flushDeviceInfo(MainConnectDevice device) {
        if(device.getTargetHostname() == null) {

            Socket socket = null;
            try {
                //对服务端发起连接请求
                socket = new Socket(device.getTargetAddress(), MouseConst.PORT);

                OutputStream os = socket.getOutputStream();
                os.write("ifo".getBytes());
                os.flush();

                //接受服务端消息并打印
                InputStream is = socket.getInputStream();
                byte[] b = new byte[MouseConst.BUFFER_SIZE];
                if(is.read(b) != -1) {
                    String ret = new String(b, StandardCharsets.US_ASCII);

                    //解析属性
                    if(ret.length() > 3 && "ok".equals(ret.substring(0, 2))) {
                        String [] arr = ret.split("\\$");
                        if(arr.length >= 3) {
                            String type = arr[1];
                            if("win".equals(type)) device.setTargetType(MainConnectDevice.DEVICE_TYPE_WIN);
                            else if("max".equals(type)) device.setTargetType(MainConnectDevice.DEVICE_TYPE_MAC);
                            else if("lin".equals(type)) device.setTargetType(MainConnectDevice.DEVICE_TYPE_LINUX);
                            device.setTargetHostname(arr[2].trim());
                            device.setOnline(true);

                            Message msg = new Message();
                            msg.what = MSG_FLUSH_ONE;
                            msg.obj = dataService.getConnectDevices().indexOf(device);
                            mainHandler.sendMessageDelayed(msg, 200);
                        }
                    }
                }

                os.close();
                is.close();

            } catch (IOException e) {
                e.printStackTrace();

                device.setOnline(false);

                Message msg = new Message();
                msg.what = MSG_FLUSH_ONE;
                msg.obj = dataService.getConnectDevices().indexOf(device);
                mainHandler.sendMessageDelayed(msg, 100);

            } finally {
                if(socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    private void flushAllDeviceInfo() {

        for(MainConnectDevice device : dataService.getConnectDevices()) {
            if(device.getTargetHostname() == null)
                flushDeviceInfo(device);
        }

        mainHandler.sendEmptyMessageDelayed(MSG_FLUSH_ALL, 200);
    }

    private void scanNetDevices() {
        for(MainConnectDevice mainConnectDevice : dataService.getConnectDevices())
            mainConnectDevice.setOnline(false);

        if(!NetUtils.isWifiConnected(this)) {
            new CommonDialog(this)
                    .setTitle(R.string.text_wifi_not_connect)
                    .setMessage(R.string.text_wifi_not_connect_explanatory_text)
                    .setImageResource(R.drawable.ic_big_wifi)
                    .show();
        }
        else {
            DeviceSearcher.search(new DeviceSearcher.OnSearchListener() {
                @Override
                public void onSearchStart() {
                   titleBar.setTitle(getString(R.string.text_scan_device));
                }
                @Override
                public void onSearchedNewOne(Device device) {
                    if(dataService.isInExcludeDevices(device.getIp()))
                        return;
                    MainConnectDevice dev = dataService.findConnectDevice(device.getIp());
                    if(dev == null) {
                        dev = new MainConnectDevice();
                        dev.setTargetAddress(device.getIp());
                        dev.setOnline(true);
                        dataService.getConnectDevices().add(dev);
                        mainListAdapter.notifyItemInserted(dataService.getConnectDevices().size() - 1);
                    } else {
                        dev.setOnline(true);
                    }
                }
                @Override
                public void onSearchFinish() {
                    new Thread(() -> flushAllDeviceInfo()).start();
                    titleBar.setTitle(getString(R.string.app_name));
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_CODE_MOUSE && data != null) {
            int index = data.getIntExtra("devIndex", -1);
            if(index > 0)
                flushDeviceInfoBackground(dataService.getConnectDevices().get(index));
            if(data.getBooleanExtra("needRestart", false)) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("restartDevAddress", data.getStringExtra("restartDevAddress"));
                startActivity(intent);
                finish();
            }
        }
        else if(requestCode == REQUEST_CODE_SETTINGS && data != null) {
            if(data.getBooleanExtra("needRestart", false)) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}