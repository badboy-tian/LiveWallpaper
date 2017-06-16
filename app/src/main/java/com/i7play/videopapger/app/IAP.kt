package com.i7play.videopapger.app

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.SharedPreferences

import com.avos.avoscloud.AVOSCloud
import com.liulishuo.filedownloader.FileDownloader
import android.app.WallpaperManager
import android.content.res.Configuration
import android.net.Uri
import com.crossbowffs.remotepreferences.RemotePreferences
import com.google.android.gms.ads.MobileAds
import com.i7play.videopapger.R
import com.i7play.videopapger.ext.toast
import com.i7play.videopapger.service.LiveWallpaperService

/**
 * Created by Administrator on 2017/6/1.
 */

class IAP : Application() {
    override fun onCreate() {
        super.onCreate()
        iap = this
        database = RemotePreferences(applicationContext, author, "main_prefs")
        //MobileAds.initialize(this, "ca-app-pub-1743332321423234~8787116708")
        AVOSCloud.initialize(this, "appid", "clientid")
        AVOSCloud.setDebugLogEnabled(true)

        FileDownloader.init(this)
    }

    /**
     * 判断一个动态壁纸是否已经在运行

     * @param context
     * *            :上下文
     * *
     * @param tagetPackageName
     * *            :要判断的动态壁纸的包名
     * *
     * @return
     */
    fun isLiveWallpaperRunning(): Boolean {
        val wallpaperManager = WallpaperManager.getInstance(this)// 得到壁纸管理器
        val wallpaperInfo = wallpaperManager.wallpaperInfo// 如果系统使用的壁纸是动态壁纸话则返回该动态壁纸的信息,否则会返回null
        if (wallpaperInfo != null) { // 如果是动态壁纸,则得到该动态壁纸的包名,并与想知道的动态壁纸包名做比较
            val currentLiveWallpaperPackageName = wallpaperInfo.packageName
            if (currentLiveWallpaperPackageName == packageName) {
                return true
            }
        }
        return false
    }

    companion object {
        var author = "com.i7play.videopapger.app.preferences"
        lateinit var iap: IAP
        lateinit var database: RemotePreferences

        var currentVideo = ""

        public fun goHome() {
            val intent = Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.flags = FLAG_ACTIVITY_NEW_TASK
            iap.startActivity(intent)
        }

        fun savePath(path: String) {
            database.edit().putString(Key_Path, path).apply()
            //database.edit().putString(Key_Path, path).apply()
        }

        fun getPath(): String? {
            //return database.getString(Key_Path, "")
            return database.getString(Key_Path, "null")
        }

        val Key_Voice = "voice"
        val Key_Path = "path"

        fun changeVoice(value: Boolean) {
            if (iap.isLiveWallpaperRunning()) {
                //database.edit().putBoolean(Key_Voice, value).apply()
                database.edit().putBoolean(Key_Voice, value).apply()
                //LiveWallpaperService.voice(iap, value)
            } else {
                iap.toast(iap.getString(R.string.choosea))
            }
        }

        fun tab():Boolean{
            return database.getBoolean("tab", true)
        }

        fun voice(): Boolean {
            //return database.getBoolean(Key_Voice, false)
            return database.getBoolean(Key_Voice, false)
        }

        fun jumpAppStore(pkgName: String){
            val uri = Uri.parse("market://details?id=" + pkgName)
            var intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            iap.startActivity(intent)
        }
    }
}
