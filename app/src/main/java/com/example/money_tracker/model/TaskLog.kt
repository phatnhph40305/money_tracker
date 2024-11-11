package com.example.money_tracker.model

enum class LogType{
    ADD,
    SUBTRACT
}
data class TaskLog(val id: Int = 0, var name: String , var money: Int , var type: LogType)