package com.example.budgeta

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.example.budgeta.Utilities.GraphData
import com.example.budgeta.Utilities.Graphs
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView
import org.w3c.dom.Text

class AnalyticCredit : AppCompatActivity() {
    private lateinit var dailyCredit:TextView
    private lateinit var dailyCreditCash:TextView
    private lateinit var monthlyCashCredit:TextView
    private lateinit var monthlyCreditL:TextView
    private lateinit var receivablesLableM:TextView
    private lateinit var deptDailyCash:TextView
    private lateinit var annaulDeptL:TextView
    private lateinit var deptAnnual:TextView
    private lateinit var graph: AAChartView
    private lateinit var creditAnalyticTool:Toolbar
    private lateinit var monthCardCred:CardView
    private lateinit var annualCardCred:CardView
    private lateinit var monthCardRec:CardView
    private lateinit var annualCardRec:CardView
    private lateinit var backButton: ImageButton
    private lateinit var title:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytic_credit)
        dailyCredit=findViewById(R.id.daily_credit_label)
        dailyCreditCash=findViewById(R.id.daily_credit_cash)
        monthlyCashCredit=findViewById(R.id.daily_monthly_cash)
        monthlyCreditL=findViewById(R.id.monthly_credit_label)
        receivablesLableM=findViewById(R.id.daily_dept_label)
        deptDailyCash=findViewById(R.id.dialy_dept_cash)
        annaulDeptL=findViewById(R.id.annual_dept_label)
        deptAnnual=findViewById(R.id.annual_dept_cash)
        graph=findViewById(R.id.aa_chart_view)
        monthCardCred=findViewById(R.id.daily_credit)
        annualCardCred=findViewById(R.id.monthly_credit)
        monthCardRec=findViewById(R.id.debt_daily)
        annualCardRec=findViewById(R.id.annual_received)
        backButton=findViewById(R.id.back_btn)
        title=findViewById(R.id.toolbar_title)
        title.text="Payables/Receivable"
        backButton.setOnClickListener {
            val intent= Intent(AnalyticCredit@this,Home::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
        providerData()
        navigate()
    }
    private fun providerData(){
        val credentials=getSharedPreferences("Credentials", Context.MODE_PRIVATE)
        val currencyName=credentials.getString("Currency","None")
        val graphPresent=Graphs(AnalyticCredit@this)
        val dashBoardData=GraphData(AnalyticCredit@this).creditAnalyticData()
        graph.aa_drawChartWithChartModel(graphPresent.comparisonAnnual("Payables","Receivables",2))
        dailyCredit.text="${dashBoardData.monthName} Payables"
        dailyCreditCash.text="$currencyName ${dashBoardData.monthlyPayable}"
        monthlyCreditL.text="${dashBoardData.year} Payables"
        monthlyCashCredit.text="$currencyName ${dashBoardData.annualPayable}"
        receivablesLableM.text="${dashBoardData.monthName} Receivables"
        deptDailyCash.text="$currencyName ${dashBoardData.monthlyDept}"
        annaulDeptL.text="${dashBoardData.year} Receivables"
        deptAnnual.text="$currencyName ${dashBoardData.anualDept}"

    }
    private fun navigate(){
      val intent= Intent(AnalyticCredit@this,Present::class.java)
        monthCardCred.setOnClickListener{
        intent.putExtra("index",1)
         intent.putExtra("groupId",1)
         startActivity(intent)
      }
    annualCardCred.setOnClickListener{
        intent.putExtra("index",2)
        intent.putExtra("groupId",1)
        startActivity(intent)
        }
    monthCardRec.setOnClickListener {
        intent.putExtra("index",3)
        intent.putExtra("groupId",1)
        startActivity(intent)
     }

    annualCardRec.setOnClickListener {
        intent.putExtra("index",4)
        intent.putExtra("groupId",1)
        startActivity(intent)
        }
    }
}