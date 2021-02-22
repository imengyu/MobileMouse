package com.imengyu.mobilemouse.mouse;

import android.util.Log;

import com.imengyu.mobilemouse.constant.MouseConst;
import com.imengyu.mobilemouse.model.MainConnectDevice;
import com.imengyu.mobilemouse.utils.StringUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 鼠标客户端
 */
public class MouseClient {

    public interface OnMouseClientStatusChangedListener {
        void onMouseClientStatusChangedListener(int status);
    }

    public static final int STATUS_OK = 1;
    public static final int STATUS_NEED_PASSWORD = 2;
    public static final int STATUS_COON_LOST = 3;
    public static final int STATUS_FAILED = 4;
    public static final int STATUS_END = 5;
    public static final int STATUS_PAUSE = 6;
    public static final int STATUS_CONNECTING = 7;

    private static final String TAG = MouseClient.class.getSimpleName();
    private MouseClientThread thread = null;
    private Timer heartbeatTimer = null;
    private String targetAddress = null;
    private String password = "";
    private OnMouseClientStatusChangedListener onMouseClientStatusChangedListener = null;
    private String key = "";

    public void setTargetDevice(MainConnectDevice targetDevice) {
        this.targetAddress = targetDevice.getTargetAddress();
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setOnMouseClientStatusChangedListener(OnMouseClientStatusChangedListener onMouseClientStatusChangedListener) {
        this.onMouseClientStatusChangedListener = onMouseClientStatusChangedListener;
    }

    private int lastErr = 0;
    private String lastErrString = null;

    public static final int ERROR_RET_ERROR = 1;
    public static final int ERROR_PASS_ERROR = 2;
    public static final int ERROR_TIME_OUT = 3;
    public static final int ERROR_EXCEPTION = 4;

    /**
     * 获取上次错误
     * @return 错误代码
     */
    public String getLastErrString() {
        return lastErrString;
    }
    /**
     * 获取上次错误
     * @return 错误代码
     */
    public int getLastErr() {
        return lastErr;
    }
    /**
     * 获取连接是否成功
     * @return 连接是否成功
     */
    public boolean isConnectSuccess() {
        if(thread != null)
            return thread.isConnectSuccess();
        return false;
    }

    /**
     * 启动
     */
    public void start() {
        if(thread == null) {
            thread = new MouseClientThread(this);
            thread.startClient();
            thread.start();

            heartbeatTimer = new Timer();
            heartbeatTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    heartbeat();
                }
            }, 5000, 15000);
        }
    }
    /**
     * 暂停
     */
    public void stop() {
        if(thread != null) {
            thread.stopClient();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thread = null;
        }
        if(heartbeatTimer != null) {
            heartbeatTimer.cancel();
            heartbeatTimer = null;
        }
        instructions.clear();
    }
    /**
     * 暂停
     */
    public void pause() {
        if(thread != null) {
            thread.pauseClient();
        }
        if(heartbeatTimer != null) {
            heartbeatTimer.cancel();
            heartbeatTimer = null;
        }
    }

    private void callbackStatus(int status) {
        if(onMouseClientStatusChangedListener != null)
            onMouseClientStatusChangedListener.onMouseClientStatusChangedListener(status);
    }
    private void heartbeat() {
        if (instructions.size() > 0
                && instructions.get(instructions.size() - 1) instanceof HeartbeatInstruction)
            return;

        pushInstruction(new HeartbeatInstruction());
    }

    private final List<Instruction> instructions = new ArrayList<>();
    private final List<ControlInstruction> controlInstructions = new ArrayList<>();

    /**
     * 推送指令到发送队列
     * @param instruction 指令
     */
    public void pushInstruction(ControlInstruction instruction) {
        controlInstructions.add(instruction);
    }
    /**
     * 推送指令到发送队列
     * @param instruction 指令
     */
    public void pushInstruction(Instruction instruction) {
        instructions.add(instruction);
    }

    /**
     * 主工作线程
     */
    private static class MouseClientThread extends Thread {

        public MouseClientThread(MouseClient client) {
            this.client = client;
        }

        public void startClient() {
            running = true;
            isPause = false;
        }
        public void stopClient() {
            running = false;
            isPause = false;
        }
        public void pauseClient() {
            running = false;
            isPause = true;
        }

        public boolean isConnectSuccess() {
            return connectSuccess;
        }

        private boolean connectSuccess = false;
        private boolean running = false;
        private boolean isPause = false;
        private final MouseClient client;

        private Socket socket = null;
        private DatagramSocket socketUdp = null;
        private DataOutputStream out = null;
        private DataInputStream input = null;

        private void send(String command) throws IOException {
            out.write(command.getBytes(StandardCharsets.US_ASCII));
            out.flush();
            Log.d(TAG, "Send: " + command);
        }
        private void send(byte[] command) throws IOException {
            out.write(command);
            out.flush();
        }
        private String receive() throws IOException {
            byte[] bytes = new byte[MouseConst.BUFFER_SIZE];

            int len = input.read(bytes);
            if(len != -1) {
                //find 0 in bytes
                for (int i = 0; i < MouseConst.BUFFER_SIZE; i++) {
                    if (bytes[i] == 0) {
                        len = i;
                        break;
                    }
                }

                //str
                byte[] bytesStr = new byte[len];
                System.arraycopy(bytes, 0, bytesStr, 0, len);

                String s = new String(bytesStr, StandardCharsets.US_ASCII);
                Log.d(TAG, "Receive: " + s);
                return s;
            }
            return null;
        }

        private void setConnectSuccess() {
            connectSuccess = true;
            client.callbackStatus(STATUS_OK);
        }
        private void setConnectFailed(int err) {
            client.lastErr = err;
            connectSuccess = false;
            client.callbackStatus(STATUS_FAILED);
        }

        @Override
        public void run() {
            try {
                client.callbackStatus(STATUS_CONNECTING);

                SocketAddress endpoint = new InetSocketAddress(client.targetAddress, MouseConst.PORT);
                SocketAddress endpointUdp = new InetSocketAddress(client.targetAddress, MouseConst.CONTROL_PORT);

                socketUdp = new DatagramSocket(MouseConst.CONTROL_PORT);

                socket = new Socket();
                socket.setSoTimeout(6000);
                socket.connect(endpoint, 5000);

                out = new DataOutputStream(socket.getOutputStream());
                input = new DataInputStream(socket.getInputStream());

                //进行连接
                //====================

                send("coon");

                String ret = receive();
                if ("ok".equals(ret)) {
                    //连接成功
                    setConnectSuccess();
                    Log.i(TAG, "Server coon ok");
                }
                else if ("needpass".equals(ret)) {

                    Log.i(TAG, "Server needpass");

                    //Need password
                    if (client.password.isEmpty()) {
                        client.callbackStatus(STATUS_NEED_PASSWORD);//请求用户输入密码
                        return;
                    }

                    //发送密码验证
                    send("pas" + client.password);

                    //回传密钥
                    ret = receive();
                    if (StringUtils.isNullOrEmpty(ret) || ret.length() < 3) {
                        setConnectFailed(ERROR_RET_ERROR);
                        Log.e(TAG, "Un know server return: " + ret);
                        return;
                    }
                    else {
                        if("ok ".equals(ret.substring(0, 3))) {
                            client.key = ret.substring(3);
                            setConnectSuccess();
                            Log.i(TAG, "Server coon ok");
                        } else if("badpass".equals(ret)) {
                            setConnectFailed(ERROR_PASS_ERROR);
                            Log.i(TAG, "Server badpass");
                            return;
                        } else {
                            Log.e(TAG, "Un know server return: " + ret);
                            setConnectFailed(ERROR_RET_ERROR);
                            return;
                        }
                    }
                }
                else {
                    setConnectFailed(ERROR_RET_ERROR);
                    Log.w(TAG, "Send coon with failed ret: " + ret);
                    return;
                }

                //进入控制流程
                //====================

                final byte[] instructionByte = new byte[16];
                final byte[] instructionByteFull = new byte[36];

                while (running) {

                    if(client.instructions.size() > 0) {

                        //Empty instructionByte
                        Arrays.fill(instructionByte, (byte) 0);
                        Arrays.fill(instructionByteFull, (byte) 0);

                        Instruction instruction = client.instructions.get(0);
                        client.instructions.remove(0); //弹出队列

                        String prefix = instruction.getPrefix();
                        if(prefix == null || prefix.length() != 3) {
                            Log.e(TAG, "The getPrefix must return a string with 3 char!");
                            continue;
                        }

                        //3 Char
                        instructionByteFull[0] = (byte)prefix.charAt(0);
                        instructionByteFull[1] = (byte)prefix.charAt(1);
                        instructionByteFull[2] = (byte)prefix.charAt(2);

                        //Copy instruction
                        instruction.pack(instructionByte);
                        System.arraycopy(client.key.getBytes(StandardCharsets.US_ASCII), 0, instructionByteFull, 3, client.key.length());
                        System.arraycopy(instructionByte, 0, instructionByteFull, 3 + 16, 16);

                        //Send
                        send(instructionByteFull);

                        if(instruction.isNeedCheckReturn()) {
                            ret = receive();
                            instruction.setReturnData(ret);
                            if(ret != null &&!ret.startsWith("ok"))
                                Log.w(TAG, "Send instruction with failed ret: " + ret);
                        }

                        if(instruction instanceof MouseInstruction)
                            client.returnMouseInstruction((MouseInstruction)instruction);
                        else if(instruction instanceof KeyInstruction)
                            client.returnKeyInstruction((KeyInstruction)instruction);

                    }
                    else if(client.controlInstructions.size() > 0) {

                        //Empty instructionByte
                        Arrays.fill(instructionByte, (byte) 0);
                        Arrays.fill(instructionByteFull, (byte) 0);

                        ControlInstruction instruction = client.controlInstructions.get(0);
                        client.controlInstructions.remove(0); //弹出队列

                        String prefix = instruction.getPrefix();
                        if(prefix == null || prefix.length() != 3) {
                            Log.e(TAG, "The getPrefix must return a string with 3 char!");
                            continue;
                        }

                        //3 Char
                        instructionByteFull[0] = (byte)prefix.charAt(0);
                        instructionByteFull[1] = (byte)prefix.charAt(1);
                        instructionByteFull[2] = (byte)prefix.charAt(2);

                        //Copy instruction
                        instruction.pack(instructionByte);
                        System.arraycopy(client.key.getBytes(StandardCharsets.US_ASCII), 0, instructionByteFull, 3, client.key.length());
                        System.arraycopy(instructionByte, 0, instructionByteFull, 3 + 16, 16);

                        //Send
                        DatagramPacket packet = new DatagramPacket(instructionByteFull, instructionByteFull.length, endpointUdp);
                        socketUdp.send(packet);
                    }
                    else {
                        //noinspection BusyWait
                        Thread.sleep(100);
                    }
                }

                //断开连接流程
                //====================

                if(!isPause) {
                    send("end");

                    ret = receive();
                    if (!"ok".equals(ret))
                        Log.w(TAG, "Send end with failed ret: " + ret);

                    out.close();
                    input.close();

                    client.callbackStatus(STATUS_END);
                }
                else {
                    client.callbackStatus(STATUS_PAUSE);
                }

            }
            catch (SocketTimeoutException e) {
                setConnectFailed(ERROR_TIME_OUT);
            }
            catch (SocketException e) {
                Log.e(TAG, "SocketException: " + e.getMessage(), e);
                if(connectSuccess) {
                    client.callbackStatus(STATUS_COON_LOST);
                    connectSuccess = false;
                } else {
                    client.lastErrString = e.getLocalizedMessage();
                    setConnectFailed(ERROR_EXCEPTION);
                }
            }
            catch (Exception e) {
                Log.e(TAG, "Client error: " + e.getMessage(), e);
                client.lastErrString = e.getLocalizedMessage();
                setConnectFailed(ERROR_EXCEPTION);
            }
            finally {
                if (socket != null) {
                    Log.i(TAG, "Close socket");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        socket = null;
                        Log.e(TAG, "Socket error: " + e.getMessage(), e);
                    }
                }
                if (socketUdp != null) {
                    socketUdp.close();
                    socketUdp = null;
                }

                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                client.thread = null;
                isPause = false;
                connectSuccess = false;
            }
        }
    }

    //指令池
    private final ArrayList<MouseInstruction> mouseInstructionPool = new ArrayList<>();
    private final ArrayList<KeyInstruction> keyInstructionPool = new ArrayList<>();

    public KeyInstruction requestKeyInstruction() {
        if(keyInstructionPool.size() == 0) {
            for (int i = 0; i < 10; i++)
                keyInstructionPool.add(new KeyInstruction(KeyInstruction.KEY_EMPTY));
        }

        KeyInstruction result = keyInstructionPool.get(0);
        keyInstructionPool.remove(0);
        return result;
    }
    public void returnKeyInstruction(KeyInstruction instruction) {
        instruction.setType(0);
        instruction.setKey(0);
        keyInstructionPool.add(instruction);
    }
    public MouseInstruction requestMouseInstruction() {
        if(mouseInstructionPool.size() == 0) {
            for (int i = 0; i < 10; i++)
                mouseInstructionPool.add(new MouseInstruction(MouseInstruction.MOUSE_EMPTY));
        }

        MouseInstruction result = mouseInstructionPool.get(0);
        mouseInstructionPool.remove(0);
        return result;
    }
    public void returnMouseInstruction(MouseInstruction instruction) {
        instruction.setType(0);
        instruction.setButton(0);
        instruction.setX(0);
        instruction.setY(0);
        mouseInstructionPool.add(instruction);
    }
}
