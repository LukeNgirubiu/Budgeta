package com.example.budgeta.Adpters

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.os.postDelayed
import androidx.recyclerview.widget.RecyclerView
import com.example.budgeta.AddCredit
import com.example.budgeta.CreditList
import com.example.budgeta.Models.Budget
import com.example.budgeta.Models.Cash
import com.example.budgeta.Models.Credit
import com.example.budgeta.R
import com.example.budgeta.Repository.SqliteHelper
import com.example.budgeta.Utilities.DateFormating
import com.example.budgeta.Utilities.Dialogs
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CreditAdpter(internal var context:Context,internal var credits:ArrayList<Credit>,internal var currency:String,internal var Index:Int) :RecyclerView.Adapter<CreditAdpter.CreditView>() {
    private lateinit var msDialog:AlertDialog


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreditView {
        val layout=LayoutInflater.from(this.context).inflate(R.layout.credit_adapter_view,parent,false)
        return CreditView(layout)
    }

    override fun onBindViewHolder(holder: CreditView, position: Int) {
        val credit=credits[position]
        val timing=DateFormating()
        holder.creditName.text=credit.NAME
        holder.creditAmount.text="${credit.CURRENCY} "+credit.AMOUNT.toString()
        holder.cellphoneNumber.text=credit.CELLPHONENO
        holder.dueDate.text=timing.toSlashedDate(credit.PAYMENTDATE)
        if(credit.STARTED==2){
            holder.action.setBackgroundTintList(this.context.getResources().getColorStateList(R.color.budget_done))
        }
        if (credit.BALANCE>0.0){
            holder.creditBal.text= credit.CURRENCY+" ${credit.BALANCE} Unpaid"
        }
        if(credit.BALANCE==0.0){
            holder.creditBal.text= "Settled"
            holder.creditBal.setTextColor(Color.rgb(61, 92, 92))
        }
        holder.action.setOnClickListener {
            actions(holder.action,Index,credit,position,holder)
        }
    }

    override fun getItemCount(): Int {
        return credits.size
    }
    inner class CreditView(view: View): RecyclerView.ViewHolder(view) {
     val creditName=view.findViewById<TextView>(R.id.name_cred)
     val creditAmount=view.findViewById<TextView>(R.id.total_amount)
     val creditBal=view.findViewById<TextView>(R.id.amount_due)
     val dueDate=view.findViewById<TextView>(R.id.due_date)
     val cellphoneNumber=view.findViewById<TextView>(R.id.cell_number)
     val action=view.findViewById<Button>(R.id.credit_action)
    }
    private fun actions(action: Button?, index: Int, credit:Credit,position:Int,holder: CreditView) {
     val popup=PopupMenu(context,action)
     popup.inflate(R.menu.credit_actions)
     val updateOption=popup.menu.findItem(R.id.credit_update)
     val deleteOption=popup.menu.findItem(R.id.credit_delete)
     val archiveOption=popup.menu.findItem(R.id.credit_archive)
     val creditDateCh=popup.menu.findItem(R.id.credit_date)
     if (credit.STARTED==1){
         creditDateCh.setVisible(true)
     }
        if (credit.STARTED==0){
            updateOption.setVisible(true)
            deleteOption.setVisible(true)
        }
        if (credit.STARTED==2){
            archiveOption.setVisible(true)
        }

     popup.setOnMenuItemClickListener { item:MenuItem->
         val dialog=Dialogs(context)
         when(item.itemId){
             R.id.credit_update->{
                 val intent= Intent(this.context, AddCredit::class.java)
                 intent.putExtra("Id",credit.ID)
                 intent.putExtra("Index",index)
                 context.startActivity(intent)
             }
             R.id.repay->{
                 val intent= Intent(this.context, CreditList::class.java)
                 intent.putExtra("Id",credit.ID)
                 intent.putExtra("Index",index)
                 intent.putExtra("Status",0)
                 context.startActivity(intent)
             }
             R.id.credit_delete->{
                 creditCancel(credit,index,position)
             }
             R.id.call->{
                 dialog.callsSmsDialog(1,credit)
             }
             R.id.text->{
                 dialog.callsSmsDialog(2,credit,index)
             }
             R.id.credit_date->{
                 editingCreditDate(credit,index,this.context,holder)
             }
             R.id.credit_archive->{
                 credit.STARTED=3
                 val db= SqliteHelper(this.context)
                 val dialogs=Dialogs(this.context)
                 if(db.updateCashCredit(credit,index)>0){
                     credits.removeAt(position)
                     notifyItemRemoved(position);
                     val status=dialogs.statusDialog("${credit.NAME}.\n Archiving succeeded",true)//"Deleted ${budget.NAME} successfully",true
                     android.os.Handler().postDelayed({
                        status.cancel()
                     },2000)
                 }
                 else{
                     val status=dialogs.statusDialog("Failed to archive ${credit.NAME}",false)//"Deleted ${budget.NAME} successfully",true
                     android.os.Handler().postDelayed({
                         status.cancel()
                     },2000)
                 }

             }

         }
         true
     }
     popup.show()
    }


    fun creditCancel(credit: Credit,index: Int,position:Int){
        val dialogs=Dialogs(this.context)
        val db= SqliteHelper(this.context)
        val totalCash=db.lastCashTransaction()
        if (index==1){
            if (credit.AMOUNT>totalCash.CTOTAL){
                val status=dialogs.statusDialog("Cancellation of this transaction not possible",false)//"Deleted ${budget.NAME} successfully",true
                android.os.Handler().postDelayed({
                    status.cancel()
                },2000)
            }
         if (credit.AMOUNT<=totalCash.CTOTAL){
             val alertBuild=AlertDialog.Builder(this.context)
             alertBuild.setTitle("Payable transaction cancellation")
             alertBuild.setMessage(credit.NAME)
             alertBuild.setCancelable(false)
             alertBuild.setPositiveButton("Yes"){dialogueInterface,which->
                 val timings= DateFormating()
                 val cash= Cash()
                 val lastCash=db.lastCashTransaction()
                 cash.TDATE=timings.timeNow()
                 cash.AMOUNT=credit.AMOUNT
                 cash.TTYPE=1 //Debit zero and 1 credit
                 cash.TINDEX=8
                 cash.DETAILS="Transaction cancelled and deleted"
                 cash.CURRENCY=currency
                 cash.TID=credit.ID//Zero since can't be linked directly to any table
                 cash.CTOTAL=lastCash.CTOTAL-credit.AMOUNT

                 //
                 val upId=db.deleteCredit(index,credit.ID)
                 val cashId=db.cashOperations(cash)
                 if (upId>0&&cashId>0){
                     val status=dialogs.statusDialog("Transaction cancelled successfully",true)//"Deleted ${budget.NAME} successfully",true
                     android.os.Handler().postDelayed({
                         status.cancel()
                     },2000)
                     credits.removeAt(position)
                     notifyItemRemoved(position);
                 }
             }
             alertBuild.setNegativeButton("No"){dialogueInterface,which->
                 dialogueInterface.cancel()
             }
             alertBuild.create().show()
             return
         }
        }
        if (index==2){
            val alertBuild=AlertDialog.Builder(this.context)
            alertBuild.setTitle("Receivable transaction cancellation")
            alertBuild.setMessage(credit.NAME)
            alertBuild.setCancelable(false)
            alertBuild.setPositiveButton("Yes"){dialogueInterface,which->
                val timings= DateFormating()
                val cash= Cash()
                val lastCash=db.lastCashTransaction()
                cash.TDATE=timings.timeNow()
                cash.AMOUNT=credit.AMOUNT
                cash.TTYPE=0 //Debit zero and 1 credit
                cash.TINDEX=8
                cash.DETAILS="Transaction cancelling"
                cash.CURRENCY=currency
                cash.TID=credit.ID//Zero since can't be linked directly to any table
                cash.CTOTAL=lastCash.CTOTAL+credit.AMOUNT
                val upId=db.deleteCredit(index,credit.ID)
                val cashId=db.cashOperations(cash)
                if (upId>0&&cashId>0){
                    val status=dialogs.statusDialog("Transaction cancelled successfully",true)//"Deleted ${budget.NAME} successfully",true
                    android.os.Handler().postDelayed({
                        status.cancel()
                    },2000)
                    credits.removeAt(position)
                    notifyItemRemoved(position);
                }
            }
            alertBuild.setNegativeButton("No"){dialogueInterface,which->}
            alertBuild.create().show()

        }

    }
    private fun editingCreditDate(credit: Credit,index: Int,context: Context,holder: CreditView){
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
                        if (dateFm.toDashedDate(dateStr).equals(credit.PAYMENTDATE)){
                            Toast.makeText(context,"Date is the same",Toast.LENGTH_LONG).show()
                        }
                        else{
                            credit.PAYMENTDATE=dateFm.toDashedDate(dateStr)
                            val upDate=db.updateCashCredit(credit,index!!)
                            if(upDate>0){
                                val updateDate=credit.PAYMENTDATE.split("-")
                                holder.dueDate.text="${updateDate[2]}/${updateDate[1]}/${updateDate[0]}"
                                Toast.makeText(context,"Date updated successfully",Toast.LENGTH_LONG).show()
                            }
                            else{
                                Toast.makeText(context,"Date update failed",Toast.LENGTH_SHORT).show()
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