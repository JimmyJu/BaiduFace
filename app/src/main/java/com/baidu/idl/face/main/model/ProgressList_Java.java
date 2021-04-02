package com.baidu.idl.face.main.model;

import java.util.Objects;

public class ProgressList_Java {
    private boolean isProgress;
    private int faceLibNum;
    private int success;
    private int unsuccess;

    public boolean isProgress() {
        return isProgress;
    }

    public void setProgress(boolean progress) {
        isProgress = progress;
    }

    public int getFaceLibNum() {
        return faceLibNum;
    }

    public void setFaceLibNum(int faceLibNum) {
        this.faceLibNum = faceLibNum;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getUnsuccess() {
        return unsuccess;
    }

    public void setUnsuccess(int unsuccess) {
        this.unsuccess = unsuccess;
    }
}
