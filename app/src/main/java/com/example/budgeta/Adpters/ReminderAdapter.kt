package com.example.budgeta.Adpters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.budgeta.BudgetItems
import com.example.budgeta.CreditList
import com.example.budgeta.Models.Remind
import com.example.budgeta.R
import com.example.budgeta.Repository.InterStorage
import com.example.budgeta.Repository.SqliteHelper

class ReminderAdapter(internal val context: Context,internal val notices:ArrayList<Remind>):RecyclerView.Adapter<ReminderAdapter.Reminder>() {
   inner class Reminder(view:View):RecyclerView.ViewHolder(view) {
       val intro=view.findViewById<TextView>(R.id.intro)
       val title=view.findViewById<TextView>(R.id.title)
       val details=view.findViewById<TextView>(R.id.details)
       val body=view.findViewById<CardView>(R.id.body)
       val status=view.findViewById<TextView>(R.id.status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Reminder {
        val view=LayoutInflater.from(this.context).inflate(R.layout.reminder_layout,parent,false)
        return Reminder(view)
    }

    override fun onBindViewHolder(holder: Reminder, position: Int) {
        val notice=notices[position]
        holder.title.text=notice.name
        holder.details.text=notice.details
        val intrStr=when(notice.categ){
            0->"Budget expiring midnight today"
            1->"Debt is due midnight today"
            2->"Credit is due midnight today"
            else->""
        }
        if (notice.checked==true){
            holder.status.text="Seen"
            holder.status.setTextColor(ContextCompat.getColor(this.context,R.color.app_color))
        }
        if (notice.checked==false){
            holder.status.text="New"
            holder.status.setTextColor(ContextCompat.getColor(this.context,R.color.cash_color))
        }
        holder.intro.text=intrStr
        val sql=SqliteHelper(this.context)
        val budget=sql.getBudget(notice.ID)
        var intent=Intent(this.context,BudgetItems::class.java)
        holder.body.setOnClickListener{
            val store=InterStorage(this.context)
            val done=store.updateReminder(notices,position)
            if (done==1){
                when(notice.categ){
                    0->{
                        intent.putExtra("BudgetId",budget.ID)
                        intent.putExtra("Name",budget.NAME)
                        intent.putExtra("StartDate",budget.STARTDATE)
                        intent.putExtra("expiryDate",budget.ENDDATE)
                        intent.putExtra("budgetCategory",budget.CATEGORY)
                        intent.putExtra("Status",1)
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        context.startActivity(intent)
                    }
                    1->{
                        intent=Intent(this.context,CreditList::class.java)
                        intent.putExtra("Index",2)
                        intent.putExtra("Id",notice.ID.toLong())
                        intent.putExtra("Status",1)
                        context.startActivity(intent)
                    }
                    2->{
                        intent=Intent(this.context,CreditList::class.java)
                        intent.putExtra("Index",1)
                        intent.putExtra("Id",notice.ID.toLong())
                        intent.putExtra("Status",1)
                        context.startActivity(intent)
                    }
                }
            }
            }

        }
    override fun getItemCount(): Int =notices.size


}
