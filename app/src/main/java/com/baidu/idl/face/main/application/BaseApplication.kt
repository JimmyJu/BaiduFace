package com.baidu.idl.face.main.application

import android.app.Application
import com.tencent.bugly.crashreport.CrashReport

/**
 * @Date: 2020/4/20 17:30
 * @Author: Summer
 * @Description: Application基类
 */
class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        //初始化BugLy
        initBugLy("221e6606c1", true)
    }

    /**
     *初始化BugLy
     *
     * @param appId BugLy平台自己的ID
     * @param bool SDK调试模式开关
     * 调试模式的行为特性如下：输出详细的Bugly SDK的Log
     * 每一条Crash都会被立即上报
     * 自定义日志将会在Logcat中输出
     * 建议在测试阶段建议设置成true，发布时设置为false
     */
    private fun initBugLy(appId: String, bool: Boolean) {
        val strategy = CrashReport.UserStrategy(applicationContext)
        CrashReport.initCrashReport(applicationContext, appId, bool, strategy);

    }
}