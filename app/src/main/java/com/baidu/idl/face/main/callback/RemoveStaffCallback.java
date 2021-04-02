package com.baidu.idl.face.main.callback;

/**
 * @Date: 2020/5/13 9:53
 * @Author: Summer
 * @Description: 移除用户信息 成功、失败，接口回调
 */
public interface RemoveStaffCallback {
    /**
     * 成功
     */
    void removeStaffSuccess();

    /**
     * 失败
     */
    void removeStaffFailure();
}
