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

import com.baidu.idl.face.main.api.Wiegand;
import com.baidu.idl.face.main.callback.CameraDataCallback;
import com.baidu.idl.face.main.callback.FaceDetectCallBack;
import com.baidu.idl.face.main.camera.AutoTexturePreviewView;
import com.baidu.idl.face.main.camera.CameraPreviewManager;
import com.baidu.idl.face.main.constant.BaseConstant;
import com.baidu.idl.face.main.manager.FaceSDKManager;
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


/**
 * @Time 2019/06/02
 * @Author v_shishuaifeng
 * @Description RGB关闭Debug页面
 */
public class FaceRGBCloseDebugSearchActivity extends BaseActivity {
    private GPIOManager manager;
    private Wiegand mWiegand;
    //亮度值
    private static final int BRIGHTNESS_VALUE = 128;
    //补光灯状态
    private int whiteLight_Status = 0;
    private int RedLight_Status = 0;
    private int GreenLight_Status = 0;
    //检测灯状态
    private boolean checkLight = true;

    // 图片越大，性能消耗越大，也可以选择640*480， 1280*720
    private static final int PREFER_WIDTH = 640;
    private static final int PERFER_HEIGH = 480;

    private Context mContext;

    // 关闭Debug 模式
    private AutoTexturePreviewView mAutoCameraPreviewView;
    private TextureView mFaceDetectImageView;
    private Paint paint;
    private RectF rectF;
    private RelativeLayout relativeLayout;
    private int mLiveType;
    private float mRgbLiveScore;
    private Button mMenubtn;
    private ImageView switchPort;

    //播报mp3
    private SoundPool mSoundPool = null;
    private MediaPlayer mediaPlayer = null;
    private final HashMap<Integer, Integer> soundID = new HashMap<>();
    private float volume;

    private Hashtable<String, Long> faceTime = new Hashtable<>();
    /**
     * 存储人脸照片
     */
    private final Hashtable<String, byte[]> faceImage = new Hashtable<>();
    private Bitmap mRBmp;

    /**
     * 双击监听
     */
    private DoubleClickListener doubleClickListener;
    /**
     * 时间戳标识
     */
    private String timeFlag;
    /**
     * 监听时间戳线程标识
     */
    private boolean timeFlagBool = true;
    /**
     * 卡号
     */
    private byte[] cardNumberByte = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    /**
     * 人员名称
     */
    private byte[] personnelNameByte = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};

    /**
     * 照片信息头
     */
    private byte[] imageHead = new byte[]{(byte) 0xFF, (byte) 0xD8};
    /**
     * 照片信息尾
     */
    private byte[] imageEnd = new byte[]{(byte) 0xFF, (byte) 0xD9};
    /**
     * 检测人脸间隔标识
     */
    private boolean faceFlag = false;
    private boolean faceFlagss = false;
    /**
     * 检测人脸间隔 handler
     */
    private Handler handler = new Handler();
    /**
     * 识别成功、失败view
     */
    private RelativeLayout rlDiscernBg;
    private ImageView detect_reg_image_item;
    private TextView tvDiscernSucceed;
    private ImageView imgLine;
    private TextView tvName;
    private TextView tvDiscernFailure;
    /**
     * 底部状态View 活体检测、识别通过、识别未通过、发送信息、服务器状态
     */
    private TextView mLiveTextView, mAdoptTextView, mErrorTextView, mSendTextView, mServerStateTextView;
    private int mLiveNum, mAdoptNum, mErrorNum;

    /**
     * 蓝牙扫描
     */
    private BluetoothLeScanner mBLEScanner;
    /**
     * 蓝牙Adapter
     */
    private BluetoothAdapter bluetoothAdapter;
    /**
     * 字节数组输出流
     */
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    //切换寄存器地址 false: 21       true:22(需要注册的)
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

        getDevicesNum();

        //监听时间戳，获取被赋值的时间比当前时间小于1秒，说明屏幕卡住不动了
        monitorTimestamp();

        //监听状态灯
//        checkLightState();

        initSP();
        // 屏幕的宽
        int displayWidth = DensityUtils.getDisplayWidth(mContext);
        // 屏幕的高
        int displayHeight = DensityUtils.getDisplayHeight(mContext);
        // 当屏幕的宽大于屏幕宽时
        if (displayHeight < displayWidth) {
            // 获取高
            int height = displayHeight;
            // 获取宽
            int width = (int) (displayHeight * ((9.0f / 16.0f)));
            // 设置布局的宽和高
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
            // 设置布局居中
            params.gravity = Gravity.CENTER;
            relativeLayout.setLayoutParams(params);
        }

        BluetoothManager manager = MainActivity.getManager(this);
        if (manager != null) {
            bluetoothAdapter = manager.getAdapter();
        }

        //扫描BLE设备
//        scanLeDevice();

        //接收数据
        liveDataBus();
    }

    private void liveDataBus() {
        LiveDataBus.get().with("heart", Boolean.class).observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean heart) {
                if (heart) {
                    mServerStateTextView.setText("在线");
                } else {
                    mServerStateTextView.setText("离线");
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
     *  关闭Debug 模式view
     */

    private void initView() {
        // 活体状态
        mLiveType = SingleBaseConfig.getBaseConfig().getType();
        // 活体阈值
        mRgbLiveScore = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
        // 获取整个布局
        relativeLayout = findViewById(R.id.all_relative);
        // 画人脸框
        paint = new Paint();
        rectF = new RectF();
        mFaceDetectImageView = findViewById(R.id.draw_detect_face_view);
        mFaceDetectImageView.setOpaque(false);
        //保持长亮
        mFaceDetectImageView.setKeepScreenOn(true);

        // 单目摄像头RGB 图像预览
        mAutoCameraPreviewView = findViewById(R.id.auto_camera_preview_view);
        mAutoCameraPreviewView.setVisibility(View.VISIBLE);

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


        // 菜单按钮
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
                        showExitDialog("注册模式");
                        break;
                    case 1:
                        switchPortNum = false;
                        flag = 0;
                        showExitDialog("正常模式");
                        break;
                }
            }
        });
    }

    /**
     * 监听时间戳，获取被赋值的时间比当前时间小于1秒，说明屏幕卡住不动了
     */
    private void monitorTimestamp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (timeFlagBool) {
                    try {
                        Thread.sleep(1000);
                        if (!TextUtils.isEmpty(timeFlag)) {
                            //Long.parseLong(DateUtil.timeStamp()) - 10 ,当前时间-1秒
                            if (Long.parseLong(timeFlag) < Long.parseLong(DateUtil.timeStamp()) - 10) {
                                //发送平板关机消息
                                LiveDataBus.get().with(BaseConstant.slabShutdownMessage).postValue(1);
                            }
                        }
                        if (DateUtil.getHHmmss().equals("00:00:00")) {
                            //发送平板关机消息
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
     * 声音播报
     */
    private void initSP() {
        if (Build.VERSION.SDK_INT > 21) {
            SoundPool.Builder builder = new SoundPool.Builder();
            //传入音频数量
            builder.setMaxStreams(1);
            //AudioAttributes是一个封装音频各种属性的方法
            AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
            //设置音频流的合适的属性
            attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);//STREAM_MUSIC
            //加载一个AudioAttributes
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
    }

    private void startTestCloseDebugRegisterFunction() {
        // 设置USB摄像头
        CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);
        CameraPreviewManager.getInstance().startPreview(this, mAutoCameraPreviewView,
                PREFER_WIDTH, PERFER_HEIGH, new CameraDataCallback() {
                    @Override
                    //相机预览页面回调方法
                    public void onGetCameraData(byte[] data, Camera camera, int width, int height) {
                        //每次回调后获取当前时间赋值给timeFlag
                        timeFlag = DateUtil.timeStamp();
                        // 摄像头预览数据进行人脸检测
                        FaceSDKManager.getInstance().onDetectCheck(data, null, null,
                                height, width, mLiveType, new FaceDetectCallBack() {
                                    @Override
                                    public void onFaceDetectCallback(LivenessModel livenessModel) {
                                        // 输出结果
                                        checkCloseResult(livenessModel, width, height);
                                    }

                                    //识别失败
                                    @Override
                                    public void onTip(int code, String msg) {
                                        displayTip(code, msg);
                                    }

                                    //绘制人脸框
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
                    //识别失败
                    discernFailureView();
                }
            }
        });
    }

    /**
     * 输出结果
     *
     * @param livenessModel
     * @param width
     * @param height
     */
    private void checkCloseResult(final LivenessModel livenessModel, int width, int height) {
        // 当未检测到人脸UI显示.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
               /* Log.e("bright", "白色补光的状态是: " + manager.getWhiteLightStatus()
                        + "绿色补光的状态是: " + manager.getGreenLightStatus()
                        + "红色补光的状态是: " + manager.getRedLightStatus());*/

                if (livenessModel == null || livenessModel.getFaceInfo() == null || !faceSizeFilter(livenessModel.getFaceInfo(), width, height)) {
                    //隐藏识别成功、失败窗口
                    rlDiscernBg.setVisibility(View.GONE);
                    //延迟关闭状态灯
//                    closeAllLight();
                    delayClose_Red_GedreenLight();
                    whiteLight_Status = 0;
                    RedLight_Status = 0;
                    GreenLight_Status = 0;
                } else {
                    float rgbLivenessScore = livenessModel.getRgbLivenessScore();
                    if (rgbLivenessScore < mRgbLiveScore) {
//                        mRelativeLayout.setVisibility(View.VISIBLE);
//                        mTrackText.setText("识别失败");
//                        mRelativeLayout.setBackgroundColor(Color.RED);
//                        mDetectText.setText("活体检测未通过");
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

                    //获取照片
                    mRBmp = BitmapUtils.getInstaceBmp(livenessModel.getBdFaceImageInstance());
                    //获取照片亮度值
                    int bright = getBright(mRBmp);
                    //如果这个值比128大，则这个图片较亮，如果这个值比128小，则这个图比较暗。
                    /*if (whiteLight_Status == 0 && bright > BRIGHTNESS_VALUE) {
                        delayLight();
                    }*/
                    if (whiteLight_Status == 0 && bright < BRIGHTNESS_VALUE) {
                        manager.pullUpWhiteLight();
                    }

                    Log.e("bright", "图片亮度: " + bright + "白色补光的状态是: " + manager.getWhiteLightStatus());
                    //压缩照片
                    mRBmp.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                    byte[] imageData = Utils.addBytes(imageHead, byteArrayOutputStream.toByteArray(), imageEnd);
                    byteArrayOutputStream.reset();

                    User user = livenessModel.getUser();
                    if (user == null) {
                        if (livenessModel.getFeatureContrastValue() < 80.00) {

                            if (GreenLight_Status == 1) {
                                manager.pullDownGreenLight();
                                GreenLight_Status = 0;
                            }

//                            manager.pullUpRedLight();
//                            RedLight_Status = 1;
                            delayRedLight();
                            //延迟3秒发送给后台图片、特征值
                            delaySendData(livenessModel, imageData, null);
                            //识别失败
                            discernFailureView();
                            //发送串口数据
                            sendSerialPortData(null);
                        }
                    } else {
                        if (RedLight_Status == 1) {
                            manager.pullDownRedLight();
                            RedLight_Status = 0;
                        }

                        delayGreenLight();
//                        manager.pullUpGreenLight();
//                        GreenLight_Status = 1;


                        if (faceImage.containsKey(user.getUserName())) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(faceImage.get(user.getUserName()), 0, faceImage.get(user.getUserName()).length);
                            //识别成功
                            discernSucceedView(user.getUserName(), bitmap);
                        } else {
                            //获取照片
                            mRBmp = BitmapUtils.getInstaceBmp(livenessModel.getBdFaceImageInstance());
                            //压缩照片
                            mRBmp.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                            faceImage.put(user.getUserName(), byteArrayOutputStream.toByteArray());
                            byteArrayOutputStream.reset();
                            //识别成功
                            discernSucceedView(user.getUserName(), mRBmp);
                        }
                        //延迟3秒发送给后台图片，特征，人名，卡号
                        delaySendData(livenessModel, imageData, user);
                        //发送串口数据
                        sendSerialPortData(user);
                    }
                }
            }
        });
    }


    /**
     * 人脸大小过滤
     *
     * @param faceInfo
     * @param bitMapWidth
     * @param bitMapHeight
     * @return
     */
    public boolean faceSizeFilter(FaceInfo faceInfo, int bitMapWidth, int bitMapHeight) {
        if (faceInfo.centerX > bitMapWidth * 3 / 4) {
            //人脸在屏幕中太靠右
            return false;
        } else if (faceInfo.centerX < bitMapWidth / 4) {
            //人脸在屏幕中太靠左
            return false;
        }
        return true;
    }

    private void delayGreenLight() {
//        if (manager.getRedLightStatus().equals("1")) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                manager.pullDownWhiteLight();
                whiteLight_Status = 1;
                manager.pullUpGreenLight();
                GreenLight_Status = 1;
            }
        }, 200);
//        }
    }

    private void delayRedLight() {
//        if (manager.getRedLightStatus().equals("1")) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                manager.pullDownWhiteLight();
                whiteLight_Status = 1;
                manager.pullUpRedLight();
                RedLight_Status = 1;
            }
        }, 200);
//        }
    }

    private void delayClose_Red_GedreenLight() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                manager.pullDownGreenLight();
                manager.pullDownRedLight();
            }
        }, 200);
    }


    /**
     * 延迟3秒发送给后台图片，特征，人名，卡号
     *
     * @param livenessModel 模型
     * @param imageData     照片字节
     * @param user          user = null 识别失败 , user != null 识别成功
     */
    private void delaySendData(LivenessModel livenessModel, byte[] imageData, User user) {
        if (!faceFlag) {

            faceFlag = true;

            handler.postDelayed(() -> {

                if (livenessModel.getFeature() != null) {
                    if (user == null) {
                        LiveDataBus.get().with("switchPort").postValue(switchPortNum);
                        //发送给后台图片、特征值
                        byte[] registerData = Utils.concat(
                                //拼接特征值、卡号字节
                                Utils.concat(livenessModel.getFeature(), cardNumberByte),
                                //拼接人员名称、照片信息字节
                                Utils.concat(personnelNameByte, imageData)
                        );
                        LiveDataBus.get().with("registerData").postValue(registerData);
                    } else {
                        //拼接 特征、卡号、人名、照片，字节
                        try {
                            byte[] registerData = Utils.addBytes(
                                    //拼接 特征、卡号
                                    Utils.concat(livenessModel.getFeature(), Utils.hexString2Bytes(user.getUserInfo())),
                                    //人名
                                    user.getUserName().getBytes("GB2312"),
                                    //照片
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
     * 发送串口数据
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
                        //播放音频
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
                    //播放音频
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
                    //crc检验过后，拼接得到需要发送的卡号
                    byte[] crcUuid = Utils.getSendId(Utils.hexString2Bytes(Utils.addZero(user.getUserInfo())));
                    //韦根输出
                    wiegandOutput34(Utils.addZero(user.getUserInfo()));
                    //发送串口数据
                    LiveDataBus.get().with("SerialData").setValue(crcUuid);
                    LiveDataBus.get().with("SerialData").setValue(Utils.getGreenLightData());
                    if (SingleBaseConfig.getBaseConfig().getMusicSwitch() == 1) {
                        //播放音频

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
                //crc检验过后，拼接得到需要发送的卡号
                byte[] crcUuid = Utils.getSendId(Utils.hexString2Bytes(Utils.addZero(user.getUserInfo())));
                //韦根输出
                wiegandOutput34(Utils.addZero(user.getUserInfo()));
                //发送串口数据
                LiveDataBus.get().with("SerialData").setValue(crcUuid);
                LiveDataBus.get().with("SerialData").setValue(Utils.getGreenLightData());
                if (SingleBaseConfig.getBaseConfig().getMusicSwitch() == 1) {
                    //播放音频
                    mSoundPool.play(soundID.get(2), volume, volume, 1, 0, 1);
//                    playFromRawFile(2);
                }
                mAdoptTextView.setText("" + mAdoptNum++);
                mLiveTextView.setText("" + mLiveNum++);
            }
        }
    }

    /**
     * 绘制人脸框。
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
                    // 清空canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mFaceDetectImageView.unlockCanvasAndPost(canvas);
                    return;
                }
                FaceInfo[] faceInfos = model.getTrackFaceInfo();
                if (faceInfos == null || faceInfos.length == 0) {
                    // 清空canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mFaceDetectImageView.unlockCanvasAndPost(canvas);
                    return;
                }
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                FaceInfo faceInfo = faceInfos[0];

                rectF.set(FaceOnDrawTexturViewUtil.getFaceRectTwo(faceInfo));
                // 检测图片的坐标和显示的坐标不一样，需要转换。
                FaceOnDrawTexturViewUtil.mapFromOriginalRect(rectF,
                        mAutoCameraPreviewView, model.getBdFaceImageInstance());
                paint.setColor(Color.GREEN);
                paint.setStyle(Paint.Style.STROKE);
                // 绘制框
                canvas.drawRect(rectF, paint);
                mFaceDetectImageView.unlockCanvasAndPost(canvas);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWiegand.release();
        if (SingleBaseConfig.getBaseConfig().getMusicSwitch() == 1) {
//            mSoundPool.release();
            if (mediaPlayer != null) {
                //释放
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }

//        closeAllLight();
        delayClose_Red_GedreenLight();
        //关闭监听屏幕有没有卡住线程
        timeFlagBool = false;
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
        builder.setView(view).setTitle("请输入用户名和密码")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (etUsername.getText().toString().length() == 0 || etPassword.getText().toString().length() == 0) {
                            ToastUtils.toast(getApplicationContext(),"账号或密码不能为空!");
                        } else if (etUsername.getText().toString().equals("Admin")
                                && etPassword.getText().toString().equals("123456")) {
                            startActivity(new Intent(mContext, FaceMainSearchActivity.class));
                            finish();
                        } else {
                            ToastUtils.toast(getApplicationContext(),"账号或密码不正确!");
                        }
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
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
                .setTitle("提示")
                .setMessage(text)
                .setPositiveButton("确定", null)
                .show();
    }

    /**
     * 播放音频
     *
     * @param mInt 1 未注册， 2 成功
     */
    private void playFromRawFile(int mInt) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        //重置
        mediaPlayer.reset();
        if (mInt == 1) {
            mediaPlayer = MediaPlayer.create(mContext, R.raw.unregistered);
        } else if (mInt == 2) {
            mediaPlayer = MediaPlayer.create(mContext, R.raw.success);
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.start();
        //设置音量，需要在start后
        mediaPlayer.setVolume(1.0f, 1.0f);
    }

    /**
     * 识别成功
     *
     * @param name   姓名
     * @param bitmap 照片
     */
    private void discernSucceedView(String name, Bitmap bitmap) {
        rlDiscernBg.setVisibility(View.VISIBLE);
        rlDiscernBg.setBackgroundResource(R.mipmap.discern_succeed_bg);

        detect_reg_image_item.setImageBitmap(bitmap);

        tvDiscernSucceed.setVisibility(View.VISIBLE);

        imgLine.setVisibility(View.VISIBLE);

        tvName.setVisibility(View.VISIBLE);
        tvName.setText("姓名：" + name);

        tvDiscernFailure.setVisibility(View.GONE);
    }

    /**
     * 识别失败
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
     * 扫描BLE设备
     */
    private void scanLeDevice() {
        if (mBLEScanner == null) {
            mBLEScanner = bluetoothAdapter.getBluetoothLeScanner();
        }
        //SDK < 21使用bluetoothAdapter.startLeScan(new BluetoothAdapter.LeScanCallback())
        mBLEScanner.startScan(scanCallback);

        //设置结束扫描
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBLEScanner.stopScan(scanCallback);
                scanLeDevice();
            }
        }, 10 * 3000); //30秒内不可断开、连接重复5次
    }


    /**
     * 输出韦根34位
     */
    private void wiegandOutput34(String id) {
        try {
            BigInteger data = new BigInteger(id, 16);
            int result = mWiegand.output34(data.longValue());
            Log.i("TAG", "Wiegand26 output result:" + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取图片亮度
     * bitmap.getPixel返回的是ARGB值，通过移位操作获取到R、G、B的值，
     * 使用亮度=0.229×R + 0.587*G + 0.114*B进行亮度值计算，
     * 将所有点的亮度值相加后取一个平均值，
     * 如果这个值比128大，则这个图片较亮，如果这个值比128小，则这个图比较暗。
     */
    private int getBright(Bitmap bm) {
        Log.d("TAG", "getBright start");
        if (bm == null) return -1;
        int width = bm.getWidth();
        int height = bm.getHeight();
        int r, g, b;
        int count = 0;
        int bright = 0;
        count = width * height;
        int[] buffer = new int[width * height];
        bm.getPixels(buffer, 0, width, 0, 0, width, height);
        Log.d("TAG", "width:" + width + ",height:" + height);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int localTemp = buffer[j * width + i];//bm.getPixel(i, j);
                r = (localTemp >> 16) & 0xff;
                g = (localTemp >> 8) & 0xff;
                b = localTemp & 0xff;
                bright = (int) (bright + 0.299 * r + 0.587 * g + 0.114 * b);
            }
        }
        Log.d("TAG", "getBright end");
        return bright / count;

    }

    //关闭所有状态灯
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
            tvDeviceID.setText("设备ID：" + deviceID);
            Log.d("TAG", "onCreate: 设备ID" + deviceID);
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
