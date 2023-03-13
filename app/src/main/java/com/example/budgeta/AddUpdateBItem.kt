package com.example.budgeta

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.budgeta.Models.Budget
import com.example.budgeta.Models.BudgetItem
import com.example.budgeta.Repository.SqliteHelper
import com.example.budgeta.Utilities.DateFormating
import com.example.budgeta.Utilities.Dialogs
import java.util.*

class AddUpdateBItem : AppCompatActivity() {
    private lateinit var itemName:EditText
    private lateinit var number:EditText
    private lateinit var cost:EditText
    private lateinit var nameError:TextView
    private lateinit var numberError:TextView
    private lateinit var costError:TextView
    private lateinit var addItem:Button
    private lateinit var unitName:TextView
    private lateinit var unitNameError:TextView
    private lateinit var  db:SqliteHelper
    private lateinit var budgetItem:BudgetItem
    private lateinit var dialogs:Dialogs
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_update_b_item)
        itemName=findViewById(R.id.name)
        number=findViewById(R.id.number_unit)
         cost=findViewById(R.id.cost_unit)
         nameError=findViewById(R.id.name_errors)
        numberError=findViewById(R.id.number_errors)
        costError=findViewById(R.id.cost_errors)
        addItem=findViewById(R.id.add_item)
        unitName=findViewById(R.id.name_unit)
        unitNameError=findViewById(R.id.unit_name_errors)
         db=SqliteHelper(AddUpdateBItem@this)
       dialogs=Dialogs(AddUpdateBItem@this)
        val operationType=intent.getIntExtra("operationType",1)
        val budgetId=intent.getIntExtra("budgetId",0)
        if (operationType==1){
            budgetItem=BudgetItem()
        }
        if(operationType==2){
            val bgItemId=intent.getIntExtra("budgetItemId",0)
            budgetItem=db.getBudgetItem(bgItemId)
            itemName.setText(budgetItem.NAME)
            unitName.setText(budgetItem.UNITNAME)
            number.setText(budgetItem.QUANTITY.toString())
            cost.setText(budgetItem.UNITCOST.toString())
            addItem.text="Update"
        }
        val validationPrefix="Kindly share the"
        addItem.setOnClickListener {
            addItem.isEnabled=false
            var valid=true
            if (itemName.text.isEmpty()){
                nameError.visibility= View.VISIBLE
                nameError.text="$validationPrefix item/service name"
                valid=false
            }
            if(!itemName.text.isEmpty()){
                nameError.visibility= View.GONE
                nameError.text=""
            }
            if(number.text.isEmpty()){
                numberError.text="$validationPrefix quantity"
                numberError.visibility= View.VISIBLE
                valid=false
            }
            if(!number.text.isEmpty()){
                numberError.visibility= View.GONE
                numberError.text=""
            }
            if(cost.text.isEmpty()){
                costError.text="$validationPrefix cost"
                costError.visibility= View.VISIBLE
                valid=false
            }
            if(!cost.text.isEmpty()){
                costError.text=""
                costError.visibility= View.GONE
            }
            if (unitName.text.isEmpty()){
                unitNameError.visibility= View.VISIBLE
                unitNameError.text="$validationPrefix unit name"
                valid=false
            }
            if(!unitName.text.isEmpty()) {
                unitNameError.visibility = View.GONE
            }
            if(valid){
                val timing=DateFormating()
                budgetItem.NAME=itemName.text.trim().toString()
                budgetItem.UNITNAME=unitName.text.trim().toString()
                budgetItem.BUDGETID=budgetId
                budgetItem.QUANTITY=number.text.trim().toString().toInt()
                budgetItem.UNITCOST=cost.text.trim().toString().toDouble()
                budgetItem.TAMOUNT=dialogs.roundToOne(budgetItem.UNITCOST*budgetItem.QUANTITY)
                val credentials=getSharedPreferences("Credentials", Context.MODE_PRIVATE)
                val currencyType=credentials.getString("Currency","None").toString()
                val budget=db.getBudget(budgetId)//Updating the budget
                if(operationType==1){
                    budget.T_EST= budgetItem.TAMOUNT+budget.T_EST
                    budget.ITEMS=budget.ITEMS+1
                    if (budget.STARTED==2){
                        budget.STARTED=1
                    }
                    budgetItem.TICKED=0 //0 for not ticked and 1 for ticked
                    budgetItem.ADATE=timing.timeNow()
                    budgetItem.CURRENCY=currencyType
                    val itemId=db.budgetItemAdd(budgetItem)
                    if(itemId>0){
                        customiseDialog(budget,"Added successfully")
                        addItem.isEnabled=true
                    }
                    else{
                        val statusDialog=dialogs.statusDialog("Failed to add",false)
                        Handler().postDelayed({statusDialog.dismiss()},3000)
                        addItem.isEnabled=true
                    }
                }
                if(operationType==2){
                    val bgitem=db.getBudgetItem(budgetItem.ID)
                    if(!budgetItem.NAME.equals(bgitem.NAME)||
                        !budgetItem.UNITNAME.equals(bgitem.UNITNAME)||
                        budgetItem.QUANTITY!=bgitem.QUANTITY||
                        budgetItem.UNITCOST!=bgitem.UNITCOST
                    ){
                        var Amount=budget.T_EST-bgitem.TAMOUNT
                        budget.T_EST=Amount+ budgetItem.TAMOUNT
                        val itemId=db.budgetItemUpdate(budgetItem)
                        if(itemId>0){
                            customiseDialog(budget,"Updated successfully")
                            addItem.isEnabled=true
                        }
                        else{
                            val statusDialog=dialogs.statusDialog("Failed to add",false)
                            Handler().postDelayed({statusDialog.dismiss()},3000)
                            addItem.isEnabled=true
                        }
                    }
                    else{
                        customiseDialog(budget,"", change = 2)
                        addItem.isEnabled=true
                    }
                }
            }
        }


    }
    fun customiseDialog(budget: Budget, operation:String,change:Int=1){
        val  intent=Intent(AddUpdateBItem@this,BudgetItems::class.java)
        intent.putExtra("BudgetId",budget.ID)
        intent.putExtra("Name",budget.NAME)
        intent.putExtra("StartDate",budget.STARTDATE)
        intent.putExtra("expiryDate",budget.ENDDATE)
        intent.putExtra("budgetCategory",budget.CATEGORY)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        if (change==2){
            Toast.makeText(this,"No changes on items",Toast.LENGTH_LONG).show()
            Handler().postDelayed({
                startActivity(intent)
            },2000)
        }
        if (change==1){
            val db=SqliteHelper(AddUpdateBItem@this)
            val bgId=db.updateBudget(budget)
            if (bgId>0){
                val statusDialog=dialogs.statusDialog("$operation",true)
                Handler().postDelayed({statusDialog.dismiss()
                    startActivity(intent)
                },3000)
            }
            else{
                val statusDialog=dialogs.statusDialog("Failed to update the budget",false)
                Handler().postDelayed({statusDialog.dismiss()

                },3000)
            }
        }

    }
}