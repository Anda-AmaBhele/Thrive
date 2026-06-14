package com.yourname.thrive

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "thrive.db"
        const val DATABASE_VERSION = 1

        // Users table
        const val TABLE_USERS = "users"
        const val COL_USER_ID = "id"
        const val COL_USERNAME = "username"
        const val COL_PASSWORD = "password"
        const val COL_FULL_NAME = "full_name"

        // Categories table
        const val TABLE_CATEGORIES = "categories"
        const val COL_CAT_ID = "id"
        const val COL_CAT_NAME = "name"
        const val COL_CAT_COLOR = "color"
        const val COL_CAT_USER_ID = "user_id"

        // Expenses table
        const val TABLE_EXPENSES = "expenses"
        const val COL_EXP_ID = "id"
        const val COL_EXP_AMOUNT = "amount"
        const val COL_EXP_DESCRIPTION = "description"
        const val COL_EXP_DATE = "date"
        const val COL_EXP_CATEGORY_ID = "category_id"
        const val COL_EXP_USER_ID = "user_id"
        const val COL_EXP_PHOTO = "photo_path"

        // Budget table
        const val TABLE_BUDGET = "budget"
        const val COL_BUD_ID = "id"
        const val COL_BUD_USER_ID = "user_id"
        const val COL_BUD_CATEGORY_ID = "category_id"
        const val COL_BUD_AMOUNT = "amount"
        const val COL_BUD_MONTH = "month"

        // Savings table
        const val TABLE_SAVINGS = "savings_goals"
        const val COL_SAV_ID = "id"
        const val COL_SAV_USER_ID = "user_id"
        const val COL_SAV_NAME = "name"
        const val COL_SAV_TARGET = "target_amount"
        const val COL_SAV_SAVED = "saved_amount"
        const val COL_SAV_DATE = "target_date"

        // Badges table
        const val TABLE_BADGES = "badges"
        const val COL_BAD_ID = "id"
        const val COL_BAD_USER_ID = "user_id"
        const val COL_BAD_NAME = "name"
        const val COL_BAD_EARNED = "earned"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_USERS (
                $COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_FULL_NAME TEXT NOT NULL,
                $COL_USERNAME TEXT NOT NULL UNIQUE,
                $COL_PASSWORD TEXT NOT NULL
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_CATEGORIES (
                $COL_CAT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CAT_NAME TEXT NOT NULL,
                $COL_CAT_COLOR TEXT NOT NULL,
                $COL_CAT_USER_ID INTEGER,
                FOREIGN KEY($COL_CAT_USER_ID) REFERENCES $TABLE_USERS($COL_USER_ID)
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_EXPENSES (
                $COL_EXP_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_EXP_AMOUNT REAL NOT NULL,
                $COL_EXP_DESCRIPTION TEXT,
                $COL_EXP_DATE TEXT NOT NULL,
                $COL_EXP_CATEGORY_ID INTEGER,
                $COL_EXP_USER_ID INTEGER,
                $COL_EXP_PHOTO TEXT,
                FOREIGN KEY($COL_EXP_CATEGORY_ID) REFERENCES $TABLE_CATEGORIES($COL_CAT_ID),
                FOREIGN KEY($COL_EXP_USER_ID) REFERENCES $TABLE_USERS($COL_USER_ID)
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_BUDGET (
                $COL_BUD_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_BUD_USER_ID INTEGER,
                $COL_BUD_CATEGORY_ID INTEGER,
                $COL_BUD_AMOUNT REAL NOT NULL,
                $COL_BUD_MONTH TEXT NOT NULL,
                FOREIGN KEY($COL_BUD_USER_ID) REFERENCES $TABLE_USERS($COL_USER_ID),
                FOREIGN KEY($COL_BUD_CATEGORY_ID) REFERENCES $TABLE_CATEGORIES($COL_CAT_ID)
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_SAVINGS (
                $COL_SAV_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_SAV_USER_ID INTEGER,
                $COL_SAV_NAME TEXT NOT NULL,
                $COL_SAV_TARGET REAL NOT NULL,
                $COL_SAV_SAVED REAL DEFAULT 0,
                $COL_SAV_DATE TEXT NOT NULL,
                FOREIGN KEY($COL_SAV_USER_ID) REFERENCES $TABLE_USERS($COL_USER_ID)
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_BADGES (
                $COL_BAD_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_BAD_USER_ID INTEGER,
                $COL_BAD_NAME TEXT NOT NULL,
                $COL_BAD_EARNED INTEGER DEFAULT 0,
                FOREIGN KEY($COL_BAD_USER_ID) REFERENCES $TABLE_USERS($COL_USER_ID)
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BADGES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SAVINGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BUDGET")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }
}