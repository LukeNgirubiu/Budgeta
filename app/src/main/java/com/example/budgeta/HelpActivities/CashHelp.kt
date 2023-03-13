package com.example.budgeta.HelpActivities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.example.budgeta.Cash
import com.example.budgeta.Help

class CashHelp : AppCompatActivity() {
    private lateinit var backBtn: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.budgeta.R.layout.activity_cash_help)
        backBtn=findViewById(com.example.budgeta.R.id.back_btn)
        backBtn.setOnClickListener {
            if (intent.getStringExtra("From").equals("Cash")){
                val intents= Intent(applicationContext, Cash::class.java)
                startActivity(intents)
                finishAffinity()
            }
            else{
                val intents= Intent(applicationContext, Help::class.java)
                startActivity(intents)
            }
        }
    }
}