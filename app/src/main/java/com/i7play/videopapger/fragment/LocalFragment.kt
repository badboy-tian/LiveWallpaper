package com.i7play.videopapger.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import cn.bingoogolapple.alertcontroller.BGAAlertAction
import cn.bingoogolapple.alertcontroller.BGAAlertController
import com.avos.avoscloud.AVAnalytics
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.AdRequest
import com.i7play.videopapger.R
import com.i7play.videopapger.activity.DetailActivity
import com.i7play.videopapger.activity.MainActivity
import com.i7play.videopapger.app.IAP
import com.i7play.videopapger.bean.EventType
import com.i7play.videopapger.bean.MessageEvent
import com.i7play.videopapger.ext.covertSize
import com.i7play.videopapger.ext.toast
import com.i7play.videopapger.service.LiveWallpaperService
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
import com.zhy.adapter.recyclerview.CommonAdapter
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter
import com.zhy.adapter.recyclerview.base.ViewHolder
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import kotlinx.android.synthetic.main.empty_view.*
import kotlinx.android.synthetic.main.fragment.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File
import java.io.FilenameFilter
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Administrator on 2017/6/2.
 */
class LocalFragment : Fragment() {
    lateinit var datas: ArrayList<File>
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment, container, false)
        println("LocalFragment onCreateView")
        return rootView
    }

    lateinit var adapter: CommonAdapter<File>
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        datas = ArrayList<File>()
        adapter = object : CommonAdapter<File>(activity, R.layout.recyclerview_local_item, datas) {
            override fun convert(holder: ViewHolder?, t: File?, position: Int) {
                holder?.let {
                    holder.setText(R.id.local_item_name, t?.nameWithoutExtension)
                    val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    holder.setText(R.id.local_item_date, date.format(Date(t?.lastModified()!!)))
                    holder.setText(R.id.local_item_size, t?.covertSize())
                    Glide.with(activity).load(t)
                            .apply(RequestOptions().centerCrop())
                            .thumbnail(0.1f)
                            .into(holder.getView<ImageView>(R.id.local_item_head))
                }
            }
        }

        initListener()
        list.layoutManager = LinearLayoutManager(activity)
        list.adapter = adapter
        list.itemAnimator = SlideInLeftAnimator()
        list.addItemDecoration(HorizontalDividerItemDecoration
                .Builder(activity).color(activity.resources.getColor(R.color.div))
                .size(1)
                .build())
        initData()
        initAd()
    }

    private fun initAd() {
        adView.loadAd(AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build())
    }

    private fun initListener() {
        adapter.setOnItemClickListener(object : MultiItemTypeAdapter.OnItemClickListener {
            override fun onItemLongClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int): Boolean {
                return false
            }

            override fun onItemClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int) {
                popMenu(position)
            }
        })
    }

    private fun popMenu(position: Int) {
        var dialog = BGAAlertController(activity, getString(R.string.options), getString(R.string.choose_options), BGAAlertController.AlertControllerStyle.ActionSheet)
        dialog.addAction(BGAAlertAction(getString(R.string.Set_As_Wallpaper), BGAAlertAction.AlertActionStyle.Default, BGAAlertAction.Delegate {
            IAP.savePath(datas[position].path)
            if (!IAP.iap.isLiveWallpaperRunning()) {
                LiveWallpaperService.setToWallPaper(activity)
            } else {
                IAP.goHome()
                //LiveWallpaperService.changeVideo(activity, IAP.getPath())
            }
        }))

        dialog.addAction(BGAAlertAction(getString(R.string.play_video), BGAAlertAction.AlertActionStyle.Default, BGAAlertAction.Delegate {
            val i = Intent(activity, DetailActivity::class.java)
            i.putExtra("url", datas[position].path)
            i.putExtra("type", "local")
            startActivity(i)
        }))

        dialog.addAction(BGAAlertAction(getString(R.string.share_send), BGAAlertAction.AlertActionStyle.Default, BGAAlertAction.Delegate {
            val intent = Intent(Intent.ACTION_SEND);
            intent.type = "video/*"
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(datas[position]))
            startActivity(Intent.createChooser(intent, getString(R.string.share_vedios)))
        }))

        dialog.addAction(BGAAlertAction(getString(R.string.deletefile), BGAAlertAction.AlertActionStyle.Destructive, BGAAlertAction.Delegate {
            if (IAP.getPath() != datas[position].path) {
                val file = datas.removeAt(position)
                file.delete()
                initData()
            }else{
                activity.toast(getString(R.string.cannotdelete))
            }
        }))

        dialog.addAction(BGAAlertAction(getString(R.string.cancel), BGAAlertAction.AlertActionStyle.Cancel, BGAAlertAction.Delegate { }))

        dialog.show()
    }

    fun initData() {
        if (datas.size != 0) {
            datas.clear()
        }
        val file = File(MainActivity.path)
        file.listFiles(FilenameFilter { dir, name ->
            name?.let {
                return@FilenameFilter name.endsWith("mp4")
            }

            false
        })?.forEach {
            datas.add(it)
        }

        if (datas.size != 0) {
            empty.visibility = View.GONE
        } else {
            empty_text.text = getString(R.string.nofile)
        }

        list.adapter.notifyDataSetChanged()
    }

    @Subscribe
    fun onEvent(event: MessageEvent) {
        if (event.type == EventType.UPDATEFILES) {
            initData()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this);
    }

    override fun onPause() {
        super.onPause()
        AVAnalytics.onFragmentEnd("localfragment")
    }

    override fun onResume() {
        super.onResume()
        AVAnalytics.onFragmentStart("localfragment")
    }
}