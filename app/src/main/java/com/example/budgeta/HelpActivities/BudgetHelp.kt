package com.example.budgeta.HelpActivities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class BudgetHelp : AppCompatActivity() {
    private lateinit var backBtn:ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.budgeta.R.layout.activity_budget_help)
        backBtn=findViewById(com.example.budgeta.R.id.back_btn)
        backBtn.setOnClickListener {
            val intents=Intent(applicationContext,com.example.budgeta.Help::class.java)
            startActivity(intents)
        }
    }
}