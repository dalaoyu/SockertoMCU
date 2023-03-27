package com.example.sockertomcu;


import android.content.Intent;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;


import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import com.example.sockertomcu.databinding.ActivityLoginBinding;

import java.net.Socket;

public class LoginActivity extends AppCompatActivity {

    public static TcpSocket socket;
    private ActivityLoginBinding binding;
    private EditText ed_ip, ed_host;
    private Button loginButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ed_ip = binding.username;
        ed_host = binding.password;
        loginButton = binding.login;
        socket = new TcpSocket();
        socket.setListener(new TcpListener());
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect(binding.getRoot());
            }
        });
    }

    public void connect(View view) {
        String ip = ed_ip.getText().toString();
        String host = ed_host.getText().toString();
        System.out.println("ip:" + ip + "\n" + "host:" + host);
        socket.connect(ip, Integer.parseInt(host));

    }



    //tcp监听器
    private class TcpListener implements TcpSocket.OnReadListener {


        @Override
        public void onConnecting() {

        }

        @Override
        public void onConnectSuccess(String ip) {
            System.out.println("连接成功");
            Intent intent = new Intent();
            intent.setClass(binding.container.getContext(), MainActivity.class);
            startActivity(intent);
        }

        @Override
        public void onConnectLost() {
            System.out.println("连接丢失");
        }


        @Override
        public void onConnectTimeOut() {
            System.out.println("连接超时");

        }

        @Override
        public void onRead(byte[] b, int len, Socket socket) {
            String msg = new String(b, 0, len);
            System.out.println(msg);

        }
    }

}