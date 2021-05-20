package com.baidu.idl.face.main.application

import android.app.Application
import android.util.Log
import com.baidu.idl.face.main.utils.AppStateMonitor
import com.baidu.idl.face.main.utils.AppStateMonitor.AppStateChangeListener
import com.tencent.bugly.crashreport.CrashReport
import java.util.*

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

        AppStateMonitor.track(this, object : AppStateChangeListener {
            override fun appTurnIntoForeground() {
                // 处理app到前台的逻辑
                Log.e("tag", "App - 处于前台")
            }

            override fun appTurnIntoBackGround() {
                // app处理到到后台的逻辑
                Log.e("tag", "App - 处于后台")
            }
        })
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