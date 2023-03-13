package com.example.budgeta.Adpters

import android.content.Context
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.budgeta.Models.Pay
import com.example.budgeta.R
import org.w3c.dom.Text

class TransAdapter(internal var context: Context,internal var pays:ArrayList<Pay>,internal var currency:String,internal var credit:Double):RecyclerView.Adapter<TransAdapter.TransView>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransView {
        val layout=LayoutInflater.from(this.context).inflate(R.layout.credit_transaction,parent,false)
        return TransView(layout)
    }
    override fun onBindViewHolder(holder: TransView, position: Int) {
        val pay=pays[position]
        holder.creditedAmount.text="${pay.CURRENCY} ${pay.AMOUNT}"
        holder.pendingAmount.text="${pay.CURRENCY} ${pay.BALANCE}"
        holder.paidAmount.text="${pay.CURRENCY} ${credit-pay.BALANCE}"
        holder.transDate.text="${pay.TDATE}"
    }
    override fun getItemCount(): Int {
        return pays.size
    }
    inner class TransView(view: View):RecyclerView.ViewHolder(view) {
        val creditedAmount=view.findViewById<TextView>(R.id.amount_now)
        val pendingAmount=view.findViewById<TextView>(R.id.pending_amount)
        val paidAmount=view.findViewById<TextView>(R.id.paid_amount)
        val transDate=view.findViewById<TextView>(R.id.transaction_date)
        
    }
}