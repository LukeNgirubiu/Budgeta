package com.example.budgeta
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgeta.Adpters.CashAdpter
import com.example.budgeta.HelpActivities.CashHelp
import com.example.budgeta.Models.Cash
import com.example.budgeta.Repository.SqliteHelper
import com.example.budgeta.Utilities.Database
import com.example.budgeta.Utilities.Dialogs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.formula.functions.Index
import java.io.File


class Cash : AppCompatActivity() {
    private lateinit var cashRecy:RecyclerView
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var recyAdapter:CashAdpter
    private lateinit var db:SqliteHelper
    private lateinit var noData:RelativeLayout
    private lateinit var currencyName:String
    private lateinit var backButton: ImageButton
    private lateinit var menuBtn: ImageButton
    private lateinit var searchMenu: ImageButton
    private lateinit var backToolbar: ImageButton
    private lateinit var title:TextView
    private lateinit var toolbarBody:Toolbar
    private lateinit var searchBody:Toolbar
    private lateinit var searching:SearchView
    private lateinit var noTxtData:TextView
    private lateinit var searchData:ArrayList<Cash>

//
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cash)
        cashRecy=findViewById(R.id.cashRecycler)
        toolbarBody=findViewById(R.id.toolbar_body)
        searchBody=findViewById(R.id.search_body)
        noData=findViewById(R.id.no_data)
        backButton=findViewById(R.id.back_btn)
        searchMenu=findViewById(R.id.search_menu)
        menuBtn=findViewById(R.id.menu_btn)
        backToolbar=findViewById(R.id.back_toolbar)
        title=findViewById(R.id.toolbar_title)
        searching=findViewById(R.id.search_bar)
        noTxtData=findViewById(R.id.no_data_txt)

        title.text="Cash Records"
        db= SqliteHelper(Cash@this)
        sharedPrefs=getSharedPreferences("Credentials",Context.MODE_PRIVATE)
        currencyName=sharedPrefs.getString("Currency","None").toString()
        menuBtn.setOnClickListener {
            popUpMenu()
       }
        backButton.setOnClickListener {
            val intent= Intent(Cash@this,Home::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    searchMenu.setOnClickListener {
        toolbarBody.visibility=View.GONE
        searchBody.visibility=View.VISIBLE
    }
    backToolbar.setOnClickListener {
        toolbarBody.visibility=View.VISIBLE
        searchBody.visibility=View.GONE
        listingCashes(currencyName!!,sharedPrefs.getInt("cashTransId",1))
    }
    searching.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
        override fun onQueryTextChange(newText: String?): Boolean {
            searching(currencyName!!,sharedPrefs.getInt("cashTransId",1),newText!!)
            return false
        }
        override fun onQueryTextSubmit(query: String?): Boolean {
            searching(currencyName!!,sharedPrefs.getInt("cashTransId",1),query!!)
            return false
        }
    })
    }

    override fun onResume() {
        super.onResume()
        listingCashes(currencyName!!,sharedPrefs.getInt("cashTransId",1))
    }
    fun listingCashes(currency:String,index:Int){
        searchData=db.getAllCash(index)
        if (searchData.size>0) {
            noData.visibility=View.GONE
            cashRecy.visibility=View.VISIBLE
            recyAdapter = CashAdpter(searchData, Cash@ this, currency!!)
            cashRecy.layoutManager = LinearLayoutManager(Cash@ this)
            cashRecy.adapter = recyAdapter
        }
        else{
            noData.visibility=View.VISIBLE
            cashRecy.visibility=View.GONE
        }
    }
    fun searching(currency:String,index:Int,searchStr:String){
        lifecycleScope.launch(Dispatchers.Default){
            var searchDt = ArrayList<Cash>()
            searchData=db.getAllCash(index)
            if (!searchStr.isBlank()){
                for (item in searchData){
                    if (
                        item.DETAILS.lowercase().contains(searchStr.lowercase()))
                    {
                        searchDt.add(item)
                    }
                }
                searchData=searchDt
                withContext(Dispatchers.Main){
                    if (searchData.size>0) {
                        noData.visibility=View.GONE
                        cashRecy.visibility=View.VISIBLE
                        recyAdapter = CashAdpter(searchData, applicationContext, currency!!)
                        cashRecy.layoutManager = LinearLayoutManager(applicationContext)
                        cashRecy.adapter = recyAdapter
                    }
                    else{
                        noData.visibility=View.VISIBLE
                        cashRecy.visibility=View.GONE
                        noTxtData.text="You search doesn't match any data"
                    }
                }
            }
            if(searchStr.isBlank()){
                withContext(Dispatchers.Main){
                    listingCashes(currency,index)
                }
            }
        }
    }
    fun popUpMenu(){
        val popUp=PopupMenu(this,menuBtn)
        val dbutil=Database()
        popUp.inflate(R.menu.cash_menu)
        popUp.setOnMenuItemClickListener {
           when(it.itemId){
            R.id.deposit->{
                startActivity(Intent(Cash@this,Deposit::class.java))
            }
            R.id.withdraw->{
                startActivity(Intent(Cash@this,WithDraw::class.java))
               }
           R.id.balance->{
               val dialog= Dialogs(this)
               val totalCash=db.lastCashTransaction()
               dialog.customDialog("Available cash","Balance $currencyName.  ${totalCash.CTOTAL}")
               }
           R.id.viewWith->{
               dbutil.createCashCsvShare(db.getAllCash(),currencyName,this,filesDir.absolutePath)
           }
           R.id.viewExpense->{
                   dbutil.createCashCsvShare(db.getAllCash(condition=" WHERE TINDEX IN(1,3)"),currencyName,this,filesDir.absolutePath,fileStr="budgeta_expenses_cash.csv")
               }
           R.id.help->{
               val toCash=Intent(Cash@this, CashHelp::class.java)
               toCash.putExtra("From","Cash")
               startActivity(toCash)
           }
           }
            false
        }
        popUp.show()
    }
}