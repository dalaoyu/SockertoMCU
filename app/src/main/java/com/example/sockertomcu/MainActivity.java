package com.example.sockertomcu;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.net.Socket;
import java.time.ZoneId;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    private Button intel_bt, ordinary_bt;

    private SeekBar output_bar;

    private int outputVal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        intel_bt = findViewById(R.id.inteligence);
        ordinary_bt = findViewById(R.id.ordinary);
        intel_bt.setOnClickListener(this);
        ordinary_bt.setOnClickListener(this);
        output_bar = findViewById(R.id.outputBar);
        output_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                outputVal = progress;//输出值更新为SeekBar的进度值（默认范围是0-100）
                LoginActivity.socket.sendDataToServer(String.valueOf(outputVal)+"." );//发送输出值，结尾加个/:用以标识
            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
//                log.setText(log.getText() + "\n" + "事件：输出改变-" + outputVal);
                System.out.println("进度条到了："+outputVal);
            }
        });
    }

    /**
     * 设置滚条能否使用
     * @param i
     */
    private void setSeekBarClickable(int i) {
        if (i == 1) {
            //启用状态
            output_bar.setClickable(true);
            output_bar.setEnabled(true);
            output_bar.setSelected(true);
            output_bar.setFocusable(true);
        } else {
            //禁用状态
            output_bar.setClickable(false);
            output_bar.setEnabled(false);
            output_bar.setSelected(false);
            output_bar.setFocusable(false);

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.inteligence:
//                System.out.println("智能");
                LoginActivity.socket.sendDataToServer("101");
                setSeekBarClickable(0);
                break;
            case R.id.ordinary:
//                System.out.println("普通");
                LoginActivity.socket.sendDataToServer("102");
                setSeekBarClickable(1);
                break;
        }
    }
}
