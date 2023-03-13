package com.example.budgeta

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgeta.Adpters.budgetItemAdapter
import com.example.budgeta.Models.BudgetItem
import com.example.budgeta.Repository.SqliteHelper
import com.example.budgeta.Utilities.Dialogs
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class BudgetItems : AppCompatActivity() {
    private lateinit var budgetName:String
    private lateinit var budgetItm:FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var titleText:TextView
    private lateinit var backBtn:ImageButton
    private lateinit var itemsAdapter:budgetItemAdapter
    private lateinit var itemFound:TextView
    private lateinit var db:SqliteHelper
    private var startDate:String?=null
    private var expiryDate:String?=null
    private var  budgetID:Int?=null
    private var  budgetStatus:Int?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_items)
        recyclerView=findViewById(R.id.budget_recy)
        titleText=findViewById(R.id.toolbar_title)
        backBtn=findViewById(R.id.back_btn)
        budgetItm=findViewById(R.id.add_budget_item)
        itemFound=findViewById(R.id.no_data)
        val budgetCategory=intent.getIntExtra("budgetCategory",0)
        budgetName=intent.getStringExtra("Name").toString()
        startDate=intent.getStringExtra("StartDate").toString()
        expiryDate=intent.getStringExtra("expiryDate").toString()
        budgetID=intent.getIntExtra("BudgetId",0)
        budgetStatus=intent.getIntExtra("Status",0)
        db= SqliteHelper(BudgetItems@this)
        val budgetDetails=db.getBudget(budgetID!!)
        val calender=Calendar.getInstance()
        val expiry=budgetDetails.ENDDATE.split("-")
        val sdf = SimpleDateFormat("MM/dd/yyyy")
        val exDateStr="${expiry[1]}/${expiry[2]}/${expiry[0]}"
        val todayDateStr="${calender.get(Calendar.MONTH)+1}/${calender.get(Calendar.DAY_OF_MONTH)}/${calender.get(Calendar.YEAR)}"
        if(sdf.parse(exDateStr).before(sdf.parse(todayDateStr))||sdf.parse(exDateStr).equals(sdf.parse(todayDateStr))){
            budgetItm.visibility=View.GONE
        }
        titleText.text=budgetName
        backBtn.setOnClickListener {
            if (budgetStatus==0){
                val intent= Intent(BudgetItems@this,Budgets::class.java)
                intent.putExtra("BGI",budgetCategory)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
            if (budgetStatus==1){
                val intent= Intent(BudgetItems@this,Notifications::class.java)
                startActivity(intent)
            }

        }
        budgetItm.setOnClickListener {
            val update=Intent(BudgetItems@this,AddUpdateBItem::class.java)
            update.putExtra("operationType",1)
            update.putExtra("budgetId",budgetID)
            startActivity(update)
        }
    }
    private fun listing(budgetID:Int){
        val items=db?.getBudgetItems(budgetID)
        if (items!!.size>0) {
            itemFound.visibility=View.GONE
            recyclerView.visibility=View.VISIBLE
            val credentials = getSharedPreferences("Credentials", Context.MODE_PRIVATE)
            val currencyType = credentials.getString("Currency", "None")
            itemsAdapter = budgetItemAdapter(items, BudgetItems@this, currencyType,startDate,expiryDate)
            recyclerView.layoutManager = LinearLayoutManager(BudgetItems@ this)
            recyclerView.adapter = itemsAdapter
            val helpPref = getSharedPreferences("Help", Context.MODE_PRIVATE)
            val helBuy=helpPref.getBoolean("Buy",false)
            if (helBuy==false){
                val alertBuild=AlertDialog.Builder(this)
                alertBuild.setTitle("Help on executing budget")
                alertBuild.setCancelable(false)
                alertBuild.setMessage("Long press on any budget item which has a red tick on it's top right or" +
                        " pressing on the red tick, you can change the status of the item to bought.\n That is if the item total cost is less than your cash balance on this app")
                alertBuild.setPositiveButton("Noted",{dialog,which->
                    val editHelp=helpPref.edit()
                    editHelp.apply{
                        putBoolean("Buy",true)
                        commit()
                    }
                    dialog.dismiss()
                })
                val alert=alertBuild.create()
                alert.show()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        listing(budgetID!!)
    }
}