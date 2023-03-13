package com.example.budgeta.Utilities

import android.content.Context
import com.example.budgeta.Constants.Constas
import com.example.budgeta.Models.GraphReturn
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartFontWeightType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AAStyle


class Graphs(val context: Context) {
    fun comparisonAnnual(item1:String,item2:String,graphIndex:Int=1):AAChartModel{
        val graphData=GraphData(this.context)
        var dataLabels=graphData.revExPerMonth()
        if (graphIndex==2){
            dataLabels=graphData.creditsPerMonth()
        }
        val aaChartModel : AAChartModel = AAChartModel()
                .chartType(AAChartType.Bar)
                .yAxisTitle("Amount")
                .axesTextColor("#FFFFFFFF")
                .dataLabelsStyle(AAStyle.Companion.style(color = "#FFFFFFFF"))//#ff0000
                .title("Graph for $item1/$item2")
                .titleStyle(AAStyle.Companion.style(color = "#FFFFFFFF"))
                .subtitle("Months/Amount")
                .legendEnabled(true)
                .subtitleStyle(AAStyle.Companion.style(color = "#FFFFFFFF"))
                .backgroundColor(Constas.bgColor1)
                //#55c8a8a Marron
                .dataLabelsEnabled(true)
                .legendEnabled(true)
                .categories(dataLabels.months)
                .series(arrayOf(
                        AASeriesElement()
                                .name(item1).color(Constas.appColor)// #336600
                                .data(dataLabels.column2),
                        AASeriesElement()
                                .name(item2).color(Constas.colorRed)
                                .data(dataLabels.column1),
                )
                )

       return aaChartModel
    }
    fun oneItemGraph(title:String,subTitle:String,graphIndex: Int):GraphReturn{
        //AAChartModel
        val graphData=GraphData(this.context)
        val data=graphData.getSingleRanges(graphIndex)
        var available=false
        val aaChartModel : AAChartModel = AAChartModel()
                .chartType(AAChartType.Bar)
                .yAxisTitle("Amount")
                .axesTextColor("#FFFFFFFF")
                .dataLabelsStyle(AAStyle.Companion.style(color = "#FFFFFFFF"))//#ff0000
                .title("Graph for $title")
                .titleStyle(AAStyle.Companion.style(color = "#FFFFFFFF"))
                .subtitle("$subTitle/Cash")
                .legendEnabled(true)
                .subtitleStyle(AAStyle.Companion.style(color = "#FFFFFFFF"))
                .backgroundColor(Constas.bgColor1)
                .dataLabelsEnabled(true)
                .legendEnabled(true)
                .categories(data.lables)
                .series(arrayOf(
                        AASeriesElement()
                                .name(title).color(Constas.colorRed)// #336600
                                .data(data.data),
                )
                )
          if(data.data.size>0){
          available=true
          }
        return GraphReturn(aaChartModel,available)
    }

}