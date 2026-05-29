package com.thunder.view.main

import android.app.Application
import android.content.Context
import com.vbox.VBoxCore
import com.vbox.app.BActivityThread.getUserId
import com.vbox.app.configuration.AppLifecycleCallback
import com.vbox.app.configuration.ClientConfiguration
import com.vbox.utils.Slog
import com.thunder.app
import com.thunder.biz.cache.AppSharedPreferenceDelegate
import java.io.File

class BlackBoxLoader {
    // Game package list
    private val packages = listOf("com.pubg.imobile","com.tencent.ig","com.pubg.krmobile")
    // Map package → correct .so file
    private val libMap = mapOf("com.pubg.imobile" to "libbgmi.so","com.tencent.ig"   to "libpubgm.so","com.pubg.krmobile" to "libkorea.so")
    
    private var mHideRoot by AppSharedPreferenceDelegate(app.getContext(), false)
    private var mDaemonEnable by AppSharedPreferenceDelegate(app.getContext(), false)
    private var mShowShortcutPermissionDialog by AppSharedPreferenceDelegate(app.getContext(), true)

    fun hideRoot(): Boolean {
        return mHideRoot
    }

    fun invalidHideRoot(hideRoot: Boolean) {
        this.mHideRoot = hideRoot
    }
    
    fun daemonEnable(): Boolean {
        return mDaemonEnable
    }

    fun invalidDaemonEnable(enable: Boolean) {
        this.mDaemonEnable = enable
    }

    fun showShortcutPermissionDialog(): Boolean {
        return mShowShortcutPermissionDialog
    }

    fun addLifecycleCallback() {
        VBoxCore.get().addAppLifecycleCallback(object : AppLifecycleCallback() {
            override fun beforeMainLaunchApk(packageName: String, userId: Int) {
            }

            override fun beforeCreateApplication(packageName: String?, processName: String?, context: Context?, userId: Int) {
                Slog.d(TAG, "beforeCreateApplication: pkg $packageName, processName $processName, userID:${getUserId()}")
            }

            override fun beforeApplicationOnCreate(packageName: String?, processName: String?, application: Application?, userId: Int) {
                Slog.d(TAG, "beforeApplicationOnCreate: pkg $packageName, processName $processName")
            }

            override fun afterApplicationOnCreate(packageName: String?, processName: String?, application: Application?, userId: Int) {
                Slog.d(TAG, "afterApplicationOnCreate: pkg $packageName, processName $processName")
            }
        })
    }

    fun attachBaseContext(context: Context) {
        VBoxCore.get().doAttachBaseContext(context, object : ClientConfiguration() {
            override fun getHostPackageName(): String {
                return context.packageName
            }

            override fun setHideRoot(): Boolean {
                return mHideRoot
            }

            override fun isEnableDaemonService(): Boolean {
                return mDaemonEnable
            }

            override fun requestInstallPackage(file: File?): Boolean {
                return false
            }
        })
    }

    fun doOnCreate() {
        VBoxCore.get().doCreate()
    }

    companion object {
        val TAG: String = BlackBoxLoader::class.java.simpleName
    }
}