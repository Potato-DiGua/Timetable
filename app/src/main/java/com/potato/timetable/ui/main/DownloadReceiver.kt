package com.potato.timetable.ui.main

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log


class DownloadReceiver(private val downloadID: Long) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) == downloadID) {
            val dm = context!!.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadFileUri = dm.getUriForDownloadedFile(downloadID)
            Log.d("download", downloadID.toString())
            install(context, downloadFileUri)
        }
    }

    private fun install(context: Context?, uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW)
        //apk对应的MIME类型
        val type = "application/vnd.android.package-archive"
        intent.setDataAndType(uri, type);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context?.startActivity(intent)
        context?.unregisterReceiver(this)
    }
}