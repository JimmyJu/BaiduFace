package com.baidu.idl.face.main.service;

import android.app.Service;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.baidu.idl.face.main.api.FaceApi;
import com.baidu.idl.face.main.callback.RemoveStaffCallback;
import com.baidu.idl.face.main.constant.BaseConstant;
import com.baidu.idl.face.main.db.DBManager;
import com.baidu.idl.face.main.manager.UserInfoManager;
import com.baidu.idl.face.main.model.ProgressList;
import com.baidu.idl.face.main.utils.LiveDataBus;
import com.baidu.idl.face.main.utils.NetWorkUtils;
import com.baidu.idl.face.main.utils.SPUtils;
import com.baidu.idl.face.main.utils.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import utils.SerialPortUtils;

public class TcpService extends Service {
    public static final String TAG = TcpService.class.getSimpleName();

    private final Handler mHandler = new Handler();
    /**
     * Socket
     */
    private Socket mSocket;
    /**
     * 输入流
     */
    private BufferedInputStream mBis;
    /**
     * 输出流
     */
    private BufferedOutputStream mBos;
    /**
     * 创建读取服务器心跳、数据线程
     */
    private ReadThread mReadThread;
    /**
     * 创建发送心跳线程
     */
    private HeardThread mHeardThread;
    /**
     * 线程池执行服务
     */
    private ExecutorService mExecutorService;
    /**
     * Socket重连次数
     */
    private int SocketReconnectNumber = 1;
    /**
     * 数据发送失败次数
     */
    private int numberOfDataTransmissionFailures = 1;
    /**
     * 我收到来自服务器的消息计数器
     */
    private int dataNum = 0;

    private final byte[] mRuleHead = new byte[]{(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xAA, (byte) 0x55};
    private final byte[] mHeartAdress = new byte[]{(byte) 0x00, (byte) 0x10, (byte) 0x00, (byte) 0x1d, (byte) 0x00, (byte) 0x01};
    private final byte[] mRegisterAdress = new byte[]{(byte) 0x00, (byte) 0x10, (byte) 0x00, (byte) 0x21};
    private final byte[] mRegisterAdress22 = new byte[]{(byte) 0x00, (byte) 0x10, (byte) 0x00, (byte) 0x22};
    private final byte[] mCheckCode = new byte[]{(byte) 0x00, (byte) 0x00};
    private final byte[] mHeartCategory = new byte[]{(byte) 0x00, (byte) 0x1D};
    private final byte[] mRegisterCategory = new byte[]{(byte) 0x00, (byte) 0x21};
    private final byte[] mFaceLibCategory = new byte[]{(byte) 0x00, (byte) 0x1B};
    private final byte[] mRequestFaceLib = new byte[]{(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xAA, (byte) 0x55, (byte) 0x00, (byte) 0x10, (byte) 0x00, (byte) 0x1B, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00};
    /**
     * 队列大小
     */
    private final int mQueueSize = 1000;
    /**
     * 队列
     */
    private final LinkedBlockingQueue<byte[]> mQueue = new LinkedBlockingQueue<>((mQueueSize));
    /**
     * 进度条
     */
    private final ProgressList progressList = new ProgressList();
    private final Hashtable<String, ProgressList> progress = new Hashtable<>();

    /**
     * 串口相关
     */
    private final SerialPortUtils serialPortUtils = new SerialPortUtils();
    /**
     * 串口心跳线程标志
     */
    private boolean serialHeartbeatThreadFlag = true;

    private boolean switchPort;
    /**
     * 首页发送次数
     */
    private int mSendNum = 0;
    /**
     * 总人员信息
     */
    private int inTotalInfo;
    /**
     * 只获取第一次接收后台数据标识
     */
    private boolean inTotalInfoBool = true;
    /**
     * 数据库添加成功标识
     */
    private boolean isSuccess;
    /**
     * 添加数据库成功人数
     */
    private int success = 0;
    /**
     * 是否弹出进度条标识
     */
    private boolean progressFlag = false;
    /**
     * 请求标识
     */
    private boolean isRequest = true;
    /**
     * 新增人员
     * 寄存器起始地址
     */
    private final byte[] mNewStaffCategory = new byte[]{(byte) 0x00, (byte) 0x15};
    /**
     * 删除人员
     * 寄存器起始地址
     */
    private final byte[] mRemoveStaffCategory = new byte[]{(byte) 0x00, (byte) 0x35};
    private final byte[] registrationReply = new byte[]{(byte) 0x4F / 0x45, (byte) 0x4B / 0x52};
    /**
     * 设备编号
     * 寄存器起始地址
     */
    private final byte[] deviceID = new byte[]{(byte) 0x00, (byte) 0x19};
    /**
     * (byte) 0x00, (byte) 0x10
     */
    private final byte[] m_00x00_00x10 = new byte[]{(byte) 0x00, (byte) 0x10};
    /**
     * 存储分包byte
     */
    byte[] newByte = new byte[5274];
    /**
     * 单个线程池
     */
    ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

    private String ip = "192.168.1.201";
    private String port = "8099";


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ClientBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //开始连接
        startConnect();
        //打开串口
        serialPortUtils.openSerialPort();
        //开启串口心跳线程
        serialHeartbeatThread.start();

        LiveDataBus.get().with("SerialData", byte[].class).observeForever(observer);

        LiveDataBus.get().with("registerData", byte[].class).observeForever(faceObserver);
        //获取平板关机消息
        LiveDataBus.get().with(BaseConstant.slabShutdownMessage, Integer.class).observeForever(getSlabShutdownMessageObserver);

        LiveDataBus.get().with("switchPort", Boolean.class).observeForever(switchPortObserver);
    }

    public class ClientBinder extends Binder {
        public TcpService getService() {
            return TcpService.this;
        }
    }

    /**
     * 开始连接
     */
    public void startConnect() {
        //在子线程进行网络操作
        // Service也是运行在主线程，千万不要以为Service意思跟后台运行很像，就以为Service运行在后台子线程
        if (mExecutorService == null) {
            mExecutorService = Executors.newCachedThreadPool();
        }
        mExecutorService.execute(connectRunnable);
    }

    private final Runnable connectRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                String ed_ip = (String) SPUtils.get(getApplicationContext(), "IP", "");
                String ed_port = (String) SPUtils.get(getApplicationContext(), "PORT", "");
                if (!ed_ip.isEmpty() && !ed_port.isEmpty()) {
                    ip = ed_ip;
                    port = ed_port;
                }
                SPUtils.put(getApplicationContext(),"IP",ip);
                SPUtils.put(getApplicationContext(),"PORT",port);
                Log.e(TAG, "run--------: " + ip + ":" + port);
                // 建立Socket连接
                if (mSocket == null) {
                    mSocket = new Socket();
                    mSocket.connect(new InetSocketAddress(ip, Integer.parseInt(port)), 10);
                    mBis = new BufferedInputStream(mSocket.getInputStream());
                    mBos = new BufferedOutputStream(mSocket.getOutputStream());
                    //发送设备ID
                    sendDeviceID();

                    //创建发送心跳线程
                    mHeardThread = new HeardThread();
                    mHeardThread.start();

                    mHandler.post(mSendRunnable);

                    //创建读取服务器心跳、数据线程
                    mReadThread = new ReadThread();
                    mReadThread.start();

                    //发送“在线”状态
                    LiveDataBus.get().with("heart").postValue(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                //释放资源
                releaseSocket(true);
            }
        }
    };

    /**
     * 发送设备ID
     */
    private void sendDeviceID() {
        String substringDeviceID = NetWorkUtils.getLocalIpAddress();

        if (substringDeviceID != null) {
            String newString = substringDeviceID.replace(".", "");

            String newSubstringDeviceID = newString.substring(newString.length() - 6);

            byte[] byte1 = Utils.merge2BytesTo1Byte("00");
            byte[] byte2 = Utils.merge2BytesTo1Byte(newSubstringDeviceID.substring(0, 2));
            byte[] byte3 = Utils.merge2BytesTo1Byte(newSubstringDeviceID.substring(2, 4));
            byte[] byte4 = Utils.merge2BytesTo1Byte(newSubstringDeviceID.substring(4, 6));

            byte[] deviceIDByte = Utils.addBytes(
                    Utils.concat(mRuleHead, m_00x00_00x10),
                    Utils.concat(deviceID, m_00x00_00x10),
                    Utils.concat(Utils.concat(Utils.concat(byte1, byte2), Utils.concat(byte3, byte4)), mCheckCode)
            );
            if (mQueue.size() == mQueueSize) {
                mQueue.poll();
            }
            mQueue.offer(deviceIDByte);
        }
    }


    public class ReadThread extends Thread {
        @Override
        public void run() {
//            int count = mBis.available();
            /*
              size = 一个包10条人员信息，共计5274字节 - 14字节 = 5260字节
              包头 6 byte
              设备地址 1 byte
              功能码 1 byte
              寄存器起始地址 1 byte
              寄存器数量 2 byte
              人员信息 5260 byte
              校验码 2 byte
              总字节长度
             */
            int size;
            try {
                byte[] buffers = new byte[1024 * 1024 * 10];
                while ((size = mBis.read(buffers)) != -1) {
                    if (isRequest) {
                        DBManager.getInstance().deleteGroup("default");
                        mQueue.offer(mRequestFaceLib);
                        Log.e("------------", "请求");
                    }
                    isRequest = false;

                    Log.e(TAG, "size: " + size);

                    //获取后台返回的字节
                    byte[] buffer = new byte[size];
                    System.arraycopy(buffers, 0, buffer, 0, size);

                    if (size == 14) { // size == 14说明收到心跳包
                        byte[] category = new byte[2];
                        System.arraycopy(buffer, 8, category, 0, 2);
                        //网络心跳包 寄存器起始地址
                        if (Arrays.equals(category, mHeartCategory)) {
                            byte[] heart = new byte[14];
                            System.arraycopy(buffer, 0, heart, 0, 14);
                            String data = Utils.byte2hex(heart);
                            dataNum++;
                            //收到心跳消息以后，首先移除断连消息，然后创建一个新的25秒后执行断连的消息。
                            //这样每次收到心跳后都会重新创建一个25秒的延时消息，在25秒后还没收到心跳消息，表明服务器已死，就会执行断开Socket连接
                            //在25秒钟内如果收到过一次心跳消息，就表明服务器还活着，可以继续与之通讯。
                            mHandler.removeCallbacks(disConnectRunnable);
                            mHandler.postDelayed(disConnectRunnable, 25000);
                            Log.d(TAG, "我收到来自服务器的消息: " + data + "--计数" + dataNum);
                        }
                    } else if (size == 18) { // size == 18说明是收到删除人员信息
                        registerStartAddress(buffer, size);
                    } else if (size > 14) {
                        //获取后台返回最后两位字节
                        byte[] checkByte = new byte[2];
                        System.arraycopy(buffer, buffer.length - 2, checkByte, 0, 2);
                        //检验是不是完整的包
                        if (!Utils.byteToHex(checkByte).equals("0000")) {
                            newByte = buffer;
                        } else {
                            if ((size - 14) % 526 == 0) { //没有余数说明是完整的包
                                registerStartAddress(buffer, size);
                            } else {
                                //合并两个byte数组
                                byte[] concatByte = Utils.concat(newByte, buffer);
                                registerStartAddress(concatByte, concatByte.length);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                //释放资源
                releaseSocket(true);
            }
        }
    }

    /**
     * 判断对应的寄存器起始地址
     *
     * @param buffer
     * @param size
     */
    private void registerStartAddress(byte[] buffer, int size) {

        byte[] category = new byte[2];
        System.arraycopy(buffer, 8, category, 0, 2);

        //脸探头设备人员注册 寄存器起始地址
        if (Arrays.equals(category, mRegisterCategory)) {
            LiveDataBus.get().with("registerFlag").postValue(true);
        }
        //获取特征库 寄存器起始地址
        else if (Arrays.equals(category, mFaceLibCategory)) {
            byte[] faceNumByte = new byte[2];
            System.arraycopy(buffer, 10, faceNumByte, 0, 2);
            //寄存器数量
            int faceNum = Utils.bytesToInt(faceNumByte);
            if (inTotalInfoBool) {
                //总人员信息
                inTotalInfo = faceNum + (size - 14) / 526;
                inTotalInfoBool = false;
            }
            //获取后台人脸特征库
            onRegister(buffer, size);
        }
        //新增人员 寄存器起始地址
        else if (Arrays.equals(category, mNewStaffCategory)) {
            onRegister(buffer, size);
            FaceApi.getInstance().initDatabases(true);
        }
        //删除人员 寄存器起始地址
        else if (Arrays.equals(category, mRemoveStaffCategory)) {
            removeStaff(buffer);
        }
    }


    private void onRegister(byte[] data, int size) {


//        //校验码
//        byte[] checkCodeByte = new byte[2];
//        System.arraycopy(data, data.length - 2, checkCodeByte, 0, 2);
//
//        int checkCodeNum = Utils.bytesToInt(checkCodeByte);
//        Log.e(TAG, "校验码: " + checkCodeNum);

        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                //人员信息字节长度
                int personnelInfoSize = size - 14;

                for (int i = 1; i <= personnelInfoSize / 526; i++) {

                    byte[] personnelInfoByte = new byte[personnelInfoSize];
                    System.arraycopy(data, 12, personnelInfoByte, 0, personnelInfoSize);

                    int j = personnelInfoSize / (personnelInfoSize / 526);

                    byte[] featureByte = new byte[512];

                    byte[] cardByte = new byte[4];

                    byte[] nameByte = new byte[10];

                    if (i == 1) {
                        //人脸信息
                        System.arraycopy(personnelInfoByte, 0, featureByte, 0, 512);
                        //卡号
                        System.arraycopy(personnelInfoByte, j * i - 14, cardByte, 0, 4);
                        //人名
                        System.arraycopy(personnelInfoByte, j * i - 10, nameByte, 0, 10);
                    } else {
                        //人脸信息
                        if (j * (i - 1) <= personnelInfoSize) {
                            System.arraycopy(personnelInfoByte, j * (i - 1), featureByte, 0, 512);
                        }
                        //卡号
                        System.arraycopy(personnelInfoByte, j * (i - 1) + 512, cardByte, 0, 4);
                        //人名
                        System.arraycopy(personnelInfoByte, j * (i - 1) + 516, nameByte, 0, 10);
                    }

                    //卡号
                    String card = Utils.byteToHex(cardByte);

                    //人名
                    String username = null;
                    try {
                        username = new String(nameByte, "GB2312");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    //添加到数据库中
                    isSuccess = FaceApi.getInstance().registerUserIntoDBmanager("default", username, "imagename.jpg", card, featureByte);
                    if (isSuccess) {
                        success++;
                    }
                }
                Log.e(TAG, "run: " + success);
                progressList.setProgress(progressFlag);
                progressList.setFaceLibNum(inTotalInfo);
                progressList.setSuccess(success);
                LiveDataBus.get().with("progressData").postValue(progressList);
                progressFlag = true;
            }
        });
    }

    /**
     * 新增人员
     *
     * @param data 新增人员数据
     */
    private void newStaff(byte[] data) {
        //照片特征值
        byte[] featureByte = new byte[512];
        //卡号
        byte[] cardByte = new byte[4];
        //人员名称
        byte[] userNameByte = new byte[10];
        //校验码
        byte[] faceNumByte = new byte[2];

        System.arraycopy(data, 12, featureByte, 0, 512);
        System.arraycopy(data, 524, cardByte, 0, 4);
        System.arraycopy(data, 528, userNameByte, 0, 10);
        System.arraycopy(data, 10, faceNumByte, 0, 2);

        //卡号
        String card = Utils.byteToHex(cardByte);
        //人名
        String username = null;
        try {
            username = new String(userNameByte, "GB2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //校验码
        int faceNum = Utils.bytesToInt(faceNumByte);

        Log.e(TAG, "newStaff:特征值： " + Arrays.toString(featureByte));
        Log.e(TAG, "newStaff:卡号： " + card);
        Log.e(TAG, "newStaff:人名： " + username);
        Log.e(TAG, "newStaff:校验码： " + faceNum);

        FaceApi.getInstance().registerUserIntoDBmanager("default", username, "imagename.jpg", card, featureByte);
    }

    /**
     * 删除人员
     *
     * @param data 删除人员数据
     */
    private void removeStaff(byte[] data) {
        //卡号
        byte[] cardByte = new byte[4];
        System.arraycopy(data, 12, cardByte, 0, 4);
        //卡号
        String card = Utils.byteToHex(cardByte);
        Log.e(TAG, "removeStaff: 卡号：" + card);

        //删除单个用户信息
        UserInfoManager.getInstance().deleteUserInfo(card);
        UserInfoManager.getInstance().setRemoveStaffCallback(new RemoveStaffCallback() {
            @Override
            public void removeStaffSuccess() {
                Log.e("TAG", "removeStaffSuccess: 成功");
                // 数据变化，更新内存
                FaceApi.getInstance().initDatabases(true);
            }

            @Override
            public void removeStaffFailure() {
                Log.e("TAG", "removeStaffFailure: 失败");
            }
        });
    }

    @SuppressWarnings({"BusyWait", "InfiniteLoopStatement"})
    public class HeardThread extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    byte[] mHeartData = new byte[]{(byte) 0xaa, (byte) 0xaa, (byte) 0xaa, (byte) 0xaa};
                    byte[] mHeartSend = Utils.addBytes(Utils.concat(mRuleHead, mHeartAdress), mHeartData, mCheckCode);
                    if (mQueue.size() == mQueueSize) {
                        mQueue.poll();
                    }
                    mQueue.offer(mHeartSend);
                    Thread.sleep(20000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private final Runnable mSendRunnable = new Runnable() {
        @Override
        public void run() {
            sendData();
        }
    };

    private void sendData() {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mQueue.size() != 0) {
                        if (mBos != null) {
                            mBos.write((byte[]) mQueue.poll());
                            mBos.flush();
                            LiveDataBus.get().with("sendNum").postValue(mSendNum++);
                        }
                    }
                    mHandler.removeCallbacks(mSendRunnable);
                    mHandler.postDelayed(mSendRunnable, 10);
                } catch (Exception e) {  //数据发送失败
                    e.printStackTrace();
                    //释放资源
                    releaseSocket(true);
                }
            }
        });
    }


    private final Runnable disConnectRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "正在执行断连: disConnect");
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    //释放资源
                    releaseSocket(true);
                }
            });
        }
    };


    /**
     * 串口心跳线程
     */
    @SuppressWarnings("BusyWait")
    private final Thread serialHeartbeatThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                while (serialHeartbeatThreadFlag) {
                    long times = System.currentTimeMillis();
                    String cTime = Utils.formatTime(times, "yyyyMMddHHmmss");
                    byte[] heartData = Utils.heartData(cTime);
                    //发送串口心跳数据
                    serialPortUtils.sendSerialPort(heartData);
                    LiveDataBus.get().with("sendNum").postValue(mSendNum++);
                    Thread.sleep(5000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });

    /**
     * 释放资源
     *
     * @param bool true重连Socket，false不重连Socket
     */
    private void releaseSocket(boolean bool) {
        try {
            //发送“离线”状态
            LiveDataBus.get().with("heart").postValue(false);

            mHandler.removeCallbacks(mSendRunnable);

            if (mHeardThread != null) {
                mHeardThread.interrupt();
            }

            if (mReadThread != null) {
                mReadThread.interrupt();
            }

            if (mBos != null) {
                mBos.close();
                mBos = null;
            }

            if (mBis != null) {
                mBis.close();
                mBis = null;
            }

            if (mSocket != null) {
//                if (mSocket.isConnected()) {
//                    shutdownInput  shutdownOutput 关流不关闭Socket连接
//                    mSocket.shutdownInput();
//                    mSocket.shutdownOutput();
//                }
                mSocket.close();
                mSocket = null;
                if (bool) {
                    mHandler.postDelayed(() -> {
                        Log.d(TAG, "Socket连接建立失败,正在尝试第" + SocketReconnectNumber++ + "次重连");
                        mExecutorService.execute(connectRunnable);
                    }, 3000);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final Observer<byte[]> observer = new Observer<byte[]>() {
        @Override
        public void onChanged(@Nullable byte[] bytes) {
            if (bytes != null) {
                serialPortUtils.sendSerialPort(bytes);
            }
            LiveDataBus.get().with("sendNum").postValue(mSendNum++);
        }
    };

    private final Observer<byte[]> faceObserver = new Observer<byte[]>() {
        @Override
        public void onChanged(@Nullable byte[] bytes) {
            if (bytes != null) {
                if (switchPort) {
                    byte[] sendData = Utils.addBytes(Utils.concat(mRuleHead, mRegisterAdress22), bytes, mCheckCode);
                    if (mQueue.size() == mQueueSize) {
                        mQueue.poll();
                    }
                    mQueue.offer(sendData);
                } else {
                    byte[] sendData = Utils.addBytes(Utils.concat(mRuleHead, mRegisterAdress), bytes, mCheckCode);
                    if (mQueue.size() == mQueueSize) {
                        mQueue.poll();
                    }
                    mQueue.offer(sendData);
                }
            }
        }
    };

    private final Observer<Boolean> switchPortObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(@Nullable Boolean sw_port) {
            switchPort = sw_port;
        }
    };

    /**
     * 获取平板关机消息
     */
    private final Observer<Integer> getSlabShutdownMessageObserver = new Observer<Integer>() {
        @Override
        public void onChanged(@Nullable Integer integer) {
            //停止发送串口心跳线程
            serialHeartbeatThreadFlag = false;
            Log.e(TAG, "onChanged: 停止发送串口心跳线程");
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        //释放资源
        releaseSocket(false);

        //停止发送串口心跳线程
        serialHeartbeatThreadFlag = false;

        LiveDataBus.get().with("SerialData", byte[].class).removeObserver(observer);

        LiveDataBus.get().with("registerData", byte[].class).removeObserver(faceObserver);

        LiveDataBus.get().with(BaseConstant.slabShutdownMessage, Integer.class).removeObserver(getSlabShutdownMessageObserver);

        LiveDataBus.get().with("switchPort", Boolean.class).removeObserver(switchPortObserver);
    }
}