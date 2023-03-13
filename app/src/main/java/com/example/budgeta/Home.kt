  package com.example.budgeta

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.*
import androidx.core.app.ActivityCompat
import com.example.budgeta.Repository.SqliteHelper
import kotlin.collections.ArrayList

  class Home : AppCompatActivity() {
    private lateinit var contempBudget:RelativeLayout
    private lateinit var weeklyBudget:RelativeLayout
    private lateinit var monthlyBudget:RelativeLayout
    private lateinit var annualBudget:RelativeLayout
    private lateinit var cashBudget:RelativeLayout
    private lateinit var debtsBudget:RelativeLayout
    private lateinit var contemptLs:TextView
    private lateinit var contempCount:TextView
    private lateinit var weeklyCash:TextView
    private lateinit var weeklyCount:TextView
    private lateinit var monthlyCash:TextView
    private lateinit var monthlyCount:TextView
    private lateinit var annualCash:TextView
    private lateinit var annualCount:TextView
    private lateinit var cashAnalytics:RelativeLayout
    private lateinit var creditAnalytics:RelativeLayout
    private lateinit var cashBalance:TextView
    private lateinit var credentials: SharedPreferences
    private lateinit var mainMenu:ImageButton
    private val channelId="channelid"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        contempBudget=findViewById(R.id.contemp_budget)
        weeklyBudget=findViewById(R.id.weekly_budget)
        monthlyBudget=findViewById(R.id.monthly_budget)
        annualBudget=findViewById(R.id.annual_budget)
        cashBudget=findViewById(R.id.cash_budget)
        debtsBudget=findViewById(R.id.debts_budget)
        contemptLs=findViewById(R.id.contemp_all)
        contempCount=findViewById(R.id.contemp_all_value)
        weeklyCash=findViewById(R.id.weekly_all)
        weeklyCount=findViewById(R.id.weekly_all_value)
        monthlyCash=findViewById(R.id.monthly_all)
        monthlyCount=findViewById(R.id.monthly_all_value)
        annualCash=findViewById(R.id.annually_all)
        annualCount=findViewById(R.id.annually_all_value)
        cashAnalytics=findViewById(R.id.cash_analytics)
        creditAnalytics=findViewById(R.id.credit_analytics)
        cashBalance=findViewById(R.id.cash_all_value)
        mainMenu=findViewById(R.id.home_menu)
        credentials = getSharedPreferences("Credentials", Context.MODE_PRIVATE)
        val toBudget= Intent(Home@this,Budgets::class.java)
       contempBudget.setOnClickListener {
           toBudget.putExtra("BGI",1)
           startActivity(toBudget)
       }
        weeklyBudget.setOnClickListener {
            toBudget.putExtra("BGI",2)
            startActivity(toBudget)
        }
        monthlyBudget.setOnClickListener {
            toBudget.putExtra("BGI",3)
            startActivity(toBudget)
        }
        annualBudget.setOnClickListener {
            toBudget.putExtra("BGI",4)
            startActivity(toBudget)
        }
        cashBudget.setOnClickListener {
             startActivity(Intent(Home@this,Cash::class.java))
        }
        debtsBudget.setOnClickListener {
            val intent=Intent(Home@this,Credits::class.java)
            intent.putExtra("Index",1)
            startActivity(intent)
        }
        cashAnalytics.setOnClickListener {
            startActivity(Intent(Home@this,AnalyticCash::class.java))
        }
        creditAnalytics.setOnClickListener {
            startActivity(Intent(Home@this,AnalyticCredit::class.java))
        }
        mainMenu.setOnClickListener {
            val popup=PopupMenu(Home@this,mainMenu)
            popup.inflate(R.menu.home_menu)
            popup.setOnMenuItemClickListener { item:MenuItem->
                when(item.itemId){
                    R.id.setting->{
                        val settings=Intent(Home@this,Setting::class.java)
                        settings.putExtra("Status",1)
                        startActivity(settings)
                    }
                    R.id.notifications->{
                        startActivity(Intent(Home@this,Notifications::class.java))
                    }
                    R.id.help->{
                        startActivity(Intent(Home@this,Help::class.java))
                    }
                    R.id.about->{
                        val aboutIntents=Intent(Home@this,Abouts::class.java)
                        aboutIntents.putExtra("Status",1)
                        startActivity(aboutIntents)
                    }
                    R.id.budget_arch->{
                        startActivity(Intent(Home@this,budget_archive::class.java))
                    }
                    R.id.credit_arc->{
                        startActivity(Intent(Home@this,creditArchive::class.java))
                    }
                }
             true
            }
            popup.show()
        }

        if(!checkPermissions()){
            requestPermission()
        }
    }
    override fun onResume() {
        super.onResume()
        DataView()

    }
    private fun  DataView() {
        val available = ArrayList<Int>()
        available.add(1)
        available.add(2)
        available.add(3)
        available.add(4)
        val currencyType = credentials.getString("Currency", "None")
        val db = SqliteHelper(Home@ this)
          if (credentials.getBoolean("setArchive",false)){
              db.archiveBudget(credentials.getInt("budgetArchiveId",1))
          }
        if(credentials.getBoolean("setCreditArchive",false)){
            db.archiveCredits(credentials.getInt("creditArchiveId",1))
        }
        val items=db.homeAnalytics()
        cashBalance.text="$currencyType ${db.lastCashTransaction().CTOTAL}"
        for (dt in items) {
            if (dt.count > 0) {
                when (dt.categ) {
                    1 -> {
                        contemptLs.text = "$currencyType ${dt.ttAmount }"
                        contempCount.text = "${dt.count}"
                        available.remove(1)
                    }
                    2 -> {
                        weeklyCash.text = "$currencyType ${dt.ttAmount}"
                        weeklyCount.text = "${dt.count}"
                        available.remove(2)
                    }
                    3 -> {
                        monthlyCash.text = "$currencyType ${dt.ttAmount}"
                        monthlyCount.text = "${dt.count}"
                        available.remove(3)
                    }
                    4 -> {
                        annualCash.text = "$currencyType ${dt.ttAmount}"
                        annualCount.text = "${dt.count}"
                        available.remove(4)
                    }
                }
            }

        }
        for (av in available) {
         when(av){
             1->{
                 contempCount.text = "Start"
             }
             2->{
                 weeklyCount.text = "Start"
             }
             3->{
                 monthlyCount.text = "Start"
             }
             4->{
                 annualCount.text = "Start"
             }
         }
        }

}

      private fun checkPermissions():Boolean{
          if (ActivityCompat.checkSelfPermission(this,
                  Manifest.permission.SEND_SMS)== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.CALL_PHONE) ==PackageManager.PERMISSION_GRANTED){
              return true
          }
          return false
      }
      private fun requestPermission() {
          ActivityCompat.requestPermissions(this, arrayOf(
              Manifest.permission.SEND_SMS,Manifest.permission.CALL_PHONE),
              100)

      }

      override fun onRequestPermissionsResult(
          requestCode: Int,
          permissions: Array<out String>,
          grantResults: IntArray
      ) {
          super.onRequestPermissionsResult(requestCode, permissions, grantResults)
          if (requestCode== 100){
              if(grantResults.isEmpty() || grantResults[0]!= PackageManager.PERMISSION_GRANTED){
                 // Toast.makeText(applicationContext,"Grant sending sms permission on settings for this application",Toast.LENGTH_LONG).show()
              }
          }
      }
    }

