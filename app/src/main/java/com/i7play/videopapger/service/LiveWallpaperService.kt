package com.i7play.videopapger.service

import android.app.Activity
import android.app.WallpaperManager
import android.content.*
import android.media.MediaPlayer
import android.os.Build
import android.preference.PreferenceManager
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import android.widget.Toast
import com.crossbowffs.remotepreferences.RemotePreferences
import com.i7play.videopapger.R
import com.i7play.videopapger.app.IAP
import java.io.IOException
import android.content.Intent


/**
 * Created by danjj on 2017/5/16.
 */
/*class LiveWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        println("LiveWallpaperService#onCreateEngine")
        return LiveEngine()
    }

    inner class LiveEngine : Engine() {
        private var mediaPlayer: MediaPlayer? = null
        val intentFilter = IntentFilter(LiveWallpaperService.VIDEO_PARAMS_CONTROL_ACTION)
        var path = ""

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            println("LiveEngine#onCreate")
            if (!isPreview) {
                registerReceiver(broadcastReceiver, intentFilter)
            }
        }

        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.getIntExtra(LiveWallpaperService.KEY_ACTION, -1)
                when (action) {
                    LiveWallpaperService.ACTION_VOICE_NORMAL -> mediaPlayer?.setVolume(1.0f, 1.0f)
                    LiveWallpaperService.ACTION_VOICE_SILENCE -> mediaPlayer?.setVolume(0f, 0f)
                    LiveWallpaperService.ACTION_CHANGE_VIDEO -> {
                        path = intent.getStringExtra("path")
                        println("broadcastReceiver:${path}")
                        mediaPlayer!!.reset()
                        mediaPlayer!!.setDataSource(path)
                        mediaPlayer!!.prepare()
                        mediaPlayer!!.isLooping = true
                        toast(getString(R.string.update_success))
                    }
                }
            }
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            println("LiveEngine#onSurfaceCreated")
            mediaPlayer = MediaPlayer()
            mediaPlayer!!.setSurface(holder.surface)
            try {
                path = IAP.getPath()
                println("onSurfaceCreated:${path}")
                mediaPlayer!!.setDataSource(path)
                mediaPlayer!!.isLooping = true
                if (IAP.voice()) {
                    mediaPlayer!!.setVolume(1f, 1f)
                } else {
                    mediaPlayer!!.setVolume(0f, 0f)
                }
                mediaPlayer!!.prepare()
                //mediaPlayer!!.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            println("LiveEngine#onVisibilityChanged")
            if (visible) {
                mediaPlayer!!.start()
            } else {
                mediaPlayer!!.pause()
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            println("LiveEngine#onSurfaceDestroyed")
            mediaPlayer!!.release()
            mediaPlayer = null
        }

        override fun onDestroy() {
            println("LiveEngine#onDestroy")
            if (!isPreview) {
                unregisterReceiver(broadcastReceiver)
            }
            super.onDestroy()
        }
    }

    companion object {
        val VIDEO_PARAMS_CONTROL_ACTION = "com.i7play.videopaper"
        val KEY_ACTION = "action"
        val ACTION_VOICE_SILENCE = 110
        val ACTION_VOICE_NORMAL = 111
        val ACTION_CHANGE_VIDEO = 112
        val ACTION_CHANGE_TAP = 113

        fun voice(context: Context, value: Boolean) {
            val intent = Intent(VIDEO_PARAMS_CONTROL_ACTION)
            intent.putExtra(KEY_ACTION, if (value) ACTION_VOICE_NORMAL else ACTION_VOICE_SILENCE)
            context.sendBroadcast(intent)
        }

        fun changeVideo(context: Context, path: String) {
            val intent = Intent(VIDEO_PARAMS_CONTROL_ACTION)
            intent.putExtra(KEY_ACTION, ACTION_CHANGE_VIDEO)
            intent.putExtra("path", path)
            context.sendBroadcast(intent)
        }

        fun setToWallPaper(context: Activity) {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(context, LiveWallpaperService::class.java))
            intent.putExtra("SET_LOCKSCREEN_WALLPAPER", true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.flags = WallpaperManager.FLAG_LOCK
            }
            context.startActivityForResult(intent, 1000)
        }
    }
}
*/
class LiveWallpaperService : WallpaperService() {
    override fun onCreateEngine(): WallpaperService.Engine {
        return CubeEngine(this)
    }

    inner class CubeEngine(internal var ww: WallpaperService) : WallpaperService.Engine(), SurfaceHolder.Callback {
        internal var player: MediaPlayer? = null
        internal var Holder: SurfaceHolder? = null
        internal var wc = false
        internal var bfz = false
        internal var dz: String = ""
        internal var database: RemotePreferences? = null
        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(false)
            surfaceHolder.addCallback(this)

            database = IAP.database
        }

        override fun onDestroy() {
            if (player != null) {
                player!!.reset()
                player!!.release()
                player = null
            }
            wc = false
            super.onDestroy()
        }

        internal var i = 0
        internal var ij = 0
        override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
            Holder = surfaceHolder
            super.onSurfaceCreated(surfaceHolder)
            start()
        }

        fun start() {
            if (player != null) {
                player!!.reset()
                player!!.release()
                player = null
                wc = false
                bfz = false
            }

            player = MediaPlayer()
            player!!.setAudioStreamType(3)
            player!!.isLooping = true
            player!!.setSurface(Holder?.surface)
            try {

                dz = database?.getString("path", "null")!!
                if (dz == "null") {
                    //Toast.makeText(ww, "还未设置视频，到文件管理器点击视频并选择打开方式为星空视频壁纸极简版吧！", Toast.LENGTH_LONG).show()
                }
                player!!.setDataSource(dz)

                player!!.setVolume(0f, 0f)

                player!!.prepare()

                player!!.start()

                wc = true
                bfz = true
            } catch (e: IllegalArgumentException) {
            } catch (e: IOException) {
            } catch (e: IllegalStateException) {
            }

            player!!.setOnErrorListener { mp, what, extra ->
                if (bfz) {
                    i = 1
                    if (ij < 5) {
                        Toast.makeText(ww, "严重错误 $what $extra\n重新进入桌面可尝试修复", Toast.LENGTH_SHORT).show()
                        ij++
                    }
                } else {
                    i = 1
                }
                false
            }
        }

        override fun surfaceDestroyed(arg0: SurfaceHolder) {
            super.onSurfaceDestroyed(arg0)
        }

        override fun surfaceChanged(p1: SurfaceHolder, p2: Int, p3: Int, p4: Int) {
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible)
            //如果可见
            {
                if (wc && !bfz) {
                    if (i == 1) {
                        start()
                        i = 0
                    } else {
                        bfz = true
                        player!!.start()
                    }
                }
                val xr2 = database?.getString("path", "null")
                if (xr2 != dz) {
                    start()
                    Toast.makeText(ww, getString(R.string.update_success), Toast.LENGTH_SHORT).show()
                }
                //
            } else
            //如果不可见
            {
                if (wc && bfz) {
                    bfz = false
                    player!!.pause()
                }
            }
        }

    }

    companion object {
        fun setToWallPaper(context: Activity) {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(context, LiveWallpaperService::class.java))
            intent.putExtra("SET_LOCKSCREEN_WALLPAPER", true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.flags = WallpaperManager.FLAG_LOCK
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                val intents = Intent()
                intents.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intents.action = WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER
                try {
                    context.startActivity(intents)
                } catch (e2: Exception) {
                    Toast.makeText(context, context.getString(R.string.compoment_error), Toast.LENGTH_LONG).show()
                }
            }

        }
    }
}