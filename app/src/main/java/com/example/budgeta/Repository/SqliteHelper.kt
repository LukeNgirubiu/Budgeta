package com.example.budgeta.Repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.budgeta.Models.*
import com.example.budgeta.Utilities.Database
import com.example.budgeta.Utilities.DateFormating
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class SqliteHelper(context: Context):SQLiteOpenHelper(context,DB_Name,null, DB_Version) {
    companion object {
        private val DB_Name = "BudgetDbs"
        private val DB_Version = 1
        private val ID = "ID"
        private val TABLES = arrayOf("ESTIMATES", "CASHBOOK", "BUDGETITEM", "DEBTORS", "CREDITORS","CREDITPAY","DEBTPAY")
        //val tableIndexCredit=if (index==1) 4 else 3
        //Transaction index for cashbook 1-Tick budget item, 2-Debtor pay, 3- Debt repayment,4-Cash deposit,5-Cash withdrawal,6-Credit to debtor account
    }

    override fun onCreate(database: SQLiteDatabase?) {
        database?.execSQL("CREATE TABLE ${TABLES[0]}($ID INTEGER PRIMARY KEY AUTOINCREMENT,NAME  TEXT,CATEGORY INTEGER,DETAILS TEXT,EXPIRYDATE TEXT,STARTED INTEGER,STARTDATE TEXT,TODAYDATE TEXT,ITEMS INTEGER,TOTALCOST REAL,CURRENCY TEXT)")
        database?.execSQL("CREATE TABLE ${TABLES[1]}($ID INTEGER PRIMARY KEY AUTOINCREMENT,TDATE TEXT,DETAILS TEXT,AMOUNT REAL,TINDEX INTEGER,TID INTEGER,TTYPE INTEGER,CTOTAL REAL,CURRENCY TEXT)")
        database?.execSQL("CREATE TABLE ${TABLES[2]}($ID INTEGER PRIMARY KEY AUTOINCREMENT,BUDGETID INTEGER,NAME TEXT,QUANTITY INTEGER,UNITCOST REAL,UNITNAME TEXT,TICKED INTEGER,TAMOUNT REAL,ADATE TEXT,CURRENCY TEXT,FOREIGN KEY(BUDGETID) REFERENCES ${TABLES[0]}($ID))")
        database?.execSQL("CREATE TABLE ${TABLES[3]}($ID INTEGER PRIMARY KEY AUTOINCREMENT,TDATE TEXT,PAYMENTDATE TEXT,NAME TEXT,DETAILS TEXT,CELLPHONENO TEXT,STARTED INTEGER,BALANCE REAL, AMOUNT REAL,CURRENCY TEXT)")
        database?.execSQL("CREATE TABLE ${TABLES[4]}($ID INTEGER PRIMARY KEY AUTOINCREMENT,TDATE TEXT,PAYMENTDATE TEXT,NAME TEXT,DETAILS TEXT,CELLPHONENO TEXT,STARTED INTEGER,BALANCE REAL, AMOUNT REAL,CURRENCY TEXT)")
        database?.execSQL("CREATE TABLE ${TABLES[5]}($ID INTEGER PRIMARY KEY AUTOINCREMENT,CREDITID INTEGER,AMOUNT REAL, BALANCE REAL,TDATE TEXT,CURRENCY TEXT,FOREIGN KEY(CREDITID) REFERENCES ${TABLES[4]}($ID))")
        database?.execSQL("CREATE TABLE ${TABLES[6]}($ID INTEGER PRIMARY KEY AUTOINCREMENT,DEBTID INTEGER, AMOUNT REAL,BALANCE REAL,TDATE TEXT,CURRENCY TEXT,FOREIGN KEY(DEBTID) REFERENCES ${TABLES[5]}($ID))")
    }

    override fun onUpgrade(database: SQLiteDatabase?, p1: Int, p2: Int) {
        for (item in TABLES) {
            database?.execSQL("DROP TABLE IF EXISTS $item")
        }
        onCreate(database)

    }

    fun addBudget(budget: Budget): Long {
        val db: SQLiteDatabase = writableDatabase
        val utilDb = Database()
        val budgetCont = utilDb.budgetWrapper(budget)
        val id = db.insert(TABLES[0], null, budgetCont)
        db.close()
        if (id > 0) {
            return id
        } else {
            return 0
        }
        db.close()
    }

    fun getBudget(id: Int): Budget {
        val db: SQLiteDatabase = writableDatabase
        val query = db.rawQuery("SELECT * FROM ${TABLES[0]} WHERE $ID=$id", null)
        var budget = Budget()
        if (query != null && query.count > 0) {
            try{
                query.moveToFirst()
                val utilDb = Database()
                budget = utilDb.budgetCursor(query)
            }
            catch (ex:Exception){

            }
        }
        db.close()
        return budget
    }

    fun getBudgets(categoryId: Int=0): ArrayList<Budget> {
        val db: SQLiteDatabase = writableDatabase
        val query=if(categoryId==0){
            "SELECT * FROM ${TABLES[0]} WHERE STARTED==3"
        }
        else{
            "SELECT * FROM ${TABLES[0]} WHERE CATEGORY=$categoryId AND STARTED!=3"
        }
        val cursor: Cursor = db.rawQuery(query, null)
        val budgets = ArrayList<Budget>()
        if (cursor != null && cursor.count > 0) {
            if (cursor.moveToFirst()) {
                do {
                    try{
                        val dbUtils = Database()
                        budgets.add(dbUtils.budgetCursor(cursor))
                    }
                    catch (ex:Exception){
                    }
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        return budgets
    }
  fun archiveBudget(timExpId:Int){
      val db: SQLiteDatabase = writableDatabase
      val calender= Calendar.getInstance()
      if (timExpId==1){
          calender.add(Calendar.DAY_OF_MONTH,-1)
      }
      if(timExpId==2){
          calender.add(Calendar.DAY_OF_MONTH,-7)
      }
      if (timExpId==3){
          calender.add(Calendar.MONTH,-1)
      }
      val timing=DateFormating()
      val expirlyDate="${calender.get(Calendar.YEAR)}-${timing.numberFormating(calender.get(Calendar.MONTH)+1)}-${timing.numberFormating(calender.get(Calendar.DAY_OF_MONTH))}"
      val sqlQuery="UPDATE ESTIMATES SET STARTED=3 WHERE STARTED=2 AND EXPIRYDATE<=\'$expirlyDate\'"//Archive query
      db.execSQL(sqlQuery)
      db.close()
  }
    fun updateBudget(budget: Budget): Long {
        val db: SQLiteDatabase = writableDatabase
        val utilDb = Database()
        val contents = utilDb.budgetWrapper(budget)
        val upDatedId = db.update(TABLES[0], contents, ID + "=?", arrayOf(budget.ID.toString())).toLong()
        db.close()
        return upDatedId
    }
    fun deleteBudget(condition: String,conditionId: String):Boolean{
        dropBudgetItem("BUDGETID=?",conditionId)
        val db: SQLiteDatabase = writableDatabase
        val id=db.delete(TABLES[0],condition , arrayOf(conditionId))>0
        db.close()
        return id
    }

    fun budgetItemAdd(budget: BudgetItem): Long {
        val utilDb = Database()
        val values = utilDb.budgetItemWrapper(budget)
        val db: SQLiteDatabase = writableDatabase
        val id = db.insert(TABLES[2], null, values)
        db.close()
        return id
    }

    fun budgetItemUpdate(budget: BudgetItem): Long {
        val db: SQLiteDatabase = writableDatabase
        val utilDb = Database()
        val contents = utilDb.budgetItemWrapper(budget)
        val upDatedId = db.update(TABLES[2], contents, ID + "=?", arrayOf(budget.ID.toString())).toLong()
         db.close()
        return upDatedId
    }
    fun checkItemsTicked(budgetId:Long):Int{
        val db:SQLiteDatabase=writableDatabase
        val query=db.rawQuery("SELECT COUNT(*) AS NUMBER FROM BUDGETITEM WHERE BUDGETID=${budgetId} AND TICKED=1",null)
        if (query!=null && query.count>0){
            try{
                query.moveToFirst()
                return query.getString(query.getColumnIndexOrThrow("NUMBER")).toInt()
            }
            catch (ex:Exception){

            }
        }
        db.close()
        return 0
    }

    fun getBudgetItems(budgetId: Int): ArrayList<BudgetItem> {
        val db: SQLiteDatabase = writableDatabase
        val query = "SELECT * FROM ${TABLES[2]} WHERE BUDGETID=$budgetId"
        val cursor: Cursor = db.rawQuery(query, null)
        val budgetsItems = ArrayList<BudgetItem>()
        if (cursor != null && cursor.count > 0) {
            if (cursor.moveToFirst()) {
                do {
                    try{
                        val dbUtils = Database()
                        budgetsItems.add(dbUtils.budgetItemCursor(cursor))
                    }
                    catch (ex:Exception){

                    }

                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        return budgetsItems
    }

    fun getBudgetItem(budgetItemId: Int): BudgetItem {
        val db: SQLiteDatabase = writableDatabase
        val query = db.rawQuery("SELECT * FROM ${TABLES[2]} WHERE $ID=$budgetItemId", null)
        var budgetItem = BudgetItem()
        if (query != null && query.count > 0) {
            try{
                query.moveToFirst()
                val utilDb = Database()
                budgetItem = utilDb.budgetItemCursor(query)
            }
            catch (ex:Exception){
            }
        }
        db.close()
        return budgetItem
    }
 fun dropBudgetItem(condition: String,conditionId:String):Boolean{
     val db: SQLiteDatabase = writableDatabase
     val status= db.delete(TABLES[2],condition , arrayOf(conditionId))>0
     db.close()
     return status
 }
    fun cashOperations(cash: Cash): Long {
        val utilDb = Database()
        val values = utilDb.wrapCash(cash)
        val db: SQLiteDatabase = writableDatabase
        val id = db.insert(TABLES[1], null, values)
        db.close()
        return id
    }
    fun cashUpdate(cash: Cash): Long {
        val db: SQLiteDatabase = writableDatabase
        val utilDb = Database()
        val contents = utilDb.wrapCash(cash)
        val upDatedId = db.update(TABLES[1], contents, ID + "=?", arrayOf(cash.ID.toString())).toLong()
        db.close()
        return upDatedId
    }

    fun lastCashTransaction(): Cash {
        val db: SQLiteDatabase = writableDatabase
        val query = db.rawQuery("SELECT * FROM ${TABLES[1]} ORDER BY ID DESC LIMIT 1", null)
        var cash = Cash()
        if (query != null && query.count > 0) {
            try{
                query.moveToFirst()
                val utilDb = Database()
                cash = utilDb.cashCursor(query)
            }
            catch (ex:Exception){
            }
        }
        query.close()
        return cash
    }
//WHERE TINDEX IN(1,3)
    fun getAllCash(categId:Int=4,condition: String=""): ArrayList<Cash> {
        val db: SQLiteDatabase = writableDatabase
        val limit=if(categId==1){
            " LIMIT 10"
        }
        else if (categId==2){
            " LIMIT 20"
        }
        else if (categId==3){
            " LIMIT 30"
        }
         else{
             ""
         }
        var sqlQuery="SELECT * FROM ${TABLES[1]}$condition ORDER BY TDATE DESC$limit"
        val cursor: Cursor = db.rawQuery( sqlQuery, null)
        val cashes = ArrayList<Cash>()
        if (cursor != null && cursor.count > 0) {
            if (cursor.moveToFirst()) {
                do {
                    try{
                        val dbUtils = Database()
                        cashes.add(dbUtils.cashCursor(cursor))
                    }
                    catch (ex:Exception){
                    }
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        return cashes
    }
    fun addCashCredit(credit: Credit,index:Int): Long {
        val tableIndex=if (index==1) 4 else 3
        val utilDb = Database()
        val values = utilDb.wrapCredit(credit)
        val db: SQLiteDatabase = writableDatabase
        val id = db.insert(TABLES[tableIndex], null, values)
        db.close()
        return id
    }
    fun updateCashCredit(credit:Credit,index:Int): Long {
        val tableIndex=if (index==1) 4 else 3
        val db: SQLiteDatabase = writableDatabase
        val utilDb = Database()
        val contents = utilDb.wrapCredit(credit)
        val upDatedId = db.update(TABLES[tableIndex], contents, ID + "=?", arrayOf(credit.ID.toString())).toLong()
        db.close()
        return upDatedId
    }
    fun getAllCredits(index: Int,archived:Boolean=false): ArrayList<Credit> {
        val tableIndex=if (index==1) 4 else 3
        val statusStated=if (archived==true) "STARTED==3" else "STARTED!=3"
        val db: SQLiteDatabase = writableDatabase
        val query = "SELECT * FROM ${TABLES[tableIndex]} WHERE $statusStated ORDER BY PAYMENTDATE ASC"
        val cursor: Cursor = db.rawQuery(query, null)
        val cashes = ArrayList<Credit>()
        if (cursor != null && cursor.count > 0) {
            if (cursor.moveToFirst()) {
                do {
                    try{
                        val dbUtils = Database()
                        cashes.add(dbUtils.creditCursor(cursor))
                    }
                    catch (ex:Exception){
                    }
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        return cashes
    }
    fun getCreditRecord(ID:Long,index:Int): Credit {
        val db: SQLiteDatabase = writableDatabase
        val tableIndex=if (index==1) 4 else 3
        val query = db.rawQuery("SELECT * FROM ${TABLES[tableIndex]} WHERE ID=$ID", null)
        var credit = Credit()
        if (query != null && query.count > 0) {
            try{
                query.moveToFirst()
                val utilDb = Database()
                credit = utilDb.creditCursor(query)
            }
            catch (ex:Exception){

            }
        }
        query.close()
        return credit
    }
    fun deleteCredit(index:Int,creditId:Long):Int{
        val db: SQLiteDatabase = writableDatabase
        val tableId=if (index==1) "CREDITID" else "DEBTID"
        val tableIndexPay=if (index==1) 5 else 6
        val tableIndexCredit=if (index==1) 4 else 3
        val deptPaySql="DELETE FROM ${TABLES[tableIndexPay]} WHERE ${tableId}=$creditId"
        db.execSQL(deptPaySql)
        val id=db.delete(TABLES[tableIndexCredit],"ID=?" , arrayOf(creditId.toString()))
        db.close()
        return id
    }
    fun addCreditSettling(pay:Pay,index:Int): Pay {
        val tableIndex=if (index==1) 5 else 6
        val tableId=if (index==1) "CREDITID" else "DEBTID"
        val utilDb = Database()
        val values = utilDb.settleWrap(pay,tableId)
        val db: SQLiteDatabase = writableDatabase
        val id = db.insert(TABLES[tableIndex], null, values)
        pay.ID=id
        db.close()
        return pay
    }
    fun getCreditSettling(ID:Long,index:Int): Pay {
        val db: SQLiteDatabase = writableDatabase
        val tableIndex=if (index==1) 5 else 6
        val tableId=if (index==1) "CREDITID" else "DEBTID"
        val sqlStr="SELECT * FROM ${TABLES[tableIndex]} WHERE $tableId=$ID ORDER BY ID DESC LIMIT 1"
        val query = db.rawQuery(sqlStr, null)
        var pay = Pay()
        if (query != null && query.count > 0) {
            try{
                query.moveToFirst()
                val utilDb = Database()
                pay = utilDb.settleCursor(query, tableId)
            }
            catch (ex:Exception){

            }
        }
        query.close()
        return pay
    }
    fun getAllSettling(ID:Long,index:Int): ArrayList<Pay> {
        val db: SQLiteDatabase = writableDatabase
        val tableIndex=if (index==1) 5 else 6
        val tableId=if (index==1) "CREDITID" else "DEBTID"
        val queryStr="SELECT * FROM ${TABLES[tableIndex]} WHERE $tableId=$ID ORDER BY TDATE DESC"
        val query = db.rawQuery(queryStr, null)
        val payments=ArrayList<Pay>()
        if (query != null && query.count > 0) {
            if (query.moveToFirst()) {
                do {
                    try{
                        val utilDb = Database()
                        payments.add(utilDb.settleCursor(query,tableId))
                    }
                    catch (ex:Exception){
                    }
                } while (query.moveToNext())
            }
        }
        query.close()
        return payments
    }
    fun archiveCredits(timExpId:Int){
        val db: SQLiteDatabase = writableDatabase
        val calender= Calendar.getInstance()
        if (timExpId==1){
            calender.add(Calendar.DAY_OF_MONTH,-1)
        }
        if(timExpId==2){
            calender.add(Calendar.DAY_OF_MONTH,-7)
        }
        if (timExpId==3){
            calender.add(Calendar.MONTH,-1)
        }
        val timing=DateFormating()
        val expirlyDate="${calender.get(Calendar.YEAR)}-${timing.numberFormating(calender.get(Calendar.MONTH)+1)}-${timing.numberFormating(calender.get(Calendar.DAY_OF_MONTH))}"
        val sqlDEBTORS="UPDATE DEBTORS SET STARTED=3 WHERE STARTED=2 AND PAYMENTDATE<=\'$expirlyDate\'"//Archive query
        val sqlCREDITORS="UPDATE CREDITORS SET STARTED=3 WHERE STARTED=2 AND PAYMENTDATE<=\'$expirlyDate\'"
        db.execSQL(sqlDEBTORS)
        db.execSQL(sqlCREDITORS)
        db.close()
    }

    fun homeAnalytics():ArrayList<DashBoardEstimate>{
        val db: SQLiteDatabase = writableDatabase
        val calender= Calendar.getInstance()
        val timing=DateFormating()
        val dateToday="${calender.get(Calendar.YEAR)}-${timing.numberFormating(calender.get(Calendar.MONTH)+1)}-${timing.numberFormating(calender.get(Calendar.DAY_OF_MONTH))}"
        val queryStr="SELECT SUM(TOTALCOST) AS COST, COUNT(*) AS NUMBER,CATEGORY FROM ESTIMATES WHERE STARTED<2 AND EXPIRYDATE>\'$dateToday\' GROUP BY CATEGORY ORDER BY CATEGORY ASC"
        val query = db.rawQuery(queryStr, null)
        val data=ArrayList<DashBoardEstimate>()
        if (query != null && query.count > 0) {
            if (query.moveToFirst()) {
                do {
                    try{
                        val dash=DashBoardEstimate()
                        dash.categ=query.getString(query.getColumnIndexOrThrow("CATEGORY")).toInt()
                        dash.count=query.getString(query.getColumnIndexOrThrow("NUMBER")).toInt()
                        dash.ttAmount=query.getString(query.getColumnIndexOrThrow("COST")).toDouble()
                        data.add(dash)
                    }
                    catch (ex:Exception){
                    }
                } while (query.moveToNext())
            }
        }
        query.close()
       return data
    }
    //"TINDEX IN(1,3)"
    //Data for expenditure/Revenue monthly this year
    fun getExpenditureYear(indexStr:String):HashMap<Int,Double>{
        val db:SQLiteDatabase=writableDatabase
        val time=DateFormating()
        val query="SELECT SUM(AMOUNT) AS CASH, strftime(\'%m\', TDATE) AS MONTH FROM CASHBOOK WHERE ${indexStr} AND TDATE BETWEEN \'${time.dateRangeYear()[0]}\' AND \'${time.dateRangeYear()[1]}\' GROUP BY MONTH"
        val queryEx=db.rawQuery(query,null)
        val labelValue:HashMap<Int,Double> = HashMap ()
        if (queryEx!=null && queryEx.count!=0){
            if(queryEx.moveToFirst()){
                do {
                    try{
                        val month=queryEx.getString(queryEx.getColumnIndexOrThrow("MONTH")).toInt()
                        val cash=queryEx.getString(queryEx.getColumnIndexOrThrow("CASH")).toDouble()
                        labelValue.put(month,cash)
                    }
                    catch (ex:Exception){

                    }
                }while (queryEx.moveToNext())
            }
        }
        db.close()
        return labelValue
    }
    //Deposits/Revenue per month this year
    fun getCreditYear(index:Int):HashMap<Int,Double>{
        val labelValue:HashMap<Int,Double> = HashMap ()
        val time=DateFormating()
        val db:SQLiteDatabase=writableDatabase
        val queryStr="SELECT SUM(AMOUNT) AS CASH,strftime(\'%m\', TDATE) AS MONTH FROM CASHBOOK WHERE TINDEX=$index AND TDATE BETWEEN \'${time.dateRangeYear()[0]}\' AND \'${time.dateRangeYear()[1]}\' GROUP BY MONTH"
        val executionSql=db.rawQuery(queryStr,null)
        if (executionSql!=null && executionSql.count>0){
            if (executionSql.moveToFirst()){
                do{
                    try{
                        val month=executionSql.getString(executionSql.getColumnIndexOrThrow("MONTH")).toInt()
                        val cash=executionSql.getString(executionSql.getColumnIndexOrThrow("CASH")).toDouble()
                        labelValue.put(month,cash)
                    }
                    catch (ex:Exception){

                    }
                }while (executionSql.moveToNext())
            }
        }
        db.close()
        return labelValue
    }
    fun getCashTotal(queryStr:String,startTime:String,endTime:String):Double{
    val db:SQLiteDatabase=writableDatabase
    val query="SELECT SUM(AMOUNT) AS CASH FROM CASHBOOK WHERE $queryStr AND TDATE BETWEEN \'$startTime\' AND \'$endTime\'"
    var totalCash=0.0
    val executeQuery=db.rawQuery(query,null)
    if (executeQuery!=null && executeQuery.count>0){
        if (executeQuery.moveToFirst()){
            try {
                totalCash= executeQuery.getString(executeQuery.getColumnIndexOrThrow("CASH")).toDouble()
            }
            catch (exception:Exception){
            }
        }
    }
    db.close()
    return totalCash
    }
    fun getCreditTotal(table:Int,startTime:String,endTime:String):Double{
        val db:SQLiteDatabase=writableDatabase
        val query="SELECT SUM(AMOUNT) AS CASH FROM ${TABLES[table]} WHERE TDATE BETWEEN \'$startTime\' AND \'$endTime\'"
        var totalCash=0.0
        val executeQuery=db.rawQuery(query,null)
        if (executeQuery!=null && executeQuery.count>0){
            if (executeQuery.moveToFirst()){
                try {
                    totalCash= executeQuery.getString(executeQuery.getColumnIndexOrThrow("CASH")).toDouble()
                }
                catch (exception:Exception){
                }
            }
        }
        db.close()
        return totalCash
    }
    fun getGroupedCreditCash(tableId:Int,intervalTime:String,timeConfig:String,timeLower:String,timeUpper:String,condition:String):HashMap<String,Double>{
        val db:SQLiteDatabase=writableDatabase
        val groupbyStr="$timeConfig "+intervalTime
        val query="SELECT SUM(AMOUNT) AS CASH, $groupbyStr FROM ${TABLES[tableId]} $condition TDATE BETWEEN \'$timeLower\' AND \'$timeUpper\' GROUP BY $intervalTime"
        val data:HashMap<String,Double> = HashMap()
        val queryExc=db.rawQuery(query,null)
        if (queryExc!=null && queryExc.count>0){
            if (queryExc.moveToFirst()){
                do{
                    try{
                        val cash=queryExc.getString(queryExc.getColumnIndexOrThrow("CASH")).toDouble()
                        val timeUnit=queryExc.getString(queryExc.getColumnIndexOrThrow("$intervalTime"))
                        data.put(timeUnit,cash)
                    }
                    catch (e:Exception){

                    }
                }while(queryExc.moveToNext())
            }
        }
        queryExc.close()
        return data

    }
    fun reminder():ArrayList<String>{
         val db:SQLiteDatabase=writableDatabase
        val timings=DateFormating()
        val cal = Calendar.getInstance()
        val expiring ="${cal.get(Calendar.YEAR)}-${timings.numberFormating(cal.get(Calendar.MONTH)+1)}-${timings.numberFormating(cal.get(Calendar.DAY_OF_MONTH)+1)}"
        val reminds=ArrayList<String>()
        val sql1="SELECT ID, NAME, DETAILS FROM ESTIMATES WHERE EXPIRYDATE==\'$expiring\'"
        val sql2="SELECT ID, NAME, DETAILS FROM DEBTORS WHERE BALANCE>0.0 AND PAYMENTDATE==\'$expiring\'"
        val sql3="SELECT ID, NAME, DETAILS FROM CREDITORS WHERE BALANCE>0.0 AND PAYMENTDATE==\'$expiring\'"
        val sqls= arrayOf(sql1,sql2,sql3)
        var sqlExc:Cursor? = null
        for(sql in 0..sqls.size-1){
            sqlExc=db.rawQuery(sqls[sql],null)
            if (sqlExc!=null && sqlExc.count>0) {
                if (sqlExc.moveToFirst()) {
                    do {
                        val id = sqlExc.getString(sqlExc.getColumnIndexOrThrow("ID")).toInt()
                        val name = sqlExc.getString(sqlExc.getColumnIndexOrThrow("NAME"))
                        val details = sqlExc.getString(sqlExc.getColumnIndexOrThrow("DETAILS"))
                        val row="$id,$name,$details,$sql,false"
                        reminds.add(row)
                    } while (sqlExc.moveToNext())
                    sqlExc.close()
                }
            }
        }
        db.close()
        return reminds
    }
      // BALANCE REAL  "DEBTORS", "CREDITORS"
    fun deptCredit():ArrayList<Double>{
        val db:SQLiteDatabase=writableDatabase
        val queryDebtor="SELECT SUM(BALANCE) AS AMOUNT FROM DEBTORS"
        val queryCredit="SELECT SUM(BALANCE) AS AMOUNT FROM CREDITORS"
        val amounts=ArrayList<Double>()
        val queriesStr= arrayOf(queryDebtor,queryCredit)
        queriesStr.forEach {
            var totalCash=0.0
            val executeQuery=db.rawQuery(it,null)
            if (executeQuery!=null && executeQuery.count>0){
                if (executeQuery.moveToFirst()){
                    try {
                        totalCash= executeQuery.getString(executeQuery.getColumnIndexOrThrow("AMOUNT")).toDouble()
                        amounts.add(totalCash)
                    }
                    catch (exception:Exception){
                        amounts.add(0.0)
                    }
                }
            }
        }
          db.close()
     return amounts
      }
    fun monthlyLimit():Double{
        val db:SQLiteDatabase=writableDatabase
        val time=DateFormating()
        val query="SELECT SUM(AMOUNT) AS CASH FROM CASHBOOK WHERE TDATE BETWEEN \'${time.dateMonthRange()[0]}\' AND \'${time.dateMonthRange()[1]}\' AND TINDEX IN(1,5)"
        val executeQuery=db.rawQuery(query,null)
        var totalCash=0.0
        if (executeQuery!=null && executeQuery.count>0){
            if (executeQuery.moveToFirst()){
                try {
                    totalCash= executeQuery.getString(executeQuery.getColumnIndexOrThrow("CASH")).toDouble()
                }
                catch (exception:Exception){
                }
            }
        }
        db.close()
        return totalCash
    }
    fun expenseCash():ArrayList<Cash>{
        val db: SQLiteDatabase = writableDatabase
        var sqlQuery="SELECT * FROM ${TABLES[1]} WHERE TINDEX IN(1,3) ORDER BY TDATE DESC"
        val cursor: Cursor = db.rawQuery( sqlQuery, null)
        val cashes = ArrayList<Cash>()
        if (cursor != null && cursor.count > 0) {
            if (cursor.moveToFirst()) {
                do {
                    try{
                        val dbUtils = Database()
                        cashes.add(dbUtils.cashCursor(cursor))
                    }
                    catch (ex:Exception){
                    }
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        return cashes
    }

}
