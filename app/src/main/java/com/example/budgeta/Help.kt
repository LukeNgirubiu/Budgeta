package com.example.budgeta

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.PopupMenu
import com.example.budgeta.HelpActivities.*

class Help : AppCompatActivity() {
    private lateinit var ImageButton:ImageButton
    private lateinit var BackButton:ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        ImageButton=findViewById(R.id.menu)
        BackButton=findViewById(R.id.back_btn)
        ImageButton.setOnClickListener {
            val menu=PopupMenu(Help@this,ImageButton)
            menu.inflate(R.menu.help_menu)
            menu.setOnMenuItemClickListener { item:MenuItem->
                when(item.itemId){
                    R.id.budgets->{
                        startActivity(Intent(Help@this,BudgetHelp::class.java))
                    }
                    R.id.credits->{
                        startActivity(Intent(Help@this,CreditHelp::class.java))
                    }
                    R.id.Cash->{
                        val toCash=Intent(Help@this,CashHelp::class.java)
                        toCash.putExtra("From","Help")
                        startActivity(toCash)
                    }
                    R.id.graphs->{
                        startActivity(Intent(Help@this,GraphHelp::class.java))
                    }

                }
                true
            }
            menu.show()
        }
      BackButton.setOnClickListener {
          startActivity(Intent(Help@this,Home::class.java))
          finishAffinity()
      }
    }
}