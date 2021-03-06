package com.baidu.idl.face.main.activity.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.activity.SDCardFileExplorerTestActivity;
import com.baidu.idl.facesdkdemo.R;

/**
 * description :功能设置界面
 */
public class SettingMainActivity extends BaseActivity implements View.OnClickListener {
    // 如果pageType 为"search","register",页面结束时候返回FaceConfigActivity
    private String pageType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settingmain);

        Intent intent = getIntent();
        if (intent != null) {
            pageType = intent.getStringExtra("pagetype");
        }
        init();
    }

    public void init() {
        TextView smFaceDetectAngle = findViewById(R.id.sm_facedetectangle);
        TextView smCameraDisplayAngle = findViewById(R.id.sm_cameradisplayangle);
        TextView smMinFace = findViewById(R.id.sm_minface);
        TextView smRecognizeModleThreshold = findViewById(R.id.sm_recognizemodlethreshold);
        TextView smDetectTrailStrategy = findViewById(R.id.sm_detecttrailstrategy);
        TextView smFaceLivinessType = findViewById(R.id.sm_facelivinesstype);
        TextView smFaceLivenessThreshold = findViewById(R.id.sm_facelivenessthreshold);
        TextView smDebugMode = findViewById(R.id.sm_debugmode);
        TextView smRecognizeModleType = findViewById(R.id.sm_recognizemodletype);
        TextView smDetectFollowStarategy = findViewById(R.id.sm_detectfollowstarategy);
        TextView smQualityControl = findViewById(R.id.sm_qualitycontrol);
        TextView smMirror = findViewById(R.id.sm_mirror);
        TextView smWiegand = findViewById(R.id.sm_wiegand_test);
        TextView smIp = findViewById(R.id.sm_ip);

        TextView tvFaceDetectAngle = findViewById(R.id.tv_facedetectangle);
        TextView tvCameraDisplayAngle = findViewById(R.id.tv_cameradisplayangle);
        TextView tvMinface = findViewById(R.id.tv_minface);
        TextView tvRecognizeModleThreshold = findViewById(R.id.tv_recognizemodlethreshold);
        TextView tvDetectTrailStrategy = findViewById(R.id.tv_detecttrailstrategy);
        TextView tvFaceLivinessType = findViewById(R.id.tv_facelivinesstype);
        TextView tvFaceLivenessThreshold = findViewById(R.id.tv_facelivenessthreshold);
        TextView tvDebugMode = findViewById(R.id.tv_debugmode);
        TextView tvRecognizeModleType = findViewById(R.id.tv_recognizemodletype);
        TextView tvDetectFollowStarategy = findViewById(R.id.tv_detectfollowstarategy);
        TextView tvQualityControl = findViewById(R.id.tv_qualitycontrol);
        TextView tvMirror = findViewById(R.id.tv_mirror);
        TextView tvWiegand = findViewById(R.id.tv_wiegand_test);
        TextView tvIp = findViewById(R.id.tv_ip);

        ImageButton smBack = findViewById(R.id.sm_back);

        LinearLayout photo_detection = findViewById(R.id.photo_detection);

        smFaceDetectAngle.setOnClickListener(this);
        smCameraDisplayAngle.setOnClickListener(this);
        smMinFace.setOnClickListener(this);
        smRecognizeModleThreshold.setOnClickListener(this);
        smDetectTrailStrategy.setOnClickListener(this);
        smFaceLivinessType.setOnClickListener(this);
        smFaceLivenessThreshold.setOnClickListener(this);
        smDebugMode.setOnClickListener(this);
        smRecognizeModleType.setOnClickListener(this);
        smDetectFollowStarategy.setOnClickListener(this);
        smQualityControl.setOnClickListener(this);
        smMirror.setOnClickListener(this);
        smWiegand.setOnClickListener(this);
        smIp.setOnClickListener(this);

        tvFaceDetectAngle.setOnClickListener(this);
        tvCameraDisplayAngle.setOnClickListener(this);
        tvMinface.setOnClickListener(this);
        tvRecognizeModleThreshold.setOnClickListener(this);
        tvDetectTrailStrategy.setOnClickListener(this);
        tvFaceLivinessType.setOnClickListener(this);
        tvFaceLivenessThreshold.setOnClickListener(this);
        tvDebugMode.setOnClickListener(this);
        tvRecognizeModleType.setOnClickListener(this);
        tvDetectFollowStarategy.setOnClickListener(this);
        tvQualityControl.setOnClickListener(this);
        tvMirror.setOnClickListener(this);
        tvWiegand.setOnClickListener(this);
        tvIp.setOnClickListener(this);

        smBack.setOnClickListener(this);

        photo_detection.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sm_facedetectangle:
                // 人脸检测角度设置
                goActivity(FaceDetectAngleActivity.class);
                break;
            case R.id.sm_cameradisplayangle:
                // 摄像头视频流回显角度
                goActivity(CameraDisplayAngleActivity.class);
                break;
            case R.id.sm_minface:
                // 最小人脸界面
                goActivity(MinFaceActivity.class);
                break;
            case R.id.sm_recognizemodlethreshold:
                // 识别模型阀值
                goActivity(RecognizeModleThresholdActivity.class);
                break;
            case R.id.sm_facelivinesstype:
                // 活体检测模式
                goActivity(FaceLivinessType.class);
                break;
            case R.id.sm_facelivenessthreshold:
                // 活体检测阀值
                goActivity(FaceLivenessThresholdActivity.class);
                break;
            case R.id.sm_detecttrailstrategy:
                // 检测追踪策略
                goActivity(DetectTrailStrategyActivity.class);
                break;
            case R.id.sm_debugmode:
                // 调试模式配置
                goActivity(DebugModeActivity.class);
                break;
            case R.id.sm_recognizemodletype:
                // 识别模型选择
                goActivity(RecognizeModleTypeAcctivity.class);
                break;
            case R.id.sm_detectfollowstarategy:
                // 检测跟踪策略
                goActivity(DetectFllowStrategyActivity.class);
                break;
            case R.id.sm_qualitycontrol:
                goActivity(QualityControlActivity.class);
                break;
            case R.id.sm_mirror:
                // 进入镜像调节页面
                goActivity(MirrorSettingActivity.class);
                break;

            case R.id.sm_wiegand_test:
                //韦根测试
                goActivity(WiegandActivity.class);
                break;

            case R.id.sm_ip:
                //服务器地址修改
                goActivity(IpAddressActivity.class);
                break;

            case R.id.tv_facedetectangle:
                // 人脸检测角度设置
                goActivity(FaceDetectAngleActivity.class);
                break;
            case R.id.tv_cameradisplayangle:
                // 摄像头视频流回显角度
                goActivity(CameraDisplayAngleActivity.class);
                break;
            case R.id.tv_minface:
                // 最小人脸界面
                goActivity(MinFaceActivity.class);
                break;
            case R.id.tv_recognizemodlethreshold:
                // 识别模型阀值
                goActivity(RecognizeModleThresholdActivity.class);
                break;
            case R.id.tv_facelivinesstype:
                // 活体检测模式
                goActivity(FaceLivinessType.class);
                break;
            case R.id.tv_facelivenessthreshold:
                // 活体检测阀值
                goActivity(FaceLivenessThresholdActivity.class);
                break;
            case R.id.tv_detecttrailstrategy:
                // 检测追踪策略
                goActivity(DetectTrailStrategyActivity.class);
                break;
            case R.id.tv_debugmode:
                // 调试模式配置
                goActivity(DebugModeActivity.class);
                break;
            case R.id.tv_recognizemodletype:
                // 识别模型选择
                goActivity(RecognizeModleTypeAcctivity.class);
                break;
            case R.id.tv_detectfollowstarategy:
                // 检测跟踪策略
                goActivity(DetectFllowStrategyActivity.class);
                break;
            case R.id.tv_qualitycontrol:
                goActivity(QualityControlActivity.class);
                break;
            case R.id.tv_mirror:
                // 进入镜像调节页面
                goActivity(MirrorSettingActivity.class);
                break;
            case R.id.tv_wiegand_test:
                //韦根测试
                goActivity(WiegandActivity.class);
                break;

            case R.id.photo_detection:
                goActivity(SDCardFileExplorerTestActivity.class);
                break;
            case R.id.tv_ip:
                //服务器地址修改
                goActivity(IpAddressActivity.class);
                break;

            case R.id.sm_back:
                finish();
                break;
            default:
                break;
        }
    }


    public void goActivity(Class<?> mClass) {
        Intent intent = new Intent(this, mClass);
        startActivity(intent);
    }

}
