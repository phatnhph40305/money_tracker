package com.example.money_tracker.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DbConfig {
    companion object {
        const val DB_NAME = "app-money-tracker"
        const val DB_VERSION = 1
    }

    object TaskLog {
        const val TABLE_NAME = "task_log";
        const val COL_ID = "_id";
        const val COL_NAME = "taskName";
        const val COL_MONEY = "money";
        const val COL_TYPE = "type";
        fun buildSchema() =
            """
                CREATE TABLE $TABLE_NAME(
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME TEXT,
                $COL_MONEY TEXT ,
                $COL_TYPE TEXT
                )
           """

        fun dropTable() = "DROP TABLE IF EXISTS $TABLE_NAME"
    }
}

class MoneyTrackerDb : SQLiteOpenHelper {
    constructor(context: Context) : super(context, DbConfig.DB_NAME, null, DbConfig.DB_VERSION)

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(DbConfig.TaskLog.buildSchema().toString())
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(DbConfig.TaskLog.dropTable())
        db?.execSQL(DbConfig.TaskLog.buildSchema().toString())

    }
}