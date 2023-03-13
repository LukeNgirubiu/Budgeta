package com.example.budgeta.Repository

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.example.budgeta.Models.Remind
import com.example.budgeta.Models.RemindStatus
import com.example.budgeta.Utilities.DateFormating
import java.util.*
import kotlin.collections.ArrayList

class InterStorage(val context:Context) {
    companion object {
        private val fileName="reminders.txt"
    }
    fun storeReminder(items:ArrayList<String>){
        context.deleteFile(fileName)
        val dateForm=DateFormating()
        var itemsStr="${dateForm.shorterTime()}:${items.size}\n"
        for (itm in items){
            val item=itm.split(",")
            itemsStr=itemsStr+"${item[0]},${item[1]},${item[2]},${item[3]},${false}\n"
        }
        this.context.openFileOutput(fileName, Context.MODE_PRIVATE).use { output ->
            output.write(itemsStr.toByteArray())
            output.close()
        }
    }
    //updateReminder
    fun updateReminder(reminderArr:ArrayList<Remind>,index:Int):Int{
        var remind=reminderArr[index]
        reminderArr.removeAt(index)
        remind.checked=true
        reminderArr.add(index,remind)
       return saveReminder(reminderArr)
    }

    fun provideData():RemindStatus{
        var dataStr=""
        var found=0
        val timings=DateFormating()
        val remind=ArrayList<Remind>()
        try{
            context.openFileInput(fileName).use { stream ->
                dataStr = stream.bufferedReader().use {
                    it.readText()
                }
                stream.close()
            }
        }
        catch (e:Exception){
            println("Exception "+e.message)
        }

        if (dataStr.length>0){
            val items=dataStr.trim().split("\n")
            val cal = Calendar.getInstance()
            val expires ="${cal.get(Calendar.YEAR)}-${timings.numberFormating(cal.get(Calendar.MONTH)+1)}-${timings.numberFormating(cal.get(
                Calendar.DAY_OF_MONTH)+1)}"
            val info=items[0]
            if (info.split(":")[0].trim().equals(expires) && info.split(":")[1].trim().toInt()>0){
                found=1
                for (reminder in items.slice(1..items.size-1)){
                    val row=reminder.trim().split(",")
                    var rm=Remind(row[0].toInt(),row[1],row[2],row[3].toInt(),row[4].toBoolean())
                    remind.add(rm)
                }
            }
            if(info.split(":")[0].trim().equals(expires) && info.split(":")[1].trim().toInt()<0){
                found=2
            }

        }
        return RemindStatus(remind,found)
    }

    fun updateReminder(reminderArr:ArrayList<String>){
        var dataStr:String?=null
        try{
            context.openFileInput(fileName).use { stream ->
                dataStr = stream.bufferedReader().use {
                    it.readText()
                }
                stream.close()
            }
        }
        catch (e:Exception){
            println("Exception "+e.message)
        }
        var items=dataStr!!.split("\n")[0].split(":")
        var updateStr=""
        val dataBody=dataStr!!.substringAfter("\n")
        val availableRecord=dataBody.trim().split("\n")
        for (itm in reminderArr){
            var repetation=availableRecord.find { it.split(",")[0].trim().toInt()==itm.split(",")[0].trim().toInt() &&
                    it.split(",")[3].trim().toInt()==itm.split(",")[3].trim().toInt()
             }
            if (repetation==null){
                updateStr=updateStr+"$itm \n"
            }
        }
       dataStr="${items[0]}:${reminderArr.size}\n ${dataBody}"+updateStr
        context.deleteFile(fileName)
        this.context.openFileOutput(fileName, Context.MODE_PRIVATE).use { output ->
            output.write(dataStr?.toByteArray())
            output.close()
        }
    }
    fun upDateByColumn(tableId:Int,indexRemind:Int){
        val data2=provideData()
        var position=-1
        if (data2.found==1){
            data2.items.forEachIndexed{index,data->
                if(data.ID==tableId&&data.categ==indexRemind){
                    position=index
                }
            }
            if (position!=-1){
                data2.items.removeAt(position)
                saveReminder(data2.items)
            }
        }
    }
    private fun saveReminder(reminderArr:ArrayList<Remind>):Int{
        var status=0
        val dateForm=DateFormating()
        var itemsStr="${dateForm.shorterTime()}:${reminderArr.size}\n"
        for (itm in reminderArr){
            itemsStr=itemsStr+"${itm.ID},${itm.name},${itm.details},${itm.categ},${itm.checked}\n"
        }
        try {
            context.deleteFile(fileName)
            this.context.openFileOutput(fileName, Context.MODE_PRIVATE).use { output ->
                output.write("$itemsStr".toByteArray())
                output.close()
            }
            status=1
        }
        catch (e:Exception){
            status=2
        }
        return status
    }


}