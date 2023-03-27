package com.example.sockertomcu;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public final class TcpSocket {
    private Handler handler;
    private Socket so;
    public boolean isConnet = false;
    private boolean isConnetIng = false;
    private OutputStream out;
    private InputStream in;
    private OnReadListener listener;
    private String ip;
    private int port;
    public TcpSocket() {
        handler = new Handler();
    }
    public void connect(final String dstName, final int dstPort) {
        if (!isConnet && !isConnetIng) {
            isConnetIng = true;
            new Thread(new ConnectThread(dstName, dstPort)).start();
        }
    }
    public Socket getSocket() {
        if (so != null) {
            return so;
        }
        return null;
    }
    public void setListener(OnReadListener listener) {
        this.listener = listener;
    }
    public void sendDataToServer(final byte[] data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (out != null) {
                    try {
                        out.write(data);
                        out.flush();
                        Log.d("MSG", "发送success");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    public void sendDataToServer(final String data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (out != null) {
                    try {
                        Log.d("MSG", "发送:" + data);
                        out.write(data.getBytes());
                        out.flush();
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                        if (e.getMessage().contains("Socket closed")) {
                            handler.post(new MainThread(200));
                        }
                    }
                }
            }
        }).start();
    }
    public void closeConnet() {
        if (so != null) {
            if (so.isConnected()) {
                try {
                    so.close();
                    so = null;
                    isConnet = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private class MainThread implements Runnable {
        int what;
        public MainThread(int what) {
            this.what = what;
        }
        @Override
        public void run() {
            if (listener == null) {
                return;
            }
            switch (what) {
                case 100:
                    Log.e("DATA", "链接超时或者未知地址");
                    listener.onConnectTimeOut();
                    break;
                case 200:
                    Log.e("DATA", "与服务器断开了");
                    listener.onConnectLost();
                    break;

                case 300:
                    Log.e("DATA", "连接成功");
                    listener.onConnectSuccess(ip);
                    break;
                case 400:
                    Log.e("DATA", "正在连接");
                    listener.onConnecting();
                    break;
            }
            isConnetIng = false;
        }
    }
    private class ConnectThread implements Runnable {
        public ConnectThread(String dstName, int dstPort) {
            ip = dstName;
            port = dstPort;
        }
        @Override
        public void run() {
            try {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.e("DATA", "正在连接到IP:" + ip);
                if (listener != null) {
                    handler.post(new MainThread(400));
                }
                so = new Socket();
                InetSocketAddress isa = new InetSocketAddress(ip, port);
                so.connect(isa, 2000);
                out = so.getOutputStream();
                in = so.getInputStream();
                isConnet = true;
                if (listener != null) {
                    handler.post(new MainThread(300));
                }
                byte[] b = new byte[6666];
                int hasRead = 0;
                while ((hasRead = in.read(b)) > 0) {
                    if (listener != null) {
                        int finalHasRead = hasRead;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onRead(b, finalHasRead, so);
                            }
                        });
                    }
                }
            } catch (UnknownHostException e) {
                handler.post(new MainThread(100));
            } catch (IOException e) {
                String err = e.getMessage();
                if (err.contains("Socket closed")) {
                    handler.post(new MainThread(200));
                } else if (err.contains("failed to connect")) {
                    handler.post(new MainThread(100));
                } else {
                    e.printStackTrace();
                }
            } finally {
                if (so != null) {
                    if (!isConnetIng) {
                        handler.post(new MainThread(200));
                    }
                    try {
                        so.close();
                        isConnet = false;
                        if (out != null) {
                            out.close();
                        }
                        if (in != null) {
                            in.close();
                        }
                        so = null;
                        out = null;
                        in = null;
                        isConnetIng = false;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    public interface OnReadListener {
        public void onConnecting();
        public void onConnectSuccess(String ip);
        public void onConnectTimeOut();
        public void onConnectLost();
        public void onRead(byte[] b, int length, Socket socket);
    }
}