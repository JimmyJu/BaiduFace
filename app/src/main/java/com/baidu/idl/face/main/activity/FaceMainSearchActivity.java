package com.baidu.idl.face.main.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.baidu.idl.face.main.api.FaceApi;
import com.baidu.idl.face.main.activity.setting.SettingMainActivity;
import com.baidu.idl.facesdkdemo.R;
import com.example.yfaceapi.GPIOManager;

/**
 * 1：N 人脸检索
 */
public class FaceMainSearchActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;
    public static final int PAGE_TYPE = 999;
    GPIOManager manager;
    private Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_search);
        manager = GPIOManager.getInstance(this);

        mContext = this;
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        delayCloseRedLight();
    }

    /**
     * 初始化View 相关
     */
    private void initView() {
        findViewById(R.id.btn_register).setOnClickListener(this);
        findViewById(R.id.btn_user).setOnClickListener(this);
        findViewById(R.id.btn_setting).setOnClickListener(this);
        findViewById(R.id.btn_auth).setOnClickListener(this);
        findViewById(R.id.btn_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_user:
                startActivity(new Intent(mContext, FaceUserGroupListActivity.class));
                break;
            case R.id.btn_register:
                Intent intent2 = new Intent(mContext, SDCardFileExplorerActivity.class);
                startActivity(intent2);
                break;
            case R.id.btn_auth:
                startActivity(new Intent(mContext, FaceAuthActicity.class));
                break;
            case R.id.btn_back:
                startActivity(new Intent(mContext, FaceRGBCloseDebugSearchActivity.class));
                break;
            case R.id.btn_setting:
                startActivity(new Intent(mContext, SettingMainActivity.class));
                break;
            default:
                break;
        }
    }

    private void delayCloseRedLight() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                manager.pullDownRedLight();
                manager.pullDownGreenLight();
                manager.pullDownWhiteLight();
            }
        }, 2000);
    }
}
