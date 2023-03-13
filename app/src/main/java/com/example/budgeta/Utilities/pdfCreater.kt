package com.example.budgeta.Utilities

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.budgeta.Models.Budget
import com.example.budgeta.Repository.SqliteHelper
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.*


class pdfCreater(val context: Context) {
    fun createTable(budget:Budget,absolutePath:String,currency: String,send:Int,fromStr:String="",namesLs:String=""){
        val dateTimeStr= DateFormating()
        val file = File(absolutePath, "budgeta_items.pdf")
        val document = Document()
        document.setMargins(20f, 20f, 25f, 25f)
        document.pageSize = PageSize.A4
        val pdf = PdfWriter.getInstance(document, FileOutputStream(file))
        pdf.setFullCompression()
        document.open()
        val paragraphApp = Paragraph("Budgeta Application", Font(Font.FontFamily.TIMES_ROMAN,25f,Font.BOLD))
        paragraphApp.alignment=Element.ALIGN_CENTER
        document.add(paragraphApp)
        document.add(Paragraph(" "))
        val timeDate=dateTimeStr.timeNow().split(" ")
        val dateAlone=timeDate[0].split("-").reversed().joinToString("/")
        val dateTime=Paragraph(timeDate[1]+"  "+dateAlone,Font(Font.FontFamily.TIMES_ROMAN,14f,Font.BOLDITALIC))
        dateTime.alignment=Element.ALIGN_RIGHT
        document.add(dateTime)
        val paragraphTitle = Paragraph(budget.NAME,Font(Font.FontFamily.TIMES_ROMAN,18f,Font.BOLD))
        document.add(paragraphTitle)
        document.add(Paragraph(" "))
        document.add(dateParagraph("Created:",budget.TODAYDATE.split("-").reversed().joinToString("/")))
        document.add(Paragraph(" "))
        document.add(dateParagraph("Started:",budget.STARTDATE.split("-").reversed().joinToString("/")))
        document.add(Paragraph(" "))
        document.add(dateParagraph("Expiry:", budget.ENDDATE.split("-").reversed().joinToString("/"),baseColor=BaseColor(204, 51, 0)))
        val detailsHeader = Paragraph("Budget description",Font(Font.FontFamily.TIMES_ROMAN,15f,Font.UNDERLINE or Font.BOLD))
        document.add(Paragraph(" "))
        detailsHeader.alignment=Element.ALIGN_LEFT
        document.add(detailsHeader)
        val paragraphDetail = Paragraph(budget.DETAILS,Font(Font.FontFamily.TIMES_ROMAN,13f,Font.NORMAL))
        paragraphDetail.alignment=Element.ALIGN_JUSTIFIED
        document.add(paragraphDetail)
        document.add(Paragraph(" "))
        val tb=tableWithData(floatArrayOf(0.5f,2f,2f,1.4f,1.5f,2f,1.4f),budget,currency)
        document.add(tb)
        if (fromStr.isNotEmpty()){
            document.add(Paragraph(" "))
            document.add(Paragraph(" "))
            var customParagraph=Paragraph()
            val phraseFrom=Phrase("From  ",Font(Font.FontFamily.TIMES_ROMAN,14f,Font.BOLD))
            val phraseFromName=Phrase(fromStr,Font(Font.FontFamily.TIMES_ROMAN,14f,Font.BOLDITALIC,BaseColor(153, 38, 0)))
            customParagraph.add(phraseFrom)
            customParagraph.add(phraseFromName)
            document.add(customParagraph)
            document.add(Paragraph(" "))
            if (!namesLs.equals("Blank")){
                document.add(Paragraph("To",Font(Font.FontFamily.TIMES_ROMAN,14f,Font.BOLD)))
                val recieversName=namesLs.split(",")
                recieversName.forEachIndexed {index, s ->
                    var lsItem=""
                    if (index!=recieversName.size-1){
                        lsItem=s.trim()+","
                    }
                    if(index==recieversName.size-1){
                        lsItem=s.trim()
                    }
                    document.add(Paragraph(lsItem,Font(Font.FontFamily.TIMES_ROMAN,14f,Font.BOLDITALIC)))
                }
            }
        }
        document.addCreator("Budgeta")
        document.close()
        pdf.close()
        if (send==2){
            showFile(absolutePath,budget.NAME,action=Intent.ACTION_SEND)
        }
        else{
            showFile(absolutePath, budget.NAME)
        }
    }
    fun showFile(absolutePath:String,budgetName:String,action:String=Intent.ACTION_VIEW){
        try{
            val file= File(absolutePath+"/budgeta_items.pdf")
            val uriFile=if (Build.VERSION.SDK_INT< Build.VERSION_CODES.N) Uri.fromFile(file) else FileProvider.getUriForFile(context,
                "com.example.budgeta.provider",file)
            val share= Intent()
            share.setAction(action)
            if (action.equals("android.intent.action.SEND")){
             share.putExtra(Intent.EXTRA_STREAM, uriFile)
            share.setType("*/*")
            }
            else
            {
                share.setDataAndType (uriFile, "application/pdf");
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            }
            context.startActivity(share)
        }
        catch (e:Exception){
            val errorCaptured=e.message.toString()
            if (errorCaptured.contains("No Activity found to handle")){
                val cust=Dialogs(context)
                cust.customDialog("Opening pdf failure","A pdf application reader is not available on your device.\n Failed to open pdf for $budgetName")
            }
            else{
                Toast.makeText(context,"Failed to open the document for $budgetName",Toast.LENGTH_LONG).show()
            }

        }
    }

  private fun tableWithData(columns:FloatArray,budget: Budget,currency:String):PdfPTable{
      val db=SqliteHelper(this.context)
      val items=db?.getBudgetItems(budget.ID)
      val headerColumns= arrayOf(" ","Name","Unit Name","Quantity","Unit Cost ($currency)","Total ($currency)","Spent on")
      val table = PdfPTable(headerColumns.size)
      table.widthPercentage = 100f
      table.setWidths(columns)
      table.headerRows = 1
      table.defaultCell.verticalAlignment = Element.ALIGN_CENTER
      table.defaultCell.horizontalAlignment = Element.ALIGN_CENTER
      headerColumns.forEach {
          table.addCell(singleCell(it, header = true))
      }
      var totalBought=0.0
      var totalNot=0.0
      items!!.forEachIndexed { index, budgetItem ->
          var bought=""

          var backgroundColor=BaseColor(255, 204, 0)
          var fontColor=BaseColor(255,255,255)
          if (budgetItem.TICKED==1){
              bought="Yes"
              totalBought=totalBought+budgetItem.TAMOUNT
              backgroundColor=BaseColor(0,102,0)
          }
          else{
              totalNot=totalNot+budgetItem.TAMOUNT
              bought="No"
              fontColor=BaseColor(255, 0, 0)
          }
          val num=index+1
          table.addCell(singleCell(num.toString()))
          table.addCell(singleCell(budgetItem.NAME))
          table.addCell(singleCell(budgetItem.UNITNAME))
          table.addCell(singleCell(budgetItem.QUANTITY.toString()))
          table.addCell(singleCell(budgetItem.UNITCOST.toString()))
          table.addCell(singleCell(budgetItem.TAMOUNT.toString()))
          table.addCell(singleCell(bought, backgroundColor=backgroundColor, textColor = fontColor))
      }

      table.addCell(boldCell("Spent",12f, colspan = 4,font=Font(Font.FontFamily.TIMES_ROMAN, 17f, Font.BOLD,BaseColor(0, 0, 0))))
      table.addCell(boldCell("$currency. "+totalBought.toString(),22f,colspan = 3))
      table.addCell(boldCell("Unspent",12f, colspan = 4,font=Font(Font.FontFamily.TIMES_ROMAN, 17f, Font.BOLD,BaseColor(0, 0,  0))))
      table.addCell(boldCell("$currency. "+totalNot.toString(),12f,colspan = 3,font=Font(Font.FontFamily.TIMES_ROMAN, 17f, Font.BOLD,BaseColor(51, 102, 153))))
      table.addCell(boldCell("Total", 15f, colspan = 4,font=Font(Font.FontFamily.TIMES_ROMAN, 20f, Font.BOLD,BaseColor(0, 0, 0))))
      table.addCell(boldCell("$currency. "+budget.T_EST.toString(),22f,colspan = 3,font=Font(Font.FontFamily.TIMES_ROMAN, 20f, Font.BOLD,BaseColor(255, 102, 0))))
    return table
  }
private fun dateParagraph(title:String,date:String,baseColor:BaseColor=BaseColor(0, 102, 0)):Paragraph{
    val paragraph=Paragraph()
    paragraph.add(Phrase(title,Font(Font.FontFamily.TIMES_ROMAN,14f,Font.BOLD)))
    paragraph.add(Phrase("   "))
    paragraph.add(Phrase(date,Font(Font.FontFamily.TIMES_ROMAN,14f,Font.BOLDITALIC,baseColor)))
    return paragraph
}
private fun singleCell(content:String,backgroundColor:BaseColor=BaseColor( 255, 255, 255),header:Boolean=false,textColor:BaseColor= BaseColor(0,0,0)):PdfPCell{
    val phrase=if (header==true) Phrase(content,Font(Font.FontFamily.TIMES_ROMAN, 13f, Font.BOLD,textColor)) else Phrase(content,Font(Font.FontFamily.TIMES_ROMAN, 16f, Font.NORMAL,textColor))
   val pdCell= PdfPCell(phrase)
   pdCell.backgroundColor=backgroundColor
   pdCell.horizontalAlignment = Element.ALIGN_CENTER
   pdCell.verticalAlignment = Element.ALIGN_MIDDLE
   pdCell.setPadding(8f)
   pdCell.isUseAscender = true
   pdCell.paddingLeft = 4f
   pdCell.paddingRight = 4f
   pdCell.paddingTop = 8f
   pdCell.paddingBottom = 8f
  return pdCell
}
 private fun boldCell(content:String,padding:Float,backgroundColor:IntArray= intArrayOf(255, 255, 255),colspan:Int=1,font:Font=Font(Font.FontFamily.TIMES_ROMAN, 17f, Font.BOLD,BaseColor(0, 102, 0))):PdfPCell{
     val phrase=Phrase(content,font)
     val pdCell= PdfPCell(phrase)
     pdCell.backgroundColor=BaseColor(backgroundColor[0],backgroundColor[1],backgroundColor[2])
     pdCell.horizontalAlignment = Element.ALIGN_CENTER
     pdCell.verticalAlignment = Element.ALIGN_MIDDLE
     pdCell.setPadding(8f)
     pdCell.isUseAscender = true
     pdCell.paddingLeft = 2f
     pdCell.paddingRight = 4f
     pdCell.paddingTop = padding
     pdCell.paddingBottom = padding
     pdCell.colspan=colspan
     return pdCell
 }
}