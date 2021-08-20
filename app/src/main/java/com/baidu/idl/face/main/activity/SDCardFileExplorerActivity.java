package com.baidu.idl.face.main.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.idl.face.main.api.FaceApi;
import com.baidu.idl.face.main.listener.OnItemClickListener;
import com.baidu.idl.face.main.utils.FileUtils;
import com.baidu.idl.face.main.utils.LiveDataBus;
import com.baidu.idl.face.main.utils.ToastUtils;
import com.baidu.idl.face.main.utils.Utils;
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;

/**
 * SD卡文件资源管理器
 */
public class SDCardFileExplorerActivity extends BaseActivity {

    /**
     * 照片信息头
     */
    private byte[] imageHead = new byte[]{(byte) 0xFF, (byte) 0xD8};
    /**
     * 照片信息尾
     */
    private byte[] imageEnd = new byte[]{(byte) 0xFF, (byte) 0xD9};

    private final byte[] mRegisterAdress23 = new byte[]{(byte) 0x00, (byte) 0x10, (byte) 0x00, (byte) 0x23};

    private final byte[] mRuleHead = new byte[]{(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xAA, (byte) 0x55};

    private Bitmap bitmap = null;

    private boolean isSuss;
    private boolean flag = true;


    Handler mHandler = new Handler();


    public static final String TAG = SDCardFileExplorerActivity.class.getSimpleName();

    private Context mContext;

    private Button bt_return;
    private RecyclerView recyclerView;
    private SDCardFileExplorerAdapter adapter;
    private TextView logView, logFail, tv_mSuccess, tv_mFail, tv_mTotal;
    private int mSuccess = 1, mFail = 1, mTotal;

    // 记录当前的父文件夹
    File currentParent;
    // 记录当前路径下的所有文件夹的文件数组
    File[] currentFiles;

    private ProgressDialog progressDialog;

    /**
     * 字节数组输出流
     */
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sd_card_file_explorer);
        mContext = this;

        bt_return = findViewById(R.id.bt_return);
        bt_return.setOnClickListener(v -> finish());

        // 获取系统的SDCard的目录
        String[] allSdPaths = FileUtils.getAllSdPaths(this);

        if (allSdPaths.length >= 2) {
            File root = new File(allSdPaths[1]);
            currentFiles = root.listFiles();
            initView();
        } else {
            ToastUtils.toast(this, "请插入USB外部储存卡");
        }


        liveDataBus();
    }

    private void liveDataBus() {
        LiveDataBus.get().with("bulkUpload", Boolean.class).observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                isSuss = aBoolean;
            }
        });

    }

    private void initView() {
        tv_mSuccess = findViewById(R.id.mSuccess);
        tv_mFail = findViewById(R.id.mFail);
        tv_mTotal = findViewById(R.id.mTotal);

        logFail = findViewById(R.id.logFail);
        logView = (TextView) findViewById(R.id.logView);
        //配置TextView的滚动方式
        logView.setMovementMethod(ScrollingMovementMethod.getInstance());
        logFail.setMovementMethod(ScrollingMovementMethod.getInstance());

        recyclerView = findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SDCardFileExplorerAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                showAlertDialog(position);
            }
        });
    }

    /**
     * @param msg 展示log信息
     */
    void refreshLogView(String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logView.append(Html.fromHtml(msg));
                int offset = logView.getLineCount() * logView.getLineHeight();
                if (offset > logView.getHeight()) {
                    //更新文字时，使用View的scrollTo(int x,int y)方法使其自动滚动到最后一行
                    logView.scrollTo(0, offset - logView.getHeight());
                }
            }
        });
    }

    /**
     * @param msg 单独展示不合格的名字
     */
    void refreshLogFail(String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logFail.append(Html.fromHtml(msg));
                int offset = logFail.getLineCount() * logFail.getLineHeight();
                if (offset > logFail.getHeight()) {
                    //更新文字时，使用View的scrollTo(int x,int y)方法使其自动滚动到最后一行
                    logFail.scrollTo(0, offset - logFail.getHeight());
                }
            }
        });
    }


    /**
     * 显示dialog
     *
     * @param position
     */
    private void showAlertDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("导入" + currentFiles[position].getName() + "目录下照片");
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                if (progressDialog == null) {
                    progressDialog = new ProgressDialog(mContext);
                }
                progressDialog.setTitle("导入人脸库");
                progressDialog.setMessage("正在导入中...");
                progressDialog.setMax(FileUtils.getImagePathFromSDLength(mContext, currentFiles[position].getName()));
                mTotal = FileUtils.getImagePathFromSDLength(mContext, currentFiles[position].getName());
                tv_mTotal.setText("总数: " + mTotal);
                progressDialog.setCancelable(false);
                progressDialog.setIndeterminate(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.show();


                if (FileUtils.isFileExist(mContext, currentFiles[position].getName())) {

//                    DBManager.getInstance().deleteGroup("default");

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int i = 1;
                            int j = 1;

                            for (HashMap.Entry<String, String> map : FileUtils.getImagePathFromSD(mContext, currentFiles[position].getName()).entrySet()) {

                                //这里获取的为工号
                                String name = map.getKey().substring(0, map.getKey().length() - 4);
                                String imagePath = map.getValue();

                                // 设置参数
                                try {
                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inJustDecodeBounds = true; // 只获取图片的大小信息，而不是将整张图片载入在内存中，避免内存溢出
                                    BitmapFactory.decodeFile(imagePath, options);
                                    int height = options.outHeight;
                                    int width = options.outWidth;

//                                    float hh = 640f;// 这里设置高度为640f
//                                    float ww = 480f;// 这里设置宽度为480f
                                    int inSampleSize = 2; // 默认像素压缩比例，压缩为原图的1/2  1表示不缩放
//                                    // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
//                                    if (width > height && width > ww) {
//                                        inSampleSize = (int) (options.outWidth / ww);
//                                    } else if (width < height && height > hh) {
//                                        inSampleSize = (int) (options.outHeight / hh);
//                                    }
//                                    if (inSampleSize <= 0) {
//                                        inSampleSize = 1;
//                                    }


                                    int minLen = Math.min(height, width); // 原图的最小边长
                                    if (minLen > 200) { // 如果原始图像的最小边长大于100dp（此处单位我认为是dp，而非px）
                                        float ratio = (float) minLen / 200.0f; // 计算像素压缩比例
                                        inSampleSize = (int) ratio;
                                    }
                                    options.inJustDecodeBounds = false; // 计算好压缩比例后，这次可以去加载原图了
                                    options.inSampleSize = inSampleSize; // 设置为刚才计算的压缩比例
//                                    options.inScaled = false;
                                    bitmap = BitmapFactory.decodeFile(imagePath, options); // 解码文件
//                                    Log.w("TAG", "卡号：" + name + " size: " + bitmap.getByteCount() + " width: " + bitmap.getWidth() + " heigth:" + bitmap.getHeight()); // 输出图像数据
                                } catch (OutOfMemoryError e) {
                                    ToastUtils.toast(SDCardFileExplorerActivity.this, "" + e.toString());
                                }


//                                Bitmap bitmap = BitmapFactory.decodeFile(map.getValue());

                                //Bitmap转换成byte[]
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//                                Bitmap compressedBm = BitmapFactory.decodeByteArray(byteArrayOutputStream.toByteArray(), 0, byteArrayOutputStream.toByteArray().length);
                                byte[] imageData = Utils.addBytes(imageHead, byteArrayOutputStream.toByteArray(), imageEnd);
//                                Log.w("TAG", "----卡号：" + name + " size: " + (compressedBm.getByteCount()>>10) + " width: " + compressedBm.getWidth() + " heigth:" + compressedBm.getHeight()); // 输出图像数据

                                byteArrayOutputStream.reset();

                                //特征值获取
                                byte[] bytes = new byte[512];
                                float ret = FaceApi.getInstance().getFeature(bitmap, bytes, BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO);
//
//                                FaceApi.getInstance().registerUserIntoDBmanager("default", name, "image" + map.getKey(), name, bytes);
//
//                                FaceApi.getInstance().initDatabases(true);

                                //发送数据
                                if (bitmap != null) {
                                    if (ret == -1) {
                                        Log.e("TAG", "initSuccess: " + name + "__" + "未检测到人脸");
//                                        refreshLogView(name + ": " + "未检测到人脸,图片不合格");
                                        refreshLogView("<font color='#ff0000'> " + name + "： </font>" + "<font color='#ff0000'> 未检测到人脸,图片不合格 </font> <br/>");
                                        refreshLogFail("<font color='#ff0000'> 工号 ： </font>" + "<font color='#ff0000'> " + name + " </font> <br/>");
                                        tv_mFail.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                tv_mFail.setText(Html.fromHtml("<font color='#ff0000'> 不合格： </font>" + "<font color='#ff0000'> " + mFail++ + " </font>"));
                                            }
                                        });
                                    } else if (ret == 128) {
                                        j++;
                                        Log.e("TAG", "initSuccess: " + name + "__" + "图片合格 " + Utils.addZero(name));
//                                        refreshLogView(name + ": " + "图片合格");
                                        refreshLogView("<font color='#16CC77'> " + name + "： </font>" + "<font color='#16CC77'> 图片合格 </font> <br/>");


                                        //发送后台数据
                                        try {
                                            //拼接特征、工号、照片字节
                                            byte[] bulkData = Utils.concat(
                                                    //拼接 特征值、工号字节
                                                    Utils.concat(bytes, Utils.hexString2Bytes(Utils.addZero(name))),
                                                    //照片信息字节
                                                    imageData
                                            );
                                            byte[] send = Utils.addBytes(mRuleHead, Utils.concat(mRegisterAdress23, Utils.hexString2Bytes(Utils.addZero1(j++ + ""))), bulkData);
//                                            byte[] bytes1 = Utils.hexString2Bytes(Utils.addZero1(j++ + ""));
//                                            String s = Utils.byteToHex(bytes1);
//                                            Log.e(TAG, "run: " + s);
//                                            LiveDataBus.get().with("NUM", Integer.class).postValue(Integer.valueOf(s));

//                                            if (flag) {
                                                LiveDataBus.get().with("bulkData").postValue(send);
//                                            }

//                                            mHandler.postDelayed(() -> {
//                                                if (isSuss) {
//                                                    Log.e(TAG, "run: " + isSuss);
//                                                    flag = true;
//                                                } else {
//                                                    Log.e(TAG, "run: " + isSuss);
////                                                    flag = false;
////                                                LiveDataBus.get().with("bulkData").postValue(send);
//
//                                                }
//                                            }, 1000);

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        tv_mSuccess.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                tv_mSuccess.setText(Html.fromHtml("<font color='#16CC77'> 合格： </font>" + "<font color='#16CC77'> " + mSuccess++ + " </font>"));
                                            }
                                        });
                                    }

                                } else {
                                    Log.e("TAG", "initSuccess: " + name + "__" + "该图片转成Bitmap失败");
                                }
//                                Log.e("TAG", "initSuccess: " + name + "__" + ret + "——" + Arrays.toString(bytes));
                                progressDialog.setProgress(i++);

                                try {
                                    Thread.sleep(300);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            progressDialog.dismiss();
                        }
                    }).start();

                }
            }
        });

        builder.show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 先判断是否已经回收
        if (bitmap != null && !bitmap.isRecycled()) {
            // 回收并且置为null
            bitmap.recycle();
            bitmap = null;
        }
        System.gc();
    }

    private static class SDCardFileExplorerHolder extends RecyclerView.ViewHolder {
        private View itemView;

        private ImageView imgFile;
        private TextView tvSDCardFileName;

        public SDCardFileExplorerHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;

            imgFile = itemView.findViewById(R.id.imgFile);
            tvSDCardFileName = itemView.findViewById(R.id.tvSDCardFileName);
        }
    }

    public class SDCardFileExplorerAdapter extends RecyclerView.Adapter<SDCardFileExplorerActivity.SDCardFileExplorerHolder> implements View.OnClickListener {

        private OnItemClickListener mItemClickListener;

        private void setItemClickListener(OnItemClickListener itemClickListener) {
            mItemClickListener = itemClickListener;
        }

        @NonNull
        @Override
        public SDCardFileExplorerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sd_card_file_list, parent, false);
            SDCardFileExplorerActivity.SDCardFileExplorerHolder viewHolder = new SDCardFileExplorerActivity.SDCardFileExplorerHolder(view);
            view.setOnClickListener(this);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull SDCardFileExplorerHolder holder, int position) {
            holder.itemView.setTag(position);

            if (currentFiles[position].isDirectory()) {
                holder.imgFile.setImageResource(R.mipmap.icon_file);
            }
            holder.tvSDCardFileName.setText(currentFiles[position].getName());
        }

        @Override
        public int getItemCount() {
            return currentFiles != null ? currentFiles.length : 0;
        }

        @Override
        public void onClick(View view) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(view, (Integer) view.getTag());
            }
        }
    }
}
