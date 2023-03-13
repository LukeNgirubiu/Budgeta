package com.example.budgeta

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgeta.Adpters.BudgetAdapters
import com.example.budgeta.Models.Budget
import com.example.budgeta.Repository.SqliteHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton


class Budgets : AppCompatActivity() {
    private lateinit var recyclerV:RecyclerView
    private  lateinit var addItem:FloatingActionButton
    private lateinit var budgetsAdapt:BudgetAdapters
    private lateinit var db:SqliteHelper
    private lateinit var backButton: ImageButton
    private lateinit var budgetNoData:TextView
    private lateinit var title:TextView
    private val Logs="Budgets"
    private var budgetType:Int?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budgets)
        recyclerV=findViewById(R.id.budgetRecycler)
        backButton=findViewById(R.id.back_btn)
        budgetType=intent.getIntExtra("BGI",0)
        addItem=findViewById(R.id.add_item)
        budgetNoData=findViewById(R.id.no_data)
        title=findViewById(R.id.toolbar_title)
        title.text="Budgets"
        val toAddBudg= Intent(Budgets@this,AddBudget::class.java)
        addItem.setOnClickListener {
            toAddBudg.putExtra("BID",budgetType)
            toAddBudg.putExtra("BCU",0)
            startActivity(toAddBudg)
        }
        backButton.setOnClickListener {
            val intent= Intent(Budgets@this,Home::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

    }
    override fun onResume() {
        super.onResume()
        listBudgets(budgetType!!)
    }


    fun listBudgets(budgetType:Int){
        val credentials=getSharedPreferences("Credentials", Context.MODE_PRIVATE)
        val currencyType=credentials.getString("Currency","None")
        db= SqliteHelper(Budgets@this)
        val budgets:ArrayList<Budget> = db.getBudgets(budgetType)
        if (budgets!!.size>0) {
            budgetNoData.visibility=View.GONE
            recyclerV.visibility=View.VISIBLE
            budgetsAdapt = BudgetAdapters(budgets,Budgets@ this, currencyType, filePath = filesDir.absolutePath)
            recyclerV.layoutManager = LinearLayoutManager(Budgets@ this)
            recyclerV.adapter = budgetsAdapt
        }
    }
}