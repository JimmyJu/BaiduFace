/*
 * Copyright (C) 2019 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.idl.face.main.callback;


/**
 * 人脸特征抽取回调接口。
 *
 */
public interface FaceFeatureCallBack {

    public void onFaceFeatureCallBack( float featureSize, byte[] feature);

}
