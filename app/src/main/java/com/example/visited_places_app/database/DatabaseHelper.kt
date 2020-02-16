package com.example.visited_places_app.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.visited_places_app.model.VisitedPlace
import com.google.android.gms.maps.model.LatLng


/**
 * Created by jonesrandom on 11/14/17.
 *
 * #JanganLupaBahagia
 *
 */
class DatabaseHelper(ctx: Context) :
    SQLiteOpenHelper(ctx, DatabaseCreds.DATABASE_NAME, null, DatabaseCreds.DATABASE_VERSION) {

    companion object {
        private lateinit var INSTANCE: DatabaseHelper
        private lateinit var database: SQLiteDatabase
        private var databaseOpen: Boolean = false

        fun closeDatabase() {
            if (database.isOpen && databaseOpen) {
                database.close()
                databaseOpen = false

                Log.i("Database", "Database close")
            }
        }

        fun initDatabaseInstance(ctx: Context): DatabaseHelper {
            INSTANCE = DatabaseHelper(ctx)
            return INSTANCE
        }

        fun insertData(visitedPlace: VisitedPlace): Long {

            if (!databaseOpen) {
                database = INSTANCE.writableDatabase
                databaseOpen = true

                Log.i("Database", "Database Open")
            }

            val values = ContentValues()
            values.put(DatabaseCreds.ROW_ID, visitedPlace.GetID())
            values.put(DatabaseCreds.ROW_PICTURE, visitedPlace.GetImage())
            values.put(DatabaseCreds.ROW_LATITUDE, visitedPlace.GetPosition().latitude)
            values.put(DatabaseCreds.ROW_LONGITUDE, visitedPlace.GetPosition().longitude)
            return database.insert(DatabaseCreds.DATABASE_TABLE, null, values)
        }

        fun updateData(visitedPlace: VisitedPlace): Int {
            if (!databaseOpen) {
                database = INSTANCE.writableDatabase
                databaseOpen = true

                Log.i("Database", "Database Open")
            }

            val values = ContentValues()
            values.put(DatabaseCreds.ROW_PICTURE, visitedPlace.GetImage())
            values.put(DatabaseCreds.ROW_LATITUDE, visitedPlace.GetPosition().latitude)
            values.put(DatabaseCreds.ROW_LONGITUDE, visitedPlace.GetPosition().longitude)
            return database.update(
                DatabaseCreds.DATABASE_TABLE,
                values,
                "${DatabaseCreds.ROW_ID} = '${visitedPlace.GetID()}'",
                null
            )
        }

        fun getAllData(): HashMap<LatLng, VisitedPlace> {
            if (!databaseOpen) {
                database = INSTANCE.writableDatabase
                databaseOpen = true

                Log.i("Database", "Database Open")
            }

            val data: HashMap<LatLng, VisitedPlace> = HashMap()
            val cursor = database.rawQuery("SELECT * FROM ${DatabaseCreds.DATABASE_TABLE}", null)
            cursor.use { cur ->
                if (cursor.moveToFirst()) {
                    do {
                        val latitude = cur.getDouble(cur.getColumnIndex(DatabaseCreds.ROW_LATITUDE))
                        val longitude = cur.getDouble(cur.getColumnIndex(DatabaseCreds.ROW_LONGITUDE))
                        val position = LatLng(latitude, longitude)

                        val visitedPlace = VisitedPlace(position)
                        visitedPlace.SetID(cur.getString(cur.getColumnIndex(DatabaseCreds.ROW_ID)))
                        visitedPlace.SetImage(cur.getString(cur.getColumnIndex(DatabaseCreds.ROW_PICTURE)))

                        data[position] = visitedPlace

                    } while (cursor.moveToNext())
                }
            }
            return data
        }

        fun deleteData(id: Int): Int {
            if (!databaseOpen) {
                database = INSTANCE.writableDatabase
                databaseOpen = true

                Log.i("Database", "Database Open")
            }
            return database.delete(
                DatabaseCreds.DATABASE_TABLE,
                "${DatabaseCreds.ROW_ID} = $id",
                null
            )
        }
    }

    override fun onCreate(p0: SQLiteDatabase?) {
        p0?.execSQL(DatabaseCreds.QUERY_CREATE)
        Log.i("DATABASE", "DATABASE CREATED")
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        p0?.execSQL(DatabaseCreds.QUERY_UPGRADE)
        Log.i("DATABASE", "DATABASE UPDATED")
    }
}