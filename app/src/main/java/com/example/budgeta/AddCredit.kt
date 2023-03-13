package com.example.budgeta
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import com.example.budgeta.Models.Cash
import com.example.budgeta.Models.Credit
import com.example.budgeta.Repository.SqliteHelper
import com.example.budgeta.Utilities.DateFormating
import com.example.budgeta.Utilities.Dialogs
import java.text.SimpleDateFormat
import java.util.*
class AddCredit : AppCompatActivity() {
    private lateinit var toolbar:Toolbar
    private var index:Int?=null
    private var Id:Long=0
    private lateinit var title:EditText
    private lateinit var titleError:TextView
    private lateinit var details:EditText
    private lateinit var detailsError:TextView
    private lateinit var phoneNumber:EditText
    private lateinit var numberError:TextView
    private lateinit var dueDate:TextView
    private lateinit var errorDate:TextView
    private lateinit var submit:Button
    private var dateStr:String=""
    private lateinit var amount:EditText
    private lateinit var amountError:TextView
    private lateinit var amountLabel:TextView
    private lateinit var db:SqliteHelper
    private lateinit var dialog: Dialogs
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_credit)
        toolbar=findViewById(R.id.toolbar_form)
        title=findViewById(R.id.credit_name)
        amountLabel=findViewById(R.id.amount_label)
        titleError=findViewById(R.id.credit_name_error)
        details=findViewById(R.id.creditor_details)
        detailsError=findViewById(R.id.details_error)
        phoneNumber=findViewById(R.id.cell_phone_number)
        numberError=findViewById(R.id.phone_number_error)
        dueDate=findViewById(R.id.settle_date)
        errorDate=findViewById(R.id.date_error)
        amount=findViewById(R.id.amount)
        amountError=findViewById(R.id.amount_error)
        submit=findViewById(R.id.submit)
        db= SqliteHelper(AddCredit@this)
        index=intent.getIntExtra("Index",1)
        Id=intent.getLongExtra("Id",0)
        dialog= Dialogs(AddCredit@this)
        setSupportActionBar(toolbar)
        setTitle()
        if(Id>0){
            val timing=DateFormating()
            val credit=db.getCreditRecord(Id,index!!)
            title.setText(credit.NAME)
            details.setText(credit.DETAILS)
            phoneNumber.setText(credit.CELLPHONENO)
            amount.setText(credit.AMOUNT.toString())
            dueDate.text=timing.toSlashedDate(credit.PAYMENTDATE)
            dateStr=timing.toSlashedDate(credit.PAYMENTDATE)
            submit.text="Update"
        }

        val credentials=getSharedPreferences("Credentials", Context.MODE_PRIVATE)
        val currencyName=credentials.getString("Currency","None")
        amountLabel.text="Amount in $currencyName"
        val calender= Calendar.getInstance()
        val datePick= DatePickerDialog(this, object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                dateStr=String.format("%d/%d/%d", dayOfMonth, month + 1, year)
                dueDate.text=dateStr
            }
        }, calender.get(Calendar.YEAR), calender.get(Calendar.MONTH), calender.get(Calendar.DAY_OF_MONTH));

        dueDate.setOnClickListener {
            datePick.show()
        }
        submit.setOnClickListener {
            val valid=validate()
            if (valid==true){
                if (Id>0){
                    creditUpdate()
                }
                if (Id<1) {
                    creditCreation()
                }
            }
        }

    }
    private fun setTitle(){
        if(index==1 && Id==0L){
            supportActionBar?.title="Record payable cash"
        }
        if(index==1 && Id>0){
            supportActionBar?.title="Update payable cash"
        }
        if(index==2 && Id==0L){
            supportActionBar?.title="Record receivable cash"
        }
        if(index==2 && Id>0){
            supportActionBar?.title="Update receivable cash"
        }
    }
    private fun validate():Boolean{
        var valid =true
        if(title.text.isEmpty()){
            titleError.visibility= View.VISIBLE
            titleError.text="* Title is required"
            valid=false
        }
        if(!title.text.isEmpty()){
            titleError.visibility= View.GONE
        }
        if(details.text.isEmpty()){
            detailsError.visibility= View.VISIBLE
            detailsError.text="* Details are required"
            valid=false
        }
        if(!details.text.isEmpty()){
            detailsError.visibility= View.GONE
        }
        if(phoneNumber.text.isEmpty()){
            numberError.visibility= View.VISIBLE
            numberError.text="* Mobile number is required"
            valid=false
        }
        if(!phoneNumber.text.isEmpty() && phoneNumber.text.toString().length<8){
            numberError.visibility= View.VISIBLE
            numberError.text="* Mobile number is to short\n Recheck it kindly"
            valid=false
        }
        if(!phoneNumber.text.isEmpty() && phoneNumber.text.toString().length>8){
            numberError.visibility= View.GONE
        }
        if(amount.text.isEmpty()){
            amountError.visibility= View.VISIBLE
            amountError.text="* Amount is required"
            valid=false
        }
        if(!amount.text.isEmpty()){
            amountError.visibility= View.GONE
        }
        if(dateStr.length<1){
            errorDate.visibility=View.VISIBLE
            errorDate.text="Date due is required"
            valid=false
        }
        if (dateStr.length>1){
            val calender=Calendar.getInstance()
            val sdf = SimpleDateFormat("MM/dd/yyyy")
            val dateBr=dateStr.split("/")
            val stDateStr="${dateBr!![1].trim().toInt()}/${dateBr!![0].trim().toInt()}/${dateBr!![2].trim()}"
            val todayStr="${calender.get(Calendar.MONTH)+1}/${calender.get(Calendar.DAY_OF_MONTH)}/${calender.get(Calendar.YEAR)}"
            val todayDate=sdf.parse(todayStr)
            val choiceDate=sdf.parse(stDateStr)
            if(todayDate.after(choiceDate)){
                errorDate.visibility=View.VISIBLE
                errorDate.text="Date due should be later than today"
                valid=false
            }
            else{
                errorDate.visibility=View.GONE
            }
        }
        return valid
    }
    private fun creditCreation(){
        val credentials=getSharedPreferences("Credentials", Context.MODE_PRIVATE)
        val currencyName=credentials.getString("Currency","None").toString()
        val credit=Credit()
        val timings=DateFormating()
        val todayStr=timings.timeNow()
        credit.NAME=title.text.toString()
        credit.DETAILS=details.text.toString()
        credit.CELLPHONENO=phoneNumber.text.toString()
        credit.TDATE=todayStr
        credit.STARTED=0
        credit.BALANCE=dialog.roundToOne(amount.text.toString().toDouble())
        credit.PAYMENTDATE=timings.toDashedDate(dateStr)
        credit.CURRENCY=currencyName
        credit.AMOUNT=dialog.roundToOne(amount.text.toString().toDouble())
        val cash=db.lastCashTransaction()
        if (index==1){
                val tId:Long=db.addCashCredit(credit,index!!)
                val cashUpdate=Cash()
                cashUpdate.TDATE=todayStr
                cashUpdate.DETAILS="${credit.NAME}"
                cashUpdate.AMOUNT=credit.AMOUNT
                cashUpdate.TINDEX=4
                cashUpdate.TTYPE=0
                cashUpdate.CTOTAL=dialog.roundToOne(cash.CTOTAL+credit.AMOUNT)
                cashUpdate.TID=tId
                cashUpdate.CURRENCY=currencyName
                val cashId=db.cashOperations(cashUpdate)
                transactionStatus(cashId)
        }
        if (index==2){
            if(cash.CTOTAL> credit.AMOUNT){
                val tId:Long=db.addCashCredit(credit,index!!)
                val cashUpdate=Cash()
                cashUpdate.TDATE=todayStr
                cashUpdate.DETAILS="${credit.NAME}"
                cashUpdate.AMOUNT=credit.AMOUNT
                cashUpdate.TINDEX=6
                cashUpdate.TTYPE=1
                cashUpdate.CURRENCY=currencyName
                cashUpdate.CTOTAL=dialog.roundToOne(cash.CTOTAL-credit.AMOUNT)
                cashUpdate.TID=tId
                val cashId=db.cashOperations(cashUpdate)
                transactionStatus(cashId)
            }
            if(cash.CTOTAL< credit.AMOUNT){
                val dialog=Dialogs(AddCredit@this)
                val alert=dialog.statusDialog("Failed insufficient cash amount",false)
                Handler().postDelayed({alert.cancel()},2000)
            }
        }
    }
    private fun creditUpdate(){
        val timings=DateFormating()
        val todayStr=timings.timeNow()
        val creditU=db.getCreditRecord(Id,index!!)//Cash
        val cashO=Cash()
        val dialog=Dialogs(AddCredit@this)
        if (creditU.STARTED==0){
            val credit=Credit()
            val cashAmount=dialog.roundToOne(amount.text.toString().toDouble())
            credit.NAME=title.text.toString()
            credit.DETAILS=details.text.toString()
            credit.CELLPHONENO=phoneNumber.text.toString()
            credit.TDATE=todayStr
            credit.STARTED=0
            credit.PAYMENTDATE=timings.toDashedDate(dateStr)
            credit.AMOUNT=cashAmount
            credit.BALANCE=cashAmount
            credit.ID=creditU.ID
            val cash=db.lastCashTransaction()
            cashO.DETAILS="Update for ${credit.NAME}"
            cashO.TDATE=todayStr
            cashO.TDATE=cash.TDATE
            cashO.CURRENCY=cash.CURRENCY
            if (index==1) {
                cashO.TINDEX=4
                if (creditU.AMOUNT==cashAmount){
                    val intent= Intent(AddCredit@this,Credits::class.java)
                    intent.putExtra("Index",this.index)
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }
                if (creditU.AMOUNT > cashAmount) {
                    cashO.AMOUNT = dialog.roundToOne(creditU.AMOUNT - cashAmount)
                    if (cash.CTOTAL>= cashO.AMOUNT){
                        cashO.CTOTAL=dialog.roundToOne(cash.CTOTAL-cashO.AMOUNT)
                        cashO.TTYPE =1
                        updateTransaction(cashO,credit,dialog,"Payable")
                    }
                    if (cash.CTOTAL< cashO.AMOUNT){
                       val al=dialog.statusDialog("Failed to update\n" +
                               "Failed due to low balance",false)
                        Handler().postDelayed({al.cancel()},2000)
                    }
                }
                if (creditU.AMOUNT < cashAmount){
                    cashO.AMOUNT = dialog.roundToOne(cashAmount-creditU.AMOUNT)
                    cashO.CTOTAL=dialog.roundToOne(cash.CTOTAL+cashO.AMOUNT)
                    cashO.TTYPE =0
                    updateTransaction(cashO,credit,dialog,"Payable")
                }
                }
            if (index==2){
                cashO.TINDEX=6
                if (cashAmount==creditU.AMOUNT){
                    val intent= Intent(AddCredit@this,Credits::class.java)
                    intent.putExtra("Index",this.index)
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }
                 if(cashAmount>creditU.AMOUNT){
                     cashO.AMOUNT =  dialog.roundToOne(cashAmount-creditU.AMOUNT)
                     if (cash.CTOTAL>=cashO.AMOUNT){
                         cashO.TTYPE =1
                         cashO.CTOTAL=dialog.roundToOne(cash.CTOTAL-cashO.AMOUNT)
                         updateTransaction(cashO,credit,dialog,"Receivable")
                     }
                     if(cash.CTOTAL<cashO.AMOUNT){
                         val al=dialog.statusDialog("Failed to update\n Insufficient amount",false)
                         Handler().postDelayed({al.cancel()},2000)
                     }
                 }
                if(cashAmount<creditU.AMOUNT){
                        cashO.AMOUNT =  dialog.roundToOne(creditU.AMOUNT-cashAmount)
                        cashO.TTYPE =0
                        cashO.CTOTAL=dialog.roundToOne(cash.CTOTAL+cashO.AMOUNT)
                    updateTransaction(cashO,credit,dialog,"Receivable")
                }
              }
            }
    }
    private fun transactionStatus(id:Long){
        if(id<1){
            val dialog=Dialogs(AddCredit@this)
            val alert=dialog.statusDialog("Failed",false)
            Handler().postDelayed({alert.cancel()},2000)
        }
        if(id>0){
            val dialog=Dialogs(AddCredit@this)
            val alert=dialog.statusDialog("Updated successfully",true)
            Handler().postDelayed({
                alert.cancel()
                val intent= Intent(AddCredit@this,Credits::class.java)
                intent.putExtra("Index",this.index)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                                  },2000)
        }
    }
    private fun updateTransaction(cashO:Cash,credit:Credit,dialog:Dialogs,transaction:String){
        val id=db.updateCashCredit(credit,index!!)
        cashO.TID=id
        val cashId=db.cashOperations(cashO)
        if (cashId>0){
            val alert=dialog.statusDialog("$transaction record updated",true)
            Handler().postDelayed({alert.dismiss()
                val intent= Intent(AddCredit@this,Credits::class.java)
                intent.putExtra("Index",this.index)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                                  },2000)
        }
    }
}