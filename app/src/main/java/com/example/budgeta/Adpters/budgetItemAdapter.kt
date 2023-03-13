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

class budgetItemAdapter( internal var budgetItems:ArrayList<BudgetItem>, internal var context: Context,internal var currency:String?,internal var startDate:String?,internal var expiryDate:String?) :RecyclerView.Adapter<budgetItemAdapter.budgetItem>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): budgetItem {
        val layout=LayoutInflater.from(this.context).inflate(R.layout.budget_item,parent,false)
        return budgetItem(layout)
    }

    override fun onBindViewHolder(holder: budgetItem, position: Int) {
        val budgetIt=budgetItems[position]
        val dialog=Dialogs(this.context)
        val dateFormat=DateFormating()
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val calender= Calendar.getInstance()
        val today="${calender.get(Calendar.YEAR)}-${dateFormat.numberFormating(calender.get(Calendar.MONTH)+1)}-${dateFormat.numberFormating(calender.get(Calendar.DAY_OF_MONTH))}"
        //sdf.parse(startDate).equals(sdf.parse(today))||) sdf.parse(today).before(sdf.parse(startDate))+
        if( sdf.parse(today).after(sdf.parse(expiryDate))||sdf.parse(today).equals(sdf.parse(expiryDate))||sdf.parse(today).before(sdf.parse(startDate))){
            holder.ticker.isEnabled=false
        }
        if (budgetIt.TICKED==0){
            holder.ticker.setImageResource(R.drawable.tick_yes)
            holder.ticker.isEnabled=true
           holder.menuButton.visibility=View.VISIBLE
        }
        if (budgetIt.TICKED==1){
            holder.ticker.setImageResource(R.drawable.ticked_yes)
            holder.ticker.isEnabled=false
            holder.menuButton.visibility=View.GONE
        }
        holder.ticker.setOnClickListener {
         val db= SqliteHelper(this.context)
          val availableCash=db.lastCashTransaction()
          if (availableCash.CTOTAL>=budgetIt.TAMOUNT){
              allowSpending(availableCash.CTOTAL,db,budgetIt,true)
          }
            else{
              allowSpending(availableCash.CTOTAL,db,budgetIt,false)
            }
        }
        holder.menuButton.setOnClickListener {
            budgetItemPopup(2,holder.menuButton,budgetIt,position)
        }
        holder.itemBody.setOnLongClickListener{
            if (budgetIt.TICKED!=1){
                val db= SqliteHelper(this.context)
                val availableCash=db.lastCashTransaction()
                if (availableCash.CTOTAL>=budgetIt.TAMOUNT){
                    allowSpending(availableCash.CTOTAL,db,budgetIt,true)
                }
                else{
                    allowSpending(availableCash.CTOTAL,db,budgetIt,false)
                }
            }
            true
        }
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
     val itemBody=view.findViewById<CardView>(R.id.item_body)
    }
    private fun budgetItemPopup(operationType:Int,button:Button,budgetItem:BudgetItem,position:Int){
     val popUp=PopupMenu(this.context,button)
     popUp.inflate(R.menu.budget_item_actions)
     popUp.setOnMenuItemClickListener { item: MenuItem->
         when(item.itemId){
             R.id.item_update->{
                 val update=Intent(this.context,AddUpdateBItem::class.java)
                 update.putExtra("operationType",2)
                 update.putExtra("budgetItemId",budgetItem.ID)
                 update.putExtra("budgetId",budgetItem.BUDGETID)
                 this.context.startActivity(update)
             }
             R.id.item_delete->{
             val alertBuild=AlertDialog.Builder(this.context)
             alertBuild.setTitle("Delete a budget item")
             alertBuild.setMessage("${budgetItem.NAME}")
             alertBuild.setIcon(R.drawable.delete_items)
             alertBuild.setCancelable(false)
             alertBuild.setPositiveButton("Okay"){dialogInterface,which->
                 val db=SqliteHelper(this.context)
                 val dialogs=Dialogs(this.context)
                 dialogInterface.cancel()
                 val deleted= db.dropBudgetItem("ID=?",budgetItem.ID.toString())
                 if (deleted==true){
                     val budg=db.getBudget(budgetItem.BUDGETID)
                     budg.ITEMS=budg.ITEMS-1
                     if (budg.ITEMS==0) {
                        budg.STARTED=0
                     }
                     budg.T_EST=budg.T_EST-budgetItem.TAMOUNT
                     val budgetUpdated=db.updateBudget(budg)
                     if (budgetUpdated>0){
                         val dl=dialogs.statusDialog("${budgetItem.NAME} deleted successfully",true)
                         android.os.Handler().postDelayed({
                             dl.cancel()
                             budgetItems.removeAt(position)
                             notifyItemRemoved(position)

                         },2000)
                     }
                     else{
                         val dl=dialogs.statusDialog("Failed to delete ${budgetItem.NAME}",false)
                         android.os.Handler().postDelayed({
                             dl.cancel()
                         },2000)
                     }
                 }
                 if (deleted==false){
                     val dl=dialogs.statusDialog("Deletion failed",false)
                     dl.cancel()
                 }
             }
            alertBuild.setNegativeButton("Cancel"){dialogInterface,which->
                     dialogInterface.cancel()
                 }
                 alertBuild.create().show()
             }
         }
         true
     }
     popUp.show()
    }



    private fun allowSpending(availableCash:Double,db:SqliteHelper,item:BudgetItem,status:Boolean){
    val title=if (status==true) "Spending " else "Failed"
    val message=if (status==true) "Spend $currency. ${item.TAMOUNT} on ${item.NAME}" else "Insufficient cash \n Top up first"
    val alertBuild=AlertDialog.Builder(this.context)
    alertBuild.setTitle(title)
    alertBuild.setMessage(message)
    alertBuild.setCancelable(false)
    alertBuild.setPositiveButton("Okay"){dialogInterface,which->
        val timing=DateFormating()
        val today=timing.timeNow()
       if (status==true){
           item.TICKED=1
           val updateItem=db.budgetItemUpdate(item)
           val budgetDetails=db.getBudget(item.BUDGETID)
           val tickedItem=db.checkItemsTicked(item.BUDGETID.toLong())
           if (budgetDetails.ITEMS==tickedItem){
               budgetDetails.STARTED=2
           }
           if (budgetDetails.ITEMS>tickedItem){
               budgetDetails.STARTED=1
           }
           db.updateBudget(budgetDetails)
           val cash=Cash()
           cash.TDATE=today
           cash.DETAILS="Paid for ${item.NAME} on ${budgetDetails.NAME}"
           cash.TID=updateItem
           cash.TINDEX=1
           cash.TTYPE=1
           cash.CURRENCY=currency!!
           cash.AMOUNT=item.TAMOUNT
           cash.CTOTAL=availableCash-item.TAMOUNT
           val id=db.cashOperations(cash)
          if (id>1){
              dialogInterface.cancel()
              val dialog=Dialogs(this.context)
              val  intent=Intent(this.context, BudgetItems::class.java)
              val alert=dialog.statusDialog("Updated successful",true)
              android.os.Handler().postDelayed({
                  alert.cancel()
                  intent.putExtra("BudgetId",budgetDetails.ID)
                  intent.putExtra("Name",budgetDetails.NAME)
                  intent.putExtra("StartDate",budgetDetails.STARTDATE)
                  intent.putExtra("expiryDate",budgetDetails.ENDDATE)
                  intent.putExtra("budgetCategory",budgetDetails.CATEGORY)
                  intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                  context.startActivity(intent)
                                               },2000)
          }
        }
       if (status==false){
           dialogInterface.cancel()
//           val dialog=Dialogs(this.context)
//           val alert=dialog.statusDialog("Updating failed",false)
//           android.os.Handler().postDelayed({alert.cancel()},2000)
       }
    }
    alertBuild.setNegativeButton("Cancel"){dialogInterface,which->
        dialogInterface.cancel()
    }
    alertBuild.create().show()
}
    private fun restartActivity(intent: Intent, itemName: String, budg: Budget):Intent {
        intent.putExtra("BudgetId",budg.ID)
        intent.putExtra("Name",itemName)
        intent.putExtra("StartDate",budg.STARTDATE)
        intent.putExtra("expiryDate",budg.ENDDATE)
        intent.putExtra("budgetCategory",budg.CATEGORY)
        return intent
    }
}