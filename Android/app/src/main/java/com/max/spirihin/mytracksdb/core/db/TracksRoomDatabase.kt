package com.max.spirihin.mytracksdb.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = arrayOf(Track::class), version = 6)
@TypeConverters(Converters::class)
abstract class TracksRoomDatabase : RoomDatabase() {
    abstract fun dao(): TrackDao

    companion object {
        val MIGRATIONS: Array<Migration> = arrayOf(
                object : Migration(3, 4) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        database.execSQL("ALTER TABLE track ADD COLUMN date INTEGER NOT NULL DEFAULT (0)")
                    }
                },
                object : Migration(4, 5) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        database.execSQL("ALTER TABLE track ADD COLUMN distance INTEGER NOT NULL DEFAULT (0)")
                        database.execSQL("ALTER TABLE track ADD COLUMN duration INTEGER NOT NULL DEFAULT (0)")
                        database.execSQL("ALTER TABLE track ADD COLUMN exercise_type NVARCHAR(50) NOT NULL default('')")
                    }
                },
                object : Migration(5, 6) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        database.execSQL("ALTER TABLE track ADD COLUMN pace INTEGER NOT NULL DEFAULT (0)")
                    }
                }
        )
    }
}