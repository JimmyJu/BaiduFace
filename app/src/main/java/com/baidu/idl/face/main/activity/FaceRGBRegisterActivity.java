package com.baidu.idl.face.main.activity;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.idl.face.main.camera.CameraPreviewManager;
import com.baidu.idl.face.main.model.GlobalSet;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.activity.setting.SettingMainActivity;
import com.baidu.idl.face.main.model.User;
import com.baidu.idl.face.main.utils.BitmapUtils;
import com.baidu.idl.face.main.api.FaceApi;
import com.baidu.idl.face.main.camera.AutoTexturePreviewView;
import com.baidu.idl.face.main.callback.CameraDataCallback;
import com.baidu.idl.face.main.manager.FaceSDKManager;
import com.baidu.idl.face.main.manager.FaceTrackManager;
import com.baidu.idl.face.main.callback.FaceDetectCallBack;
import com.baidu.idl.face.main.callback.FaceFeatureCallBack;
import com.baidu.idl.face.main.model.LivenessModel;
import com.baidu.idl.face.main.utils.DensityUtils;
import com.baidu.idl.face.main.utils.FileUtils;
import com.baidu.idl.face.main.utils.ImageUtils;
import com.baidu.idl.face.main.utils.LiveDataBus;
import com.baidu.idl.face.main.utils.Utils;
import com.baidu.idl.face.main.view.CircleImageView;
import com.baidu.idl.face.main.view.FaceRoundView;
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.model.Feature;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

import static com.baidu.idl.face.main.activity.FaceMainSearchActivity.PAGE_TYPE;


/**
 * @Description: 注册的采集人脸页面 - RGB
 */

public class FaceRGBRegisterActivity extends BaseActivity implements View.OnClickListener {

    private Button backButton;
    private Button setButton;
    private AutoTexturePreviewView mPreviewView;
    private ImageView testImageview;
    private RelativeLayout mRelativeLayout;


    // 注册的 提示 view
    private TextView mTrackText;
    private TextView mDetectText;
    private CircleImageView mDetectImage;

    // RGB摄像头图像宽和高
    private static final int mWidth = 640;
    private static final int mHeight = 480;
    private boolean qualityControl;
    private String username = null;
    private String groupId = null;
    private String userInfo = null;

    private Bitmap rgbBitmap = null;

    private Context mContext;

    private boolean registerFlag = false;

    private boolean faceFlag = false;
    private byte[] imageHead = new byte[]{(byte) 0xFF, (byte) 0xD8};
    private byte[] imageEnd = new byte[]{(byte) 0xFF, (byte) 0xD9};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_reg_detect);
        mContext = this;
        // 获取页面类型 pageType =1 注册；
        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("user_name");
            groupId = intent.getStringExtra("group_id");
            userInfo = intent.getStringExtra("user_info");
        }
        initView();

        qualityControl = SingleBaseConfig.getBaseConfig().isQualityControl();
        // 注册默认开启质量检测
        SingleBaseConfig.getBaseConfig().setQualityControl(true);
        FaceSDKManager.getInstance().initConfig();
    }

    private void initView() {

        backButton = findViewById(R.id.id_regcapture_back);
        setButton = findViewById(R.id.id_regcapture_setting);
        backButton.setOnClickListener(this);
        setButton.setOnClickListener(this);
        // RGB预览
        mPreviewView = findViewById(R.id.auto_camera_preview_view);
        testImageview = findViewById(R.id.test_imgView);
        // 图像预览
        mTrackText = findViewById(R.id.track_txt);
        mDetectText = findViewById(R.id.detect_reg_text);
        mDetectImage = findViewById(R.id.detect_reg_image_item);

        // 注册页面 只支持 固定区域检测
        // 遮罩
       /* FaceRoundView rectView = findViewById(R.id.rect_view);
        rectView.setVisibility(View.VISIBLE);*/

       /* // 需要调整预览 大小
        DisplayMetrics dm = new DisplayMetrics();
        Display display = this.getWindowManager().getDefaultDisplay();
        display.getMetrics(dm);
        // 显示Size
        int mDisplayWidth = dm.widthPixels;
        int mDisplayHeight = dm.heightPixels;
        int w = mDisplayWidth;
        int h = mDisplayHeight;
        FrameLayout.LayoutParams cameraFL = new FrameLayout.LayoutParams(
                (int) (w * GlobalSet.SURFACE_RATIO), (int) (h * GlobalSet.SURFACE_RATIO),
                Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        mPreviewView.setLayoutParams(cameraFL);*/

        mRelativeLayout = findViewById(R.id.layout_info);

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
            mPreviewView.setLayoutParams(params);
        }

    }


    @Override
    public void onClick(View view) {

        if (view == backButton) {
            FaceRGBRegisterActivity.this.finish();
        } else if (view == setButton) {
            Intent intent = new Intent(this, SettingMainActivity.class);
            intent.putExtra("page_type", "register");
            startActivityForResult(intent, PAGE_TYPE);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 摄像头图像预览
        startCameraPreview();
        Log.e("qing", "start camera");
    }

    /**
     * 摄像头图像预览
     */
    private void startCameraPreview() {
        // 设置前置摄像头
//        CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
        // 设置后置摄像头
//         CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_BACK);
        // 设置USB摄像头
        CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);

        // TODO 在得力设备和部分手机上出现过 CameraPreviewManager 崩溃的问题
        CameraPreviewManager.getInstance().startPreview(this, mPreviewView, mWidth, mHeight, new CameraDataCallback() {
            @Override
            public void onGetCameraData(byte[] data, Camera camera, int width, int height) {

                // 调试模式打开 显示实际送检图片的方向，SDK只检测人脸朝上的图
                boolean isRGBDisplay = SingleBaseConfig.getBaseConfig().getDisplay();
                if (isRGBDisplay) {
                    //showDetectImage(data);
                }

                // 拿到相机帧数
                if (faceFlag == false) {
                    //摄像头数据处理
                    faceDetect(data, width, height);
                }

            }
        });
    }

    /**
     * 摄像头数据处理
     *
     * @param data   摄像头当前图像数据
     * @param width  预览宽
     * @param height 预览高
     */
    private void faceDetect(byte[] data, final int width, final int height) {

        // 摄像头预览数据进行人脸检测
        int liveType = SingleBaseConfig.getBaseConfig().getType();
        if (liveType == 1) { // 无活体检测
            FaceTrackManager.getInstance().setAliving(false);
        } else if (Integer.valueOf(liveType) == 2) { // 活体检测
            FaceTrackManager.getInstance().setAliving(true);
        }

        //人脸检测
        FaceTrackManager.getInstance().faceTrack(data, width, height, new FaceDetectCallBack() {
            @Override
            public void onFaceDetectCallback(LivenessModel livenessModel) {
                // 做过滤
                boolean isFilterSuccess = faceSizeFilter(livenessModel.getFaceInfo(), width, height);
                if (isFilterSuccess) {
                    // 展示model
                    checkResult(livenessModel);
                }

            }

            @Override
            public void onTip(int code, final String msg) {
                displayTip(msg);
            }

            @Override
            public void onFaceDetectDarwCallback(LivenessModel livenessModel) {

            }
        });


    }


    /**
     * 显示检测的图片。用于调试，如果人脸sdk检测的人脸需要朝上，可以通过该图片判断。实际应用中可注释掉
     *
     * @param //rgb
     */
  /*  private void showDetectImage(byte[] rgb) {
        if (rgb == null) {
            return;
        }
        BDFaceImageInstance rgbInstance = new BDFaceImageInstance(rgb, mHeight,
                mWidth, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_420,
                SingleBaseConfig.getBaseConfig().getDetectDirection(),
                SingleBaseConfig.getBaseConfig().getMirrorRGB());
        BDFaceImageInstance imageInstance = rgbInstance.getImage();
        final Bitmap bitmap = BitmapUtils.getInstaceBmp(imageInstance);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                testImageview.setVisibility(View.VISIBLE);
                testImageview.setImageBitmap(bitmap);
            }
        });
        // 流程结束销毁图片，开始下一帧图片检测，否则内存泄露
        rgbInstance.destory();
    }*/

    // 检测结果输出
    private void checkResult(final LivenessModel model) {
        if (model == null) {
            clearTip();
            return;
        }
        faceFlag = true;
        int liveType = SingleBaseConfig.getBaseConfig().getType();
        // 无活体
        if (Integer.valueOf(liveType) == 1) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //mTrackText.setVisibility(View.GONE);
                    mDetectText.setText("人脸采集成功");
                }
            });
            displayResult(model, null);
            // 注册
            register(model);

        } else if (Integer.valueOf(liveType) == 2) { // RGB活体检测
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //mTrackText.setVisibility(View.GONE);
                    mRelativeLayout.setVisibility(View.VISIBLE);
                    mTrackText.setBackgroundColor(Color.parseColor("#1EB9EE"));
                    mTrackText.setText("正在采集...");
                    mDetectText.setText("活体检测判断中...");
                }
            });

            displayResult(model, "livess");
            boolean livenessSuccess = false;
            float rgbLiveThreshold = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
            livenessSuccess = (model.getRgbLivenessScore() > rgbLiveThreshold) ? true : false;
            if (livenessSuccess) {
                // 注册
                register(model);
            }

        }
    }

    private void displayResult(final LivenessModel livenessModel, final String livess) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (livess != null && livess.equals("livess")) {
                    float rgbLivenessScore = livenessModel.getRgbLivenessScore();
                    float liveThreadHold = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
                    if (rgbLivenessScore < liveThreadHold) {
                        mTrackText.setVisibility(View.VISIBLE);
                        mTrackText.setText("识别失败");
                        mTrackText.setBackgroundColor(Color.RED);
                        mDetectText.setText("活体检测未通过");
                        mRelativeLayout.setVisibility(View.INVISIBLE);
                        faceFlag = false;
                    }

                }
            }

        });


    }


    private void clearTip() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDetectText.setText("未检测到人脸");
                mRelativeLayout.setVisibility(View.INVISIBLE);
            }
        });

    }


    private void displayTip(final String status) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTrackText.setText("正在采集...");
                mRelativeLayout.setVisibility(View.VISIBLE);
                mDetectText.setText(status);
            }
        });
    }


    /**
     * 注册到人脸库
     *
     * @param model 人脸数据
     */

    private void register(LivenessModel model) {

        if (model == null) {
            return;
        }

        /*if (username == null || groupId == null) {
            displayTip("注册信息缺失");
            return;
        }*/


        BDFaceImageInstance image = model.getBdFaceImageInstance();
        rgbBitmap = BitmapUtils.getInstaceBmp(image);
        // 获取选择的特征抽取模型
        int modelType = SingleBaseConfig.getBaseConfig().getActiveModel();
        if (modelType == 1) {
            // 生活照
            FaceSDKManager.getInstance().onFeatureCheck(model.getBdFaceImageInstance(), model.getLandmarks(),
                    BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO, new FaceFeatureCallBack() {
                        @Override
                        public void onFaceFeatureCallBack(float featureSize, byte[] feature) {
                            //根据特征抽取的结果 注册人脸
                            displayCompareResult(featureSize, feature);
                            Log.e("TAG", "特征size" + String.valueOf(feature.length));
                        }

                    });

        } else if (Integer.valueOf(modelType) == 2) {
            // 证件照
            FaceSDKManager.getInstance().onFeatureCheck(model.getBdFaceImageInstance(), model.getLandmarks(),
                    BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_ID_PHOTO, new FaceFeatureCallBack() {
                        @Override
                        public void onFaceFeatureCallBack(float featureSize, byte[] feature) {
                            //根据特征抽取的结果 注册人脸
                            displayCompareResult(featureSize, feature);
                        }
                    });
        }


    }


    /**
     * 根据特征抽取的结果 注册人脸
     *
     * @param ret         人脸特征大小
     * @param faceFeature 人脸特征
     */
    private void displayCompareResult(float ret, byte[] faceFeature) {

        // 特征提取成功
        if (ret == 128) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTrackText.setText("正在处理");
                }
            });
            ArrayList<Feature> featureResult = FaceSDKManager.getInstance().getFaceFeature().featureSearch(faceFeature,
                    BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO,
                    1, true);
            // TODO 返回top num = 1 个数据集合，此处可以任意设置，会返回比对从大到小排序的num 个数据集合
            if (featureResult != null && featureResult.size() > 0) {
                // 获取第一个数据
                Feature topFeature = featureResult.get(0);
                // 判断第一个阈值是否大于设定阈值，如果大于，检索成功
                if (topFeature != null && topFeature.getScore() > SingleBaseConfig.getBaseConfig().getThreshold()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTrackText.setText("处理完成!");
                            mDetectText.setText("用户已经注册,请勿重复注册");
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    SingleBaseConfig.getBaseConfig().setQualityControl(true);
                                    FaceSDKManager.getInstance().initConfig();
                                    mRelativeLayout.setVisibility(View.INVISIBLE);
                                    faceFlag = false;
                                    return;
                                }
                            }, 3000);
                        }
                    });
                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTrackText.setText("正在入库...");
                    }
                });
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                rgbBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] datas = baos.toByteArray();
                byte[] iamgedata = Utils.addBytes(imageHead, datas, imageEnd);
                final byte[] registerData = Utils.concat(faceFeature, iamgedata);
                LiveDataBus.get().with("registerData").postValue(registerData);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                LiveDataBus.get().with("registerFlag", Boolean.class).observeSticky(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(@Nullable Boolean flag) {
                        if (flag) {
                            registerFlag = true;
                        } else {
                            registerFlag = false;
                        }
                    }
                });
                if (registerFlag) {
                    username = "aa";
                    String imageName = groupId + "-" + username + ".jpg";
                    // 注册到人脸库
                    boolean isSuccess = FaceApi.getInstance().registerUserIntoDBmanager(groupId, username, imageName,
                            userInfo, faceFeature);
                    if (isSuccess) {
                        // 关闭摄像头
                        //CameraPreviewManager.getInstance().stopPreview();
                        Log.e("qing", "注册成功");

                        // 压缩、保存人脸图片至300 * 300
                        File faceDir = FileUtils.getBatchImportSuccessDirectory();
                        File file = new File(faceDir, imageName);
                        ImageUtils.resize(rgbBitmap, file, 300, 300);

                        // 数据变化，更新内存
                        FaceApi.getInstance().initDatabases(true);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTrackText.setVisibility(View.VISIBLE);
                                mTrackText.setText("注册完毕");
                                mTrackText.setBackgroundColor(Color.parseColor("#1EB9EE"));
                                mDetectText.setText("用户已经注册完毕");
                                mDetectImage.setImageBitmap(rgbBitmap);
                                //防止重复注册
                                username = null;
                                groupId = null;
                                // 做延时 finish
                                new Handler().postDelayed(new Runnable() {
                                    public void run() {
                                        SingleBaseConfig.getBaseConfig().setQualityControl(true);
                                        FaceSDKManager.getInstance().initConfig();
                                        mRelativeLayout.setVisibility(View.INVISIBLE);
                                        faceFlag = false;
                                        return;
                                    }
                                }, 3000);

                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTrackText.setVisibility(View.VISIBLE);
                                mTrackText.setText("注册失败");
                                mTrackText.setBackgroundColor(Color.RED);
                                mDetectText.setText("特征提取成功");
                                faceFlag = false;
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTrackText.setText("处理完成");
                            mDetectText.setText("注册失败,服务器未反应");
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    SingleBaseConfig.getBaseConfig().setQualityControl(true);
                                    FaceSDKManager.getInstance().initConfig();
                                    mRelativeLayout.setVisibility(View.INVISIBLE);
                                    faceFlag = false;
                                }
                            }, 3000);
                        }
                    });
                    /*mTrackText.setText("处理完成");
                    mDetectText.setText("注册失败,服务器未反应");
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            SingleBaseConfig.getBaseConfig().setQualityControl(qualityControl);
                            FaceSDKManager.getInstance().initConfig();
                            mRelativeLayout.setVisibility(View.INVISIBLE);
                            faceFlag =false;
                        }
                    }, 3000);*/
                }

            }

        } else if (ret == -1) {
            displayTip("特征提取失败");
        } else {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTrackText.setText("特征提取失败");
                    mTrackText.setBackgroundColor(Color.RED);
                    mDetectText.setText("特征提取失败");
                    faceFlag = false;
                }
            });
        }
    }

    // 人脸大小顾虑
    public boolean faceSizeFilter(FaceInfo faceInfo, int bitMapWidth, int bitMapHeight) {

        // 判断人脸大小，若人脸超过屏幕二分一，则提示文案“人脸离手机太近，请调整与手机的距离”；
        // 若人脸小于屏幕三分一，则提示“人脸离手机太远，请调整与手机的距离”
        float ratio = (float) faceInfo.width / (float) bitMapHeight;
        if (ratio > 0.6) {

            displayTip("人脸离屏幕太近，请调整与屏幕的距离");
            return false;
        } else if (ratio < 0.2) {
            displayTip("人脸离屏幕太远，请调整与屏幕的距离");
            return false;
        } else if (faceInfo.centerX > bitMapWidth * 3 / 4) {
            displayTip("人脸在屏幕中太靠右");
            return false;
        } else if (faceInfo.centerX < bitMapWidth / 4) {
            displayTip("人脸在屏幕中太靠左");
            return false;
        } else if (faceInfo.centerY > bitMapHeight * 3 / 4) {
            displayTip("人脸在屏幕中太靠下");
            return false;
        } else if (faceInfo.centerY < bitMapHeight / 4) {
            displayTip("人脸在屏幕中太靠上");
            return false;
        }

        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        CameraPreviewManager.getInstance().stopPreview();
        // 重置质检状态
        SingleBaseConfig.getBaseConfig().setQualityControl(true);
        FaceSDKManager.getInstance().initConfig();
    }
}
