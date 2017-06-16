package com.i7play.videopapger.fragment

import android.Manifest
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.v7.widget.StaggeredGridLayoutManager
import com.avos.avoscloud.*
import com.bumptech.glide.Glide
import com.i7play.videopapger.R
import com.zhy.adapter.recyclerview.CommonAdapter
import com.zhy.adapter.recyclerview.base.ViewHolder
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.widget.LinearLayout
import com.bumptech.glide.request.RequestOptions
import com.github.florent37.glidepalette.GlidePalette
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.NativeExpressAdView
import com.i7play.videopapger.activity.DetailActivity
import com.i7play.videopapger.app.IAP
import com.i7play.videopapger.ext.covertSize
import com.i7play.videopapger.ext.isChina
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter
import kotlinx.android.synthetic.main.fragment.*
import android.util.DisplayMetrics
import cn.bingoogolapple.alertcontroller.BGAAlertAction
import cn.bingoogolapple.alertcontroller.BGAAlertController
import com.google.android.gms.ads.AdListener
import com.parse.ParseObject
import com.parse.ParseQuery
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.PermissionListener
import com.yanzhenjie.permission.Rationale
import com.yanzhenjie.permission.RationaleListener


/**
 * Created by Administrator on 2017/6/2.
 */
class VideoFragment : Fragment() {
    lateinit var datas: ArrayList<AVObject>
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment, container, false)
        println("VideoFragment onCreateView")
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        list.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        datas = ArrayList<AVObject>()
        val adapter = object : CommonAdapter<AVObject>(activity, R.layout.recyclerview_item, datas) {
            override fun convert(holder: ViewHolder?, avobject: AVObject?, position: Int) {
                if (avobject != null) {
                    val name = if (context.isChina()) {
                        avobject["name"].toString()
                    } else {
                        avobject["enname"].toString()
                    }
                    val imgUrl = avobject.getAVFile<AVFile>("img").url

                    holder?.setText(R.id.text_name, name.trim())
                    holder?.setText(R.id.text_size, avobject.getAVFile<AVFile>("video").covertSize())

                    Glide.with(context.applicationContext).load(imgUrl)
                            .apply(RequestOptions().centerCrop()
                                    .error(R.drawable.loading_error)
                                    .placeholder(R.drawable.loading))
                            .listener(GlidePalette.with(imgUrl)
                                    .use(0)
                                    .intoBackground(holder?.getView(R.id.rl_banner), 0)
                                    .intoTextColor(holder?.getView(R.id.text_name), 1)
                                    .intoTextColor(holder?.getView(R.id.text_size), 2)
                                    .crossfade(true)
                            )
                            .into(holder?.getView(R.id.item_img))
                }
            }
        }
        adapter.setOnItemClickListener(object : MultiItemTypeAdapter.OnItemClickListener {
            override fun onItemLongClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int): Boolean {
                return false
            }

            override fun onItemClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int) {
                val avObj = datas[position]
                val i = Intent(activity, DetailActivity::class.java)
                i.putExtra("type", "url")
                i.putExtra("url", avObj.getAVFile<AVFile>("video").url)
                i.putExtra("name", if (context.isChina()) {
                    avObj["name"].toString()
                } else {
                    avObj["enname"].toString()
                })
                i.putExtra("img", avObj.getAVFile<AVFile>("img").url)
                startActivity(i)
            }
        })

        list.adapter = adapter

        AVQuery.getQuery<AVObject>("VideoItem").findInBackground(object : FindCallback<AVObject>() {
            override fun done(p0: MutableList<AVObject>?, p1: AVException?) {
                if (p1 != null) {
                    println(p1)
                } else {
                    p0?.let {
                        p0.sortBy {
                            it.getAVFile<AVFile>("video").size
                        }
                        p0.map {
                            datas.add(it)
                        }
                        initData()
                    }
                }
            }

        })

        initAd()
    }

    private fun initAd() {
        adView.loadAd(AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build())
    }

    fun initData() {
        list.visibility = View.VISIBLE
        empty.visibility = View.GONE
        list.adapter.notifyDataSetChanged()
    }

    override fun onPause() {
        super.onPause()
        AVAnalytics.onFragmentEnd("videofragment")
    }

    override fun onResume() {
        super.onResume()
        AVAnalytics.onFragmentStart("videofragment")
    }
}