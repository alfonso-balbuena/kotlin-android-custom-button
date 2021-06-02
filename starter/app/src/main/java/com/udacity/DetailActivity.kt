package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        val uri = intent.getStringExtra(URI)
        val status = intent.getIntExtra(STATUS,-1)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancelAll()
        txt_filename.text = uri
        val string_status = when(status) {
            DownloadManager.STATUS_SUCCESSFUL -> "Success"
            DownloadManager.STATUS_FAILED -> "Failed"
            else -> "Other"
        }
        val color = if (string_status == "Success") getColor(R.color.colorPrimaryDark) else Color.RED
        txt_status.setTextColor(color)
        txt_status.text = string_status
        btn_ok.setOnClickListener {
            motion_layout_detail.transitionToEnd {
                onBackPressed()
            }
        }
    }

    companion object {
        val URI = "URI"
        val STATUS = "STATUS"
    }
}
