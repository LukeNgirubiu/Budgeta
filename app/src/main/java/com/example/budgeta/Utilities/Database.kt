package com.example.budgeta.Utilities

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import com.example.budgeta.BuildConfig
import com.example.budgeta.Models.*
import java.io.File

class Database {
     fun budgetWrapper(budget: Budget): ContentValues {
        val budgetCont= ContentValues()
        budgetCont.put("NAME", budget.NAME)
        budgetCont.put("CATEGORY",budget.CATEGORY)
        budgetCont.put("DETAILS",budget.DETAILS)
        budgetCont.put("STARTDATE",budget.STARTDATE)
        budgetCont.put("EXPIRYDATE",budget.ENDDATE)
        budgetCont.put("TOTALCOST",budget.T_EST)
        budgetCont.put("TODAYDATE",budget.TODAYDATE)
        budgetCont.put("STARTED",budget.STARTED)
        budgetCont.put("ITEMS",budget.ITEMS)
         budgetCont.put("CURRENCY",budget.CURRENCY)
        return budgetCont
    }
     fun budgetCursor(query: Cursor): Budget {
        val budget= Budget()
        budget.ID=query.getString(query.getColumnIndexOrThrow("ID")).toInt()
        budget.NAME=query.getString(query.getColumnIndexOrThrow("NAME"))
        budget.DETAILS=query.getString(query.getColumnIndexOrThrow("DETAILS"))
        budget.CATEGORY=Integer.parseInt(query.getString(query.getColumnIndexOrThrow("CATEGORY")))
        budget.ENDDATE=query.getString(query.getColumnIndexOrThrow("EXPIRYDATE"))
        budget.STARTDATE=query.getString(query.getColumnIndexOrThrow("STARTDATE"))
        budget.TODAYDATE=query.getString(query.getColumnIndexOrThrow("TODAYDATE"))
        budget.STARTED=query.getString(query.getColumnIndexOrThrow("STARTED")).toInt()
         budget.ITEMS=query.getString(query.getColumnIndexOrThrow("ITEMS")).toInt()
        budget.T_EST=query.getString(query.getColumnIndexOrThrow("TOTALCOST")).toDouble()
         budget.CURRENCY=query.getString(query.getColumnIndexOrThrow("CURRENCY"))
        return budget
    }
    fun budgetItemCursor(query:Cursor): BudgetItem{
        val budgetItem=BudgetItem()
        budgetItem.ID=query.getString(query.getColumnIndexOrThrow("ID")).toInt()
        budgetItem.BUDGETID=query.getString(query.getColumnIndexOrThrow("BUDGETID")).toInt()
        budgetItem.NAME=query.getString(query.getColumnIndexOrThrow("NAME"))
        budgetItem.QUANTITY=query.getString(query.getColumnIndexOrThrow("QUANTITY")).toInt()
        budgetItem.UNITCOST=query.getString(query.getColumnIndexOrThrow("UNITCOST")).toDouble()
        budgetItem.ADATE=query.getString(query.getColumnIndexOrThrow("ADATE"))//
        budgetItem.UNITNAME=query.getString(query.getColumnIndexOrThrow("UNITNAME"))
        budgetItem.TICKED=query.getString(query.getColumnIndexOrThrow("TICKED")).toInt()
        budgetItem.TAMOUNT=query.getString(query.getColumnIndexOrThrow("TAMOUNT")).toDouble()
        budgetItem.CURRENCY=query.getString(query.getColumnIndexOrThrow("CURRENCY"))
        return budgetItem
    }
     fun budgetItemWrapper(budgetItem: BudgetItem): ContentValues {
        val bIcontents= ContentValues()
        bIcontents.put("NAME",budgetItem.NAME)
        bIcontents.put("BUDGETID",budgetItem.BUDGETID)
        bIcontents.put("QUANTITY",budgetItem.QUANTITY)
        bIcontents.put("UNITCOST",budgetItem.UNITCOST)
         bIcontents.put("UNITNAME",budgetItem.UNITNAME)
        bIcontents.put("TAMOUNT",budgetItem.TAMOUNT)
        bIcontents.put("ADATE",budgetItem.ADATE)
         bIcontents.put("TICKED",budgetItem.TICKED)
         bIcontents.put("CURRENCY",budgetItem.CURRENCY)
        return bIcontents
    }
    fun wrapCash(cash:Cash):ContentValues{
        val cashcontent= ContentValues()
        cashcontent.put("TDATE",cash.TDATE )
        cashcontent.put("DETAILS",cash.DETAILS)
        cashcontent.put("AMOUNT",cash.AMOUNT)
        cashcontent.put("TINDEX",cash.TINDEX)
        cashcontent.put("TID",cash.TID)
        cashcontent.put("TTYPE",cash.TTYPE)
        cashcontent.put("CTOTAL",cash.CTOTAL)
        cashcontent.put("CURRENCY",cash.CURRENCY)
        return cashcontent
    }
    fun cashCursor(cursor:Cursor):Cash{
        val cash=Cash()
        cash.ID=cursor.getString(cursor.getColumnIndexOrThrow("ID")).toInt()
        cash.TDATE=cursor.getString(cursor.getColumnIndexOrThrow("TDATE"))
        cash.DETAILS=cursor.getString(cursor.getColumnIndexOrThrow("DETAILS"))
        cash.AMOUNT=cursor.getString(cursor.getColumnIndexOrThrow("AMOUNT")).toDouble()
        cash.TINDEX=cursor.getString(cursor.getColumnIndexOrThrow("TINDEX")).toInt()
        cash.TID=cursor.getString(cursor.getColumnIndexOrThrow("TID")).toLong()
        cash.TTYPE=cursor.getString(cursor.getColumnIndexOrThrow("TTYPE")).toInt()
        cash.CTOTAL=cursor.getString(cursor.getColumnIndexOrThrow("CTOTAL")).toDouble()
        cash.CURRENCY=cursor.getString(cursor.getColumnIndexOrThrow("CURRENCY"))
        return cash
    }
    fun wrapCredit(credit:Credit):ContentValues{
        val content=ContentValues()
        content.put("TDATE",credit.TDATE)
        content.put("PAYMENTDATE",credit.PAYMENTDATE)
        content.put("NAME",credit.NAME)
        content.put("DETAILS",credit.DETAILS)
        content.put("CELLPHONENO",credit.CELLPHONENO)
        content.put("STARTED",credit.STARTED)
        content.put("BALANCE",credit.BALANCE)
        content.put("AMOUNT",credit.AMOUNT)
        content.put("CURRENCY",credit.CURRENCY)
        return content
    }
    fun creditCursor(cursor:Cursor):Credit{
        val credit=Credit()
        credit.ID=cursor.getString(cursor.getColumnIndexOrThrow("ID")).toLong()
        credit.TDATE=cursor.getString(cursor.getColumnIndexOrThrow("TDATE"))
        credit.DETAILS=cursor.getString(cursor.getColumnIndexOrThrow("DETAILS"))
        credit.AMOUNT=cursor.getString(cursor.getColumnIndexOrThrow("AMOUNT")).toDouble()
        credit.BALANCE=cursor.getString(cursor.getColumnIndexOrThrow("BALANCE")).toDouble()
        credit.NAME=cursor.getString(cursor.getColumnIndexOrThrow("NAME"))
        credit.CELLPHONENO=cursor.getString(cursor.getColumnIndexOrThrow("CELLPHONENO"))
        credit.STARTED=cursor.getString(cursor.getColumnIndexOrThrow("STARTED")).toInt()
        credit.PAYMENTDATE=cursor.getString(cursor.getColumnIndexOrThrow("PAYMENTDATE"))
        credit.CURRENCY=cursor.getString(cursor.getColumnIndexOrThrow("CURRENCY"))
        return credit
    }

    fun settleWrap(pay: Pay,creditId:String):ContentValues{
        val content=ContentValues()
        content.put("TDATE",pay.TDATE)
        content.put("AMOUNT",pay.AMOUNT)
        content.put("BALANCE",pay.BALANCE)
        content.put("$creditId",pay.CREDITID)
        content.put("CURRENCY",pay.CURRENCY)
        return content
    }
    fun settleCursor(cursor:Cursor,creditId:String):Pay{
        val pay=Pay()
        pay.ID=cursor.getString(cursor.getColumnIndexOrThrow("ID")).toLong()
        pay.TDATE=cursor.getString(cursor.getColumnIndexOrThrow("TDATE"))
        pay.BALANCE=cursor.getString(cursor.getColumnIndexOrThrow("BALANCE")).toDouble()
        pay.AMOUNT=cursor.getString(cursor.getColumnIndexOrThrow("AMOUNT")).toDouble()
        pay.CREDITID=cursor.getString(cursor.getColumnIndexOrThrow("$creditId")).toLong()
        pay.CURRENCY=cursor.getString(cursor.getColumnIndexOrThrow("CURRENCY"))
        return pay
    }
    fun transationText(transactionIndex:Int):String{
        return when(transactionIndex){
            1->"Budget item funding"
            2->"Cash Deposit"
            3->"Cash Withdrawal"
            4->"Cash from credit"
            5->"Credit repayment"
            6->"Offered credit"
            7->"Received cash from receivable"
            else->"Unavailable"
        }
    }
    fun createCashCsvShare(items:ArrayList<Cash>,currency:String,context: Context,absolutePath:String,fileStr:String="budgeta_cash.csv"){
        println("Number ${items.size}")
        if (items.size>0){
            var csvStr="\"Date\",\"Transaction Type\",\"Action\",\"Details\",\"Amount\"\n"
            val formatDate=DateFormating()
            items.forEach {
                val dateTimeArr=it.TDATE.split(" ")
                val dateArr=dateTimeArr[0].split("-").reversed()
                val processedDate=dateTimeArr[1]+" "+formatDate.numberFormating(dateArr[0].toInt())+"/"+formatDate.numberFormating(dateArr[1].toInt())+"/"+dateArr[2]
                csvStr=csvStr+"\"$processedDate\",\"${transationText(it.TINDEX)}\",\"${if (it.TTYPE==0) "Added" else  "Deducted"}\",\"${it.DETAILS}\",${currency+it.AMOUNT.toString()}\n"
            }
            context.openFileOutput(fileStr, Context.MODE_PRIVATE).use { output ->
                output.write(csvStr.toByteArray())
                output.close()
            }
            shareFile(absolutePath,context,fileStr)
        }
        else{
            Toast.makeText(context,"Currently no data",Toast.LENGTH_SHORT).show()
        }
    }
    private fun shareFile(path:String,context:Context,fileName:String){
        try {
            val file= File(path+"/$fileName")
            val uriFile=if (Build.VERSION.SDK_INT<Build.VERSION_CODES.N) Uri.fromFile(file) else FileProvider.getUriForFile(context,BuildConfig.APPLICATION_ID+".provider",file)
            val share= Intent()
            share.setAction(Intent.ACTION_VIEW)
            share.setDataAndType (uriFile, "text/csv");
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(share)
        }
        catch (e:Exception){
            if (e.message.toString()!!.contains("No Activity found")){
                val dialog=Dialogs(context)
                dialog.customDialog("Spreadsheet reader not found","Install a spreadsheet reading application")
            }
            else{
                Toast.makeText(context,"Unknown error",Toast.LENGTH_SHORT).show()
            }

        }
    }
}