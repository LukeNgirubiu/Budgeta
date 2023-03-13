package com.example.budgeta

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgeta.Adpters.budgetArchiveItemsAdapter
import com.example.budgeta.Adpters.budgetItemAdapter
import com.example.budgeta.Repository.SqliteHelper

class BudgetArchiveItems : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var titleText: TextView
    private lateinit var backBtn: ImageButton
    private lateinit var itemFound:TextView
    private var budgetId:Int?=null
    private lateinit var itemsAdapter:budgetArchiveItemsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_archive_items)
        backBtn=findViewById(R.id.back_btn)
        recyclerView=findViewById(R.id.budget_recy)
        titleText=findViewById(R.id.toolbar_title)
        itemFound=findViewById(R.id.no_data)
        budgetId=intent.getIntExtra("BudgetId",0)
        listing(budgetId!!)
        titleText.text=intent.getStringExtra("BudgetName")
        backBtn.setOnClickListener {
            startActivity(Intent(BudgetArchiveItems@this,budget_archive::class.java))
        }
    }
    private fun listing(budgetID:Int){
        val db= SqliteHelper(BudgetItems@this)
        val items=db?.getBudgetItems(budgetID)
        if (items!!.size>0) {
            itemFound.visibility= View.GONE
            recyclerView.visibility= View.VISIBLE
            itemsAdapter =budgetArchiveItemsAdapter(items, BudgetArchiveItems@this)
            recyclerView.layoutManager = LinearLayoutManager(BudgetArchiveItems@ this)
            recyclerView.adapter = itemsAdapter
        }
    }
}