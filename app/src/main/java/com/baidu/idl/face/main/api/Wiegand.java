package com.baidu.idl.face.main.api;

/**
 * Created by Administrator on 2018/1/27.
 */

public class Wiegand {
    private static Wiegand instance;

    public static synchronized Wiegand getInstance() {
        if (instance == null) {
            instance = new Wiegand();
        }
        return instance;
    }

    private Wiegand() {

    }

    /**
     * 韦根26输出
     *
     * @param value
     * @return
     */
    public int output26(long value) {
        int outputOpen = outputOpen();
        if (outputOpen > 0) {
            int result = Output26(value);
            closeOutput();
            return result;
        }
        return -1;
    }

    /**
     * 韦根34输出
     *
     * @param value
     * @return
     */
    public int output34(long value) {
        int outputOpen = outputOpen();
        if (outputOpen > 0) {
            int result = Output34(value);
            closeOutput();
            return result;
        }
        return -1;
    }

    /**
     * 关闭韦根输出
     */
    public void closeOutput() {
        outputClose();
    }

    /**
     * 打开韦根输入
     *
     * @return
     */
    public int openInput() {
        return inputOpen();
    }

    /**
     * 读取韦根输入值
     *
     * @return
     */
    public int readInput() {
        return inputRead();
    }

    /**
     * 关闭韦根输入
     */
    public void closeInput() {
        inputClose();
    }


    /**
     * 释放韦根输入输出
     */
    public void release() {
        closeInput();
        closeOutput();
    }

    static native int inputOpen();//打开

    static native void inputClose();//使用完成后关闭

    static native int inputRead();//读取

    static native int outputOpen();//打开

    static native void outputClose();//使用完成后关闭

    static native int readoutputWrite26();

    static native int readoutputWrite34();

    static native int Output26(long value);//传递整型数,将该数按照韦根26协议输出

    static native int Output34(long value);//传递整型数,将该数按照韦根34协议输出

    static {
        System.loadLibrary("wiegand");
    }
}
