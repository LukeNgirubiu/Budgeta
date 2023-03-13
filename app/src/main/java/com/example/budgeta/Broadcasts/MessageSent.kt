package com.example.budgeta.Broadcasts

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.widget.Toast

class MessageSent(val context: Context): BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {

        when(resultCode){
            Activity.RESULT_OK->{
                Toast.makeText(context, "Message sent successfully",
                    Toast.LENGTH_SHORT).show();
            }
            SmsManager.RESULT_ERROR_GENERIC_FAILURE->{
                Toast.makeText(context, "Generic failure",
                    Toast.LENGTH_SHORT).show();
            }
            SmsManager.RESULT_ERROR_NO_SERVICE->{
                Toast.makeText(context, "No service",
                    Toast.LENGTH_SHORT).show();
            }
            SmsManager.RESULT_ERROR_NULL_PDU->{
                Toast.makeText(context, "Null PDU", Toast.LENGTH_SHORT)
                    .show();
            }
            SmsManager.RESULT_ERROR_RADIO_OFF->{
                Toast.makeText(context, "Radio off",
                    Toast.LENGTH_SHORT).show();
            }

        }
    }
}