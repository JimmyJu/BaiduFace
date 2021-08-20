package com.baidu.idl.face.main.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 常用功能封装
 */
public class Utils {

    /**
     * 获取版本号
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        if (context != null) {
            try {
                return context.getPackageManager()
                        .getPackageInfo(context.getPackageName(), 0)
                        .versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return "3.0.0";
    }


//    public static void main(String args[]) {
//        int j = 1;
////        byte[] b = {(byte)0x4F,(byte)0x4B,};
////        System.out.println(Arrays.toString(b));
////        String str= new String (b);
////        System.out.print(str);
//        for (int i = 0; i <= 10; i++) {
//            byte[] bytes = Utils.hexString2Bytes(Utils.addZero1(j++ + ""));
//            System.out.println(byteToHex(bytes));
//        }
//    }

    /**
     * 时间格式转化
     *
     * @param timeStamp
     * @param pattern
     * @return
     */
    public static String formatTime(long timeStamp, String pattern) {
        Date date = new Date(timeStamp);
        DateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(date);
    }

    /**
     * 判断服务是否开启
     *
     * @param context
     * @param ServiceName
     * @return
     */
    public static boolean isServiceRunning(Context context, String ServiceName) {
        if (TextUtils.isEmpty(ServiceName)) {
            return false;
        }
        ActivityManager myManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService =
                (ArrayList<ActivityManager.RunningServiceInfo>)
                        myManager.getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString()
                    .equals(ServiceName)) {
                return true;
            }
        }
        return false;
    }

    /*
     * byte转String
     * */

    public static final String byte2hex(byte b[]) {
        if (b == null) {
            throw new IllegalArgumentException(
                    "Argument b ( byte array ) is null! ");
        }
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xff);
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs.toUpperCase();
    }

    /**
     * byte数组转hex
     *
     * @param bytes
     * @return
     */

    public static String byteToHex(byte[] bytes) {
        String strHex = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < bytes.length; n++) {
            strHex = Integer.toHexString(bytes[n] & 0xFF);
            sb.append((strHex.length() == 1) ? "0" + strHex : strHex); // 每个字节由两个字符表示，位数不够，高位补0
        }
        return sb.toString().trim();
    }



    /*
     * 16进制字符串转字节数组
     */

    public static byte[] hexString2Bytes(String hex) {

        if ((hex == null) || (hex.equals(""))) {
            return null;
        } else if (hex.length() % 2 != 0) {
            hex = "0" + hex;
            hex = hex.toUpperCase();
            int len = hex.length() / 2;
            byte[] b = new byte[len];
            char[] hc = hex.toCharArray();
            for (int i = 0; i < len; i++) {
                int p = 2 * i;
                b[i] = (byte) (charToByte(hc[p]) << 4 | charToByte(hc[p + 1]));
            }
            return b;
        } else {
            hex = hex.toUpperCase();
            int len = hex.length() / 2;
            byte[] b = new byte[len];
            char[] hc = hex.toCharArray();
            for (int i = 0; i < len; i++) {
                int p = 2 * i;
                b[i] = (byte) (charToByte(hc[p]) << 4 | charToByte(hc[p + 1]));
            }
            return b;
        }

    }

    /*
     * 字符转换为字节
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }


    public static String addZero(String id) {
        String ids = null;
        if (id.length() == 1) {
            ids = "0000000" + id;
        } else if (id.length() == 2) {
            ids = "000000" + id;
        } else if (id.length() == 3) {
            ids = "00000" + id;
        } else if (id.length() == 4) {
            ids = "0000" + id;
        } else if (id.length() == 5) {
            ids = "000" + id;
        } else if (id.length() == 6) {
            ids = "00" + id;
        } else if (id.length() == 7) {
            ids = "0" + id;
        } else if (id.length() == 8) {
            ids = id;
        }

        return ids;
    }

    public static String addZero1(String id) {
        String ids = null;
        if (id.length() == 1) {
            ids = "000" + id;
        } else if (id.length() == 2) {
            ids = "00" + id;
        } else if (id.length() == 3) {
            ids = "0" + id;
        } else if (id.length() == 4) {
            ids = id;
        }

        return ids;
    }

    static byte[] crc16_tab_h = {(byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x01, (byte) 0xC0,
            (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1,
            (byte) 0x81, (byte) 0x40, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0,
            (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0,
            (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x01, (byte) 0xC0, (byte) 0x80, (byte) 0x41, (byte) 0x00, (byte) 0xC1, (byte) 0x81, (byte) 0x40};

    static byte[] crc16_tab_l = {(byte) 0x00, (byte) 0xC0, (byte) 0xC1, (byte) 0x01, (byte) 0xC3, (byte) 0x03, (byte) 0x02, (byte) 0xC2, (byte) 0xC6, (byte) 0x06, (byte) 0x07, (byte) 0xC7, (byte) 0x05, (byte) 0xC5, (byte) 0xC4, (byte) 0x04, (byte) 0xCC, (byte) 0x0C, (byte) 0x0D, (byte) 0xCD, (byte) 0x0F, (byte) 0xCF, (byte) 0xCE, (byte) 0x0E, (byte) 0x0A, (byte) 0xCA, (byte) 0xCB, (byte) 0x0B, (byte) 0xC9, (byte) 0x09, (byte) 0x08, (byte) 0xC8, (byte) 0xD8, (byte) 0x18, (byte) 0x19, (byte) 0xD9, (byte) 0x1B, (byte) 0xDB, (byte) 0xDA, (byte) 0x1A, (byte) 0x1E, (byte) 0xDE, (byte) 0xDF, (byte) 0x1F, (byte) 0xDD, (byte) 0x1D, (byte) 0x1C, (byte) 0xDC, (byte) 0x14, (byte) 0xD4, (byte) 0xD5, (byte) 0x15, (byte) 0xD7, (byte) 0x17, (byte) 0x16, (byte) 0xD6, (byte) 0xD2, (byte) 0x12,
            (byte) 0x13, (byte) 0xD3, (byte) 0x11, (byte) 0xD1, (byte) 0xD0, (byte) 0x10, (byte) 0xF0, (byte) 0x30, (byte) 0x31, (byte) 0xF1, (byte) 0x33, (byte) 0xF3, (byte) 0xF2, (byte) 0x32, (byte) 0x36, (byte) 0xF6, (byte) 0xF7, (byte) 0x37, (byte) 0xF5, (byte) 0x35, (byte) 0x34, (byte) 0xF4, (byte) 0x3C, (byte) 0xFC, (byte) 0xFD, (byte) 0x3D, (byte) 0xFF, (byte) 0x3F, (byte) 0x3E, (byte) 0xFE, (byte) 0xFA, (byte) 0x3A, (byte) 0x3B, (byte) 0xFB, (byte) 0x39, (byte) 0xF9, (byte) 0xF8, (byte) 0x38, (byte) 0x28, (byte) 0xE8, (byte) 0xE9, (byte) 0x29, (byte) 0xEB, (byte) 0x2B, (byte) 0x2A, (byte) 0xEA, (byte) 0xEE, (byte) 0x2E, (byte) 0x2F, (byte) 0xEF, (byte) 0x2D, (byte) 0xED, (byte) 0xEC, (byte) 0x2C, (byte) 0xE4, (byte) 0x24, (byte) 0x25, (byte) 0xE5, (byte) 0x27, (byte) 0xE7,
            (byte) 0xE6, (byte) 0x26, (byte) 0x22, (byte) 0xE2, (byte) 0xE3, (byte) 0x23, (byte) 0xE1, (byte) 0x21, (byte) 0x20, (byte) 0xE0, (byte) 0xA0, (byte) 0x60, (byte) 0x61, (byte) 0xA1, (byte) 0x63, (byte) 0xA3, (byte) 0xA2, (byte) 0x62, (byte) 0x66, (byte) 0xA6, (byte) 0xA7, (byte) 0x67, (byte) 0xA5, (byte) 0x65, (byte) 0x64, (byte) 0xA4, (byte) 0x6C, (byte) 0xAC, (byte) 0xAD, (byte) 0x6D, (byte) 0xAF, (byte) 0x6F, (byte) 0x6E, (byte) 0xAE, (byte) 0xAA, (byte) 0x6A, (byte) 0x6B, (byte) 0xAB, (byte) 0x69, (byte) 0xA9, (byte) 0xA8, (byte) 0x68, (byte) 0x78, (byte) 0xB8, (byte) 0xB9, (byte) 0x79, (byte) 0xBB, (byte) 0x7B, (byte) 0x7A, (byte) 0xBA, (byte) 0xBE, (byte) 0x7E, (byte) 0x7F, (byte) 0xBF, (byte) 0x7D, (byte) 0xBD, (byte) 0xBC, (byte) 0x7C, (byte) 0xB4, (byte) 0x74,
            (byte) 0x75, (byte) 0xB5, (byte) 0x77, (byte) 0xB7, (byte) 0xB6, (byte) 0x76, (byte) 0x72, (byte) 0xB2, (byte) 0xB3, (byte) 0x73, (byte) 0xB1, (byte) 0x71, (byte) 0x70, (byte) 0xB0, (byte) 0x50, (byte) 0x90, (byte) 0x91, (byte) 0x51, (byte) 0x93, (byte) 0x53, (byte) 0x52, (byte) 0x92, (byte) 0x96, (byte) 0x56, (byte) 0x57, (byte) 0x97, (byte) 0x55, (byte) 0x95, (byte) 0x94, (byte) 0x54, (byte) 0x9C, (byte) 0x5C, (byte) 0x5D, (byte) 0x9D, (byte) 0x5F, (byte) 0x9F, (byte) 0x9E, (byte) 0x5E, (byte) 0x5A, (byte) 0x9A, (byte) 0x9B, (byte) 0x5B, (byte) 0x99, (byte) 0x59, (byte) 0x58, (byte) 0x98, (byte) 0x88, (byte) 0x48, (byte) 0x49, (byte) 0x89, (byte) 0x4B, (byte) 0x8B, (byte) 0x8A, (byte) 0x4A, (byte) 0x4E, (byte) 0x8E, (byte) 0x8F, (byte) 0x4F, (byte) 0x8D, (byte) 0x4D,
            (byte) 0x4C, (byte) 0x8C, (byte) 0x44, (byte) 0x84, (byte) 0x85, (byte) 0x45, (byte) 0x87, (byte) 0x47, (byte) 0x46, (byte) 0x86, (byte) 0x82, (byte) 0x42, (byte) 0x43, (byte) 0x83, (byte) 0x41, (byte) 0x81, (byte) 0x80, (byte) 0x40};

    /**
     * 计算CRC16校验  对外的接口
     *
     * @param data 需要计算的数组
     * @return CRC16校验值
     */
    public static int calcCrc16(byte[] data) {
        return calcCrc16(data, 0, data.length);
    }

    /**
     * 计算CRC16校验
     *
     * @param data   需要计算的数组
     * @param offset 起始位置
     * @param len    长度
     * @return CRC16校验值
     */
    public static int calcCrc16(byte[] data, int offset, int len) {
        return calcCrc16(data, offset, len, 0xffff);
    }

    /**
     * 计算CRC16校验
     *
     * @param data   需要计算的数组
     * @param offset 起始位置
     * @param len    长度
     * @param preval 之前的校验值
     * @return CRC16校验值
     */
    public static int calcCrc16(byte[] data, int offset, int len, int preval) {
        int ucCRCHi = (preval & 0xff00) >> 8;
        int ucCRCLo = preval & 0x00ff;
        int iIndex;
        for (int i = 0; i < len; ++i) {
            iIndex = (ucCRCLo ^ data[offset + i]) & 0x00ff;
            ucCRCLo = ucCRCHi ^ crc16_tab_h[iIndex];
            ucCRCHi = crc16_tab_l[iIndex];
        }
        return ((ucCRCHi & 0x00ff) << 8) | (ucCRCLo & 0x00ff) & 0xffff;
    }

    public static byte[] getSendId(byte[] id) {
        //协议包头数组
        byte[] head = new byte[2];
        head[0] = 0x55;
        head[1] = (byte) 0xAA;
        //设备地址功能码数据字节数
        byte[] address = new byte[3];
        address[0] = 0x00;
        address[1] = 0x03;
        address[2] = 0x04;
        //crc
        int crcnum = calcCrc16(concat(address, id));
        byte[] crcbyte = new byte[2];
        crcbyte[1] = (byte) ((crcnum) >> 8);
        crcbyte[0] = (byte) (crcnum >> 0);
        //拼接得到需要发送的卡号
        byte[] concat1 = concat(head, address);
        byte[] concat2 = concat(concat1, id);
        byte[] concat3 = concat(concat2, crcbyte);
        return concat3;
    }


    /*
     * 拼接数组
     * */
    public static byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }


    /**
     * 将3个byte数组进行拼接
     */
    public static byte[] addBytes(byte[] data1, byte[] data2, byte[] data3) {
        byte[] data4 = new byte[data1.length + data2.length + data3.length];
        System.arraycopy(data1, 0, data4, 0, data1.length);
        System.arraycopy(data2, 0, data4, data1.length, data2.length);
        System.arraycopy(data3, 0, data4, data1.length + data2.length, data3.length);
        return data4;
    }

    public static byte[] heartData(String time) {
        String year1 = new BigInteger(time.substring(0, 2), 10).toString(16);
        String year2 = new BigInteger(time.substring(2, 4), 10).toString(16);
        String month = new BigInteger(time.substring(4, 6), 10).toString(16);
        String day = new BigInteger(time.substring(6, 8), 10).toString(16);
        String hour = new BigInteger(time.substring(8, 10), 10).toString(16);
        String minute = new BigInteger(time.substring(10, 12), 10).toString(16);
        String second = new BigInteger(time.substring(12, 14), 10).toString(16);

        byte[] years1 = hexString2Bytes(year1);
        byte[] years2 = hexString2Bytes(year2);
        byte[] months = hexString2Bytes(month);
        byte[] days = hexString2Bytes(day);
        byte[] hours = hexString2Bytes(hour);
        byte[] minutes = hexString2Bytes(minute);
        byte[] secondes = hexString2Bytes(second);
        //拼接得到时间byte
        byte[] concat1 = concat(years1, years2);
        byte[] concat2 = concat(concat1, months);
        byte[] concat3 = concat(concat2, days);
        byte[] concat4 = concat(concat3, hours);
        byte[] concat5 = concat(concat4, minutes);
        byte[] times = concat(concat5, secondes);

        //协议包头数组
        byte[] head = new byte[2];
        head[0] = 0x55;
        head[1] = (byte) 0xAA;
        //设备地址功能码数据字节数心跳包数据
        byte[] address = new byte[7];
        address[0] = 0x00;
        address[1] = 0x05;
        address[2] = 0x0b;
        address[3] = (byte) 0xaa;
        address[4] = (byte) 0xaa;
        address[5] = (byte) 0xaa;
        address[6] = (byte) 0xaa;
        //crc
        int crcnum = calcCrc16(concat(address, times));
        byte[] crcbyte = new byte[2];
        crcbyte[1] = (byte) ((crcnum) >> 8);
        crcbyte[0] = (byte) (crcnum >> 0);

        //拼接得到需要发送的协议数据
        byte[] concats1 = concat(head, address);
        byte[] concats2 = concat(concats1, times);
        byte[] timeData = concat(concats2, crcbyte);
        return timeData;
    }

    public static byte[] getRedLightData() {
        byte[] mHeand = new byte[]{(byte) 0x55, (byte) 0xAA};
        byte[] mData = new byte[]{0x00, 0x07, 0x04, 0x00, 0x00, 0x00, (byte) 0xAA};
        int crcnum = calcCrc16(mData);
        byte[] crcbyte = new byte[2];
        crcbyte[1] = (byte) ((crcnum) >> 8);
        crcbyte[0] = (byte) (crcnum >> 0);
        byte[] mRedData = concat(concat(mHeand, mData), crcbyte);
        return mRedData;
    }

    public static byte[] getGreenLightData() {
        byte[] mHeand = new byte[]{(byte) 0x55, (byte) 0xAA};
        byte[] mData = new byte[]{0x00, 0x08, 0x04, 0x00, 0x00, 0x00, (byte) 0xBB};
        int crcnum = calcCrc16(mData);
        byte[] crcbyte = new byte[2];
        crcbyte[1] = (byte) ((crcnum) >> 8);
        crcbyte[0] = (byte) (crcnum >> 0);
        byte[] mGreenData = concat(concat(mHeand, mData), crcbyte);
        return mGreenData;
    }

    /**
     * 将int类型的数据转换为byte数组
     *
     * @param n int数据
     * @return 生成的byte数组
     */

    public static byte[] intToBytes(int n) {
        String s = String.valueOf(n);
        return s.getBytes();
    }

    /**
     * 将byte数组转换为int数据
     * @param b 字节数组
     * @return 生成的int数据
     */
   /* public static int bytesToInt(byte[] b){
        String s = new String(b);
        return Integer.parseInt(s);
    }*/

    /**
     * 将byte数组转换为整数
     */
    public static int bytesToInt(byte[] bs) {
        int a = 0;
        for (int i = bs.length - 1; i >= 0; i--) {
            a += bs[i] * Math.pow(0xFF, bs.length - i - 1);
        }
        return a;
    }


    /**
     * 字符串类型字符
     * 将两个字节合并成一个字节
     *
     * @param str
     * @return
     */
    public static byte[] merge2BytesTo1Byte(String str) {
        byte[] bytes = new byte[str.length() / 2];
        char s, e;
        for (int i = 0; i < str.length(); i += 2) {
            s = str.charAt(i);//第I个索引字符
            e = str.charAt(i + 1);//第I+1个索引字符
            if (s <= '9') {
                if (e <= '9') {
                    bytes[i / 2] = (byte) (((s - 0x30) << 4) + (e - 0x30));
                } else if ((e <= 'z' && e >= 'a')) {
                    bytes[i / 2] = (byte) (((s - 0x30) << 4) + (e - 0x57));//左移4位

                }
            } else if (s > '9') {
                if (e <= '9') {
                    bytes[i / 2] = (byte) (((s - 0x57) << 4) + (e - 0x30));
                } else if ((s <= 'z' && e >= 'a')) {
                    bytes[i / 2] = (byte) (((s - 0x57) << 4) + (e - 0x57));
                }
            }
        }
        return bytes;
    }

    /**
     * 合并多个字节数组到一个字节数组
     *
     * @param values 动态字节数字参数
     * @return byte[] 合并后的字节数字
     */
    public static byte[] mergeBytes(byte[]... values) {
        int lengthByte = 0;
        for (byte[] value : values) {
            lengthByte += value.length;
        }
        byte[] allBytes = new byte[lengthByte];
        int countLength = 0;
        for (byte[] b : values) {
            System.arraycopy(b, 0, allBytes, countLength, b.length);
            countLength += b.length;
        }
        return allBytes;
    }


    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }


    /**
     * 判断字符是否是中文
     *
     * @param c 字符
     * @return 是否是中文
     */
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    /**
     * 判断字符串是否是乱码
     *
     * @param strName
     *            字符串
     * @return 是否是乱码
     */
    public static boolean isMessyCode(String strName) {
        String temp = strName.replaceAll("[0-9a-zA-Z\\p{P}\\s]", "");
//		System.out.println(temp);
        char[] ch = temp.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (!isChinese(c)) {
//                System.out.println(i + "" + temp.charAt(i) + "is not chinese");
//                Log.e("TAG", "isMessyCode: " +temp.charAt(i) + "is not chinese");
                return true;
            } else {
//				System.out.println(i + "" + temp.charAt(i) + "is a chinese");
            }
        }
        return false;
    }
//
//    public static void main(String[] args) {
//        System.out.println(isMessyCode("Ã©Å¸Â©Ã©Â¡ÂºÃ¥Â¹Â³"));
//        System.out.println(isMessyCode("你好"));
//        System.out.println(isNumeric("836uu537"));
//    }


}




