package com.i7play.videopapger.ext

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.avos.avoscloud.AVFile
import com.crossbowffs.remotepreferences.RemotePreferences
import com.parse.ParseFile
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * Created by Administrator on 2017/6/3.
 */
fun Context.logE(msg: Any) {
    Log.e("hehe", msg.toString())
}

fun Context.logE(cls: Any, msg: Any) {
    Log.e(cls.toString(), msg.toString())
}

fun Context.toast(msg: Any) {
    Toast.makeText(this, msg.toString(), Toast.LENGTH_SHORT).show()
}

fun Context.isSDCardMounted(): Boolean {
    return Environment.getExternalStorageState().equals(
            Environment.MEDIA_MOUNTED)
}

fun Context.createDir(path: String) {
    val p = "${Environment.getExternalStorageDirectory()}/${path}"
    println(p)
    val f = File(p)
    if (!f.exists()) {
        f.mkdirs()
    }
}

fun Context.copyFile(name: String, toPath: String) {
    if (!File(toPath).exists()) {
        val bos = BufferedOutputStream(FileOutputStream(toPath))
        bos.write(assets.open(name).readBytes())
        bos.flush()
        bos.close()
    }
}

fun Context.isChina(): Boolean{
    return Locale.getDefault().language.contains("zh")
}

fun AVFile.covertSize(): String{
    val kb: Long = 1024
    val mb = kb * 1024
    val gb = mb * 1024

    if (size >= gb) {
        return String.format("%.1fG", size.toFloat() / gb)
    } else if (size >= mb) {
        val f = size.toFloat() / mb
        return String.format(if (f > 100) "%.0fm" else "%.1fm", f)
    } else if (size >= kb) {
        val f = size.toFloat() / kb
        return String.format(if (f > 100) "%.0fkb" else "%.1fkb", f)
    } else
        return String.format("%dB", size)
}

fun RemotePreferences.isFirst(value: Boolean){
    edit().putBoolean("isFirst", value).apply()
}

fun RemotePreferences.isFirst():Boolean{
    return getBoolean("isFirst", false)
}


fun File.covertSize(): String{
    val kb: Long = 1024
    val mb = kb * 1024
    val gb = mb * 1024
    val size = length()

    if (size >= gb) {
        return String.format("%.1fG", size.toFloat() / gb)
    } else if (size >= mb) {
        val f = size.toFloat() / mb
        return String.format(if (f > 100) "%.0fm" else "%.1fm", f)
    } else if (size >= kb) {
        val f = size.toFloat() / kb
        return String.format(if (f > 100) "%.0fkb" else "%.1fkb", f)
    } else
        return String.format("%dB", size)
}


fun ParseFile.covertSize(): String{
    val kb: Long = 1024
    val mb = kb * 1024
    val gb = mb * 1024
    val size = data.size

    if (size >= gb) {
        return String.format("%.1fG", size.toFloat() / gb)
    } else if (size >= mb) {
        val f = size.toFloat() / mb
        return String.format(if (f > 100) "%.0fm" else "%.1fm", f)
    } else if (size >= kb) {
        val f = size.toFloat() / kb
        return String.format(if (f > 100) "%.0fkb" else "%.1fkb", f)
    } else
        return String.format("%dB", size)
}
