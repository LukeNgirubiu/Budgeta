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
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AAStyle

class AnalyticCash : AppCompatActivity() {
   private lateinit var monthlySpendingL:TextView
   private lateinit var monthlyExpen:TextView
   private lateinit var monthRevenueL:TextView
   private lateinit var monthRevenue:TextView
   private lateinit var annualExp:TextView
   private lateinit var annualRev:TextView
   private lateinit var graph:AAChartView
   private lateinit var annualSpLb:TextView
   private lateinit var annualRvLb:TextView
   private lateinit var monthlySpCr:CardView
   private lateinit var monthlyRvCr:CardView
   private lateinit var annualExpCr:CardView
   private lateinit var annualRvCr:CardView
    private lateinit var backButton: ImageButton
    private lateinit var title:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytic_cash)
        monthlySpendingL=findViewById(R.id.monthly_spending_label)
        monthlyExpen=findViewById(R.id.monthly_spending_cash)
        monthRevenueL=findViewById(R.id.monthly_revenue_label)
        monthRevenue=findViewById(R.id.monthly_revenue_cash)
        annualExp=findViewById(R.id.annual_spending_cash)
        annualRev=findViewById(R.id.annual_revenue_cash)
        graph= findViewById(R.id.aa_chart_cash)
        annualSpLb=findViewById(R.id.annual_spending_label)
        annualRvLb=findViewById(R.id.annual_revenue_label)
        monthlySpCr=findViewById(R.id.monthly_spending)
        monthlyRvCr=findViewById(R.id.monthly_revenue)
        annualExpCr=findViewById(R.id.annual_revenue)
        annualRvCr=findViewById(R.id.annual_spending)
        backButton=findViewById(R.id.back_btn)
        title=findViewById(R.id.toolbar_title)
        title.text="Revenue/Expenses"
        backButton.setOnClickListener {
            val intent= Intent(AnalyticCash@this,Home::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
        provideData()
        navigate()
    }
   fun provideData(){
       val credentials=getSharedPreferences("Credentials", Context.MODE_PRIVATE)
       val currencyName=credentials.getString("Currency","None")
       val compareGraph=Graphs(AnalyticCash@this)
       val graphData=GraphData(this)
       graph.aa_drawChartWithChartModel(compareGraph.comparisonAnnual("Revenue","Expenses",1))
       val dashboardData=graphData.cashAnalyticData()
       monthlySpendingL.text="${dashboardData.monthName} Expenditure"
       monthlyExpen.text="$currencyName ${dashboardData.monthExpenditure}"
       monthRevenueL.text="${dashboardData.monthName} Revenue"
       monthRevenue.text="$currencyName ${dashboardData.monthRevenue}"
       annualSpLb.text="${dashboardData.year} Expenditure"
       annualExp.text="$currencyName ${dashboardData.annualExpenditure}"
       annualRvLb.text="${dashboardData.year} Revenue"
       annualRev.text="$currencyName ${dashboardData.annualRevenue}"
   }
    fun navigate(){
        val intent= Intent(AnalyticCash@this,Present::class.java)
        monthlySpCr.setOnClickListener {
            intent.putExtra("index",5)
            intent.putExtra("groupId",2)
            startActivity(intent)
        }
        monthlyRvCr.setOnClickListener {
            intent.putExtra("index",6)
            intent.putExtra("groupId",2)
            startActivity(intent)
        }
        annualExpCr.setOnClickListener {
            intent.putExtra("index",7)
            intent.putExtra("groupId",2)
            startActivity(intent)
        }
        annualRvCr.setOnClickListener {
            intent.putExtra("index",8)
            intent.putExtra("groupId",2)
            startActivity(intent)
        }
    }
}