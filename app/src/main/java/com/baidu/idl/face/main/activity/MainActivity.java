package com.baidu.idl.face.main.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.face.main.api.FaceApi;
import com.baidu.idl.face.main.db.DBManager;
import com.baidu.idl.face.main.listener.SdkInitListener;
import com.baidu.idl.face.main.manager.FaceSDKManager;
import com.baidu.idl.face.main.model.ProgressList;
import com.baidu.idl.face.main.service.TcpService;
import com.baidu.idl.face.main.utils.ConfigUtils;
import com.baidu.idl.face.main.utils.LiveDataBus;
import com.baidu.idl.face.main.utils.NavigationBarUtil;
import com.baidu.idl.face.main.utils.NetWorkUtils;
import com.baidu.idl.face.main.utils.SPUtils;
import com.baidu.idl.face.main.utils.ToastUtils;
import com.baidu.idl.face.main.utils.Utils;
import com.baidu.idl.facesdkdemo.R;
import com.example.yfaceapi.GPIOManager;

/**
 * 主功能页面，包含人脸检索入口，认证比对，功能设置，授权激活
 */
public class MainActivity extends BaseActivity {
    private Context mContext;
    private Boolean isInitConfig;
    private Boolean isConfigExit;

    private final Handler handler = new Handler();

    private BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;

    private TextView tvErrorLog;
    /**
     * 进度条
     */
    private ProgressDialog progressDialog;
    /**
     * 旧的时间
     */
    private long oldTime;
    /**
     * 人脸库是否下载完毕标识
     */
    private boolean faceLoadingFlag = false;
    /**
     * 是否跳转Activity线程标识
     */
    private boolean jumpActivityThreadFlag = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        oldTime = System.currentTimeMillis();

        // todo shangrong 增加配置信息初始化操作
        isConfigExit = ConfigUtils.isConfigExit();
        isInitConfig = ConfigUtils.initConfig();
        if (isInitConfig && isConfigExit) {
            Toast.makeText(MainActivity.this, "初始配置加载成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "初始配置失败,将重置文件内容为默认配置", Toast.LENGTH_SHORT).show();
            ConfigUtils.modityJson();
        }

        initView();

        //校时
        schoolTime();

        //设置低功耗蓝牙
        setupBLE();
    }

    private void initView() {
        tvErrorLog = findViewById(R.id.tvErrorLog);

        //设备ID
//        String substringDeviceID = Build.FINGERPRINT.substring(50, 56);
        String ip = NetWorkUtils.getLocalIpAddress();
        if (ip != null && !ip.equals("")) {
            String newIP = ip.replace(".", "");
            String deviceID = newIP.substring(newIP.length() - 6);
            TextView tvDeviceID = findViewById(R.id.tvDeviceID);
            tvDeviceID.setText("设备ID：" + deviceID);
            Log.d("TAG", "onCreate: 设备ID" + deviceID);
        }

        //接收离线激活后的状态
        LiveDataBus.get().with("offlineActivation", Boolean.class).observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean flag) {
                if (flag) {
                    oldTime = System.currentTimeMillis();
                    //开启服务
                    openService();
                }
            }
        });

        //获取后台人脸特征库
        LiveDataBus.get().with("progressData", ProgressList.class).observe(this, new Observer<ProgressList>() {
            @Override
            public void onChanged(@Nullable ProgressList progressList) {
                if (progressList != null) {
                    //下载人脸特征库进度条
                    onProgress(progressList);
                }
            }
        });
    }

    /**
     * 校时
     */
    private void schoolTime() {
        String localCurrentTime = (String) SPUtils.get(mContext, "currentTime", "");
        if (TextUtils.isEmpty(localCurrentTime)) {
            // localCurrentTime == null 表明设备是第一次安装
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        long currentTimeMillis = System.currentTimeMillis();
                        SPUtils.put(mContext, "currentTime", Long.toString(currentTimeMillis));
                        try {
                            Thread.sleep(60 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        } else {
            if (localCurrentTime != null) {
                SystemClock.setCurrentTimeMillis(Long.parseLong(localCurrentTime));
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        long currentTimeMillis = System.currentTimeMillis();
                        SPUtils.put(mContext, "currentTime", Long.toString(currentTimeMillis + 60 * 1000));
                        try {
                            Thread.sleep(60 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }

    /**
     * 设置低功耗蓝牙
     */
    private void setupBLE() {
        //检查是否支持BLE蓝牙
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "本机不支持低功耗蓝牙!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        BluetoothManager manager = getManager(this);
        if (manager != null) {
            bluetoothAdapter = manager.getAdapter();
        }
        if ((bluetoothAdapter == null) || (!bluetoothAdapter.isEnabled())) {
            //调用系统对话框启动本地蓝牙
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
        } else {
            initLicense();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static BluetoothManager getManager(Context context) {
        return (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                initLicense();
            }
        }
    }

    /**
     * 下载人脸特征库进度条
     */
    private void onProgress(ProgressList progressList) {
        if (!progressList.isProgress()) {
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setTitle("下载人脸库");
            progressDialog.setMessage("正在下载中...");
            progressDialog.setMax(progressList.getFaceLibNum());
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            NavigationBarUtil.dialogShow(progressDialog);
        }
        if (progressDialog != null) {
            progressDialog.setProgress(progressList.getSuccess());
            if (progressList.getFaceLibNum() - progressList.getSuccess() == 0) {
                FaceApi.getInstance().initDatabases(true);
                progressDialog.dismiss();
                faceLoadingFlag = true;
            }
        }
    }

    /**
     * 启动应用程序，如果之前初始过，自动初始化鉴权和模型（可以添加到Application 中）
     */
    private void initLicense() {
        if (FaceSDKManager.initStatus != FaceSDKManager.SDK_MODEL_LOAD_SUCCESS) {
            FaceSDKManager.getInstance().init(mContext, new SdkInitListener() {
                @Override
                public void initStart() {
                }

                @Override
                public void initLicenseSuccess() {
                }

                @Override
                public void initLicenseFail(int errorCode, String msg) {
                    handler.removeCallbacks(initFailRunnable);
                    // 如果授权失败，跳转授权页面
                    ToastUtils.toast(mContext, errorCode + msg);
                    startActivity(new Intent(mContext, FaceAuthActicity.class));
                }

                @Override
                public void initModelSuccess() {
                    handler.removeCallbacks(initFailRunnable);
                    //开启服务
                    openService();
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            DBManager.getInstance().deleteGroup("default");
//
//                            for (int i = 12868; i <= 12874; i++) {
//                                Log.e("TAG", "i: " + i);
//
//                                String imgName = "a" + i;
//
//                                int imgId = getResources().getIdentifier(imgName, "drawable", "com.baidu.idl.face.demo");
//
//                                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imgId);
//
//                                byte[] bytes = new byte[512];
//
//                                FaceApi.getInstance().getFeature(bitmap, bytes, BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO);
//
//                                // Log.e("TAG", "bytes: " + Arrays.toString(bytes));
//
//                                FaceApi.getInstance().registerUserIntoDBmanager("default", "测试" + imgName, "imagename" + imgName + ".jpg", imgName, bytes);
//
//                                FaceApi.getInstance().initDatabases(true);
//
//
//                            }
//                        }
//                    }).start();
                }

                @Override
                public void initModelFail(int errorCode, String msg) {

                }

                @Override
                public void initFail(int code, String response) {
                    handler.postDelayed(initFailRunnable, 2000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvErrorLog.setText("code:" + code + "\n" + "response:" + response);
                            Log.e("TAG", "run: " + "code:" + code + "\n" + "response:" + response + "\n" + System.currentTimeMillis() / 1000);
                        }
                    });
                }
            });
        }
    }

    /**
     * 开启服务
     */
    private void openService() {
        //开启socket以及串口，先判断防止重复开启服务
        if (!Utils.isServiceRunning(mContext, "com.baidu.idl.face.main.service.TcpService")) {
            Intent intent = new Intent(MainActivity.this, TcpService.class);
            startService(intent);
        }

        handler.postDelayed(() -> {
            startActivity(new Intent(MainActivity.this, FaceRGBCloseDebugSearchActivity.class));
        }, 60 * 1000);
    }

    /**
     * 初始化失败Runnable
     */
    private Runnable initFailRunnable = new Runnable() {
        @Override
        public void run() {
            initLicense();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 关闭数据库
        DBManager.getInstance().release();
        GPIOManager.getInstance(this).pullDownRedLight();
        GPIOManager.getInstance(this).pullDownGreenLight();
        GPIOManager.getInstance(this).pullDownWhiteLight();
    }

}
