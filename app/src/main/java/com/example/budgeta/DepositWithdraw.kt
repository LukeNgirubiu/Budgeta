package com.example.budgeta

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.example.budgeta.Models.Cash
import com.example.budgeta.Repository.SqliteHelper
import com.example.budgeta.Utilities.DateFormating
import com.example.budgeta.Utilities.Dialogs
import java.util.*


class DepositWithdraw : AppCompatActivity() {
    private lateinit var toolbar:Toolbar
    private lateinit var details:EditText
    private lateinit var detailsError:TextView
     private lateinit var amount:EditText
     private lateinit var amountError:TextView
     private lateinit var submit:Button
     private var operationType:Int?=null
    private var transactionTime:String?=null
    private lateinit var db:SqliteHelper
    private lateinit var dialod:Dialogs
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deposit_withdraw)
        toolbar=findViewById(R.id.toobardw)
        operationType=intent.getIntExtra("Operation",0)
        details=findViewById(R.id.detail_text)
        detailsError=findViewById(R.id.details_txt_error)
        amount=findViewById(R.id.amount)
        amountError=findViewById(R.id.amount_error)
        submit=findViewById(R.id.submit)
        setSupportActionBar(toolbar)
        dialod= Dialogs(DepositWithdraw@this)
        db= SqliteHelper(DepositWithdraw@this)
        val timings= DateFormating()
        transactionTime=timings.timeNow()
        if (operationType==1){
            supportActionBar?.title="Deposit"
        }
        else{
            supportActionBar?.title="Withdraw"
        }
        submit.setOnClickListener {
            var valid=true
            if (details.text.isEmpty()){
                detailsError.visibility=View.VISIBLE
                detailsError.text="Details feild is empty"
                valid=false
            }
            if (!details.text.isEmpty()){
                detailsError.visibility=View.GONE
            }
            if (amount.text.isEmpty()){
                amountError.visibility=View.VISIBLE
                amountError.text="No value is provided"
                valid=false
            }
            if (!amount.text.isEmpty()){
                amountError.visibility=View.GONE
            }
            if(valid==true){
                val alertBiuld=AlertDialog.Builder(DepositWithdraw@this)
                val dialogText=if(operationType==1) "Deposit" else "Withdraw"
                val wordingText=if(operationType==1) "from" else "for"
                val amountText=amount.text.toString()
                val credentials=getSharedPreferences("Credentials", Context.MODE_PRIVATE)
                val currencyName=credentials.getString("Currency","None").toString()
                alertBiuld.setTitle(dialogText+" cash")
                alertBiuld.setMessage(dialogText+" $currencyName "+amountText+" $wordingText ${details.text}")
                alertBiuld.setPositiveButton("$dialogText"){dialogInterface,which->
                    if (operationType==1){
                        val depo=deposit(dialod.roundToOne(amountText.toDouble()),details.text.toString(),currencyName)
                        if(depo>0){
                            dialogInterface.dismiss()
                            val status=dialod.statusDialog("Recorded deposit\n $currencyName $amountText",true)
                            Handler().postDelayed({
                                status.dismiss()
                                val intentBack= Intent(DepositWithdraw@this,Deposit::class.java)
                                intentBack.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intentBack)
                            },2000)
                        }
                        else{
                            dialogInterface.dismiss()
                            val status=dialod.statusDialog("Failed to deposit",false)
                            Handler().postDelayed({
                                status.dismiss()
                            },2000)
                        }
                    }
                    if (operationType==2){
                        val withdr=withdraw(amountText.toDouble(),details.text.toString(),currencyName)
                        if(withdr==true){//Success
                            dialogInterface.dismiss()
                            val status=dialod.statusDialog("Recorded withdrawal \n" +
                                    " $currencyName $amountText",true)
                            Handler().postDelayed({
                                status.dismiss()
                                val intentBack= Intent(DepositWithdraw@this,WithDraw::class.java)
                                intentBack.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intentBack)
                            },2000)
                        }
                        if (withdr==false){
                            dialogInterface.dismiss()
                            val status=dialod.statusDialog("Insufficient funds",false)
                            Handler().postDelayed({
                                status.dismiss()
                            },2000)
                        }
                    }
                }
                alertBiuld.setNegativeButton("Cancel"){dialogInterface,which->
                    dialogInterface.cancel()
                }
                alertBiuld.create().show()
            }
        }
    }
    private fun deposit(amount:Double,details:String,currency: String):Long{
        val cash=Cash()
        val lastCash=db.lastCashTransaction()
        cash.TDATE=transactionTime!!
        cash.AMOUNT=amount
        cash.TTYPE=0 //Debit zero and 1 credit
        cash.TINDEX=2
        cash.DETAILS=details
        cash.CURRENCY=currency
        cash.TID=0//Zero since can't be linked directly to any table
        cash.CTOTAL=lastCash.CTOTAL+amount
        return db.cashOperations(cash)
        return 1
    }
  private fun withdraw(amount:Double,details:String,currency: String):Boolean{
        val cash=Cash()
        val lastCash=db.lastCashTransaction()
        var status=false
         if (lastCash.CTOTAL >=amount){
             cash.TDATE= transactionTime!!
             cash.AMOUNT=amount
             cash.TTYPE=1 //Debit zero and 1 credit
             cash.TINDEX=3
             cash.DETAILS=details
             cash.CURRENCY=currency
             cash.TID=0//Zero since can't be linked directly to any table deposit and withdraw
             cash.CTOTAL=lastCash.CTOTAL-amount
             db.cashOperations(cash)
             status=true
         }
       return status
    }
    //Zero for withdraw and deposit and id for the budget item/creditor/debtors
    //Transaction index for cashbook 1-Tick budget item, 2-Debtor pay, 3- Debt repayment,4-Cash deposit,5-Cash withdrawal
}