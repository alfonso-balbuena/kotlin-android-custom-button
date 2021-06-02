package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        createNotificationChannel()
        custom_button.setOnClickListener {
            if(URL.isEmpty()) {
                val toast = Toast.makeText(applicationContext,"Please select the file to download",Toast.LENGTH_LONG)
                toast.show()
                custom_button.stop()
            } else {
                download()
            }
        }
        
    }

    fun onRadioButtonClicked(view : View) {
        if(view is RadioButton) {
            val checked = view.isChecked
            when(view.id) {
                R.id.radio_glide -> if(checked) {
                    URL = "https://github.com/bumptech/glide"
                }
                R.id.radio_loadApp -> if(checked) {
                    URL = "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter"
                }
                R.id.radio_retrofit -> if(checked) {
                    URL = "https://github.com/square/retrofit"
                }
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            custom_button.stop()
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            val data = getDataCursor(id!!)
            createNotification(data.first,data.second, id.toInt())
        }
    }

    private fun getDataCursor(id : Long) : Pair<String,Int> {
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().apply {
            setFilterById(id)
        }
        val cursor = downloadManager.query(query)
        val index = cursor.getColumnIndex(DownloadManager.COLUMN_URI)
        var status = 0
        if(cursor.moveToFirst()) {
            status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
        }
        var uri = ""
        if(cursor.moveToFirst()) {
            uri = cursor.getString(index)
        }
        return Pair(uri,status)
    }

    private fun createNotification(uri : String,status : Int,id : Int) {
        val contentIntent = Intent(applicationContext,DetailActivity::class.java).apply{
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        contentIntent.putExtra(DetailActivity.URI,uri)
        contentIntent.putExtra(DetailActivity.STATUS,status)
        val contentPendingIntent = PendingIntent.getActivity(this@MainActivity,id!!.toInt(),contentIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        val build = NotificationCompat.Builder(this@MainActivity, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(uri)
            .setContentText("Download completed")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(R.drawable.ic_launcher_foreground,"Check the status",contentPendingIntent)
        with(NotificationManagerCompat.from(this@MainActivity)) {
            notify(id,build.build())
        }
    }

    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID,"udacity",importance).apply {
                description = "Project nanodegree"
            }
            channel.enableLights(true)
            channel.enableVibration(true)
            notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun download() {
        val request =
            DownloadManager.Request(Uri.parse(URL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
        Log.d("MainActivity","$downloadID")

    }

    companion object {
        private var URL = ""
        private const val CHANNEL_ID = "channelId"
    }

}
