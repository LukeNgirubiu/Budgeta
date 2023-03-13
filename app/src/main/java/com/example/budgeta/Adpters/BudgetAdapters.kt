package com.example.budgeta.Adpters

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.budgeta.AddBudget
import com.example.budgeta.BudgetItems
import com.example.budgeta.Budgets
import com.example.budgeta.Models.Budget
import com.example.budgeta.R
import com.example.budgeta.Repository.InterStorage
import com.example.budgeta.Repository.SqliteHelper
import com.example.budgeta.Utilities.DateFormating
import com.example.budgeta.Utilities.Dialogs
import com.example.budgeta.Utilities.pdfCreater
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class BudgetAdapters(internal var budgets:ArrayList<Budget>,internal var context: Context,internal var currency:String?,internal var filePath:String=""):RecyclerView.Adapter<BudgetAdapters.BudgetView> (){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetView {
        val layout=LayoutInflater.from(context).inflate(R.layout.budget_view_adpter,parent,false)
        return BudgetView(layout)
    }

    override fun onBindViewHolder(holder: BudgetView, position: Int) {
        val budget=budgets[position]
        val expiry=budget.ENDDATE.split("-")
        holder.budgetName.text=budget.NAME
        holder.budgetDetails.text=budget.DETAILS
        holder.expiryDate.text="Expiry ${expiry[2]}/${expiry[1]}/${expiry[0]}"
        holder.budgetTotalValue.text="${budget.CURRENCY}. "+budget.T_EST.toString()
        holder.budgetItems.text="Items: "+budget.ITEMS.toString()
        holder.budgetAction= colorButton(budget.STARTED,holder.budgetAction,budget.ENDDATE)
        holder.budgetAction.setOnClickListener {
            popMenu(context,holder,budget,position)
        }
        holder.budgetBody.setOnLongClickListener{
            showItems(budget)
            true
        }
    }

    override fun getItemCount(): Int {
     return budgets.size
    }
   inner class BudgetView(view:View) :RecyclerView.ViewHolder(view) {
       var budgetName=view.findViewById<TextView>(R.id.budget_name)
       var budgetDetails=view.findViewById<TextView>(R.id.budgetsDetail)
       var budgetTotalValue=view.findViewById<TextView>(R.id.budget_value)
       var budgetItems=view.findViewById<TextView>(R.id.budget_unique_item)
       var budgetAction=view.findViewById<Button>(R.id.budget_action)
       var expiryDate=view.findViewById<TextView>(R.id.budget_expiry)
       var budgetBody=view.findViewById<CardView>(R.id.budget_body)

    }
    private fun popMenu(context:Context,holder: BudgetView,budget:Budget,position:Int){
        val popUp=PopupMenu(context,holder.budgetAction)
        popUp.inflate(R.menu.budget_pop_action)
        val updateItem=popUp.menu.findItem(R.id.budget_update)
        val deleteBudget=popUp.menu.findItem(R.id.delete_budget)
        val archive=popUp.menu.findItem(R.id.archive_budget)
        val extendDate=popUp.menu.findItem(R.id.budget_date)
        val pdfGenerate=popUp.menu.findItem(R.id.pdfGenerate)
        val sharePdf=popUp.menu.findItem(R.id.sharePdf)
        if (budget.ITEMS>0){
            pdfGenerate.setVisible(true)
            sharePdf.setVisible(true)
        }
      when(budget.STARTED){
        0->{
            deleteBudget.setVisible(true)
            updateItem.setVisible(true)
        }
        1->{
            if (budget.CATEGORY==1){
                extendDate.setVisible(true)
            }
          }
        2->{
            archive.setVisible(true)
            if (budget.CATEGORY==1){
                extendDate.setVisible(true)
            }
        }
      }
        popUp.setOnMenuItemClickListener { item: MenuItem->
            var intent:Intent?=null
            val repo=SqliteHelper(this.context)
            val dl= Dialogs(this.context)
                    when(item!!.itemId){
               R.id.budget_items->{
                   showItems(budget)
               }
               R.id.budget_update->{
                   intent=Intent(context,AddBudget::class.java)
                   intent.putExtra("BID",budget.CATEGORY)
                   intent.putExtra("BCU",budget.ID)
                   context.startActivity(intent)
               }
              R.id.delete_budget->{
                  val alertBuild=AlertDialog.Builder(this.context)
                  alertBuild.setTitle("Delete a budget")
                  alertBuild.setIcon(R.drawable.delete_items)
                  alertBuild.setMessage(budget.NAME)
                  alertBuild.setPositiveButton("Delete"){dialogueInterface,which->
                     val del=repo.deleteBudget("ID=?",budget.ID.toString())
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
             R.id.archive_budget->{
                 budget.STARTED=3
                 val idUp=repo.updateBudget(budget)
                 if (idUp>0){
                     val al=dl.statusDialog("Archived ${budget.NAME} successfully",true)
                     Handler().postDelayed({
                         al.cancel()
                         budgets.removeAt(position)
                         notifyItemRemoved(position)
                     },2000)
                 }
                 else{
                     val al=dl.statusDialog("Archiving ${budget.NAME} failed",false)
                     Handler().postDelayed({
                         al.cancel()
                     },2000)
                 }
                        }
              R.id.budget_date->{
                  editingCreditDate(budget,this.context,holder)
              }
             R.id.pdfGenerate->{
                 val pdf=pdfCreater(context)
                 pdf.createTable(budget, filePath, currency!!,1)
             }
            R.id.sharePdf->{
                dl.pdfDialogList(budget, filePath, currency!!)
            }

           }
         true
        }
        popUp.show()
    }
    private fun colorButton(status:Int,button:Button,ExpiryDate:String):Button {
        val dateFormat= DateFormating()
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val calender= Calendar.getInstance()
        val today="${calender.get(Calendar.YEAR)}-${dateFormat.numberFormating(calender.get(Calendar.MONTH)+1)}-${dateFormat.numberFormating(calender.get(
            Calendar.DAY_OF_MONTH))}"
        if(sdf.parse(today).after(sdf.parse(ExpiryDate))||sdf.parse(today).equals(sdf.parse(ExpiryDate))){
            button.setBackgroundTintList(this.context.getResources().getColorStateList(R.color.budget_expired))
        }
        if(sdf.parse(today).before(sdf.parse(ExpiryDate))){
            if (status==1){
                button.setBackgroundTintList(this.context.getResources().getColorStateList(R.color.on_going))
            }
            if (status==2){
                button.setBackgroundTintList(this.context.getResources().getColorStateList(R.color.budget_done))
            }
        }
        return button
    }
    private fun showItems(budget:Budget){
        val intent= Intent(context,BudgetItems::class.java)
        intent.putExtra("BudgetId",budget.ID)
        intent.putExtra("Name",budget.NAME)
        intent.putExtra("StartDate",budget.STARTDATE)
        intent.putExtra("expiryDate",budget.ENDDATE)
        intent.putExtra("budgetCategory",budget.CATEGORY)
        intent.putExtra("Status",0)
        context.startActivity(intent)
    }
    private fun editingCreditDate(budget:Budget,context: Context,holder: BudgetView){
        val db= SqliteHelper(this.context)
        val dateFm=DateFormating()
        var dateStr=""
        val calender= Calendar.getInstance()
        val datePick= DatePickerDialog(this.context, object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                dateStr=String.format("%d/%d/%d", dayOfMonth, month + 1, year)
                if (dateStr.length>1){
                    val calender= Calendar.getInstance()
                    val sdf = SimpleDateFormat("MM/dd/yyyy")
                    val dateBr=dateStr.split("/")
                    val stDateStr="${dateBr!![1].trim().toInt()}/${dateBr!![0].trim().toInt()}/${dateBr!![2].trim()}"
                    val todayStr="${calender.get(Calendar.MONTH)+1}/${calender.get(Calendar.DAY_OF_MONTH)}/${calender.get(
                        Calendar.YEAR)}"
                    val todayDate=sdf.parse(todayStr)
                    val choiceDate=sdf.parse(stDateStr)
                    if(choiceDate.after(todayDate)){

                        if (dateFm.toDashedDate(dateStr).equals( budget.ENDDATE)){
                            Toast.makeText(context,"Budget date is the same",Toast.LENGTH_LONG).show()
                        }
                        else{
                            budget.ENDDATE=dateFm.toDashedDate(dateStr)
                            val upDate=db.updateBudget(budget)
                            if(upDate>0){
                                val updateDate=budget.ENDDATE.split("-")
                                holder.expiryDate.text="${updateDate[2]}/${updateDate[1]}/${updateDate[0]}"
                                Toast.makeText(context,"Budget date updated successfully",Toast.LENGTH_LONG).show()
                            }
                            else{
                                Toast.makeText(context,"Budget date update failed",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    else{
                        Toast.makeText(context,"Date chosen must be later than today",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }, calender.get(Calendar.YEAR), calender.get(Calendar.MONTH), calender.get(Calendar.DAY_OF_MONTH));
        datePick.show()
    }

}
