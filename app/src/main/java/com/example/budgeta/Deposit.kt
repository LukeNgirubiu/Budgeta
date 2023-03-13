package com.example.budgeta

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgeta.Adpters.CashAdpter
import com.example.budgeta.Models.Cash
import com.example.budgeta.Repository.SqliteHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton

class Deposit : AppCompatActivity() {
    private lateinit var backButton: ImageButton
    private lateinit var title: TextView
    private lateinit var deposit:FloatingActionButton
    private lateinit var depositLs:RecyclerView
    private lateinit var db: SqliteHelper
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var recyclerAdapter: CashAdpter
    private lateinit var noDataV:RelativeLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deposit)
        deposit=findViewById(R.id.deposit)
        depositLs=findViewById(R.id.deposit_recycler)
        noDataV=findViewById(R.id.no_data)
        backButton=findViewById(R.id.back_btn)
        title=findViewById(R.id.toolbar_title)
        sharedPrefs=getSharedPreferences("Credentials",Context.MODE_PRIVATE)
        title.text="Cash deposits"
        val context=Deposit@this
        db= SqliteHelper(context)
        depositLs(context)
        deposit.setOnClickListener {
          val intent= Intent(Deposit@this,DepositWithdraw::class.java)
           intent.putExtra("Operation",1)//For deposit 1, 2 for withdraw
           startActivity(intent)
        }
        backButton.setOnClickListener {
            val intent= Intent(Deposit@this,com.example.budgeta.Cash::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }
    fun depositLs(context: Context){
        if (cashDeposits().size>0){
            noDataV.visibility=View.GONE
            depositLs.visibility=View.VISIBLE
            val credentials=getSharedPreferences("Credentials", Context.MODE_PRIVATE)
            val currencyName=credentials.getString("Currency","None")
            recyclerAdapter= CashAdpter(cashDeposits(),context,currencyName!!)
            depositLs.layoutManager=LinearLayoutManager(context)
            depositLs.adapter=recyclerAdapter
        }
    }
    fun cashDeposits():ArrayList<Cash>{
        val cashes=ArrayList<Cash>()
        val allCash=db.getAllCash(sharedPrefs.getInt("cashTransId",1))
        for (cash in allCash){
            if( cash.TINDEX==2){
                cashes.add(cash)
            }
        }
        return cashes
    }

}