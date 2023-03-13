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
import com.example.budgeta.Adpters.BudgetAdapters
import com.example.budgeta.Adpters.BudgetArchives
import com.example.budgeta.Models.Budget
import com.example.budgeta.Repository.SqliteHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton

class budget_archive : AppCompatActivity() {
    private lateinit var backButton: ImageButton
    private lateinit var recyclerV: RecyclerView
    private lateinit var budgetsAdapt:BudgetArchives
    private lateinit var budgetNoData: TextView
    private lateinit var title:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_archive)
        recyclerV=findViewById(R.id.budgetRecycler)
        backButton=findViewById(R.id.back_btn)
        budgetNoData=findViewById(R.id.no_data)
        title=findViewById(R.id.toolbar_title)
        title.text="Budget Archive"
        backButton.setOnClickListener {
            val intent=Intent(this,Home::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
           startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        listBudgets()
    }
    fun listBudgets(){
        val  db= SqliteHelper(Budgets@this)
        val budgets:ArrayList<Budget> = db.getBudgets()
        if (budgets!!.size>0) {
            budgetNoData.visibility= View.GONE
            recyclerV.visibility= View.VISIBLE
            budgetsAdapt = BudgetArchives(budgets,Budgets@ this)
            recyclerV.layoutManager = LinearLayoutManager(Budgets@ this)
            recyclerV.adapter = budgetsAdapt
        }
    }
}