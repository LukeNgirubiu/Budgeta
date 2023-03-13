package com.example.budgeta

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import com.example.budgeta.Models.Budget
import com.example.budgeta.Repository.SqliteHelper
import com.example.budgeta.Utilities.DateFormating
import com.example.budgeta.Utilities.Dialogs
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddBudget : AppCompatActivity() {
    private lateinit var budgetName:EditText
    private lateinit var budgetNameError:TextView
    private lateinit var budgetDetails:EditText
    private lateinit var budgetDetailsErrors:TextView
    private lateinit var startDate:TextView
    private lateinit var expiryDate:TextView
    private lateinit var datesErrors:TextView
    private lateinit var submitBudget:Button
    private lateinit var datesLayout:LinearLayout
    private lateinit var titleDates:TextView
    private lateinit var toolbar:Toolbar
    private var StartDate:String?=null
    private var EndDate:String?=null
    private var budgetCategory=0
    private var budgetOperation:Int=1
    private var todayDateStr:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_budget)
        budgetCategory=intent.getIntExtra("BID",0)
        budgetOperation=intent.getIntExtra("BCU",0)
        budgetName=findViewById(R.id.budget_name)
        budgetNameError=findViewById(R.id.name_errors)
        toolbar=findViewById(R.id.toobarAddBudget)
        budgetDetails=findViewById(R.id.budget_details)
        budgetDetailsErrors=findViewById(R.id.details_errors)
        startDate=findViewById(R.id.start_date)
        expiryDate=findViewById(R.id.expiry_date)
        datesLayout=findViewById(R.id.date_layout)
        datesErrors=findViewById(R.id.dates_errors)
        submitBudget=findViewById(R.id.submit_budget)
        titleDates=findViewById(R.id.dates_title)
        val calender=Calendar.getInstance()
        var year=calender.get(Calendar.YEAR)
        var yearEnd=calender.get(Calendar.YEAR)
        var month=calender.get(Calendar.MONTH)
        var monthEnd=calender.get(Calendar.MONTH)
        var date=calender.get(Calendar.DAY_OF_MONTH)
        var dateEnd=calender.get(Calendar.DAY_OF_MONTH)+1
        if(budgetCategory==1){
            datesLayout.visibility=View.VISIBLE
            titleDates.visibility=View.VISIBLE
        }
       if(budgetOperation==0){
           setSupportActionBar(toolbar)
           supportActionBar?.title="Add Budget"
       }

        if(budgetOperation>0){
           val dates= setFeildsForUpdate(budgetOperation)
            if(dates[0]=="Available"){
                val sdf = SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH)//dd-MM-yyyy
                val startDate: Date = sdf.parse(dates[1])
                val endDate=sdf.parse(dates[2])
                val calStart = Calendar.getInstance()
                val calEnd = Calendar.getInstance()
                calStart.time = startDate
                calEnd.time=endDate
                year=calStart.get(Calendar.YEAR)
                month=calStart.get(Calendar.MONTH)
                date=calStart.get(Calendar.DAY_OF_MONTH)
                yearEnd=calEnd.get(Calendar.YEAR)
                monthEnd=calEnd.get(Calendar.MONTH)
                dateEnd=calEnd.get(Calendar.DAY_OF_MONTH)
            }
        }
        val datePick1=DatePickerDialog(this, object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                StartDate=String.format("%d/%d/%d",year , month + 1, dayOfMonth)
                startDate.text=StartDate
            }
        }, year, month, date);
        val datePick2=DatePickerDialog(this, object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
              EndDate=String.format("%d/%d/%d", year , month + 1, dayOfMonth)
               expiryDate.text=EndDate
            }
        }, yearEnd, monthEnd, dateEnd);

        startDate.setOnClickListener {
            datePick1.show()
        }
        expiryDate.setOnClickListener {
            if(startDate!=null) {
                datePick2.show()
            }
        }
        submitBudget.setOnClickListener {
            submitBudget
            if(validations()){
                val alerting=Dialogs(this)
                val simpleDialog=alerting.simpleDialog("Please wait .....")
                val db=SqliteHelper(AddBudget@this)
                val credentials=getSharedPreferences("Credentials", Context.MODE_PRIVATE)
                val currencyType=credentials.getString("Currency","None").toString()
                val budget=Budget()
                budget.NAME=budgetName.text.toString()
                budget.DETAILS=budgetDetails.text.toString()
                budget.CATEGORY=budgetCategory
                if(budgetCategory==1){
                    val dates:MutableList<String> = ArrayList()
                    val timings=DateFormating()
                    dates.add(StartDate!!)
                    dates.add(EndDate!!)
                    val formatedDates=formatDates(dates)
                    budget.TODAYDATE="${calender.get(Calendar.YEAR)}-${timings.numberFormating(calender.get(Calendar.MONTH)+1)}-${timings.numberFormating(calender.get(Calendar.DAY_OF_MONTH))}"
                    budget.STARTDATE=formatedDates.get(0)
                    budget.ENDDATE=formatedDates.get(1)
                }
                else{
                  val dbDates=providingDates(budgetCategory)
                    budget.TODAYDATE=dbDates[0]
                    budget.STARTDATE=dbDates[0]
                    budget.ENDDATE=dbDates[1]
                }
                var id:Long?=null
                var dialogMessage=""
                if(budgetOperation>0){
                    val budgetDetails=db.getBudget(budgetOperation)
                    budget.ID=budgetOperation
                    budget.STARTED=budgetDetails.STARTED
                    budget.ITEMS=budgetDetails.ITEMS
                    budget.CURRENCY=currencyType
                    budget.T_EST=budgetDetails.T_EST
                    id=db.updateBudget(budget)
                    dialogMessage=if(id>0) "Updated ${budget.NAME}" else "Failed"
                }
                else{
                    budget.STARTED=0
                    budget.ITEMS=0
                    budget.T_EST=0.0
                    budget.CURRENCY=currencyType
                    id=db.addBudget(budget)
                    dialogMessage=if(id>0) "Done successfully" else "Failed"
                }
                if(id!=null){
                    simpleDialog.cancel()
                    val statusD=alerting.statusDialog(dialogMessage,true)
                    val intentBack=Intent(AddBudget@this,Budgets::class.java)
                    intentBack.putExtra("BGI",budgetCategory)
                    intentBack.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    Handler().postDelayed({statusD.dismiss()
                        startActivity(intentBack)
                                          },3000)
                }
                else{
                    simpleDialog.cancel()
                    val statusD=alerting.statusDialog(dialogMessage,false)
                    Handler().postDelayed({statusD.dismiss()},3000)
                }
            }
        }
    }
    private fun providingDates(index:Int):Array<String> {
        val timings=DateFormating()
        val calender=Calendar.getInstance()
        val today="${calender.get(Calendar.YEAR)}-${timings.numberFormating(calender.get(Calendar.MONTH)+1)}-${timings.numberFormating(calender.get(Calendar.DAY_OF_MONTH))}"
       var endDate=""
       when(index){
           2->{
               calender.add(Calendar.DATE,7)
              endDate="${calender.get(Calendar.YEAR)}-${timings.numberFormating(calender.get(Calendar.MONTH)+1)}-${timings.numberFormating(calender.get(Calendar.DAY_OF_MONTH))}"
           }
           3->{
               calender.add(Calendar.MONTH,1)
               endDate="${calender.get(Calendar.YEAR)}-${timings.numberFormating(calender.get(Calendar.MONTH)+1)}-${timings.numberFormating(calender.get(Calendar.DAY_OF_MONTH))}"
           }
           4->{
               calender.add(Calendar.YEAR,1)
               endDate="${calender.get(Calendar.YEAR)}-${timings.numberFormating(calender.get(Calendar.MONTH)+1)}-${timings.numberFormating(calender.get(Calendar.DAY_OF_MONTH))}"
           }
       }
        return arrayOf(today,endDate)
    }

    private fun setFeildsForUpdate(budgetId:Int):Array<String> {
        val db=SqliteHelper(AddBudget@this)
        val budget=db.getBudget(budgetId)
        if(budget.ID!=0){
            setSupportActionBar(toolbar)
            supportActionBar?.title=budget.NAME
            budgetName.setText(budget.NAME)
            budgetDetails.setText(budget.DETAILS)
            val stDate=budget.STARTDATE.split("-")
            val endDate=budget.ENDDATE.split("-")
            StartDate=String.format("%d/%d/%d",stDate[0].toInt() ,stDate[1].toInt(), stDate[2].toInt())
            EndDate=String.format("%d/%d/%d",endDate[0].toInt() ,endDate[1].toInt(), endDate[2].toInt())
            startDate.setText(StartDate)
            expiryDate.setText(EndDate)
            submitBudget.text="Update"
            return arrayOf("Available",budget.STARTDATE,budget.ENDDATE)
        }
        else{
            return arrayOf("Null","","")
        }
    }

    private fun validations():Boolean{
        var valid=true
        val validationPrefix="Kindly share the"
        if(budgetName.text.isEmpty()){
            budgetNameError.visibility=View.VISIBLE
            budgetNameError.text="*$validationPrefix budget name"
            valid=false
        }
        if(!budgetName.text.isEmpty()){
            budgetNameError.visibility=View.GONE
        }
        if(budgetDetails.text.isEmpty()){
            budgetDetailsErrors.visibility=View.VISIBLE
            budgetDetailsErrors.text="*$validationPrefix budget details"
           valid=false
        }
        if(!budgetDetails.text.isEmpty()){
            budgetDetailsErrors.visibility=View.GONE
        }
        if (budgetCategory==1){
            if(StartDate==null && EndDate==null){
                valid=false
                datesErrors.visibility=View.VISIBLE
                datesErrors.text="*$validationPrefix start date and the end date"
                return valid
            }
            if (StartDate==null){
                valid=false
                datesErrors.visibility=View.VISIBLE
                datesErrors.text="*$validationPrefix budget Start Date"
                return valid
            }
            if (EndDate==null){
                valid=false
                datesErrors.visibility=View.VISIBLE
                datesErrors.text="*$validationPrefix budget End Date/Expiry Date"
                return valid
            }
            if(StartDate!=null && EndDate!=null){
                val dateContentsStart=StartDate?.split("/")
                val dateContentsStartEnd=EndDate?.split("/")
                val calender=Calendar.getInstance()
                val sdf = SimpleDateFormat("MM/dd/yyyy",Locale.ENGLISH)//yyyy-MM-dd
                val stDateStr="${dateContentsStart!![1].trim()}/${dateContentsStart[2].trim()}/${dateContentsStart[0].trim()}"
                val endDateStr="${dateContentsStartEnd!![1].trim()}/${dateContentsStartEnd[2].trim()}/${dateContentsStartEnd[0].trim()}"
                val stDate=sdf.parse(stDateStr)
                val endDate=sdf.parse(endDateStr)
                todayDateStr="${calender.get(Calendar.MONTH)+1}/${calender.get(Calendar.DAY_OF_MONTH)}/${calender.get(Calendar.YEAR)}"
                val todayDate=sdf.parse(todayDateStr)
                if(todayDate.after(stDate)){
                    valid=false
                    datesErrors.visibility=View.VISIBLE
                    datesErrors.text="*The start date should be after today"
                    return valid
                }
                if(todayDate.after(endDate)){
                    valid=false
                    datesErrors.visibility=View.VISIBLE
                    datesErrors.text="*The end date/expiry date should be after today"
                    return valid
                }
                if(stDate.after(endDate)){
                    valid=false
                    datesErrors.visibility=View.VISIBLE
                    datesErrors.text="*Start date should be before end date/expiry date"
                    return valid
                }
                if(stDate.equals(endDate)){
                    valid=false
                    datesErrors.visibility=View.VISIBLE
                    datesErrors.text="*Start date shouldn't be equal to expiry date"
                    return valid
                }
                datesErrors.visibility=View.GONE
            }
        }
        return valid
    }
    private fun formatDates(dates:List<String>):List<String>{
        val timings=DateFormating()
        val formatedDates:MutableList<String> = ArrayList()
        for (date in dates){
            val dateContents=date.split("/")
            val DateStr="${dateContents[0].trim()}-${timings.numberFormating(dateContents[1].trim().toInt())}-${timings.numberFormating(dateContents[2].trim().toInt())}"
         formatedDates.add(DateStr)
        }
        //${calender.get(Calendar.MONTH)+1}/${calender.get(Calendar.DAY_OF_MONTH)}/${calender.get(Calendar.YEAR)}
        /*dayOfMonth, month + 1, year*/
        return formatedDates
    }

}
