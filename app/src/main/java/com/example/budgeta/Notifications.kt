package com.example.budgeta

import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgeta.Adpters.ReminderAdapter
import com.example.budgeta.Adpters.TransAdapter
import com.example.budgeta.Models.RemindStatus
import com.example.budgeta.Repository.InterStorage

class Notifications : AppCompatActivity() {
    private lateinit var recycler:RecyclerView
    private lateinit var adapt: ReminderAdapter
    private lateinit var backButton: ImageButton
    private lateinit var title: TextView
    private lateinit var noData:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)
        backButton=findViewById(R.id.back_btn)
        title=findViewById(R.id.toolbar_title)
        noData=findViewById(R.id.no_data)
        recycler=findViewById(R.id.recyler)
        title.text="Reminders"
        backButton.setOnClickListener {
            val intent= Intent(Notifications@this, Home::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val store= InterStorage(Notifications@this)
        val notices=store.provideData()
        if(notices.items.size>0){
            recycler.visibility=View.VISIBLE
            adapt=ReminderAdapter(Notifications@this,notices.items)
            recycler.layoutManager= LinearLayoutManager(Notifications@this)
            recycler.adapter=adapt
        }
        if(notices.items.size==0){
            noData.visibility=View.VISIBLE
            noData.text="No reminders currently"

        }


    }
}
//        setSupportActionBar(toolbar)
//        supportActionBar?.title="Reminders"