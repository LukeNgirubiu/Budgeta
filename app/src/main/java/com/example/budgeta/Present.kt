package com.example.budgeta

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.example.budgeta.Utilities.Graphs
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView

class Present : AppCompatActivity() {
    private lateinit var title:TextView
    private lateinit var backButton: ImageButton
    private lateinit var graph:AAChartView
    private lateinit var noDataLable:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_present)
        title=findViewById(R.id.toolbar_title)
        backButton=findViewById(R.id.back_btn)
        graph=findViewById(R.id.graph_present)
        noDataLable=findViewById(R.id.no_data)
        val groupId=intent.getIntExtra("groupId",0)
        val index=intent.getIntExtra("index",0)
        val credentials=getSharedPreferences("Credentials", Context.MODE_PRIVATE)
        val currencyName=credentials.getString("Currency","None")
        val present=Graphs(Present@this)
        if(groupId==1){
          title.text="Receivables/Payables"
        }
        if(groupId==2){
            title.text="Revenue/Expenses"
        }

        backButton.setOnClickListener {
            if(groupId==1){
                val intent=Intent(Present@this,AnalyticCredit::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)

            }
            if(groupId==2){
                val intent=Intent(Present@this,AnalyticCash::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)

            }
        }
        val title=graphTitle(index)[0]
        val subTitle=graphTitle(index)[1]
        val graphRt=present.oneItemGraph(title,subTitle,index)
        if (graphRt.available){
            noDataLable.visibility= View.GONE
            graph.visibility=View.VISIBLE
            graph.aa_drawChartWithChartModel(graphRt.graphData)
        }

    }
    private fun graphTitle(index:Int):Array<String>{
        return when(index){
            1->Array(2){"Payables";"Days"}
            2->Array(2){"Payables";"Months"}
            3->Array(2){"Receivables";"Days"}
            4->Array(2){"Receivables";"Months"}
            5->Array(2){"Monthly Expenditure";"Days"}
            6->Array(2){"Monthly Revenue";"Days"}
            7->Array(2){"Annual Expenditure";"Months"}
            8->Array(2){"Annual Revenue";"Months"}
            else->Array(1){""}
        }
    }
}