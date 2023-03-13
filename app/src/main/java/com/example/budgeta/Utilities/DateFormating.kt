package com.example.budgeta.Utilities
import java.text.SimpleDateFormat
import java.util.*

class DateFormating {
     fun formateDateTime(dateStr:String):String{
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val getDateStr=dateStr.split(" ")
        val formated=sdf.parse(getDateStr[0].trim())
        val calInstance = Calendar.getInstance()
        calInstance.time = formated
        val monthName=calInstance.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US)
        val datesStr=getDateStr[0].split("-")
        return "${getDateStr[1]}, ${datesStr[0]} $monthName ${datesStr[2]}"
    }
    fun numberFormating(value:Int):String{
      return if (value>=10) "$value" else "0$value"
   }
   fun timeNow():String{
      val calender=Calendar.getInstance()
      return "${calender.get(Calendar.YEAR)}-${numberFormating(calender.get(Calendar.MONTH)+1)}-${numberFormating(calender.get(Calendar.DAY_OF_MONTH))} ${numberFormating(calender.get(Calendar.HOUR_OF_DAY))}:${numberFormating(calender.get(Calendar.MINUTE))}"
   }
    fun shorterTime():String{
        val calender=Calendar.getInstance()
        return "${calender.get(Calendar.YEAR)}-${numberFormating(calender.get(Calendar.MONTH)+1)}-${numberFormating(calender.get(Calendar.DAY_OF_MONTH)+1)}"
    }
    fun toDashedDate(dateStr:String):String{
        val date=dateStr.split("/")
        return "${date[2].trim()}-${numberFormating(date[1].trim().toInt())}-${numberFormating(date[0].trim().toInt())}"
    }
    fun toSlashedDate(dateStr:String):String{
        val date=dateStr.split("-")
        return "${numberFormating(date[2].trim().toInt())}/${numberFormating(date[1].trim().toInt())}/${date[0].trim()}"
    }
    fun dateRangeYear():Array<String>{
        val calender= Calendar.getInstance()
        val yearStart="${calender.get(Calendar.YEAR)}-01-01 00:00"
        return arrayOf(yearStart,timeNow())
    }
    fun getMonthName(dateStr:String):String{
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val formated=sdf.parse(dateStr)//
        val calInstance = Calendar.getInstance()
        calInstance.time = formated
        return calInstance.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US)
    }
    fun dateMonthRange():Array<String>{
        val calender= Calendar.getInstance()
        val monthStart="${calender.get(Calendar.YEAR)}-${numberFormating(calender.get(Calendar.MONTH)+1)}-01 00:00"
        return arrayOf(monthStart,timeNow())
    }

}