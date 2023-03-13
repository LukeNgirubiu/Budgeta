package com.example.budgeta

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.budgeta.Repository.InterStorage
import com.example.budgeta.Repository.SqliteHelper
import com.example.budgeta.Utilities.DateFormating
import java.util.*

class MainActivity : AppCompatActivity() {
    private val channelId="channelid"
    private lateinit var credentials: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Handler().postDelayed({
            toHome()
        },2000)
    }
    private fun notificating(Title:String,message:String,id:Int,notifType:Int){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            val channel=
                NotificationChannel(channelId,"Reminder", NotificationManager.IMPORTANCE_HIGH).apply {
                lightColor= Color.BLUE
                enableLights(true)
            }
            val manager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        val nameCls=if (notifType==0) Notifications::class.java else Cash::class.java
        val intent = Intent(this, nameCls)
        val flagInt=if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.S){
            PendingIntent.FLAG_IMMUTABLE
        }
        else{
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, flagInt)
        val build= NotificationCompat.Builder(applicationContext,channelId).apply {
            setContentTitle(Title)
            setContentText(message)
            setSmallIcon(R.drawable.notify)
            setContentIntent(pendingIntent)
            setLargeIcon(BitmapFactory.decodeResource(resources,R.raw.logo))
            setAutoCancel(true)
        }
        val notifManage= NotificationManagerCompat.from(this)
        notifManage.notify(id,build.build())
    }
    private fun notification_utils(){
        val remind=credentials.getBoolean("Remind",false)
        val db=SqliteHelper(this)
        if(remind){
            val store= InterStorage(MainActivity@this)
            val df=db.reminder()
            val title="Reminders"
            val notices=store.provideData()
            if (notices.found==0){
                if (df.size>0){
                    store.storeReminder(df)
                    var message="${df.size} reminders for you"
                    notificating(title,message,1,0)
                }
            }
            if (notices.found==1){
                if (df.size>notices.items.size){
                    store.updateReminder(df)
                    val itms=store.provideData().items.filter { it.checked==false }.size
                    if (itms>0){
                        var message="$itms reminders for you"
                        notificating(title,message,1,0)
                    }
                }
                else{
                    val itms=notices.items.filter { it.checked==false }.size
                    if (itms>0){
                        var message="$itms reminders for you"
                        notificating(title,message,1,0)
                    }
                }
            }
        }
    }
    private fun monthlyLimit() {
        //credentials
        val montlyLt= if (credentials.getString("Limit","")!!.isBlank()) 0.0 else credentials.getString("Limit","")!!.toDouble()
        val remind=credentials.getBoolean("Remind",false)
        val calender= Calendar.getInstance()
        val formater= DateFormating()
        var date="${calender.get(Calendar.YEAR)}-${formater.numberFormating(calender.get(Calendar.MONTH)+1)}"
        val db=SqliteHelper(Home@this)
        val month=credentials.getString("Month","")
        val editing=credentials.edit()

        if(montlyLt>0.0 && montlyLt<=db.monthlyLimit()){
            val message="Spent ${credentials.getString("Currency", "None")}.${db.monthlyLimit()} so far monthly spending limit is ${credentials.getString("Currency", "None")}.$montlyLt"
            if (month!!.isBlank()){
                notificating("Monthly spending limit",message,2,1)
                editing.putString("Month",date)
                editing.commit()
            }
            if(!month.equals(date)){
                notificating("Monthly spending limit",message,2,1)
                editing.putString("Month",date)
                editing.commit()
            }

        }
    }
    private fun toHome(){
        credentials=getSharedPreferences("Credentials",Context.MODE_PRIVATE)
        val currencyName=credentials.getString("Currency","None")
        if (currencyName.equals("None")){
            val setts= Intent(MainActivity@this,Abouts::class.java)
            setts.putExtra("Status",0)
            startActivity(setts)
            finishAffinity()
        }
        else{
            notification_utils()
            monthlyLimit()
            startActivity(Intent(MainActivity@this,Home::class.java))
            finishAffinity()
        }
    }

}