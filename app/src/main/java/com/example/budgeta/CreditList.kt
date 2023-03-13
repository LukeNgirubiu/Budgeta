package com.example.budgeta
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgeta.Adpters.TransAdapter
import com.example.budgeta.Models.Account
import com.example.budgeta.Models.Cash
import com.example.budgeta.Models.Credit
import com.example.budgeta.Models.Pay
import com.example.budgeta.Repository.SqliteHelper
import com.example.budgeta.Utilities.DateFormating
import com.example.budgeta.Utilities.Dialogs
import java.util.*
import kotlin.collections.ArrayList

class CreditList : AppCompatActivity() {
    private lateinit var backBtn:ImageButton
    private lateinit var title:TextView
    private lateinit var amountForm:LinearLayout
    private lateinit var amount:EditText
    private lateinit var save:Button
    private lateinit var creditLister:RecyclerView
    private lateinit var db:SqliteHelper
    private var Index:Int?=null
    private var creditId:Long?=null
    private var creditStatus:Int?=null
    private lateinit var ledgerData:ArrayList<Pay>
    private lateinit var ledgerAdpter:TransAdapter
    private lateinit var currencyName:String
    private lateinit var credit:Credit
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credit_list)
        backBtn=findViewById(R.id.back_btn)
        title=findViewById(R.id.toolbar_title)
        amountForm=findViewById(R.id.comp)
        amount=findViewById(R.id.credit_amount)
        save=findViewById(R.id.submit)
        creditLister=findViewById(R.id.item_ls)
        Index=intent.getIntExtra("Index",0)
        creditId=intent.getLongExtra("Id",0)
        creditStatus=intent.getIntExtra("Status",0)
        val credentials=getSharedPreferences("Credentials", Context.MODE_PRIVATE)
        currencyName=credentials.getString("Currency","None").toString()
        db= SqliteHelper(CreditList@this)
        credit=db.getCreditRecord(creditId!!,Index!!)
        itemsLister()
        if (credit.STARTED>=2||creditStatus==2){
            amountForm.visibility=View.GONE
        }
       backBtn.setOnClickListener {
           if (creditStatus==0){
               val intent= Intent(CreditList@this,Credits::class.java)
               intent.putExtra("Index",Index)
               intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
               startActivity(intent)
           }
           if (creditStatus==1){
               val intent= Intent(CreditList@this,Notifications::class.java)
               startActivity(intent)
           }
           if (creditStatus==2){
               val intent= Intent(CreditList@this,creditArchive::class.java)
               startActivity(intent)
           }

       }
        title.text=credit.NAME
        save.setOnClickListener {
            if(amount.text.isEmpty()){
                Toast.makeText(CreditList@this,"Amount field is empty",Toast.LENGTH_SHORT).show()
            }
            if(!amount.text.isEmpty()){
                process(credit)
            }
        }
    }
    private fun itemsLister(){
        ledgerData=db.getAllSettling(creditId!!,Index!!)
        creditLister.layoutManager=LinearLayoutManager(CreditList@this)
        creditLister.addItemDecoration(DividerItemDecoration(
            CreditList@this,
            LinearLayoutManager.VERTICAL
        ))
        ledgerAdpter=TransAdapter(CreditList@this,ledgerData,currencyName,credit.AMOUNT)
        creditLister.adapter=ledgerAdpter
    }

    private fun process(credit:Credit){
        val inputAmount=amount.text.toString().toDouble()+0.0
        val dialog=Dialogs(CreditList@this)
        val cash=db.lastCashTransaction()
        val timing=DateFormating()
        val todayStr=timing.timeNow()
        //credit.STARTED 0 for payment not started yet and 1 for ongoing payment
            //Index 0 for payables and 1 for receivables
        if (credit.STARTED==0) {
                if (Index==1){
                    if(cash.CTOTAL>=inputAmount){
                       val acc=creditingAccounts(credit.AMOUNT,inputAmount)
                        savingTransaction(acc,credit,cash.CTOTAL,todayStr,1)
                    }
                    if (cash.CTOTAL<inputAmount){
                        val alert=dialog.statusDialog("Amount not available",false)
                        Handler().postDelayed({alert.cancel()},2000)
                    }
                }
                if (Index==2){
                     val acc=creditingAccounts(credit.AMOUNT,inputAmount)
                    savingTransaction(acc,credit,cash.CTOTAL,todayStr,2)
                }
            return
        }
        if (credit.STARTED==1) {
            val record=db.getCreditSettling(creditId!!,Index!!)
            if (Index==1){
                if (inputAmount<=cash.CTOTAL){
                    val acc=creditingAccounts(record.BALANCE,inputAmount)
                    savingTransaction(acc,credit,cash.CTOTAL,todayStr,1)
                }
                if (inputAmount>cash.CTOTAL){
                    val alert=dialog.statusDialog("Amount not available",false)
                    Handler().postDelayed({alert.cancel()},2000)
                }
            }
            if (Index==2){
                val acc=creditingAccounts(record.BALANCE,inputAmount)
                savingTransaction(acc,credit,cash.CTOTAL,todayStr,2)
            }
            return
        }
    }
    private fun savingTransaction(account: Account,credit:Credit,cash:Double,todayStr:String,creditType:Int){
        val dialog=Dialogs(CreditList@this)
        val dialogTitle=if (creditType==1) "Repay debt" else "Record received debt"
        val cashStr="$currencyName ${account.pay.AMOUNT}"
        var message=if (creditType==1) "Record debt payment \n $cashStr" else "Record received debt \n $cashStr"
        message=if (account.amountAbove>0.0) message+"\n Excess cash \n $currencyName ${account.amountAbove}\n Record it as deposit afterwards" else message
        val alert=AlertDialog.Builder(CreditList@this)
        alert.setTitle("$dialogTitle")
        alert.setMessage("$message")
        alert.setNegativeButton("Cancel"){dialogInterface,which->
            dialogInterface.dismiss()
        }
        alert.setPositiveButton("Record"){dialogInterface,which->
            if (creditType==1){
                val cred=crediting(account.pay,credit,cash,todayStr,account.started)
                if (cred==true){
                    amount.setText("")
                    dialogInterface.dismiss()
                   val credit=db.getCreditRecord(creditId!!,Index!!)
                    if (credit.STARTED==2){
                        amountForm.visibility=View.GONE
                    }
                    restartActivity()

                }
                else{
                    val alertD=dialog.statusDialog("Failed",false)
                    Handler().postDelayed({alertD.cancel()},2000)
                }
            }
            if (creditType==2){
                val debt=debting(account.pay,credit,cash,todayStr,account.started)
                if (debt==true){
                    amount.setText("")
                    dialogInterface.dismiss()
                    val credit=db.getCreditRecord(creditId!!,Index!!)
                    if (credit.STARTED==2){
                        amountForm.visibility=View.GONE
                    }
                    restartActivity()
                }
                else{
                    val alertD=dialog.statusDialog("Failed",false)
                    Handler().postDelayed({alertD.cancel()},2000)
                }
            }
        }
        alert.create().show()
    }
    private fun creditingAccounts(totalCredit:Double,input:Double):Account{
        val pay=Pay()
        var localStart=1
        var  amountAbove=0.0
        if (totalCredit>input){
            pay.BALANCE=totalCredit-input
            pay.AMOUNT=input
        }
        if (totalCredit<=input) {
            localStart=2
            pay.BALANCE=0.0
            pay.AMOUNT=totalCredit
            amountAbove=input-totalCredit
        }
        return Account(pay,amountAbove,localStart)
    }
    private fun crediting(pay:Pay,credit:Credit,cash:Double,todayStr:String,started:Int):Boolean{
          val cashAdjust=Cash()
          pay.TDATE=todayStr
          credit.STARTED=started
          credit.BALANCE=pay.BALANCE
          val upid=db.updateCashCredit(credit,Index!!)
          pay.CREDITID=credit.ID
          pay.CURRENCY=currencyName
          val settle= db.addCreditSettling(pay,Index!!)
          cashAdjust.TDATE=todayStr
          cashAdjust.DETAILS="Debt payment to "+credit.NAME
          cashAdjust.AMOUNT=pay.AMOUNT
          cashAdjust.TINDEX=5
          cashAdjust.TID=settle.ID
          cashAdjust.TTYPE=1
          cashAdjust.CURRENCY=currencyName
          cashAdjust.CTOTAL=cash-cashAdjust.AMOUNT
          val cashId=db.cashOperations(cashAdjust)
         return return if (upid>0 && settle.ID>0 && cashId>0) true else false
    }
    private fun debting(pay:Pay,credit:Credit,cash:Double,todayStr:String,started:Int):Boolean{
        val cashAdjust=Cash()
        credit.STARTED=started
        credit.BALANCE=pay.BALANCE
        val cred=db.updateCashCredit(credit,Index!!)
        pay.TDATE=todayStr
        pay.CREDITID=credit.ID
        pay.CURRENCY=currencyName
        val settle= db.addCreditSettling(pay,Index!!)
        cashAdjust.TDATE=todayStr
        cashAdjust.DETAILS="Received cash from "+credit.NAME
        cashAdjust.AMOUNT=pay.AMOUNT
        cashAdjust.CURRENCY=currencyName
        cashAdjust.TINDEX=7
        cashAdjust.TID=settle.ID
        cashAdjust.TTYPE=0
        cashAdjust.CTOTAL=cash+cashAdjust.AMOUNT
        val cashId=db.cashOperations(cashAdjust)
       return if (cred>0 && settle.ID>0 && cashId>0) true else false
    }
    private fun restartActivity(){
        val dialog=Dialogs(CreditList@this)
        val alertD=dialog.statusDialog("Record updated successfully",true)
        Handler().postDelayed({alertD.cancel()
            itemsLister()
        },2000)
    }

}