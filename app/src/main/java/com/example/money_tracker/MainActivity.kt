package com.example.money_tracker

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.money_tracker.adapter.LogAdapter
import com.example.money_tracker.db.MoneyTrackerDb
import com.example.money_tracker.db.MoneyTrackerRepo
import com.example.money_tracker.helper.SharePref
import com.example.money_tracker.helper.get
import com.example.money_tracker.helper.put
import com.example.money_tracker.model.LogType
import com.example.money_tracker.model.TaskLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity(), CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val moneyTrackerRepo: MoneyTrackerRepo by lazy {
        val moneyTrackerDb = MoneyTrackerDb(applicationContext)
            MoneyTrackerRepo.create(moneyTrackerDb)
    }




    private lateinit var logAdapter: LogAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        displayToolBar()
        // LinearLayoutmanager
        var linearLayoutManager = LinearLayoutManager(applicationContext)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        val decoration = DividerItemDecoration(this, linearLayoutManager.orientation)
        logAdapter = LogAdapter(moneyTrackerRepo = moneyTrackerRepo,
            onDeleteItem = {
                if (it.type == LogType.ADD) {
                    val currentMoney = SharePref.create(this)?.get("MONEY_ADD", 0) as Int
                    SharePref.create(this)?.put("MONEY_ADD", currentMoney - it.money)
                } else {
                    val currentMoney = SharePref.create(this)?.get("MONEY_SUBTRACT", 0) as Int
                    SharePref.create(this)?.put("MONEY_SUBTRACT", currentMoney - it.money)
                }

                if (recyclerView.adapter?.itemCount == 0) {
                    showEmptyTask()
                    SharePref.create(this)?.put("MONEY_ADD", 0)
                    SharePref.create(this)?.put("MONEY_SUBTRACT", 0)



                } else {
                    showTrackMoney()
                }
            }, onUpdateItem = {

            }
            )
        ContextCompat.getDrawable(this, R.drawable.bd_divider)
        recyclerView.apply {
            layoutManager = linearLayoutManager
            addItemDecoration(decoration)
            adapter = logAdapter


        }

        loadTask(true)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1) {
            loadTask(false)
        }
    }

    private fun loadTask(enbaeldelay: Boolean){
        showTrackMoney()
        launch{
            if(enbaeldelay){
                delay(2000L)
            }

            val listTask = moneyTrackerRepo.selectAll()

            withContext(Dispatchers.Main){
                if(listTask.isEmpty()) {
                    showEmptyTask()
                }
                else {
                    displayTask(listTask)
                }
            }
        }
    }
    private fun showTrackMoney() {
        val addMoney = SharePref.create(this)?.get("MONEY_ADD", 0)
        val subtractMoney = SharePref.create(this)?.get("MONEY_SUBTRACT", 0)

        val tvAddMoney = findViewById<TextView>(R.id.txtAddMoney)
        tvAddMoney.text = addMoney.toString()
        val tvSubtractMoney = findViewById<TextView>(R.id.txtSubTractMoney)

        tvSubtractMoney.text = subtractMoney.toString()
    }

    private fun showEmptyTask(){
        findViewById<ProgressBar>(R.id.loading).visibility = View.GONE
        findViewById<NestedScrollView>(R.id.container).visibility = View.GONE
        findViewById<TextView>(R.id.txtStatus).visibility = View.VISIBLE
        findViewById<TextView>(R.id.txtStatus).text = "Empty !!"
    }
    private fun displayTask(mutableList: MutableList<TaskLog>){
        findViewById<ProgressBar>(R.id.loading).visibility = View.GONE
        findViewById<NestedScrollView>(R.id.container).visibility = View.VISIBLE
        findViewById<TextView>(R.id.txtStatus).visibility = View.GONE
        logAdapter.setData(mutableList)
    }
    fun displayToolBar(): Unit {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Hiển thị nút back
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Money tracker"
    }

//    fun mockTaskLog(): MutableList<TaskLog> {
//        val result = mutableListOf<TaskLog>()
//        for (i in 1..10) {
//            if (i % 2 == 0) {
//                result.add(
//                    TaskLog(
//                        id = i,
//                        name = "item +${i} ",
//                        money = 1000 * i,
//                        type = LogType.ADD
//                    )
//                )
//            } else {
//                result.add(
//                    TaskLog(
//                        id = i,
//                        name = "item +${i} ",
//                        money = 1000 * i,
//                        type = LogType.SUBTRACT
//                    )
//                )
//            }
//
//        }
//        return result
//    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed() // Quay lại Activity trước
                true
            }
            R.id.addTaskLog -> {

                val intent = Intent(this, AddTaskLogActivity::class.java)
                startActivityForResult(intent , 1)
                true
            }



            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
            menuInflater.inflate(R.menu.menu_main, menu)

        return true
    }


}


