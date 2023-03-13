package com.example.budgeta
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgeta.Adpters.CreditAdpter
import com.example.budgeta.Broadcasts.Deliverer
import com.example.budgeta.Broadcasts.MessageSent
import com.example.budgeta.Constants.Constas.DELIVERED
import com.example.budgeta.Constants.Constas.SENT
import com.example.budgeta.Repository.SqliteHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
class Credits : AppCompatActivity() {
    private lateinit var title:TextView
    private lateinit var recycler:RecyclerView
    private lateinit var addItem:FloatingActionButton
    private lateinit var db:SqliteHelper
    private lateinit var backButton: ImageButton
    private var index:Int?=null
    private lateinit var noData:TextView
    private lateinit var  sendBroadcastReceiver: MessageSent
    private lateinit var deliveryBroadcastReciever: Deliverer
    private lateinit var imageBtn:ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credits)
        title=findViewById(R.id.toolbar_title)
        recycler=findViewById(R.id.credits_recycler)
        addItem=findViewById(R.id.add_item)
        db= SqliteHelper(Credits@this)
        backButton=findViewById(R.id.back_btn)
        noData=findViewById(R.id.no_data)
        imageBtn=findViewById(R.id.menu_btn)
        index=intent.getIntExtra("Index",1)
        setTitle()
        setRecycler()
        imageBtn.setOnClickListener {
            val popUp= PopupMenu(this,imageBtn)
            popUp.inflate(R.menu.credit_menus)
            popUp.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.payable->{
                        index=1
                        setTitle()
                        setRecycler()
                    }
                    R.id.credits->{
                        index=2
                        setTitle()
                        setRecycler()
                    }
                }
                false
            }
            popUp.show()
        }
        addItem.setOnClickListener {
            val intent= Intent(Credits@this,AddCredit::class.java)
            intent.putExtra("Id",0L)//0 for creation any other above zero for update
            intent.putExtra("Index",index)
            startActivity(intent)
        }
        backButton.setOnClickListener {
            val intent= Intent(Credits@this,Home::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
        val helpPref = getSharedPreferences("Help", Context.MODE_PRIVATE)
        val helpCredit=helpPref.getBoolean("Credit",false)
        if (helpCredit==false){
            val alertBuild= AlertDialog.Builder(this)
            alertBuild.setTitle("Help for executing")
            alertBuild.setCancelable(false)
            alertBuild.setMessage("You can toggle between payables and receivables by pressing on either of the two main button above")
            alertBuild.setPositiveButton("Noted",{dialog,which->
                val editHelp=helpPref.edit()
                editHelp.apply{
                    putBoolean("Credit",true)
                    commit()
                }
                dialog.dismiss()
            })
            val alert=alertBuild.create()
            alert.show()
        }
    }
    private fun setRecycler(){
        val lsCredits=db.getAllCredits(index!!)
        if (lsCredits.size>0){
            recycler.visibility= View.VISIBLE
            noData.visibility= View.GONE
        }
        if (lsCredits.size<1){
            recycler.visibility= View.GONE
            noData.visibility= View.VISIBLE
            if (index==1){
                noData.text="No payables yet"
            }
            if (index==2){
                noData.text="No receivables yet"
            }
        }
        val credentials=getSharedPreferences("Credentials", Context.MODE_PRIVATE)
        val currencyName=credentials.getString("Currency","None")
        recycler.layoutManager=LinearLayoutManager(Credits@this)
        recycler.adapter=CreditAdpter(Credits@this,lsCredits,currencyName!!,index!!)
    }
    private fun setTitle(){
        if(index==1){
            title.text="Payable cash"
        }
        else{
            title.text="Receivable cash"
        }
    }

    override fun onResume() {
        super.onResume()
        sendBroadcastReceiver= MessageSent(this)
        deliveryBroadcastReciever = Deliverer(this)
        registerReceiver(sendBroadcastReceiver, IntentFilter(SENT))
        registerReceiver(deliveryBroadcastReciever, IntentFilter(DELIVERED))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(sendBroadcastReceiver)
        unregisterReceiver(deliveryBroadcastReciever)
    }
}