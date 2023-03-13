package com.example.budgeta.Utilities
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import com.example.budgeta.Constants.Constas.DELIVERED
import com.example.budgeta.Constants.Constas.SENT
import com.example.budgeta.Models.Budget
import com.example.budgeta.Models.Credit
import com.example.budgeta.R


class Dialogs(val context: Context) {



     fun simpleDialog(dialogText:String):AlertDialog{
         val dialogBuild=AlertDialog.Builder(this.context)
        val layout=LayoutInflater.from(this.context).inflate(R.layout.simple_dialog,null)
        var dialogTextView:TextView=layout.findViewById(R.id.text_dialog)
        dialogTextView.text=dialogText
        dialogBuild.setView(layout)
       dialogBuild.setCancelable(false)
        val alert:AlertDialog=dialogBuild.create()
        alert.show()
        return alert
    }
    fun statusDialog(dialogText:String,status:Boolean):AlertDialog{
        val dialogBuild=AlertDialog.Builder(this.context)
        val layout=LayoutInflater.from(this.context).inflate(R.layout.status_dialog,null)
        var dialogTextView:TextView=layout.findViewById(R.id.textMessage)
        val imdDw:ImageView=layout.findViewById(R.id.drw_img)
        dialogTextView.text=dialogText
        if (status){
            imdDw.setImageResource(R.drawable.task_done)
        }
        else{
            dialogTextView.setTextColor(Color.RED)
            imdDw.setImageResource(R.drawable.failed_status)
        }
        dialogBuild.setView(layout)
        dialogBuild.setCancelable(false)
        val alert:AlertDialog=dialogBuild.create()
        alert.show()
        return alert
    }
    fun sendTextMessage(phoneNumber:String,messageTemplate:String,recieverName:String):AlertDialog{
        val dialogBuild=AlertDialog.Builder(this.context)
      val layout=LayoutInflater.from(this.context).inflate(R.layout.sms_layout,null)
      val contact=layout.findViewById<EditText>(R.id.phone_number)
      val message=layout.findViewById<EditText>(R.id.sms)
      val sendText=layout.findViewById<Button>(R.id.send)
      val recieverNameTxt=layout.findViewById<TextView>(R.id.name_label)
      val phoneNumberValid=layout.findViewById<TextView>(R.id.phone_number_error)
      val messageError=layout.findViewById<TextView>(R.id.sms_error)
      val cancel=layout.findViewById<Button>(R.id.cancel)
      contact.setText(phoneNumber)
      message.setText(messageTemplate)
      recieverNameTxt.setText(recieverName )

      val view=dialogBuild.setView(layout)
      val alert=view.create()
        sendText.setOnClickListener {
            var valid=true
            val contactNumber=contact.text.toString().trim()
            val textMessage=message.text.toString()
            if (contactNumber.isBlank()){
                valid=false
                phoneNumberValid.text="Phone number is required"
                phoneNumberValid.visibility=View.VISIBLE
            }
            if (!contactNumber.isBlank() && contactNumber.length<9){
                valid=false
                phoneNumberValid.text="Phone number is too short"
                phoneNumberValid.visibility=View.VISIBLE
            }
            if (textMessage.isBlank()){
                messageError.text="A text for the message is required"
                valid=false
                messageError.visibility=View.VISIBLE
            }
            if (valid){
                phoneNumberValid.visibility=View.GONE
                messageError.visibility=View.GONE
                alert.dismiss()
                Toast.makeText(context,"Sending ....",Toast.LENGTH_SHORT).show()
                val smsManager: SmsManager = SmsManager.getDefault()
                val sentIntent=Intent(SENT)
                val deliveredIntent=Intent(DELIVERED)
                smsManager.sendTextMessage(
                    contactNumber, null, textMessage,
                    PendingIntent.getBroadcast(
                        context, 0, sentIntent, PendingIntent.FLAG_IMMUTABLE
                    ), PendingIntent.getBroadcast(
                        context, 0,
                        deliveredIntent, PendingIntent.FLAG_IMMUTABLE
                    )
                )

            }
        }
        cancel.setOnClickListener {
            alert.dismiss()
        }
      alert.show()

   return alert
    }
fun customDialog(title:String,message:String,cancelable:Boolean=true){
    val dialogBuild=AlertDialog.Builder(this.context)
    dialogBuild.setTitle(title)
    dialogBuild.setMessage(message)
    dialogBuild.setCancelable(cancelable)
    dialogBuild.setPositiveButton("Okay"){dialogueInterface,which->
       dialogueInterface.cancel()
    }
    dialogBuild.setNegativeButton("Cancel"){dialogueInterface,which->
        dialogueInterface.dismiss()
    }
    val alert=dialogBuild.create()
    alert.show()
}
    fun callsSmsDialog(callOrSms:Int,credit: Credit,index:Int=0){
        val dialogBuild=AlertDialog.Builder(this.context)
        val callingSmes=if (callOrSms==1) "Charges applies when making a call from this application" else "Charges applies when texting from this application"
        dialogBuild.setTitle("Charges applies")
        dialogBuild.setMessage(callingSmes)
        dialogBuild.setCancelable(false)
        dialogBuild.setPositiveButton("Okay"){dialogueInterface,which->
            if (callOrSms==1){
                dialogueInterface.cancel()
                if(ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)== PackageManager.PERMISSION_GRANTED){
                    val phone_intent = Intent(Intent.ACTION_CALL)
                    phone_intent.data = Uri.parse("tel:${credit.CELLPHONENO}")
                    context.startActivity(phone_intent)
                }
            }
            if (callOrSms==2){
                dialogueInterface.dismiss()
                val tempText=if(index==1){
                    "Hello? I still remember that i owe you some cash. I am processing it"
                }
                else{
                    "Hello? Kindly consider settling the debt you owe me. Thanks"
                }
                if(ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.SEND_SMS)== PackageManager.PERMISSION_GRANTED){
                    sendTextMessage(credit.CELLPHONENO,tempText,"Text "+credit.NAME)
                }
                else{
                    val alertBuild=AlertDialog.Builder(this.context)
                    alertBuild.setTitle("Sending sms permission not granted")
                    alertBuild.setMessage("Grant this permission on settings for this application")
                    alertBuild.setCancelable(false)
                    alertBuild.setPositiveButton("Okay"){dialogueInterface,which->
                        dialogueInterface.dismiss()
                    }
                    alertBuild.create().show()
                }
            }
        }
        dialogBuild.setNegativeButton("Cancel"){dialogueInterface,which->
            dialogueInterface.dismiss()
        }
        val alert=dialogBuild.create()
        alert.show()
    }
    fun pdfDialogList(budget: Budget, absolutePath:String, currency: String){
        val dialogBuild=AlertDialog.Builder(this.context)
        val layout=LayoutInflater.from(this.context).inflate(R.layout.share_list,null)
        val fromName=layout.findViewById<EditText>(R.id.from_name)
        val namesList=layout.findViewById<EditText>(R.id.names_list)
        val fromNameError=layout.findViewById<TextView>(R.id.from_name_error)
        val send=layout.findViewById<Button>(R.id.send)
        val cancel=layout.findViewById<Button>(R.id.cancel)
        val view=dialogBuild.setView(layout)
        val alert=view.create()
        send.setOnClickListener {
            var valid=true
            if (fromName.text.isBlank()){
                valid=false
                fromNameError.visibility=View.VISIBLE
                fromNameError.text="* Name is required"
            }
            if (namesList.text.isBlank()){
                namesList.setText("Blank")
            }
            if (valid==true){
                fromNameError.visibility=View.GONE
                alert.cancel()
                val pdf=pdfCreater(context)
                pdf.createTable(budget,absolutePath,currency,2,fromName.text.toString(),namesList.text.toString())
            }
        }
        cancel.setOnClickListener {
            alert.cancel()
        }
        alert.show()
    }

    fun roundToOne(number:Double):Double{
        val numStr=number.toString()
        var num:Double=0.0
        if(numStr.contains(".")){
         val numSp=numStr.split(".")
            if(numSp[1].length>1){
                val numDec=numSp[1].substring(0,1)
                val fullStr=numSp[0]+"."+numDec.trim()
               num=fullStr.toDouble()
            }
            else{
                num=number
            }
        }
        else{
         num=number
        }
        return num
    }


}




