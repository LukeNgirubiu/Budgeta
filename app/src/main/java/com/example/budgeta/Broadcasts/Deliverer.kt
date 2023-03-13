package com.example.budgeta.Broadcasts

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast


class Deliverer(val context: Context): BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        when(resultCode){
            Activity.RESULT_OK->{
            Toast.makeText(context,"Delivered",Toast.LENGTH_LONG).show()
            }
            Activity.RESULT_CANCELED->{
                Toast.makeText(context, "Failed to deliver",
                    Toast.LENGTH_SHORT).show();
            }
        }

    }
}