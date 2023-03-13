package com.example.budgeta

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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

class WithDraw : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var withdraw: Button
    private lateinit var withdrawLs: RecyclerView
    private lateinit var db:SqliteHelper
    private lateinit var recyclerAdapter:CashAdpter
    private lateinit var noData:RelativeLayout
    private lateinit var backButton: ImageButton
    private lateinit var title: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_with_draw)
        backButton=findViewById(R.id.back_btn)
        title=findViewById(R.id.toolbar_title)
        withdraw=findViewById(R.id.withdraw)
        withdrawLs=findViewById(R.id.withdraw_recycler)
        noData=findViewById(R.id.no_data)
        title.text="Cash withdrawals"
        db= SqliteHelper(WithDraw@this)
        lister()
        withdraw.setOnClickListener {
            val intent= Intent(WithDraw@this,DepositWithdraw::class.java)
            intent.putExtra("Operation",2)//For deposit 1, 2 for withdraw
            startActivity(intent)
        }
        backButton.setOnClickListener {
            val intent= Intent(WithDraw@this,com.example.budgeta.Cash::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }
    fun cashWithdrawals(categ:Int):ArrayList<Cash>{
        val cashes=ArrayList<Cash>()
        val allCash=db.getAllCash(categ)
        for (cash in allCash){
            if( cash.TINDEX==3){
                cashes.add(cash)
            }
        }
        return cashes
        //var contactslist: MutableList<contactsModel> = mutableListOf()
    }
    fun lister(){
        val credentials = getSharedPreferences("Credentials", Context.MODE_PRIVATE)
        val currencyName = credentials.getString("Currency", "None")
        if (cashWithdrawals(credentials.getInt("cashTransId",1)).size>0) {
            noData.visibility=View.GONE
            withdrawLs.visibility=View.VISIBLE
            recyclerAdapter = CashAdpter(cashWithdrawals(credentials.getInt("cashTransId",1)), WithDraw@ this, currencyName!!)
            withdrawLs.layoutManager = LinearLayoutManager(WithDraw@ this)
            withdrawLs.adapter = recyclerAdapter
        }
    }

}