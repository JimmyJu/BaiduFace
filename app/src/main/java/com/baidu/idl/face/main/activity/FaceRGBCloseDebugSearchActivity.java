package com.baidu.idl.face.main.activity;

import android.arch.lifecycle.Observer;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.hardware.Camera;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.baidu.idl.face.main.api.Wiegand;
import com.baidu.idl.face.main.callback.CameraDataCallback;
import com.baidu.idl.face.main.callback.FaceDetectCallBack;
import com.baidu.idl.face.main.camera.AutoTexturePreviewView;
import com.baidu.idl.face.main.camera.CameraPreviewManager;
import com.baidu.idl.face.main.camera.CameraPreviewManager_infrared;
import com.baidu.idl.face.main.constant.BaseConstant;
import com.baidu.idl.face.main.manager.FaceSDKManager;
import com.baidu.idl.face.main.manager.FaceTrackManager;
import com.baidu.idl.face.main.model.LivenessModel;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.model.User;
import com.baidu.idl.face.main.utils.BitmapUtils;
import com.baidu.idl.face.main.utils.DateUtil;
import com.baidu.idl.face.main.utils.DensityUtils;
import com.baidu.idl.face.main.utils.FaceOnDrawTexturViewUtil;
import com.baidu.idl.face.main.utils.LiveDataBus;
import com.baidu.idl.face.main.utils.NavigationBarUtil;
import com.baidu.idl.face.main.utils.NetWorkUtils;
import com.baidu.idl.face.main.utils.ToastUtils;
import com.baidu.idl.face.main.utils.Utils;
import com.baidu.idl.face.main.view.DoubleClickListener;
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.example.yfaceapi.GPIOManager;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @Time 2019/06/02
 * @Author v_shishuaifeng
 * @Description RGB??????Debug??????
 */
public class FaceRGBCloseDebugSearchActivity extends BaseActivity {
    private GPIOManager manager;
    private Wiegand mWiegand;
    //?????????
    private static final int BRIGHTNESS_VALUE = 128;
    //???????????????
    private int whiteLight_Status = 0;
    private int RedLight_Status = 0;
    private int GreenLight_Status = 0;
    //???????????????
    private boolean checkLight = true;

    // ???????????????????????????????????????????????????640*480??? 1280*720
    private static final int PREFER_WIDTH = 640;
    private static final int PERFER_HEIGH = 480;

    private Context mContext;

    // ??????Debug ??????
    private AutoTexturePreviewView mAutoCameraPreviewView, mAuto_camera_infrared;
    private TextureView mFaceDetectImageView;
    private Paint paint;
    private RectF rectF;
    private RelativeLayout relativeLayout;
    private int mLiveType;
    private float mRgbLiveScore;
    private Button mMenubtn;
    private ImageView switchPort;
    private ToggleButton mToggleButton;

    //??????mp3
    private SoundPool mSoundPool = null;
    private MediaPlayer mediaPlayer = null;
    private final HashMap<Integer, Integer> soundID = new HashMap<>();
    private float volume;

    private Hashtable<String, Long> faceTime = new Hashtable<>();
    /**
     * ??????????????????
     */
    private final Hashtable<String, byte[]> faceImage = new Hashtable<>();
    private Bitmap mRBmp = null;

    /**
     * ????????????
     */
    private DoubleClickListener doubleClickListener;
    /**
     * ???????????????
     */
    private String timeFlag;
    /**
     * ???????????????????????????
     */
    private boolean timeFlagBool = true;
    /**
     * ??????
     */
    private byte[] cardNumberByte = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    /**
     * ???????????? 20?????????
     */
    private byte[] personnelNameByte = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};

    /**
     * ???????????????
     */
    private byte[] imageHead = new byte[]{(byte) 0xFF, (byte) 0xD8};
    /**
     * ???????????????
     */
    private byte[] imageEnd = new byte[]{(byte) 0xFF, (byte) 0xD9};
    /**
     * ????????????????????????
     */
    private boolean faceFlag = false;
    private boolean faceFlagss = false;
    /**
     * ?????????????????? handler
     */
    private Handler handler = new Handler();
    /**
     * ?????????????????????view
     */
    private RelativeLayout rlDiscernBg;
    private ImageView detect_reg_image_item;
    private TextView tvDiscernSucceed;
    private ImageView imgLine;
    private TextView tvName;
    private TextView tvDiscernFailure;
    /**
     * ????????????View ??????????????????????????????????????????????????????????????????????????????
     */
    private TextView mLiveTextView, mAdoptTextView, mErrorTextView, mSendTextView, mServerStateTextView;
    private int mLiveNum = 1, mAdoptNum = 1, mErrorNum = 1;

    /**
     * ????????????
     */
    private BluetoothLeScanner mBLEScanner;
    /**
     * ??????Adapter
     */
    private BluetoothAdapter bluetoothAdapter;
    /**
     * ?????????????????????
     */
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    /**
     * ?????????
     */
    ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    //????????????????????? false: 21       true:22(???????????????)
    private Boolean switchPortNum = false;
    int flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_rgb_close_debug);

        mContext = this;

        manager = GPIOManager.getInstance(FaceRGBCloseDebugSearchActivity.this);
        mWiegand = Wiegand.getInstance();

        initView();

        //???????????????????????????????????????????????????????????????1?????????????????????????????????
//        monitorTimestamp();

        //???????????????
//        checkLightState();

        initSP();
        // ????????????
        int displayWidth = DensityUtils.getDisplayWidth(mContext);
        // ????????????
        int displayHeight = DensityUtils.getDisplayHeight(mContext);
        // ?????????????????????????????????
        if (displayHeight < displayWidth) {
            // ?????????
            int height = displayHeight;
            // ?????????
            int width = (int) (displayHeight * ((9.0f / 16.0f)));
            // ????????????????????????
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
            // ??????????????????
            params.gravity = Gravity.CENTER;
            relativeLayout.setLayoutParams(params);
        }

        BluetoothManager manager = MainActivity.getManager(this);
        if (manager != null) {
            bluetoothAdapter = manager.getAdapter();
        }

        //??????BLE??????
//        scanLeDevice();

        //????????????
        liveDataBus();
    }

    private void liveDataBus() {
        LiveDataBus.get().with("heart", Boolean.class).observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean heart) {
                if (heart) {
                    mServerStateTextView.setText("??????");
                } else {
                    mServerStateTextView.setText("??????");
                }
            }
        });

        LiveDataBus.get().with("sendNum", Integer.class).observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer num) {
                mSendTextView.setText("" + num);
            }
        });
    }


    /***
     *  ??????Debug ??????view
     */

    private void initView() {
        // ????????????
        mLiveType = SingleBaseConfig.getBaseConfig().getType();
        // ????????????
        mRgbLiveScore = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
        // ??????????????????
        relativeLayout = findViewById(R.id.all_relative);
        // ????????????
        paint = new Paint();
        rectF = new RectF();
        mFaceDetectImageView = findViewById(R.id.draw_detect_face_view);
        mFaceDetectImageView.setOpaque(false);
        //????????????
        mFaceDetectImageView.setKeepScreenOn(true);

        //???????????????
//        mToggleButton = findViewById(R.id.toggle_btn);

        // ???????????????RGB ????????????
        mAutoCameraPreviewView = findViewById(R.id.auto_camera_preview_view);
        mAutoCameraPreviewView.setVisibility(View.VISIBLE);
        //??????????????? ????????????
        mAuto_camera_infrared = findViewById(R.id.auto_camera_infrared);
        mAuto_camera_infrared.setVisibility(View.INVISIBLE);

        rlDiscernBg = findViewById(R.id.rlDiscernBg);
        detect_reg_image_item = findViewById(R.id.detect_reg_image_item);
        tvDiscernSucceed = findViewById(R.id.tvDiscernSucceed);
        imgLine = findViewById(R.id.imgLine);
        tvName = findViewById(R.id.tvName);
        tvDiscernFailure = findViewById(R.id.tvDiscernFailure);

        mLiveTextView = findViewById(R.id.live);
        mAdoptTextView = findViewById(R.id.adopt);
        mErrorTextView = findViewById(R.id.error);
        mSendTextView = findViewById(R.id.send);
        mServerStateTextView = findViewById(R.id.server);


        // ????????????
        mMenubtn = findViewById(R.id.menu_btn);
        doubleClickListener = new DoubleClickListener() {
            @Override
            public void onDoubleClick(View v) {
                mMenubtn.setAlpha(1);
                mMenubtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mMenubtn.getAlpha() == 1) {
                            mMenubtn.setAlpha(0);
                            setUpDialog();
                        }
                        mMenubtn.setOnClickListener(doubleClickListener);
                    }
                });
            }
        };
        mMenubtn.setOnClickListener(doubleClickListener);

        switchPort = findViewById(R.id.switchPort);
        //false: 21       true:22
        switchPort.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onDoubleClick(View v) {
                switch (flag) {
                    case 0:
                        switchPortNum = true;
                        flag = 1;
                        showExitDialog("????????????");
                        break;
                    case 1:
                        switchPortNum = false;
                        flag = 0;
                        showExitDialog("????????????");
                        break;
                }
            }
        });

     /*   //???????????????
        mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    manager.pullUpWhiteLight();
                } else {
                    manager.pullDownWhiteLight();
                }

            }
        });*/

        getDevicesNum();
    }

    /**
     * ???????????????????????????????????????????????????????????????1?????????????????????????????????
     */
    private void monitorTimestamp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (timeFlagBool) {
                    try {
                        Thread.sleep(1000);
                        if (!TextUtils.isEmpty(timeFlag)) {
                            //Long.parseLong(DateUtil.timeStamp()) - 10 ,????????????-1???
                            if (Long.parseLong(timeFlag) < Long.parseLong(DateUtil.timeStamp()) - 10) {
                                //????????????????????????
                                LiveDataBus.get().with(BaseConstant.slabShutdownMessage).postValue(1);
                            }
                        }
                        if (DateUtil.getHHmmss().equals("00:00:00")) {
                            //????????????????????????
                            LiveDataBus.get().with(BaseConstant.slabShutdownMessage).postValue(1);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * ????????????
     */
    private void initSP() {
        if (Build.VERSION.SDK_INT > 21) {
            SoundPool.Builder builder = new SoundPool.Builder();
            //??????????????????
            builder.setMaxStreams(1);
            //AudioAttributes??????????????????????????????????????????
            AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
            //?????????????????????????????????
            attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);//STREAM_MUSIC
            //????????????AudioAttributes
            builder.setAudioAttributes(attrBuilder.build());
            mSoundPool = builder.build();
        } else {
            mSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 0);
        }
        if (SingleBaseConfig.getBaseConfig().getMusicSwitch() == 1) {
            soundID.put(1, mSoundPool.load(this, R.raw.unregistered, 1));
            soundID.put(2, mSoundPool.load(this, R.raw.success, 1));
        }
        AudioManager mgr = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
        float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volume = streamVolumeCurrent / streamVolumeMax;
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTestCloseDebugRegisterFunction();
//        turnOnTheInfraredCamera();
    }

    private boolean mark = true;

    //?????????????????????????????????????????? ???????????????
    private void turnOnTheInfraredCamera() {
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
        CameraPreviewManager_infrared.getInstance().setCameraFacing(CameraPreviewManager_infrared.CAMERA_FACING_BACK);
        CameraPreviewManager_infrared.getInstance().startPreview(FaceRGBCloseDebugSearchActivity.this, mAuto_camera_infrared, PREFER_WIDTH, PERFER_HEIGH, new CameraDataCallback() {
            @Override
            public void onGetCameraData(byte[] data, Camera camera, int width, int height) {
                // ???????????????????????????????????????
                        /*int liveType = SingleBaseConfig.getBaseConfig().getType();
                        if (liveType == 1) { // ???????????????
                            FaceTrackManager.getInstance().setAliving(false);
                        } else if (Integer.valueOf(liveType) == 2) { // ????????????
                            FaceTrackManager.getInstance().setAliving(true);
                        }*/

//                cachedThreadPool.execute(new Runnable() {
//                    @Override
//                    public void run() {
                if (mark) {
                    FaceTrackManager.getInstance().faceTrack(data, width, height, new FaceDetectCallBack() {
                        @Override
                        public void onFaceDetectCallback(LivenessModel livenessModel) {
                            if (livenessModel != null) {
//                                        mark = false;
//                                        manager.pullUpWhiteLight();
//                                        handler.postDelayed(() -> {
//                                            manager.pullDownWhiteLight();
//                                            mark = true;
//                                        }, 20000);
                            }

                        }

                        @Override
                        public void onTip(int code, String msg) {

                        }

                        @Override
                        public void onFaceDetectDarwCallback(LivenessModel livenessModel) {

                        }
                    });
                }
//                    }
//                });
            }
        });
//            }
//        });
    }

    private void startTestCloseDebugRegisterFunction() {
        //??????????????????
        timeFlag = DateUtil.timeStamp();

        // ??????USB?????????
        CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
        CameraPreviewManager.getInstance().startPreview(this, mAutoCameraPreviewView,
                PREFER_WIDTH, PERFER_HEIGH, new CameraDataCallback() {
                    @Override
                    //??????????????????????????????
                    public void onGetCameraData(byte[] data, Camera camera, int width, int height) {
                        //??????????????????????????????????????????timeFlag
//                        timeFlag = DateUtil.timeStamp();
                        // ???????????????????????????????????????
                        FaceSDKManager.getInstance().onDetectCheck(data, null, null,
                                height, width, mLiveType, new FaceDetectCallBack() {
                                    @Override
                                    public void onFaceDetectCallback(LivenessModel livenessModel) {
                                        // ????????????
                                        checkCloseResult(livenessModel, width, height);
                                    }

                                    //????????????
                                    @Override
                                    public void onTip(int code, String msg) {
                                        displayTip(code, msg);
                                    }

                                    //???????????????
                                    @Override
                                    public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
                                        showFrame(livenessModel);
                                    }
                                });
                    }
                });
    }


    private void displayTip(final int code, final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (code == 0) {

                } else {
                    //????????????
                    discernFailureView();
                }
            }
        });
    }

    /**
     * ????????????
     *
     * @param livenessModel
     * @param width
     * @param height
     */
    private void checkCloseResult(final LivenessModel livenessModel, int width, int height) {
        if (Long.parseLong(DateUtil.timeStamp()) - Long.parseLong(timeFlag) > 1) {
            timeFlag = DateUtil.timeStamp();
        } else {
            return;
        }
        // ?????????????????????UI??????.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
               /* Log.e("bright", "????????????????????????: " + manager.getWhiteLightStatus()
                        + "????????????????????????: " + manager.getGreenLightStatus()
                        + "????????????????????????: " + manager.getRedLightStatus());*/

                if (livenessModel == null || livenessModel.getFaceInfo() == null || !faceSizeFilter(livenessModel.getFaceInfo(), width, height)) {
                    //?????????????????????????????????
                    rlDiscernBg.setVisibility(View.GONE);
                    //?????????????????????
//                    closeAllLight();
                    try {
                        delayClose_Red_GedreenLight();
                    } catch (Exception e) {
                        Log.e("loge", "run:----- delayClose_Red_GedreenLight----??????" + e.toString());
                        ToastUtils.toast(getApplicationContext(), "delayClose_Red_GedreenLight----??????");
                    }

                    whiteLight_Status = 0;
                    RedLight_Status = 0;
                    GreenLight_Status = 0;
                } else {
                    float rgbLivenessScore = livenessModel.getRgbLivenessScore();
                    if (rgbLivenessScore < mRgbLiveScore) {
//                        mRelativeLayout.setVisibility(View.VISIBLE);
//                        mTrackText.setText("????????????");
//                        mRelativeLayout.setBackgroundColor(Color.RED);
//                        mDetectText.setText("?????????????????????");
//                        mDetectText.setVisibility(View.VISIBLE);
//                        mDetectImage.setImageResource(R.mipmap.ic_littleicon);
//                        mIdcardTextview.setVisibility(View.INVISIBLE);
//                        mNameText.setVisibility(View.INVISIBLE);
//                        if (mAliveTime.containsKey("fail")){
//                            Long oldtime= mAliveTime.get("fail").longValue();
//                            Long newtime = System.currentTimeMillis();
//                            if (newtime -oldtime>3000){
//                                mAliveTime.put("fail",newtime);
//                                mErrorNum++;
//                                mErrorTextView.setText(""+mErrorNum);
//                                LiveDataBus.get().with("SerialData").setValue(Utils.getRedLightData());
//                                //mSoundPool.play(1,1,0,0,0,0);
//                            }
//                        }else{
//                            Long time = System.currentTimeMillis();
//                            mAliveTime.put("fail",time);
//                            mErrorNum++;
//                            mErrorTextView.setText(""+mErrorNum);
//                            LiveDataBus.get().with("SerialData").setValue(Utils.getRedLightData());
//                        }
                    } else {

                    }
                    //????????????
                    try {
                        mRBmp = BitmapUtils.getInstaceBmp(livenessModel.getBdFaceImageInstance());
                    } catch (OutOfMemoryError e) {
                        ToastUtils.toast(FaceRGBCloseDebugSearchActivity.this, "" + e.toString());
                    }
                    //?????????????????????
//                    int bright = getBright(mRBmp);
                    //??????????????????128????????????????????????????????????????????????128??????????????????????????????
                    /*if (whiteLight_Status == 0 && bright > BRIGHTNESS_VALUE) {
                        delayLight();
                    }*/
                   /* try {
                        if (whiteLight_Status == 0 && bright < BRIGHTNESS_VALUE) {
                            manager.pullUpWhiteLight();
                        }

                    } catch (Exception e) {
                        Log.e("loge", "run:----- pullUpWhiteLight----??????" + e.toString() + "\r\n");
                        ToastUtils.toast(getApplicationContext(), "pullUpWhiteLight--??????");
                    }*/

//                    Log.e("bright", "????????????: " + bright + "????????????????????????: " + manager.getWhiteLightStatus());
                    //????????????
                    mRBmp.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                    byte[] imageData = Utils.addBytes(imageHead, byteArrayOutputStream.toByteArray(), imageEnd);
                    byteArrayOutputStream.reset();

                    User user = livenessModel.getUser();
                    if (user == null) {
//                        ToastUtils.toast(mContext,"????????????");
                        if (livenessModel.getFeatureContrastValue() < 80.00) {

                            try {
                                if (GreenLight_Status == 1) {
                                    manager.pullDownGreenLight();
                                    GreenLight_Status = 0;
                                }
                            } catch (Exception e) {
                                Log.e("loge", "run:----- pullDownGreenLight----??????" + e.toString());
                                ToastUtils.toast(getApplicationContext(), "pullDownGreenLight---??????");
                            }

                            try {
                                delayRedLight();
                            } catch (Exception e) {
                                Log.e("loge", "run:----- delayRedLight----??????" + e.toString());
                                ToastUtils.toast(getApplicationContext(), "delayRedLight()---??????");
                            }

                            //??????3????????????????????????????????????
                            delaySendData(livenessModel, imageData, null);
                            //????????????
                            discernFailureView();
                            //??????????????????
                            sendSerialPortData(null);

                        }
                    } else {
//                        ToastUtils.toast(mContext,"????????????");

                        try {
                            if (RedLight_Status == 1) {
                                manager.pullDownRedLight();
                                RedLight_Status = 0;
                            }

                        } catch (Exception e) {
                            Log.e("loge", "run:----- delayRedLight----??????" + e.toString());
                            ToastUtils.toast(getApplicationContext(), "pullDownRedLight--??????");
                        }

                        try {
                            delayGreenLight();
                        } catch (Exception e) {
                            Log.e("loge", "run:----- delayRedLight----??????" + e.toString());
                            ToastUtils.toast(getApplicationContext(), "delayGreenLight---??????");
                        }

                        if (faceImage.containsKey(user.getUserName())) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(faceImage.get(user.getUserName()), 0, faceImage.get(user.getUserName()).length);
                            //????????????
                            discernSucceedView(user.getUserName(), bitmap);
                        } else {
                            //????????????
                            mRBmp = BitmapUtils.getInstaceBmp(livenessModel.getBdFaceImageInstance());
                            //????????????
                            mRBmp.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                            faceImage.put(user.getUserName(), byteArrayOutputStream.toByteArray());
                            byteArrayOutputStream.reset();
                            //????????????
                            discernSucceedView(user.getUserName(), mRBmp);
                        }
                        //??????3???????????????????????????????????????????????????
                        delaySendData(livenessModel, imageData, user);
                        //??????????????????
                        sendSerialPortData(user);

                    }
                }
            }
        });
    }


    /**
     * ??????????????????
     *
     * @param faceInfo
     * @param bitMapWidth
     * @param bitMapHeight
     * @return
     */
    public boolean faceSizeFilter(FaceInfo faceInfo, int bitMapWidth, int bitMapHeight) {
        if (faceInfo.centerX > bitMapWidth * 3 / 4) {
            //???????????????????????????
            return false;
        } else if (faceInfo.centerX < bitMapWidth / 4) {
            //???????????????????????????
            return false;
        }
        return true;
    }

    private void delayGreenLight() {
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
        manager.pullUpGreenLight();
        GreenLight_Status = 1;
//            }
//        }, 200);
    }

    private void delayRedLight() {
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
        manager.pullUpRedLight();
        RedLight_Status = 1;
//            }
//        }, 200);
    }

    private void delayClose_Red_GedreenLight() {
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
        manager.pullDownGreenLight();
        manager.pullDownRedLight();
//            }
//        }, 200);
    }


    /**
     * ??????3???????????????????????????????????????????????????
     *
     * @param livenessModel ??????
     * @param imageData     ????????????
     * @param user          user = null ???????????? , user != null ????????????
     */
    private void delaySendData(LivenessModel livenessModel, byte[] imageData, User user) {
        if (!faceFlag) {

            faceFlag = true;

            handler.postDelayed(() -> {

                if (livenessModel.getFeature() != null) {
                    if (user == null) {
                        LiveDataBus.get().with("switchPort").postValue(switchPortNum);
                        //?????????????????????????????????
                        byte[] registerData = Utils.concat(
                                //??????????????????????????????
                                Utils.concat(livenessModel.getFeature(), cardNumberByte),
                                //???????????????????????????????????????
                                Utils.concat(personnelNameByte, imageData)
                        );
                        LiveDataBus.get().with("registerData").postValue(registerData);
                    } else {
                        //?????? ??????????????????????????????????????????
                        try {
                            byte[] registerData = Utils.addBytes(
                                    //?????? ???????????????
                                    Utils.concat(livenessModel.getFeature(), Utils.hexString2Bytes(user.getUserInfo())),
                                    //??????
                                    user.getUserName().getBytes("GB2312"),
                                    //??????
                                    imageData
                            );
                            LiveDataBus.get().with("registerData").postValue(registerData);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }

                faceFlag = false;

            }, 3000);
        }
    }

    /**
     * ??????????????????
     */
    private void sendSerialPortData(User user) {
        if (user == null) {
            if (faceTime.containsKey("fail")) {
                Long oldTime = faceTime.get("fail");
                Long newTime = System.currentTimeMillis();
                if (newTime - oldTime > 3000) {
                    faceTime.put("fail", newTime);
                    mErrorTextView.setText("" + mErrorNum++);
                    mLiveTextView.setText("" + mLiveNum++);
                    LiveDataBus.get().with("SerialData").setValue(Utils.getRedLightData());
                    if (SingleBaseConfig.getBaseConfig().getMusicSwitch() == 1) {
                        //????????????
                        mSoundPool.play(soundID.get(1), volume, volume, 0, 0, 1);
//                        playFromRawFile(1);
                    }
                }
            } else {
                Long time = System.currentTimeMillis();
                faceTime.put("fail", time);
                mErrorTextView.setText("" + mErrorNum++);
                mLiveTextView.setText("" + mLiveNum++);
                LiveDataBus.get().with("SerialData").setValue(Utils.getRedLightData());
                if (SingleBaseConfig.getBaseConfig().getMusicSwitch() == 1) {
                    //????????????
                    mSoundPool.play(soundID.get(1), volume, volume, 0, 0, 1);
//                    playFromRawFile(1);
                }
            }
        } else {
            if (faceTime.containsKey(user.getUserName())) {
                Long oldTime = faceTime.get(user.getUserName());
                Long newTime = System.currentTimeMillis();
                if (newTime - oldTime > 3000) {
                    faceTime.put(user.getUserName(), newTime);
//                                    String ids = new BigInteger(id, 10).toString(16);
                    //crc????????????????????????????????????????????????
                    byte[] crcUuid = Utils.getSendId(Utils.hexString2Bytes(Utils.addZero(user.getUserInfo())));
                    //????????????
                    wiegandOutput34(Utils.addZero(user.getUserInfo()));
                    //??????????????????
                    LiveDataBus.get().with("SerialData").setValue(crcUuid);
                    LiveDataBus.get().with("SerialData").setValue(Utils.getGreenLightData());
                    if (SingleBaseConfig.getBaseConfig().getMusicSwitch() == 1) {
                        //????????????

                        mSoundPool.play(soundID.get(2), volume, volume, 0, 0, 1);
//                        playFromRawFile(2);
                    }
                    mLiveTextView.setText("" + mLiveNum++);
                    mAdoptTextView.setText("" + mAdoptNum++);
                }
            } else {
                Long time = System.currentTimeMillis();
                faceTime.put(user.getUserName(), time);
//                                String ids = new BigInteger(id, 10).toString(16);
                //crc????????????????????????????????????????????????
                byte[] crcUuid = Utils.getSendId(Utils.hexString2Bytes(Utils.addZero(user.getUserInfo())));
                //????????????
                wiegandOutput34(Utils.addZero(user.getUserInfo()));
                //??????????????????
                LiveDataBus.get().with("SerialData").setValue(crcUuid);
                LiveDataBus.get().with("SerialData").setValue(Utils.getGreenLightData());
                if (SingleBaseConfig.getBaseConfig().getMusicSwitch() == 1) {
                    //????????????
                    mSoundPool.play(soundID.get(2), volume, volume, 1, 0, 1);
//                    playFromRawFile(2);
                }
                mAdoptTextView.setText("" + mAdoptNum++);
                mLiveTextView.setText("" + mLiveNum++);
            }
        }
    }

    /**
     * ??????????????????
     */
    private void showFrame(final LivenessModel model) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Canvas canvas = mFaceDetectImageView.lockCanvas();
                if (canvas == null) {
                    mFaceDetectImageView.unlockCanvasAndPost(canvas);
                    return;
                }
                if (model == null) {
                    // ??????canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mFaceDetectImageView.unlockCanvasAndPost(canvas);
                    return;
                }
                FaceInfo[] faceInfos = model.getTrackFaceInfo();
                if (faceInfos == null || faceInfos.length == 0) {
                    // ??????canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mFaceDetectImageView.unlockCanvasAndPost(canvas);
                    return;
                }
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                FaceInfo faceInfo = faceInfos[0];

                rectF.set(FaceOnDrawTexturViewUtil.getFaceRectTwo(faceInfo));
                // ??????????????????????????????????????????????????????????????????
                FaceOnDrawTexturViewUtil.mapFromOriginalRect(rectF,
                        mAutoCameraPreviewView, model.getBdFaceImageInstance());
                paint.setColor(Color.GREEN);
                paint.setStyle(Paint.Style.STROKE);
                // ?????????
                canvas.drawRect(rectF, paint);
                mFaceDetectImageView.unlockCanvasAndPost(canvas);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        CameraPreviewManager.getInstance().stopPreview();
        mWiegand.release();
        if (SingleBaseConfig.getBaseConfig().getMusicSwitch() == 1) {
//            mSoundPool.release();
            if (mediaPlayer != null) {
                //??????
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }

        delayClose_Red_GedreenLight();
        //???????????????????????????????????????
//        timeFlagBool = false;
        // ???????????????????????????
        if (mRBmp != null && !mRBmp.isRecycled()) {
            // ??????????????????null
            mRBmp.recycle();
            mRBmp = null;
        }
        System.gc();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        closeAllLight();
        delayClose_Red_GedreenLight();
    }

    public void setUpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_dialog, null);
        final EditText etUsername = view.findViewById(R.id.username);
        final EditText etPassword = view.findViewById(R.id.password);
        builder.setView(view).setTitle("???????????????????????????")
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (etUsername.getText().toString().length() == 0 || etPassword.getText().toString().length() == 0) {
                            ToastUtils.toast(getApplicationContext(), "???????????????????????????!");
                        } else if (etUsername.getText().toString().equals("Admin")
                                && etPassword.getText().toString().equals("123456")) {
                            startActivity(new Intent(mContext, FaceMainSearchActivity.class));
                            finish();
                        } else {
                            ToastUtils.toast(getApplicationContext(), "????????????????????????!");
                        }
                    }
                }).setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        NavigationBarUtil.dialogShow(dialog);
    }

    private void showExitDialog(String text) {
        new AlertDialog.Builder(this)
                .setTitle("??????")
                .setMessage(text)
                .setPositiveButton("??????", null)
                .setCancelable(false)
                .show();
    }

    /**
     * ????????????
     *
     * @param mInt 1 ???????????? 2 ??????
     */
    private void playFromRawFile(int mInt) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        //??????
        mediaPlayer.reset();
        if (mInt == 1) {
            mediaPlayer = MediaPlayer.create(mContext, R.raw.unregistered);
        } else if (mInt == 2) {
            mediaPlayer = MediaPlayer.create(mContext, R.raw.success);
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.start();
        //????????????????????????start???
        mediaPlayer.setVolume(1.0f, 1.0f);
    }

    /**
     * ????????????
     *
     * @param name   ??????
     * @param bitmap ??????
     */
    private void discernSucceedView(String name, Bitmap bitmap) {
        rlDiscernBg.setVisibility(View.VISIBLE);
        rlDiscernBg.setBackgroundResource(R.mipmap.discern_succeed_bg);

        detect_reg_image_item.setImageBitmap(bitmap);

        tvDiscernSucceed.setVisibility(View.VISIBLE);

        imgLine.setVisibility(View.VISIBLE);

        tvName.setVisibility(View.VISIBLE);
        tvName.setText("?????????" + name);

        tvDiscernFailure.setVisibility(View.GONE);
    }

    /**
     * ????????????
     */
    private void discernFailureView() {
        rlDiscernBg.setVisibility(View.VISIBLE);
        rlDiscernBg.setBackgroundResource(R.mipmap.discern_failure_bg);

        detect_reg_image_item.setImageResource(R.mipmap.discern_failure_bg_1);

        tvDiscernSucceed.setVisibility(View.GONE);

        imgLine.setVisibility(View.GONE);

        tvName.setVisibility(View.GONE);

        tvDiscernFailure.setVisibility(View.VISIBLE);
    }

    /**
     * ??????BLE??????
     */
    private void scanLeDevice() {
        if (mBLEScanner == null) {
            mBLEScanner = bluetoothAdapter.getBluetoothLeScanner();
        }
        //SDK < 21??????bluetoothAdapter.startLeScan(new BluetoothAdapter.LeScanCallback())
        mBLEScanner.startScan(scanCallback);

        //??????????????????
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBLEScanner.stopScan(scanCallback);
                scanLeDevice();
            }
        }, 10 * 3000); //30?????????????????????????????????5???
    }


    /**
     * ????????????34???
     */
    private void wiegandOutput34(String id) {
        try {
            BigInteger data = new BigInteger(id, 16);
            int result = mWiegand.output34(data.longValue());
            Log.i("TAG", "Wiegand34 output result:" + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ??????????????????
     * bitmap.getPixel????????????ARGB?????????????????????????????????R???G???B?????????
     * ????????????=0.229??R + 0.587*G + 0.114*B????????????????????????
     * ??????????????????????????????????????????????????????
     * ??????????????????128????????????????????????????????????????????????128??????????????????????????????
     */
    private int getBright(Bitmap bm) {
//        Log.d("TAG", "getBright start");
        if (bm == null) return -1;
        int width = bm.getWidth();
        int height = bm.getHeight();
        int r, g, b;
        int count = 0;
        int bright = 0;
        count = width * height;
        int[] buffer = new int[width * height];
        bm.getPixels(buffer, 0, width, 0, 0, width, height);
//        Log.d("TAG", "width:" + width + ",height:" + height);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int localTemp = buffer[j * width + i];//bm.getPixel(i, j);
                r = (localTemp >> 16) & 0xff;
                g = (localTemp >> 8) & 0xff;
                b = localTemp & 0xff;
                bright = (int) (bright + 0.299 * r + 0.587 * g + 0.114 * b);
            }
        }
//        Log.d("TAG", "getBright end");
        return bright / count;

    }

    //?????????????????????
    private void closeAllLight() {
        manager.pullDownWhiteLight();
        manager.pullDownRedLight();
        manager.pullDownGreenLight();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    public void getDevicesNum() {
        String ip = NetWorkUtils.getLocalIpAddress();
        if (ip != null && !ip.equals("")) {
            String newIP = ip.replace(".", "");
            String deviceID = newIP.substring(newIP.length() - 6);
            TextView tvDeviceID = findViewById(R.id.tvDeviceID);
            tvDeviceID.setText("??????ID???" + deviceID);
            Log.d("TAG", "onCreate: ??????ID" + deviceID);
        }
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.e("TAG", "onBatchScanResults: " + results.toString());
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (result.getScanRecord() != null) {
                Log.e("TAG", "onScanResult: " + result.toString() + "\n" + Utils.byteToHex(result.getScanRecord().getBytes()));
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e("TAG", "onScanFailed: " + errorCode);
        }
    };


}
