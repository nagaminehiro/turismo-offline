package br.com.turismooffline.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TouristSpotDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_SPOTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT NOT NULL,
                $COLUMN_LATITUDE REAL NOT NULL,
                $COLUMN_LONGITUDE REAL NOT NULL,
                $COLUMN_ADDRESS TEXT,
                $COLUMN_IMAGE_BLOB BLOB,
                $COLUMN_IMAGE_URI TEXT
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_SPOTS ADD COLUMN $COLUMN_IMAGE_BLOB BLOB")
        }
    }

    fun findAll(): List<TouristSpot> {
        val spots = mutableListOf<TouristSpot>()
        readableDatabase.query(
            TABLE_SPOTS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_NAME COLLATE NOCASE ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                spots += TouristSpot(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)),
                    longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)),
                    address = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)),
                    imageBytes = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_BLOB)),
                    imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI))
                )
            }
        }
        return spots
    }

    fun save(spot: TouristSpot): Long {
        val values = ContentValues().apply {
            put(COLUMN_NAME, spot.name)
            put(COLUMN_DESCRIPTION, spot.description)
            put(COLUMN_LATITUDE, spot.latitude)
            put(COLUMN_LONGITUDE, spot.longitude)
            put(COLUMN_ADDRESS, spot.address)
            if (spot.imageBytes == null) putNull(COLUMN_IMAGE_BLOB) else put(COLUMN_IMAGE_BLOB, spot.imageBytes)
            put(COLUMN_IMAGE_URI, spot.imageUri)
        }
        return if (spot.id == 0L) {
            writableDatabase.insertOrThrow(TABLE_SPOTS, null, values)
        } else {
            writableDatabase.update(
                TABLE_SPOTS,
                values,
                "$COLUMN_ID = ?",
                arrayOf(spot.id.toString())
            )
            spot.id
        }
    }

    fun delete(id: Long) {
        writableDatabase.delete(TABLE_SPOTS, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    companion object {
        private const val DATABASE_NAME = "turismo_offline.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_SPOTS = "tourist_spots"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_LATITUDE = "latitude"
        private const val COLUMN_LONGITUDE = "longitude"
        private const val COLUMN_ADDRESS = "address"
        private const val COLUMN_IMAGE_BLOB = "image_blob"
        private const val COLUMN_IMAGE_URI = "image_uri"
    }
}
