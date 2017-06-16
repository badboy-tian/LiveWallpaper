package com.i7play.videopapger.activity

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import cn.bingoogolapple.alertcontroller.BGAAlertAction
import cn.bingoogolapple.alertcontroller.BGAAlertController
import com.avos.avoscloud.AVAnalytics
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest

import com.i7play.videopapger.R
import com.i7play.videopapger.app.IAP
import com.i7play.videopapger.app.VideoState
import com.i7play.videopapger.bean.EventType
import com.i7play.videopapger.bean.MessageEvent
import com.i7play.videopapger.ext.createDir
import com.i7play.videopapger.ext.toast
import com.i7play.videopapger.service.LiveWallpaperService
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadListener
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.exception.*
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.PermissionListener
import com.yanzhenjie.permission.Rationale
import com.yanzhenjie.permission.RationaleListener
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard
import kotlinx.android.synthetic.main.activity_detail.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        initToolbar()
        initData()
        initAd()
    }

    private fun initData() {
        number_progress_bar.max = 100
        number_progress_bar.visibility = View.INVISIBLE

        val settingService = AndPermission.defineSettingDialog(this@DetailActivity, 400)
        // 在Fragment：
        AndPermission.with(this)
                .requestCode(100)
                .permission(
                        // 多个权限，以数组的形式传入。
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.INTERNET
                )
                .rationale(object : RationaleListener {
                    override fun showRequestPermissionRationale(requestCode: Int, rationale: Rationale?) {
                        val dialog = BGAAlertController(this@DetailActivity, getString(R.string.tip), getString(R.string.permission_msg), BGAAlertController.AlertControllerStyle.ActionSheet)
                        dialog.addAction(BGAAlertAction(getString(R.string.permission_ok), BGAAlertAction.AlertActionStyle.Default, object : BGAAlertAction.Delegate {
                            override fun onClick() {
                                rationale?.resume()
                            }
                        }))

                        dialog.addAction(BGAAlertAction(getString(R.string.permisson_cancel), BGAAlertAction.AlertActionStyle.Default, object : BGAAlertAction.Delegate {
                            override fun onClick() {
                                rationale?.cancel()
                            }
                        }))
                    }

                })
                .callback(object : PermissionListener {
                    override fun onSucceed(requestCode: Int, grantPermissions: MutableList<String>) {
                        createDir("videoWallpaper")
                    }

                    override fun onFailed(requestCode: Int, deniedPermissions: MutableList<String>) {
                        // 是否有不再提示并拒绝的权限。
                        if (AndPermission.hasAlwaysDeniedPermission(this@DetailActivity, deniedPermissions)) {

                            val dialog = BGAAlertController(this@DetailActivity, getString(R.string.tip), getString(R.string.permission_2), BGAAlertController.AlertControllerStyle.ActionSheet)
                            dialog.addAction(BGAAlertAction(getString(R.string.permission_ok), BGAAlertAction.AlertActionStyle.Default, object : BGAAlertAction.Delegate {
                                override fun onClick() {
                                    settingService.execute()
                                }
                            }))

                            dialog.addAction(BGAAlertAction(getString(R.string.permisson_cancel), BGAAlertAction.AlertActionStyle.Default, object : BGAAlertAction.Delegate {
                                override fun onClick() {
                                    settingService.cancel()
                                }
                            }))
                        }
                    }

                })
                .start()
    }

    private fun initAd() {
        adView.loadAd(AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build())
    }

    lateinit var url: String
    lateinit var type: String
    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        url = intent.getStringExtra("url")
        type = intent.getStringExtra("type")
        if (type == "local") {
            val name = File(url).nameWithoutExtension
            video.setUp(url, JCVideoPlayerStandard.SCREEN_WINDOW_FULLSCREEN, name)
            Glide.with(this).load(url).thumbnail(1f).into(video.thumbImageView)
            supportActionBar?.title = name.trim()
            state = VideoState.COMPLETED
            currentPath = url
        } else {
            video.setUp(url, JCVideoPlayerStandard.SCREEN_WINDOW_FULLSCREEN, intent.getStringExtra("name"))
            Glide.with(this).load(Uri.parse(intent.getStringExtra("img"))).into(video.thumbImageView)
            supportActionBar?.title = intent.getStringExtra("name").trim()

            val fileName = "${MainActivity.path}/${supportActionBar?.title}.mp4"
            if (File(fileName).exists()) {
                state = VideoState.COMPLETED
                currentPath = fileName
            }
        }
        video.backButton.visibility = View.GONE
        video.titleTextView.visibility = View.GONE
        video.backButton.visibility = View.GONE
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home ->
                finish()
        }
        return super.onOptionsItemSelected(item)
    }

    var state = VideoState.NORMAL
    lateinit var down: ImageView
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_details, menu)
        menu?.let {
            down = menu.findItem(R.id.toobar_download).actionView.findViewById(R.id.action_down) as ImageView
            down.setOnClickListener {
                when (state) {
                    VideoState.NORMAL -> {
                        println("normal->downlaod")
                        state = VideoState.DOWNLOAD
                        beginDownload()
                    }
                    VideoState.DOWNLOAD -> {
                        println("download->pause")
                        if (downloader != null) {
                            downloader.pause()
                            state = VideoState.PAUSE
                        }
                    }
                    VideoState.PAUSE -> {
                        println("pause->download")
                        if (downloader != null) {
                            downloader.reuse()
                            downloader.start()
                            state = VideoState.DOWNLOAD
                        }
                    }
                    VideoState.COMPLETED ->
                        popMenu()
                }
            }

            if (type == "local" || state == VideoState.COMPLETED) {
                down.setImageDrawable(resources.getDrawable(R.drawable.ic_check))
            }
        }

        return true
    }

    private fun popMenu() {
        val dialog = BGAAlertController(this, getString(R.string.tip), getString(R.string.detail_dialog_msg), BGAAlertController.AlertControllerStyle.ActionSheet)
        dialog.addAction(BGAAlertAction(getString(R.string.detail_dialog_action1), BGAAlertAction.AlertActionStyle.Default, BGAAlertAction.Delegate {
            AVAnalytics.onEvent(this@DetailActivity, "点击DetailActivity设置壁纸按钮")
            if (currentPath.isNotEmpty()) {
                println(currentPath)
                IAP.savePath(currentPath)
                if (!IAP.iap.isLiveWallpaperRunning()) {
                    LiveWallpaperService.setToWallPaper(this@DetailActivity)
                } else {
                    IAP.goHome()
                    //LiveWallpaperService.changeVideo(applicationContext, IAP.getPath())
                    finish()
                }
            }
        }))

        dialog.addAction(BGAAlertAction(getString(R.string.share_send), BGAAlertAction.AlertActionStyle.Default, object : BGAAlertAction.Delegate {
            override fun onClick() {
                val intent = Intent(Intent.ACTION_SEND);
                intent.type = "video/*"
                intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(url))
                startActivity(Intent.createChooser(intent, getString(R.string.share_vedios)))
            }
        }))

        dialog.addAction(BGAAlertAction(getString(R.string.cancel), BGAAlertAction.AlertActionStyle.Cancel, BGAAlertAction.Delegate { }))

        dialog.show()
    }

    lateinit var downloader: BaseDownloadTask
    fun beginDownload() {
        currentPath = "${MainActivity.path}/${supportActionBar?.title}.mp4"
        downloader = FileDownloader.getImpl().create(url).setPath(currentPath).setListener(object : FileDownloadListener() {
            override fun warn(task: BaseDownloadTask?) {

            }

            override fun pending(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                AVAnalytics.onEvent(this@DetailActivity, "下载:${supportActionBar?.title}")
            }

            override fun error(task: BaseDownloadTask?, e: Throwable?) {
                println(e.toString())
                if (e is FileDownloadHttpException) {

                } else if (e is FileDownloadGiveUpRetryException) {

                } else if (e is FileDownloadOutOfSpaceException) {

                } else if (e is FileDownloadNetworkPolicyException) {

                } else if (e is PathConflictException) {

                } else {

                }

                number_progress_bar.visibility = View.INVISIBLE
                state = VideoState.NORMAL
                down.setImageDrawable(resources.getDrawable(R.drawable.ic_cloud_download))
            }

            override fun progress(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                number_progress_bar.max = totalBytes
                number_progress_bar.progress = soFarBytes
            }

            override fun paused(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                down.setImageDrawable(resources.getDrawable(R.drawable.ic_file_download))
            }

            override fun completed(task: BaseDownloadTask?) {
                EventBus.getDefault().post(MessageEvent(EventType.UPDATEFILES))
                number_progress_bar.visibility = View.INVISIBLE
                down.setImageDrawable(resources.getDrawable(R.drawable.ic_check))
                state = VideoState.COMPLETED
            }

            override fun started(task: BaseDownloadTask?) {
                super.started(task)
                if (number_progress_bar.visibility == View.INVISIBLE) {
                    number_progress_bar.visibility = View.VISIBLE
                }

                down.setImageDrawable(resources.getDrawable(R.drawable.ic_pause))
            }
        })

        downloader.start()
    }

    override fun onBackPressed() {
        /*if (JCVideoPlayer.backPress()) {
            return
        }*/
        super.onBackPressed()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 400 || requestCode == 100) {

        } else {
            finish()
        }
    }

    companion object {
        var currentPath = ""
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this);
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    fun onEvent(event: MessageEvent) {

    }

    override fun onPause() {
        super.onPause()
        JCVideoPlayer.releaseAllVideos();
        AVAnalytics.onPause(this)
    }

    override fun onResume() {
        super.onResume()
        AVAnalytics.onResume(this)
    }
}
