package com.i7play.videopapger.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.net.Uri
import com.i7play.videopapger.app.IAP
import com.i7play.videopapger.service.LiveWallpaperService
import com.avos.avoscloud.AVAnalytics




class CatchActivity : AppCompatActivity() {
    var path = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent != null){
            val str = intent.dataString
            IAP.savePath(Uri.parse(str).path)
            if (!IAP.iap.isLiveWallpaperRunning()) {
                LiveWallpaperService.setToWallPaper(this)
            } else {
                //LiveWallpaperService.changeVideo(applicationContext, IAP.getPath())
            }

            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        AVAnalytics.onPause(this)
    }

    override fun onResume() {
        super.onResume()
        AVAnalytics.onResume(this)
    }
}
