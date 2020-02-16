package com.example.visited_places_app.database

class DatabaseCreds {

    companion object {
        val DATABASE_NAME = "visited_places"
        val DATABASE_VERSION = 1

        val DATABASE_TABLE = "visited_places"
        val ROW_ID = "id"
        val ROW_PICTURE = "picture"
        val ROW_LATITUDE = "latitude"
        val ROW_LONGITUDE = "longitude"

        val QUERY_CREATE =
            "CREATE TABLE IF NOT EXISTS $DATABASE_TABLE ($ROW_ID CHAR(32), $ROW_PICTURE CHAR(250), $ROW_LATITUDE REAL, $ROW_LONGITUDE REAL)"
        val QUERY_UPGRADE = "DROP TABLE IF EXISTS $DATABASE_TABLE"
    }
}