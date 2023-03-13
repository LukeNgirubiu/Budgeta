package com.example.budgeta.Utilities
import android.content.Context
import com.example.budgeta.Models.Annual
import com.example.budgeta.Models.CashAnaly
import com.example.budgeta.Models.CreditAnaly
import com.example.budgeta.Models.OneDGraph
import com.example.budgeta.Repository.SqliteHelper
import java.util.*
import kotlin.collections.HashMap

class GraphData(val context: Context) {
    fun revExPerMonth(): Annual {
      val db=SqliteHelper(this.context)
      val deposits=db.getExpenditureYear("TINDEX=2")
      val expenses=db.getExpenditureYear("TINDEX IN(1,3)")
      val calendar=Calendar.getInstance()
      val month=calendar.get(Calendar.MONTH)+1
      val timing=DateFormating()
      val monthArr = Array(month) { "" }
      val expenseValues:Array<Any> = Array(month) { 0.0 }
      val revenueValue:Array<Any> =  Array(month) { 0.0 }
        var iteratorIndex=0
        for (x in 1..month){
           val monthName=timing.getMonthName("${calendar.get(Calendar.YEAR)}-${timing.numberFormating(x)}-01")
            monthArr[iteratorIndex]=monthName
          if (deposits.containsKey(x)){
            revenueValue[iteratorIndex]=deposits.getValue(x)
          }
          if (!deposits.containsKey(x)){
              revenueValue[iteratorIndex]=0.0
          }
          if (expenses.containsKey(x)){
              expenseValues[iteratorIndex]=expenses.getValue(x)
          }
          if (!expenses.containsKey(x)){
              expenseValues[iteratorIndex]=0.0
          }
            iteratorIndex++
        }
    return Annual(monthArr, expenseValues, revenueValue)
    }
    fun creditsPerMonth(): Annual {
        val db=SqliteHelper(this.context)
        val payables=db.getCreditYear(4)
        val receivables=db.getCreditYear(6)
        val calendar=Calendar.getInstance()
        val month=calendar.get(Calendar.MONTH)+1
        val timing=DateFormating()
        val monthArr= Array(month) { "" }
        val payableArr:Array<Any> = Array(month) { 0.0 }
        val receivableArr:Array<Any> = Array(month) { 0.0 }
        var iteratorIndex=0
        for (x in 1..month){
            val monthName=timing.getMonthName("${calendar.get(Calendar.YEAR)}-${timing.numberFormating(x)}-01")
            monthArr[iteratorIndex]=monthName
            if (payables.containsKey(x)){
                payableArr[iteratorIndex]=payables.getValue(x)
            }
            if (!payables.containsKey(x)){
                payableArr[iteratorIndex]=0.0
            }
            if (receivables.containsKey(x)){
                receivableArr[iteratorIndex]=receivables.getValue(x)
            }
            if (!receivables.containsKey(x)){
                receivableArr[iteratorIndex]=0.0
            }
            iteratorIndex++
        }
        return Annual(monthArr,receivableArr, payableArr)//,
    }
    fun cashAnalyticData():CashAnaly{
     val time=DateFormating()
     val db=SqliteHelper(this.context)
     val dateToday=time.timeNow().split(" ")
     val monthName=time.getMonthName(dateToday[0])
     val year=dateToday[0].split("-")[0].trim().toInt()
     val monthlyExpenditure=db.getCashTotal("TINDEX IN(1,3)",time.dateMonthRange()[0],time.dateMonthRange()[1])
     val monthlyRevenue=db.getCashTotal("TINDEX=2",time.dateMonthRange()[0],time.dateMonthRange()[1])
     val annualExpenditure=db.getCashTotal("TINDEX IN(1,3)",time.dateRangeYear()[0],time.dateRangeYear()[1])
     val annualRevenue=db.getCashTotal("TINDEX=2",time.dateRangeYear()[0],time.dateRangeYear()[1])
     return CashAnaly(monthName,year,monthlyExpenditure,monthlyRevenue,annualExpenditure,annualRevenue)
    }
    fun creditAnalyticData():CreditAnaly{
        val time=DateFormating()
        val db=SqliteHelper(this.context)
        val dateToday=time.timeNow().split(" ")
        val monthName=time.getMonthName(dateToday[0])
        val year=dateToday[0].split("-")[0].trim().toInt()
        val monthlyPayable=db.getCreditTotal(4,time.dateMonthRange()[0],time.dateMonthRange()[1])
        val annualPayable=db.getCreditTotal(4,time.dateRangeYear()[0],time.dateRangeYear()[1])
        val monthlyDept=db.getCreditTotal(3,time.dateMonthRange()[0],time.dateMonthRange()[1])
        val anualDept=db.getCreditTotal(3,time.dateRangeYear()[0],time.dateRangeYear()[1])
        return CreditAnaly(monthName,year,monthlyPayable,annualPayable,monthlyDept,anualDept)
    }

    fun getSingleRanges(index:Int):OneDGraph{
        val db=SqliteHelper(this.context)
        val time=DateFormating()
        return  when(index){
            1->{
                convertToArr(db.getGroupedCreditCash(4,"DAY","strftime('%d', TDATE) AS",time.dateMonthRange()[0],time.dateMonthRange()[1],"WHERE"))
            }
            2->{
                convertToArr(db.getGroupedCreditCash(4,"MONTH","strftime('%m', TDATE) AS",time.dateRangeYear()[0],time.dateRangeYear()[1],"WHERE"),true)
            }
            //Credit
            3->{
                convertToArr(db.getGroupedCreditCash(3,"DAY","strftime('%d', TDATE) AS",time.dateMonthRange()[0],time.dateMonthRange()[1],"WHERE"))
            }
            4->{
                convertToArr(db.getGroupedCreditCash(3,"MONTH","strftime('%m', TDATE) AS",time.dateRangeYear()[0],time.dateRangeYear()[1],"WHERE"),true)
            }
            //Debts
            5->{
                convertToArr(db.getGroupedCreditCash(1,"DAY","strftime('%d', TDATE) AS",time.dateMonthRange()[0],time.dateMonthRange()[1],"WHERE TINDEX IN(1,5) AND"))
            }
            6->{
                convertToArr(db.getGroupedCreditCash(1,"DAY","strftime('%d', TDATE) AS",time.dateMonthRange()[0],time.dateMonthRange()[1],"WHERE TINDEX=2 AND"))
            }
            7->{
                convertToArr(db.getGroupedCreditCash(1,"MONTH","strftime('%m', TDATE) AS",time.dateRangeYear()[0],time.dateRangeYear()[1],"WHERE TINDEX IN(1,5) AND"))
            }
            8->{
                convertToArr(db.getGroupedCreditCash(1,"MONTH","strftime('%m', TDATE) AS",time.dateRangeYear()[0],time.dateRangeYear()[1],"WHERE TINDEX=2 AND"))
            }
            //Cashes Expenditures and Revenues
            else->{
                OneDGraph(Array(1){""},Array(1){0.0})
            }
        }
    }
    fun convertToArr(map:HashMap<String,Double>,month:Boolean=false):OneDGraph{
        val timeLable=Array(map.size){""}
        val data:Array<Any> = Array(map.size){0.0}
         var index=0
        val dateUtil=DateFormating()
        for (lable in map.keys){
            if (month==true){
                val calInstance = Calendar.getInstance()
               timeLable[index]=dateUtil.getMonthName("${calInstance.get(Calendar.YEAR)}-$lable-${calInstance.get(Calendar.DAY_OF_MONTH)}")
            }
            if(month==false)
            {
                timeLable[index]=lable
            }
            index++
        }
        index=0
        for (value in map.values){
            data[index]=value
            index++
        }
        return OneDGraph(timeLable,data)
    }
}
