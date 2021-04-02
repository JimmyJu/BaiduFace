package com.baidu.idl.face.main.model

class ProgressList {
    /**
     * 进度条标识
     */
    var isProgress = false

    /**
     * 人脸库数量
     */
    var faceLibNum = 0

    /**
     * 成功数量
     */
    var success = 0

    /**
     * 失败数量
     */
    var unsuccess = 0
    override fun toString(): String {
        return "ProgressList(isProgress=$isProgress, faceLibNum=$faceLibNum, success=$success, unsuccess=$unsuccess)"
    }
}