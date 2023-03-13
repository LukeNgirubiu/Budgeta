package com.example.budgeta.Adpters
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.os.postDelayed
import androidx.recyclerview.widget.RecyclerView
import com.example.budgeta.AddUpdateBItem
import com.example.budgeta.BudgetItems
import com.example.budgeta.Models.Budget
import com.example.budgeta.Models.BudgetItem
import com.example.budgeta.Models.Cash
import com.example.budgeta.R
import com.example.budgeta.Repository.SqliteHelper
import com.example.budgeta.Utilities.DateFormating
import com.example.budgeta.Utilities.Dialogs
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Handler
import kotlin.collections.ArrayList

class budgetArchiveItemsAdapter(internal var budgetItems:ArrayList<BudgetItem>, internal var context: Context) :RecyclerView.Adapter<budgetArchiveItemsAdapter.budgetItem>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): budgetItem {
        val layout=LayoutInflater.from(this.context).inflate(R.layout.budget_item,parent,false)
        return budgetItem(layout)
    }

    override fun onBindViewHolder(holder: budgetItem, position: Int) {
        val dialog=Dialogs(this.context)
        holder.ticker.visibility=View.GONE
        holder.menuButton.visibility=View.GONE
        val budgetIt=budgetItems[position]
        holder.nameItem.text=budgetIt.NAME
        holder.unitCost.text="${budgetIt.CURRENCY} "+ dialog.roundToOne(budgetIt.UNITCOST).toString()
        holder.quantity.text=budgetIt.QUANTITY.toString()
        holder.Total.text=" ${budgetIt.CURRENCY}. "+budgetIt.TAMOUNT.toString()
        holder.unitQuantity.text=budgetIt.UNITNAME
    }
    override fun getItemCount(): Int {
        return budgetItems.size
    }
    inner class budgetItem(view: View):RecyclerView.ViewHolder(view) {
     val nameItem=view.findViewById<TextView>(R.id.budget_item_name)
     val unitCost=view.findViewById<TextView>(R.id.cost_unit)
     val quantity=view.findViewById<TextView>(R.id.quantity)
     val Total=view.findViewById<TextView>(R.id.total_amount)
     val menuButton=view.findViewById<Button>(R.id.options)
     val unitQuantity=view.findViewById<TextView>(R.id.quantity_unit)
     val ticker=view.findViewById<ImageButton>(R.id.ticker)
    }
}