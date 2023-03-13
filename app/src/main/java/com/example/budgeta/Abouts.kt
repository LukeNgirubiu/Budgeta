package com.example.budgeta

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView

class Abouts : AppCompatActivity() {
    private lateinit var backButton: ImageButton
    private lateinit var title: TextView
    private var Status:Int?=null
    private lateinit var btnNext:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_abouts)
        backButton=findViewById(R.id.back_btn)
        title=findViewById(R.id.toolbar_title)
        btnNext=findViewById(R.id.next)
        Status=intent.getIntExtra("Status",0)
        title.text="Budgeta Use Cases"
        if (Status==1){
            btnNext.visibility=View.GONE
        }
        if (Status==0){
            backButton.visibility=View.GONE
        }
        backButton.setOnClickListener {
            val intent= Intent(Abouts@this,Home::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
        btnNext.setOnClickListener {
            val settinIntent=Intent(Abouts@this,Setting::class.java)
            settinIntent.putExtra("Status",0)
            startActivity(settinIntent)
        }
    }
}