package com.example.money_tracker.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.money_tracker.R
import com.example.money_tracker.db.MoneyTrackerDb
import com.example.money_tracker.db.MoneyTrackerRepo
import com.example.money_tracker.model.LogType
import com.example.money_tracker.model.TaskLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

typealias OnDeleteItem = (taskLog: TaskLog) -> Unit
typealias OnUpdateItem = (taskLog: TaskLog) -> Unit

class LogAdapter(
    private val dataSet: MutableList<TaskLog> = mutableListOf(),
    private val moneyTrackerRepo: MoneyTrackerRepo,
    private val onDeleteItem: OnDeleteItem,
    private val onUpdateItem: OnUpdateItem
) :
    RecyclerView.Adapter<LogAdapter.LogViewHolder>() {
    fun setData(dataSet: MutableList<TaskLog>) {
        this.dataSet.clear()
        this.dataSet.addAll(dataSet)
        notifyDataSetChanged()
    }

    inner class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            itemView.findViewById<ImageView>(R.id.moreAction).setOnClickListener {
                showMenuAction(it.context, it)

            }
        }

        fun bind(taskLog: TaskLog) {
            itemView.findViewById<TextView>(R.id.txtTaskName).text = taskLog.name
            if (taskLog.type == LogType.ADD) {
                itemView.findViewById<TextView>(R.id.txtMoney).apply {
                    setTextColor(Color.parseColor("#669900"))
                    text = "+ ${taskLog.money}"
                }
            } else {
                itemView.findViewById<TextView>(R.id.txtMoney).apply {
                    setTextColor(Color.RED)
                    text = "- ${taskLog.money}"
                }
            }

        }

        private fun showMenuAction(context: Context, anchorView: View) {
            var popupMenu = PopupMenu(context, anchorView)
            popupMenu.apply {
                inflate(R.menu.menu_task_actions)
                setOnMenuItemClickListener {
                    when(it.itemId) {
                        R.id.updateTaskLog -> {
                            onUpdateItem(dataSet[adapterPosition])
                            true
                        }
                        R.id.deleteTask -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                moneyTrackerRepo.delete(dataSet[adapterPosition].id)

                                withContext(Dispatchers.Main) {
                                    val currentPos = adapterPosition
                                    val task = dataSet[currentPos]

                                    dataSet.removeAt(currentPos)
                                    notifyItemRemoved(currentPos)
                                    onDeleteItem(task)
                                }
                            }
                            true
                        }
                        else -> false
                    }
                }
            }.also {
                popupMenu.show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_log, parent, false)
        return LogViewHolder(view)
    }

    override fun getItemCount(): Int = dataSet.size

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }
}