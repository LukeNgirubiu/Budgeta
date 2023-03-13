package com.example.budgeta.Adpters
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.budgeta.BudgetArchiveItems
import com.example.budgeta.Models.Budget
import com.example.budgeta.R
import com.example.budgeta.Repository.InterStorage
import com.example.budgeta.Repository.SqliteHelper
import com.example.budgeta.Utilities.Dialogs
import kotlin.collections.ArrayList
class BudgetArchives(internal var budgets:ArrayList<Budget>, internal var context: Context):RecyclerView.Adapter<BudgetArchives.BudgetView>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetView {
        val layout= LayoutInflater.from(context).inflate(R.layout.budget_view_adpter,parent,false)
        return BudgetView(layout)
    }

    override fun onBindViewHolder(holder: BudgetView, position: Int) {
        val budget=budgets[position]
        val expiry=budget.ENDDATE.split("-")
        holder.budgetName.text=budget.NAME
        holder.budgetDetails.text=budget.DETAILS
        holder.expiryDate.setTextColor(this.context.getResources().getColorStateList(R.color.archived_budget))
        holder.expiryDate.text="Expired ${expiry[2]}/${expiry[1]}/${expiry[0]}"
        holder.budgetTotalValue.text="${budget.CURRENCY}. "+budget.T_EST.toString()
        holder.budgetItems.text="Items: "+budget.ITEMS.toString()
        holder.budgetAction.text="Items"
        holder.budgetAction.setBackgroundColor(Color.rgb(128, 128, 128))
        holder.budgetBody.setOnLongClickListener{
            dialogDelete(budget,position)
            true
        }
        holder.budgetAction.setOnClickListener {
            val intent= Intent(context,BudgetArchiveItems::class.java)
            intent.putExtra("BudgetId",budget.ID)
            intent.putExtra("BudgetName",budget.NAME)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return budgets.size
    }
    class BudgetView(view: View) :RecyclerView.ViewHolder(view)  {
        var budgetName=view.findViewById<TextView>(R.id.budget_name)
        var budgetDetails=view.findViewById<TextView>(R.id.budgetsDetail)
        var budgetTotalValue=view.findViewById<TextView>(R.id.budget_value)
        var budgetItems=view.findViewById<TextView>(R.id.budget_unique_item)
        var budgetAction=view.findViewById<Button>(R.id.budget_action)
        var expiryDate=view.findViewById<TextView>(R.id.budget_expiry)
        var budgetBody=view.findViewById<CardView>(R.id.budget_body)
    }
    private fun dialogDelete(budget:Budget,position:Int){
        val alertBuild= AlertDialog.Builder(this.context)
        alertBuild.setTitle("Delete a budget")
        alertBuild.setIcon(R.drawable.delete_items)
        alertBuild.setMessage(budget.NAME)
        alertBuild.setPositiveButton("Delete"){dialogueInterface,which->
            val repo= SqliteHelper(this.context)
            val dl= Dialogs(this.context)
            val del=repo.deleteBudget("ID=?",budget.ID.toString())
            val notif=InterStorage(this.context)
            notif.upDateByColumn(budget.ID,0)
            dialogueInterface.dismiss()
            if (del){

                val al=dl.statusDialog("Deleted ${budget.NAME} successfully",true)
                Handler().postDelayed({
                    al.cancel()
                    budgets.removeAt(position)
                    notifyItemRemoved(position)
                },2000)
            }
            else
            {
                val al=dl.statusDialog("Deleting ${budget.NAME} failed",false)
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