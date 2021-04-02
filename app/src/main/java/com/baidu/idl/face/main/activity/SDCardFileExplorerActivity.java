package com.baidu.idl.face.main.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.idl.face.main.api.FaceApi;
import com.baidu.idl.face.main.db.DBManager;
import com.baidu.idl.face.main.listener.OnItemClickListener;
import com.baidu.idl.face.main.utils.FileUtils;
import com.baidu.idl.face.main.utils.ToastUtils;
import com.baidu.idl.facesdkdemo.R;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;

import java.io.File;
import java.util.HashMap;

/**
 * SD卡文件资源管理器
 */
public class SDCardFileExplorerActivity extends BaseActivity {

    public static final String TAG = SDCardFileExplorerActivity.class.getSimpleName();

    private Context mContext;

    private Button bt_return;
    private RecyclerView recyclerView;
    private SDCardFileExplorerAdapter adapter;

    // 记录当前的父文件夹
    File currentParent;
    // 记录当前路径下的所有文件夹的文件数组
    File[] currentFiles;

    private ProgressDialog progressDialog;


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
    }

    private void initView() {
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
                progressDialog.setCancelable(false);
                progressDialog.setIndeterminate(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.show();


                if (FileUtils.isFileExist(mContext, currentFiles[position].getName())) {

                    DBManager.getInstance().deleteGroup("default");

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int i = 1;

                            for (HashMap.Entry<String, String> map : FileUtils.getImagePathFromSD(mContext, currentFiles[position].getName()).entrySet()) {

                                String name = map.getKey().substring(0, map.getKey().length() - 4);

                                Bitmap bitmap = BitmapFactory.decodeFile(map.getValue());

                                byte[] bytes = new byte[512];

                                FaceApi.getInstance().getFeature(bitmap, bytes, BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO);

                                FaceApi.getInstance().registerUserIntoDBmanager("default", name, "image" + map.getKey(), name, bytes);

                                FaceApi.getInstance().initDatabases(true);

                                //Log.e("TAG", "initModelSuccess: " + Arrays.toString(bytes));
                                progressDialog.setProgress(i++);
                            }
                            progressDialog.dismiss();
                        }
                    }).start();

                }
            }
        });

        builder.show();
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
