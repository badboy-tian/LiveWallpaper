package com.i7play.videopapger.activity

import android.app.SearchManager
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.view.ViewPager
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.NavigationView
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle

import com.i7play.videopapger.R
import com.i7play.videopapger.fragment.LocalFragment
import com.i7play.videopapger.fragment.VideoFragment
import android.support.v4.view.GravityCompat
import android.support.v7.widget.SearchView
import android.telecom.Call
import android.view.*
import android.widget.Toast
import cn.bingoogolapple.alertcontroller.BGAAlertAction
import cn.bingoogolapple.alertcontroller.BGAAlertController
import com.avos.avoscloud.AVAnalytics
import com.avos.avoscloud.LogUtil
import com.avos.avoscloud.feedback.ThreadActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.i7play.videopapger.app.IAP
import com.i7play.videopapger.bean.MessageEvent
import com.i7play.videopapger.ext.createDir
import com.i7play.videopapger.ext.isFirst
import com.i7play.videopapger.ext.toast
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.appbar_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.crypto.Cipher


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private lateinit var mViewPager: ViewPager
    private lateinit var mPages: Array<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EventBus.getDefault().register(this);

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_menu)
        setSupportActionBar(toolbar)

        //设置DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer_layout.setDrawerListener(toggle);
        toggle.syncState();
        //设置NavigationView
        nav_view.setNavigationItemSelectedListener(this)

        mPages = arrayOf<Fragment>(VideoFragment(), LocalFragment())

        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        mViewPager = findViewById(R.id.container) as ViewPager

        tabs.setupWithViewPager(mViewPager)
        settingFabMenu()
        setupFiles()

        mViewPager.adapter = mSectionsPagerAdapter

        showDialog()
    }

    private fun showDialog() {
        val dialog = BGAAlertController(this, getString(R.string.tip), getString(R.string.start_dialog_msg), BGAAlertController.AlertControllerStyle.Alert)
        dialog.addAction(BGAAlertAction(getString(R.string.agree), BGAAlertAction.AlertActionStyle.Destructive, object : BGAAlertAction.Delegate {
            override fun onClick() {
                IAP.database.isFirst(true)
                fab_menu.close(true)
                drawer_layout.closeDrawer(Gravity.START, true)
            }
        }))

        dialog.addAction(BGAAlertAction(getString(R.string.noagree), BGAAlertAction.AlertActionStyle.Cancel, null))
        if (!IAP.database.isFirst()) {
            dialog.show()
            fab_menu.open(true)
            drawer_layout.openDrawer(Gravity.START, true)
        }

        dialog.setCancelable(false)
    }

    companion object {
        var path = "${Environment.getExternalStorageDirectory()}/videoWallpaper"
    }

    private fun setupFiles() {
        createDir("videoWallpaper")
        //path =
    }

    private fun settingFabMenu() {
        //设置fab菜单
        fab_close.setOnClickListener(clickListener)
        fab_sound.setOnClickListener(clickListener)
        fab_mode.setOnClickListener(clickListener)
        fab_line.setOnClickListener(clickListener)

        fab_menu.setClosedOnTouchOutside(true)
        fab_menu.setOnMenuButtonClickListener {
            if (fab_menu.isOpened) {
                fab_menu.close(true)
            } else {
                fab_sound.labelText = if (IAP.voice()) getString(R.string.closesound) else getString(R.string.opensound)
                fab_menu.open(true)
            }
        }
    }

    val clickListener = View.OnClickListener {
        when (it.id) {
            R.id.fab_close -> {
                if (IAP.iap.isLiveWallpaperRunning()) {
                    WallpaperManager.getInstance(applicationContext).clear()
                } else {
                    IAP.iap.toast(getString(R.string.setwallpaperfirst))
                }
            }
            R.id.fab_sound -> {
                IAP.changeVoice(!IAP.voice())
            }
            R.id.fab_mode -> {
                if (IAP.iap.isLiveWallpaperRunning()) {
                    val i = Intent(Intent.ACTION_SET_WALLPAPER)
                    startActivity(i)
                } else {
                    IAP.iap.toast(getString(R.string.setwallpaperfirst))
                }
            }
            R.id.fab_line -> {
                val i = Intent(this, ThreadActivity::class.java)
                startActivity(i)
            }
        }

        fab_menu.close(true)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.nav_locale -> {//打开
                val intent = Intent()
                intent.type = "video/*" //选择视频 （mp4 3gp 是android支持的视频格式）
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(intent, 1000)
            }
            R.id.nav_camera -> {//拍摄视频
                val intent = Intent()
                intent.action = MediaStore.ACTION_VIDEO_CAPTURE
                //设置视频录制的画质
                startActivityForResult(intent, 2000);
            }
            R.id.nav_cut -> {//剪切
                toast(getString(R.string.develop))
            }
            R.id.nav_share -> {//分享
                //val msg = "我在使用视频壁纸软件:<<${getString(R.string.app_name)}>>哟, 里面有各种流行的视频壁纸,比如灵梦,舔屏什么的, 你懂的. 只需要在应用商店搜索: \"${getString(R.string.app_name)}\" 就可以下载咯!"
                val msg = String.format(getString(R.string.share_msg), getString(R.string.app_name), getString(R.string.app_name))
                val i = Intent(Intent.ACTION_SEND)
                i.putExtra(Intent.EXTRA_TEXT, msg)
                i.type = "text/plain"
                startActivity(Intent.createChooser(i, getString(R.string.action_share)))
            }
            R.id.nav_rate -> {//好评
                IAP.jumpAppStore(packageName)
            }
            R.id.nav_feedback -> {//反馈
                val i = Intent(this, ThreadActivity::class.java)
                startActivity(i)
            }
            R.id.nav_about -> {//关于我
                showAbout()
            }
            R.id.nav_voicechanger -> {//跳转app
                IAP.jumpAppStore("com.tian.voicechangerpro")
            }
        }
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showAbout() {
        var dialog = BGAAlertController(this, getString(R.string.aboutme), getString(R.string.about_msg), BGAAlertController.AlertControllerStyle.Alert)
        dialog.addAction(BGAAlertAction(getString(R.string.know), BGAAlertAction.AlertActionStyle.Default, null))
        dialog.show()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main2, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.action_main_share -> {
                val msg = String.format(getString(R.string.share_msg), getString(R.string.app_name), getString(R.string.app_name))
                val i = Intent(Intent.ACTION_SEND)
                i.putExtra(Intent.EXTRA_TEXT, msg)
                i.type = "text/plain"
                startActivity(Intent.createChooser(i, getString(R.string.action_share)))
            }
        }

        return super.onOptionsItemSelected(item)
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return mPages.get(position)
        }

        override fun getCount(): Int {
            return mPages.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return getString(R.string.faxian)
                1 -> return getString(R.string.local)
            }
            return null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    fun onEvent(event: MessageEvent) {

    }

    override fun onPause() {
        super.onPause()
        AVAnalytics.onPause(this)
    }

    override fun onResume() {
        super.onResume()
        AVAnalytics.onResume(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == 1000) && resultCode == RESULT_OK) {
            if (data != null) {
                val uri = data.data
                val i = Intent(this, DetailActivity::class.java)
                i.putExtra("url", uri.path)
                i.putExtra("type", "local")
                startActivity(i)
            }
        }

        if ((requestCode == 2000) && resultCode == RESULT_OK) {
            val cursor = this
                    .contentResolver
                    .query(data?.data,
                            arrayOf(android.provider.MediaStore.Video.VideoColumns.DATA), null, null, null)
            cursor!!.moveToFirst()
            val i = Intent(this, DetailActivity::class.java)
            i.putExtra("url", cursor.getString(0))
            i.putExtra("type", "local")
            startActivity(i)
            cursor.close()
        }
    }

    override fun onBackPressed() {
        // super.onBackPressed()
        val controller = BGAAlertController(this, getString(R.string.tip), getString(R.string.exit_msg), BGAAlertController.AlertControllerStyle.ActionSheet)
        controller.addAction(BGAAlertAction(getString(R.string.dialog_ok), BGAAlertAction.AlertActionStyle.Destructive, BGAAlertAction.Delegate {
            finish()
            android.os.Process.killProcess(android.os.Process.myPid())
        }))

        controller.addAction(BGAAlertAction(getString(R.string.dialog_cancel), BGAAlertAction.AlertActionStyle.Cancel, null))

        controller.show()
    }
}
