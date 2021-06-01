package com.baidu.idl.face.main.activity.setting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.baidu.idl.face.main.utils.SPUtils;
import com.baidu.idl.facesdkdemo.R;

public class IpAddressActivity extends AppCompatActivity {
    TextView ip_change, ip_port_show;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip_address);
        ip_port_show = findViewById(R.id.ip_port_show);
        ip_change = findViewById(R.id.ip_change);

        String ip = (String) SPUtils.get(this, "IP", "");
        String port = (String) SPUtils.get(this, "PORT", "");

        if (!ip.isEmpty() && !port.isEmpty()) {
            ip_port_show.setText("当前服务器: " + ip + ":" + port);
        }

        ip_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomizeDialog();
            }
        });
    }

    private void showCustomizeDialog() {
        /* @setView 装入自定义View ==> R.layout.dialog_customize
         * dialog_customize.xml可自定义更复杂的View
         */
        AlertDialog.Builder customizeDialog =
                new AlertDialog.Builder(IpAddressActivity.this);
        final View dialogView = LayoutInflater.from(IpAddressActivity.this)
                .inflate(R.layout.dialog_customize, null);
        customizeDialog.setTitle("提示");
        customizeDialog.setView(dialogView);
        customizeDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 获取EditView中的输入内容
                        EditText edit_ip =
                                (EditText) dialogView.findViewById(R.id.ed_ip);
                        EditText edit_port =
                                (EditText) dialogView.findViewById(R.id.ed_port);
                        String ed_ip = edit_ip.getText().toString();
                        String ed_port = edit_port.getText().toString();
                        if (!ed_ip.isEmpty() && !ed_port.isEmpty()) {
                            SPUtils.put(IpAddressActivity.this, "IP", ed_ip);
                            SPUtils.put(IpAddressActivity.this, "PORT", ed_port);
                            Log.e("TAG", "onClick: " + edit_ip.getText().toString() + ":" + edit_port.getText().toString());
                            String ip = (String) SPUtils.get(IpAddressActivity.this, "IP", "");
                            String port = (String) SPUtils.get(IpAddressActivity.this, "PORT", "");
                            if (!ip.isEmpty() && !port.isEmpty()) {
                                ip_port_show.setText("当前服务器: " + ip + ":" + port);
                            }
                        }

                    }
                });
        customizeDialog.show();
    }
}