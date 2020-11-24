package me.hekr.sthome.service;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @author skygge
 * @date 2020/7/9.
 * GitHub：javofxu@github.com
 * email：skygge@yeah.net
 * description：
 */
public class TcpClientThread extends Thread {
    private static final String TAG = "TcpClientThread";
    private BufferedReader bufReader = null;
    private Socket socket = null;
    //IP地址
    private String address;
    //端口
    private int port;
    //发送内容
    private String msg;
    private Handler mHandler;

    public TcpClientThread(Handler handler, String address, int port, String msg) {
        this.mHandler = handler;
        this.address = address;
        this.port = port;
        this.msg = msg;
    }

    @Override
    public void run() {
        super.run();
        sendSocket();
    }

    /**
     * 设置
     */
    private void sendSocket() {
        try {
            // 1、创建连接
            socket = new Socket(address, port);
            if (socket.isConnected()) {
                Log.i(TAG, "connect to Server success");
            }

            // 2、设置读流的超时时间
            socket.setSoTimeout(8000);

            // 3、获取输出流与输入流
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();

            // 4、发送信息
            byte[] sendData = msg.getBytes(StandardCharsets.UTF_8);
            outputStream.write(sendData, 0, sendData.length);
            outputStream.flush();

            // 5、接收信息
            byte[] buf = new byte[1024];
            int len = inputStream.read(buf);
            String receiveData = new String(buf, 0, len, StandardCharsets.UTF_8);
            Log.i(TAG, receiveData);
            sendMsg(0, receiveData);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "error: " + e.toString());
        }catch (StringIndexOutOfBoundsException e2){
            e2.printStackTrace();
            sendMsg(0x111, "");
        }
        finally {
            if (socket != null) {
                try {
                    socket.close();
                    socket = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 发送消息
     */
    private void sendMsg(int what, Object object) {
        Message msg = new Message();
        msg.what = what;
        msg.obj = object;
        mHandler.sendMessage(msg);
    }
}