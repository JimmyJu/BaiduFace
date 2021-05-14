package com.baidu.idl.face.main.activity.setting;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.api.Wiegand;
import com.baidu.idl.facesdkdemo.R;

public class WiegandActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "WiegandFragment";
    EditText edit26, edit34;
    Button bt26, bt34;
    Wiegand mWiegand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wiegand);
        initView();
    }

    private void initView() {
        edit26 = findViewById(R.id.output_wiegand26);
        edit34 = findViewById(R.id.output_wiegand34);
        bt26 = findViewById(R.id.output_bt_26);
        bt34 = findViewById(R.id.output_bt_34);
        mWiegand = Wiegand.getInstance();

        bt26.setOnClickListener(this);
        bt34.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWiegand.release();
    }

    @Override
    public void onClick(View v) {
        if (v == bt26) {
            try {
                int result = mWiegand.output26(Long.parseLong(edit26.getText().toString()));
                Log.i(TAG, "Wiegand26 output result:" + result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (v == bt34) {
            try {
                int result = mWiegand.output34(Long.parseLong(edit34.getText().toString()));
                Log.i(TAG, "Wiegand34 output result:" + result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}