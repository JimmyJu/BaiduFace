/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.idl.face.main.model

import android.util.Base64

/**
 * 用户实体类
 */
class User {
    var id = 0

    var userId = ""

    /**
     * 名字
     */
    var userName = ""

    /**
     * 组名
     */
    var groupId = ""

    /**
     * 时间
     */
    var ctime: Long = 0
    var updateTime: Long = 0
    /**
     * 卡号
     */
    var userInfo = ""
    var faceToken = ""
        get() {
            if (feature != null) {
                val base = Base64.encode(feature, Base64.NO_WRAP)
                field = String(base)
            }
            return field
        }
    var imageName = ""
    var feature: ByteArray? = null
    var isChecked = false
    override fun toString(): String {
        return "User(id=$id, userId='$userId', userName='$userName', groupId='$groupId', ctime=$ctime, updateTime=$updateTime, userInfo='$userInfo', imageName='$imageName', feature=${feature?.contentToString()}, isChecked=$isChecked)"
    }


}