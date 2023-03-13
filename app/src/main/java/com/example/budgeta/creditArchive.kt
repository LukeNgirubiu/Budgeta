package com.example.budgeta
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgeta.Adpters.CreditsArchives
import com.example.budgeta.Repository.SqliteHelper
import com.example.budgeta.Utilities.Database
import com.example.budgeta.Utilities.Dialogs

class creditArchive : AppCompatActivity() {
    private lateinit var title: TextView
    private lateinit var noData:TextView
    private var index:Int?=1
    private lateinit var recycler: RecyclerView
    private lateinit var backButton: ImageButton
    private lateinit var imageBtn:ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credit_archive)
        title=findViewById(R.id.toolbar_title)
        backButton=findViewById(R.id.back_btn)
        recycler=findViewById(R.id.credits_recycler)
        noData=findViewById(R.id.no_data)
        imageBtn=findViewById(R.id.menu_btn)
        setRecycler()
        setTitle()
        imageBtn.setOnClickListener {
            val popUp=PopupMenu(this,imageBtn)
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
        backButton.setOnClickListener {
            val intent= Intent(Credits@this,Home::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }
    private fun setRecycler(){
        val  db= SqliteHelper(creditArchive@this)
        val listArchived=db.getAllCredits(index!!,true)
        if (listArchived.size>0){
            recycler.visibility=View.VISIBLE
            noData.visibility=View.GONE
        }
        if (listArchived.size<1){
            recycler.visibility=View.GONE
            noData.visibility=View.VISIBLE
            if (index==1){
                noData.text="No archived payables yet"
            }
            if (index==2){
                noData.text="No archived receivables yet"
            }
        }
        recycler.layoutManager= LinearLayoutManager(Credits@this)
        recycler.adapter= CreditsArchives(Credits@this,listArchived,index!!)
    }
    private fun setTitle(){
        if(index==1){
            title.text="Archived Payables"
        }
        else{
            title.text="Archived Receivable"
        }
    }

}