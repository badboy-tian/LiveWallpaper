package com.i7play.videopapger.activity

import android.media.MediaPlayer
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.i7play.videopapger.R
import com.i7play.videopapger.ext.toast
import com.i7play.videopapger.service.LiveWallpaperService
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard
import kotlinx.android.synthetic.main.activity_trim.*
import com.avos.avoscloud.AVAnalytics



class CutActivity : android.support.v7.app.AppCompatActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trim)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val url = intent.getStringExtra("url")
        if (url != null){
            video.setUp(url, JCVideoPlayerStandard.SCREEN_WINDOW_FULLSCREEN, intent.getStringExtra("name"))
            Glide.with(this).load(Uri.parse(intent.getStringExtra("img"))).into(video.thumbImageView)
        }

        /*mCbVoice!!.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // 静音
                LiveWallpaperService.voiceSilence(applicationContext)
            } else {
                LiveWallpaperService.voiceNormal(applicationContext)
            }
        }*/

        //startActivity(android.content.Intent(this, MainActivity::class.java))
    }

    val completedListener = MediaPlayer.OnCompletionListener { this@CutActivity.toast("播发结束!") }

    fun setVideoToWallPaper(view: android.view.View) {
        LiveWallpaperService.Companion.setToWallPaper(this)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_cut->
                toast("剪切!")
            android.R.id.home->
                    finish()
        }

        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_cut, menu)
        return true
    }

    override fun onBackPressed() {
        if (JCVideoPlayer.backPress()) {
            return;
        }
        super.onBackPressed()
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
