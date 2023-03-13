package com.example.budgeta
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat

class Setting : AppCompatActivity() {
    private lateinit var currency:EditText
    private lateinit var archive:SwitchCompat
    private lateinit var allowReminder:SwitchCompat
    private lateinit var creditSwitch:SwitchCompat
    private lateinit var limit:EditText
    private lateinit var continueHome:Button
    private lateinit var editError:TextView
    private lateinit var currencylabel:TextView
    private lateinit var backButton: ImageButton
    private lateinit var sharedPrefs:SharedPreferences
    private lateinit var groupTrans:RadioGroup
    private lateinit var radioTrans10:RadioButton
    private lateinit var radioTrans20:RadioButton
    private lateinit var radioTrans30:RadioButton
    private lateinit var budgetArchiveGroup:RadioGroup
    private lateinit var budgetDay:RadioButton
    private lateinit var budgetMonth:RadioButton
    private lateinit var budgetWeek:RadioButton
    private lateinit var creditBudgetGroup:RadioGroup
    private lateinit var creditHour:RadioButton
    private lateinit var creditDay:RadioButton
    private lateinit var creditWeek:RadioButton
    private var cashTransId:Int?=null
    private var budgetArchiveId:Int?=null
    private var creditArchiveId:Int?=null
    private var Status:Int?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        currency=findViewById(R.id.currency)
        currencylabel=findViewById(R.id.currency_label)
        archive=findViewById(R.id.budget_archive)
        allowReminder=findViewById(R.id.allow_reminder)
        creditSwitch=findViewById(R.id.credit_archive)
        limit=findViewById(R.id.monthly_limit)
        continueHome=findViewById(R.id.continue_home)
        editError=findViewById(R.id.currency_error)
        backButton=findViewById(R.id.back_btn)
        groupTrans=findViewById(R.id.trans_Group)
        radioTrans10=findViewById(R.id.cash_show_10)
        radioTrans20=findViewById(R.id.cash_show_20)
        radioTrans30=findViewById(R.id.cash_show_30)
        budgetArchiveGroup=findViewById(R.id.budgetArchiveGroup)
        budgetDay=findViewById(R.id.budgetDay)
        budgetWeek=findViewById(R.id.budgetWeek)
        budgetMonth=findViewById(R.id.budgetWeek)
        creditBudgetGroup=findViewById(R.id.creditBudgetGroup)
        creditHour=findViewById(R.id.creditHour)
        creditDay=findViewById(R.id.creditDay)
        creditWeek=findViewById(R.id.creditWeek)
        Status=intent.getIntExtra("Status",0)
        sharedPrefs=getSharedPreferences("Credentials",Context.MODE_PRIVATE)
        cashTransId=sharedPrefs.getInt("cashTransId",1)
        budgetArchiveId=sharedPrefs.getInt("budgetArchiveId",1)//
        creditArchiveId=sharedPrefs.getInt("creditArchiveId",1)
        val creditArchive=sharedPrefs.getBoolean("setCreditArchive",false)
        val setBudgetArchive=sharedPrefs.getBoolean("setArchive",false)
        archive.isChecked=setBudgetArchive
        creditSwitch.isChecked=creditArchive
        if(setBudgetArchive){
            budgetArchiveGroup.visibility=View.VISIBLE
        }
        else{
            budgetArchiveGroup.visibility=View.GONE
        }
        if(creditArchive){
            creditBudgetGroup.visibility=View.VISIBLE
        }
        else{
            creditBudgetGroup.visibility=View.GONE
        }

        archive.setOnCheckedChangeListener{_,isChecked->
            if(isChecked){
                budgetArchiveGroup.visibility=View.VISIBLE
            }
            else{
                budgetArchiveGroup.visibility=View.GONE
            }
        }
        creditSwitch.setOnCheckedChangeListener{_,isChecked->
            if(isChecked){
                creditBudgetGroup.visibility=View.VISIBLE
            }
            else{
                creditBudgetGroup.visibility=View.GONE
            }
        }

        when(cashTransId){
            1->{
                radioTrans10.isChecked=true
            }
            2->{
                radioTrans20.isChecked=true
            }
            3->{
                radioTrans30.isChecked=true
            }
        }
        when(budgetArchiveId){
            1->{
                budgetDay.isChecked=true
            }
            2->{
                budgetWeek.isChecked=true
            }
            3->{
                budgetMonth.isChecked=true
            }
        }
        when(creditArchiveId){
            1->{
                creditHour.isChecked=true
            }
            2->{
                creditDay.isChecked=true
            }
            3->{
                creditWeek.isChecked=true
            }
        }
        budgetArchiveGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.budgetDay -> {
                    budgetArchiveId=1
                }
                R.id.budgetWeek-> {
                    budgetArchiveId=2
                }
                R.id.budgetMonth -> {
                    budgetArchiveId=3
                }
            }
        }
        creditBudgetGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.creditHour -> {
                    creditArchiveId=1
                }
                R.id.creditDay -> {
                    creditArchiveId=2
                }
                R.id.creditWeek -> {
                    creditArchiveId=3
                }
            }
        }
        //SELECT * FROM CASHBOOK LIMIT 10
        groupTrans.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.cash_show_10-> {
                    cashTransId=1
                }
                R.id.cash_show_20 -> {
                    cashTransId=2
                }
                R.id.cash_show_30 -> {
                    cashTransId=3
                }
            }
        }




        if (Status==0){
            continueHome.visibility=View.VISIBLE
        }
        if(Status==1){
            val montlyLt= if (sharedPrefs.getString("Limit","")!!.isBlank()) 0.0 else sharedPrefs.getString("Limit","")!!.toDouble()
            limit.setText(montlyLt.toString())
            allowReminder.isChecked=sharedPrefs.getBoolean("Remind",false)
            backButton.visibility=View.VISIBLE
            currency.visibility=View.GONE
            currencylabel.visibility=View.GONE

        }

        continueHome.setOnClickListener {
          updateSetting()
        }
        backButton.setOnClickListener {
          updateSetting()
        }

    }
    fun updateSetting(){
        val editor=sharedPrefs.edit()
        val setArchive:Boolean=archive.isChecked
        val remind=allowReminder.isChecked
        val creditArchive=creditSwitch.isChecked
        val currencySymbol=currency.text.toString()
        val montlyLimit=if (limit.text.toString().isBlank()) "0.0" else limit.text.toString()
        if (Status==0){
            if (!currencySymbol.isBlank()){
                    editor.apply {
                        putString("Currency",currencySymbol)
                        putString("Limit",montlyLimit)
                        putBoolean("Remind",remind)
                        putBoolean("setArchive",setArchive)
                        putBoolean("setCreditArchive",creditArchive)
                        putInt("cashTransId",cashTransId!!)
                        putInt("budgetArchiveId",budgetArchiveId!!)
                        putInt("creditArchiveId",creditArchiveId!!)
                        commit()
                    }
                startActivity(Intent(Setting@this,Home::class.java))
                finishAffinity()
            }
            else{
                editError.visibility=View.VISIBLE
                editError.text="*This feild is required"
            }
        }
        if(Status==1){
            editor.apply {
                putString("Limit",montlyLimit)
                putBoolean("setArchive",setArchive)
                putBoolean("Remind",remind)
                putBoolean("setCreditArchive",creditArchive)
                putInt("cashTransId",cashTransId!!)
                putInt("budgetArchiveId",budgetArchiveId!!)
                putInt("creditArchiveId",creditArchiveId!!)
                commit()
            }
            startActivity(Intent(Setting@this,Home::class.java))
            finishAffinity()
        }

    }

}
