package com.potato.timetable.util

import android.content.Context
import androidx.appcompat.app.AlertDialog

object DialogUtils {

    @JvmStatic
    fun showTipDialog(context: Context, tip: String) {
        showTipDialog(context, "提示", tip)
    }

    @JvmStatic
    fun showTipDialog(context: Context, title: String, tip: String) {
        AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(tip)
                .setPositiveButton("知道了", null)
                .create()
                .show()
    }
}