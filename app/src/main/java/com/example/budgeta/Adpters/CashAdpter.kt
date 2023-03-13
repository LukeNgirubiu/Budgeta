package com.example.budgeta.Adpters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.budgeta.Models.Cash
import com.example.budgeta.R
import com.example.budgeta.Utilities.Database
import com.example.budgeta.Utilities.DateFormating
class CashAdpter(internal var cashLs:ArrayList<Cash>,internal var context:Context,internal var currency:String) :RecyclerView.Adapter<CashAdpter.CashView>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CashView {
        val layout=LayoutInflater.from(this.context).inflate(R.layout.cash_card_layout,parent,false)
        return CashView(layout)
    }
    override fun onBindViewHolder(holder: CashView, position: Int) {
       val cash=cashLs[position]
        val formatDate=DateFormating()
        val dbUtil=Database()
        holder.transactType.text=dbUtil.transationText(cash.TINDEX)
        val transactionCategoryTxt=if (cash.TTYPE==0) "Added" else  "Deducted"
        holder.transactionCategory.text=transactionCategoryTxt
        holder.Tdate.text=formatDate.formateDateTime(cash.TDATE)
        holder.amountTxt.text="${cash.CURRENCY}. "+cash.AMOUNT.toString()
        holder.detailsTxt.text=cash.DETAILS
    }

    override fun getItemCount(): Int {
        return cashLs.size
    }
    inner class CashView(itemView: View): RecyclerView.ViewHolder(itemView) {
       val Tdate=itemView.findViewById<TextView>(R.id.date_txt)
       val detailsTxt=itemView.findViewById<TextView>(R.id.details)
       val amountTxt=itemView.findViewById<TextView>(R.id.amount)
        val transactionCategory=itemView.findViewById<TextView>(R.id.transact_category)
       val transactType=itemView.findViewById<TextView>(R.id.transactType)
    }


}