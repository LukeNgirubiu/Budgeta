package com.example.budgeta.Adpters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.budgeta.CreditList
import com.example.budgeta.Models.Credit
import com.example.budgeta.R
import com.example.budgeta.Repository.InterStorage
import com.example.budgeta.Repository.SqliteHelper
import com.example.budgeta.Utilities.DateFormating
import com.example.budgeta.Utilities.Dialogs

class CreditsArchives(internal var context: Context, internal var credits:ArrayList<Credit>,internal var index:Int):RecyclerView.Adapter<CreditsArchives.CreditsArchivesView>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreditsArchivesView {
        val layout=
            LayoutInflater.from(this.context).inflate(R.layout.credit_adapter_view,parent,false)
        return CreditsArchivesView(layout)
    }

    override fun onBindViewHolder(holder: CreditsArchivesView, position: Int) {
        val credit=credits[position]
        val timing= DateFormating()
        holder.creditName.text=credit.NAME
        holder.creditAmount.text="${credit.CURRENCY} "+credit.AMOUNT.toString()
        holder.cellphoneNumber.text=credit.CELLPHONENO
        holder.dueDate.text="Expired on "+timing.toSlashedDate(credit.PAYMENTDATE)
        holder.creditBal.setTextColor(Color.rgb(61, 92, 92))
        if (credit.BALANCE>0.0){
            holder.creditBal.text= credit.CURRENCY+" ${credit.BALANCE} Unpaid"
        }
        if(credit.BALANCE==0.0){
            holder.creditBal.text= "Settled"
            holder.creditBal.setTextColor(Color.rgb(61, 92, 92))
        }
        holder.action.text="Record"
        holder.action.setBackgroundColor(Color.rgb(128, 128, 128))
        holder.action.setOnClickListener {
            val intent=Intent(context,CreditList::class.java)
            intent.putExtra("Id",credit.ID)
            intent.putExtra("Index",index)
            intent.putExtra("Status",2)
            context.startActivity(intent)
        }
        holder.itemBody.setOnLongClickListener {
            deleteCredit(credit,position)
            true
        }
    }

    override fun getItemCount(): Int {
        return credits.size
    }
    class CreditsArchivesView(view: View):RecyclerView.ViewHolder(view) {
        val creditName=view.findViewById<TextView>(R.id.name_cred)
        val creditAmount=view.findViewById<TextView>(R.id.total_amount)
        val creditBal=view.findViewById<TextView>(R.id.amount_due)
        val dueDate=view.findViewById<TextView>(R.id.due_date)
        val cellphoneNumber=view.findViewById<TextView>(R.id.cell_number)
        val action=view.findViewById<Button>(R.id.credit_action)
        val itemBody=view.findViewById<ConstraintLayout>(R.id.bodyItem)
    }
    private fun deleteCredit(credet:Credit,position:Int){
        val alertBuild= AlertDialog.Builder(this.context)
        alertBuild.setTitle("Deleting archive credit completely")
        alertBuild.setIcon(R.drawable.delete_items)
        alertBuild.setMessage(credet.NAME)
        alertBuild.setPositiveButton("Delete"){dialogueInterface,which->
            val repo= SqliteHelper(this.context)
            val dl= Dialogs(this.context)
            val del= repo.deleteCredit(index,credet.ID)
            val indexRemind=if (index==1) 2 else 1
            val notif= InterStorage(this.context)
            notif.upDateByColumn(credet.ID.toInt(),indexRemind)
            dialogueInterface.dismiss()
            if (del>0){
                val al=dl.statusDialog("Deleted ${credet.NAME} successfully",true)
                Handler().postDelayed({
                    al.cancel()
                    credits.removeAt(position)
                    notifyItemRemoved(position)
                },2000)
            }
            else
            {
                val al=dl.statusDialog("Deleting ${credet.NAME} failed",false)
                Handler().postDelayed({
                    al.cancel()
                },2000)
            }
        }
        alertBuild.setNegativeButton("Cancel"){dialogueInterface,which->
            dialogueInterface.dismiss()
        }
        alertBuild.create().show()

    }
}