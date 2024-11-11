package com.example.money_tracker

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.money_tracker.db.MoneyTrackerDb
import com.example.money_tracker.db.MoneyTrackerRepo
import com.example.money_tracker.helper.SharePref
import com.example.money_tracker.helper.get
import com.example.money_tracker.helper.put
import com.example.money_tracker.model.LogType
import com.example.money_tracker.model.TaskLog
import io.ghyeok.stickyswitch.widget.StickySwitch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import studio.carbonylgroup.textfieldboxes.ExtendedEditText
import kotlin.coroutines.CoroutineContext

class AddTaskLogActivity : AppCompatActivity(), CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val moneyTrackerRepo: MoneyTrackerRepo by lazy {
        val moneyTrackerDb = MoneyTrackerDb(applicationContext)
        MoneyTrackerRepo.create(moneyTrackerDb)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_task_log)
        displayToolBar()

        var logType = LogType.ADD
        //sticky_switch
        findViewById<StickySwitch>(R.id.sticky_switch).setA(object :
            StickySwitch.OnSelectedChangeListener {
            override fun onSelectedChange(direction: StickySwitch.Direction, text: String) {
                logType = LogType.valueOf(text)
            }
        })
        val btnAdd = findViewById<Button>(R.id.btnAdd)
        btnAdd.setOnClickListener {
            val taskName =
                findViewById<ExtendedEditText>(R.id.extended_edit_task_name).text.toString()
            val taskMoney =
                findViewById<ExtendedEditText>(R.id.extended_edit_task_money).text.toString()

            launch {
                moneyTrackerRepo.insert(
                    TaskLog(
                        name = taskName,
                        money = taskMoney.toInt(),
                        type = logType
                    )
                )
                //
                calculateTrackingMoney(logType,taskMoney.toInt())

                finish()
                setResult(1)
            }
        }

    }

    private fun calculateTrackingMoney(logType: LogType, money: Int) {
        if (logType == LogType.ADD) {
            val currentMoney = SharePref.create(this)?.get("MONEY_ADD", 0) as Int
            SharePref.create(this)?.put("MONEY_ADD", currentMoney + money)
        } else {
            val currentMoney = SharePref.create(this)?.get("MONEY_SUBTRACT", 0) as Int
            SharePref.create(this)?.put("MONEY_SUBTRACT", currentMoney + money)
        }
    }

    fun displayToolBar(): Unit {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Hiển thị nút back
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add task"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed() // Quay lại Activity trước
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


}