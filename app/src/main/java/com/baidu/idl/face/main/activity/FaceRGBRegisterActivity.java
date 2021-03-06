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
 * @Description: ??????????????????????????? - RGB
 */

public class FaceRGBRegisterActivity extends BaseActivity implements View.OnClickListener {

    private Button backButton;
    private Button setButton;
    private AutoTexturePreviewView mPreviewView;
    private ImageView testImageview;
    private RelativeLayout mRelativeLayout;


    // ????????? ?????? view
    private TextView mTrackText;
    private TextView mDetectText;
    private CircleImageView mDetectImage;

    // RGB????????????????????????
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
        // ?????????????????? pageType =1 ?????????
        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("user_name");
            groupId = intent.getStringExtra("group_id");
            userInfo = intent.getStringExtra("user_info");
        }
        initView();

        qualityControl = SingleBaseConfig.getBaseConfig().isQualityControl();
        // ??????????????????????????????
        SingleBaseConfig.getBaseConfig().setQualityControl(true);
        FaceSDKManager.getInstance().initConfig();
    }

    private void initView() {

        backButton = findViewById(R.id.id_regcapture_back);
        setButton = findViewById(R.id.id_regcapture_setting);
        backButton.setOnClickListener(this);
        setButton.setOnClickListener(this);
        // RGB??????
        mPreviewView = findViewById(R.id.auto_camera_preview_view);
        testImageview = findViewById(R.id.test_imgView);
        // ????????????
        mTrackText = findViewById(R.id.track_txt);
        mDetectText = findViewById(R.id.detect_reg_text);
        mDetectImage = findViewById(R.id.detect_reg_image_item);

        // ???????????? ????????? ??????????????????
        // ??????
       /* FaceRoundView rectView = findViewById(R.id.rect_view);
        rectView.setVisibility(View.VISIBLE);*/

       /* // ?????????????????? ??????
        DisplayMetrics dm = new DisplayMetrics();
        Display display = this.getWindowManager().getDefaultDisplay();
        display.getMetrics(dm);
        // ??????Size
        int mDisplayWidth = dm.widthPixels;
        int mDisplayHeight = dm.heightPixels;
        int w = mDisplayWidth;
        int h = mDisplayHeight;
        FrameLayout.LayoutParams cameraFL = new FrameLayout.LayoutParams(
                (int) (w * GlobalSet.SURFACE_RATIO), (int) (h * GlobalSet.SURFACE_RATIO),
                Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        mPreviewView.setLayoutParams(cameraFL);*/

        mRelativeLayout = findViewById(R.id.layout_info);

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
        // ?????????????????????
        startCameraPreview();
        Log.e("qing", "start camera");
    }

    /**
     * ?????????????????????
     */
    private void startCameraPreview() {
        // ?????????????????????
//        CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
        // ?????????????????????
//         CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_BACK);
        // ??????USB?????????
        CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);

        // TODO ?????????????????????????????????????????? CameraPreviewManager ???????????????
        CameraPreviewManager.getInstance().startPreview(this, mPreviewView, mWidth, mHeight, new CameraDataCallback() {
            @Override
            public void onGetCameraData(byte[] data, Camera camera, int width, int height) {

                // ?????????????????? ????????????????????????????????????SDK???????????????????????????
                boolean isRGBDisplay = SingleBaseConfig.getBaseConfig().getDisplay();
                if (isRGBDisplay) {
                    //showDetectImage(data);
                }

                // ??????????????????
                if (faceFlag == false) {
                    //?????????????????????
                    faceDetect(data, width, height);
                }

            }
        });
    }

    /**
     * ?????????????????????
     *
     * @param data   ???????????????????????????
     * @param width  ?????????
     * @param height ?????????
     */
    private void faceDetect(byte[] data, final int width, final int height) {

        // ???????????????????????????????????????
        int liveType = SingleBaseConfig.getBaseConfig().getType();
        if (liveType == 1) { // ???????????????
            FaceTrackManager.getInstance().setAliving(false);
        } else if (Integer.valueOf(liveType) == 2) { // ????????????
            FaceTrackManager.getInstance().setAliving(true);
        }

        //????????????
        FaceTrackManager.getInstance().faceTrack(data, width, height, new FaceDetectCallBack() {
            @Override
            public void onFaceDetectCallback(LivenessModel livenessModel) {
                // ?????????
                boolean isFilterSuccess = faceSizeFilter(livenessModel.getFaceInfo(), width, height);
                if (isFilterSuccess) {
                    // ??????model
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
     * ???????????????????????????????????????????????????sdk???????????????????????????????????????????????????????????????????????????????????????
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
        // ???????????????????????????????????????????????????????????????????????????
        rgbInstance.destory();
    }*/

    // ??????????????????
    private void checkResult(final LivenessModel model) {
        if (model == null) {
            clearTip();
            return;
        }
        faceFlag = true;
        int liveType = SingleBaseConfig.getBaseConfig().getType();
        // ?????????
        if (Integer.valueOf(liveType) == 1) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //mTrackText.setVisibility(View.GONE);
                    mDetectText.setText("??????????????????");
                }
            });
            displayResult(model, null);
            // ??????
            register(model);

        } else if (Integer.valueOf(liveType) == 2) { // RGB????????????
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //mTrackText.setVisibility(View.GONE);
                    mRelativeLayout.setVisibility(View.VISIBLE);
                    mTrackText.setBackgroundColor(Color.parseColor("#1EB9EE"));
                    mTrackText.setText("????????????...");
                    mDetectText.setText("?????????????????????...");
                }
            });

            displayResult(model, "livess");
            boolean livenessSuccess = false;
            float rgbLiveThreshold = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
            livenessSuccess = (model.getRgbLivenessScore() > rgbLiveThreshold) ? true : false;
            if (livenessSuccess) {
                // ??????
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
                        mTrackText.setText("????????????");
                        mTrackText.setBackgroundColor(Color.RED);
                        mDetectText.setText("?????????????????????");
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
                mDetectText.setText("??????????????????");
                mRelativeLayout.setVisibility(View.INVISIBLE);
            }
        });

    }


    private void displayTip(final String status) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTrackText.setText("????????????...");
                mRelativeLayout.setVisibility(View.VISIBLE);
                mDetectText.setText(status);
            }
        });
    }


    /**
     * ??????????????????
     *
     * @param model ????????????
     */

    private void register(LivenessModel model) {

        if (model == null) {
            return;
        }

        /*if (username == null || groupId == null) {
            displayTip("??????????????????");
            return;
        }*/


        BDFaceImageInstance image = model.getBdFaceImageInstance();
        rgbBitmap = BitmapUtils.getInstaceBmp(image);
        // ?????????????????????????????????
        int modelType = SingleBaseConfig.getBaseConfig().getActiveModel();
        if (modelType == 1) {
            // ?????????
            FaceSDKManager.getInstance().onFeatureCheck(model.getBdFaceImageInstance(), model.getLandmarks(),
                    BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO, new FaceFeatureCallBack() {
                        @Override
                        public void onFaceFeatureCallBack(float featureSize, byte[] feature) {
                            //??????????????????????????? ????????????
                            displayCompareResult(featureSize, feature);
                            Log.e("TAG", "??????size" + String.valueOf(feature.length));
                        }

                    });

        } else if (Integer.valueOf(modelType) == 2) {
            // ?????????
            FaceSDKManager.getInstance().onFeatureCheck(model.getBdFaceImageInstance(), model.getLandmarks(),
                    BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_ID_PHOTO, new FaceFeatureCallBack() {
                        @Override
                        public void onFaceFeatureCallBack(float featureSize, byte[] feature) {
                            //??????????????????????????? ????????????
                            displayCompareResult(featureSize, feature);
                        }
                    });
        }


    }


    /**
     * ??????????????????????????? ????????????
     *
     * @param ret         ??????????????????
     * @param faceFeature ????????????
     */
    private void displayCompareResult(float ret, byte[] faceFeature) {

        // ??????????????????
        if (ret == 128) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTrackText.setText("????????????");
                }
            });
            ArrayList<Feature> featureResult = FaceSDKManager.getInstance().getFaceFeature().featureSearch(faceFeature,
                    BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO,
                    1, true);
            // TODO ??????top num = 1 ?????????????????????????????????????????????????????????????????????????????????num ???????????????
            if (featureResult != null && featureResult.size() > 0) {
                // ?????????????????????
                Feature topFeature = featureResult.get(0);
                // ???????????????????????????????????????????????????????????????????????????
                if (topFeature != null && topFeature.getScore() > SingleBaseConfig.getBaseConfig().getThreshold()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTrackText.setText("????????????!");
                            mDetectText.setText("??????????????????,??????????????????");
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
                        mTrackText.setText("????????????...");
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
                    // ??????????????????
                    boolean isSuccess = FaceApi.getInstance().registerUserIntoDBmanager(groupId, username, imageName,
                            userInfo, faceFeature);
                    if (isSuccess) {
                        // ???????????????
                        //CameraPreviewManager.getInstance().stopPreview();
                        Log.e("qing", "????????????");

                        // ??????????????????????????????300 * 300
                        File faceDir = FileUtils.getBatchImportSuccessDirectory();
                        File file = new File(faceDir, imageName);
                        ImageUtils.resize(rgbBitmap, file, 300, 300);

                        // ???????????????????????????
                        FaceApi.getInstance().initDatabases(true);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTrackText.setVisibility(View.VISIBLE);
                                mTrackText.setText("????????????");
                                mTrackText.setBackgroundColor(Color.parseColor("#1EB9EE"));
                                mDetectText.setText("????????????????????????");
                                mDetectImage.setImageBitmap(rgbBitmap);
                                //??????????????????
                                username = null;
                                groupId = null;
                                // ????????? finish
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
                                mTrackText.setText("????????????");
                                mTrackText.setBackgroundColor(Color.RED);
                                mDetectText.setText("??????????????????");
                                faceFlag = false;
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTrackText.setText("????????????");
                            mDetectText.setText("????????????,??????????????????");
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
                    /*mTrackText.setText("????????????");
                    mDetectText.setText("????????????,??????????????????");
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
            displayTip("??????????????????");
        } else {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTrackText.setText("??????????????????");
                    mTrackText.setBackgroundColor(Color.RED);
                    mDetectText.setText("??????????????????");
                    faceFlag = false;
                }
            });
        }
    }

    // ??????????????????
    public boolean faceSizeFilter(FaceInfo faceInfo, int bitMapWidth, int bitMapHeight) {

        // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        // ???????????????????????????????????????????????????????????????????????????????????????????????????
        float ratio = (float) faceInfo.width / (float) bitMapHeight;
        if (ratio > 0.6) {

            displayTip("???????????????????????????????????????????????????");
            return false;
        } else if (ratio < 0.2) {
            displayTip("???????????????????????????????????????????????????");
            return false;
        } else if (faceInfo.centerX > bitMapWidth * 3 / 4) {
            displayTip("???????????????????????????");
            return false;
        } else if (faceInfo.centerX < bitMapWidth / 4) {
            displayTip("???????????????????????????");
            return false;
        } else if (faceInfo.centerY > bitMapHeight * 3 / 4) {
            displayTip("???????????????????????????");
            return false;
        } else if (faceInfo.centerY < bitMapHeight / 4) {
            displayTip("???????????????????????????");
            return false;
        }

        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        CameraPreviewManager.getInstance().stopPreview();
        // ??????????????????
        SingleBaseConfig.getBaseConfig().setQualityControl(true);
        FaceSDKManager.getInstance().initConfig();
    }
}
