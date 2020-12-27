package com.max.spirihin.mytracksdb.utilities

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.room.RoomDatabase
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.Comparator

/**
 * Copyright 2020 Raphael Ebner
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * https://github.com/rafi0101/Android-Room-Database-Backup
 */
class RoomBackup {

    interface OnCompleteListener {
        fun onComplete(success: Boolean, message: String)
    }

    companion object {
        private var TAG = "debug_RoomBackup"
        private lateinit var INTERNAL_BACKUP_PATH: Path
        private lateinit var TEMP_BACKUP_PATH: Path
        private lateinit var TEMP_BACKUP_FILE: Path
        private lateinit var EXTERNAL_BACKUP_PATH: Path
        private lateinit var DATABASE_FILE: Path
    }

    private lateinit var dbName: String

    private var context: Context? = null
    private var roomDatabase: RoomDatabase? = null
    private var enableLogDebug: Boolean = false
    private var onCompleteListener: OnCompleteListener? = null
    private var customRestoreDialogTitle: String = "Choose file to restore"
    private var customBackupFileName: String? = null
    private var useExternalStorage: Boolean = true
    private var maxFileCount: Int? = null

    /**
     * Set Context
     *
     * @param context Context
     */
    fun context(context: Context): RoomBackup {
        this.context = context
        return this
    }

    /**
     * Set RoomDatabase instance
     *
     * @param roomDatabase RoomDatabase
     */
    fun database(roomDatabase: RoomDatabase): RoomBackup {
        this.roomDatabase = roomDatabase
        return this
    }

    /**
     * Set LogDebug enabled / disabled
     *
     * @param enableLogDebug Boolean
     */
    fun enableLogDebug(enableLogDebug: Boolean): RoomBackup {
        this.enableLogDebug = enableLogDebug
        return this
    }

    /**
     * Set onCompleteListener, to run code when tasks completed
     *
     * @param onCompleteListener OnCompleteListener
     */
    fun onCompleteListener(onCompleteListener: OnCompleteListener): RoomBackup {
        this.onCompleteListener = onCompleteListener
        return this
    }

    /**
     * Set onCompleteListener, to run code when tasks completed
     *
     * @param listener (success: Boolean, message: String) -> Unit
     */
    fun onCompleteListener(listener: (success: Boolean, message: String) -> Unit): RoomBackup {
        this.onCompleteListener = object : OnCompleteListener {
            override fun onComplete(success: Boolean, message: String) {
                listener(success, message)
            }
        }
        return this
    }

    /**
     * Set custom log tag, for detailed debugging
     *
     * @param customLogTag String
     */
    fun customLogTag(customLogTag: String): RoomBackup {
        TAG = customLogTag
        return this
    }

    /**
     * Set custom Restore Dialog Title, default = "Choose file to restore"
     *
     * @param customRestoreDialogTitle String
     */
    fun customRestoreDialogTitle(customRestoreDialogTitle: String): RoomBackup {
        this.customRestoreDialogTitle = customRestoreDialogTitle
        return this
    }

    /**
     * Set custom Backup File Name, default = "$dbName-$currentTime.sqlite3"
     *
     * @param customBackupFileName String
     */
    fun customBackupFileName(customBackupFileName: String): RoomBackup {
        this.customBackupFileName = customBackupFileName
        return this
    }

    /**
     * Set export / import to External Storage enabled / disabled, if you want to export / import the backup to / from external storage
     * then you have access to the backup and can save it somewhere else
     *
     *
     * @param useExternalStorage Boolean, default = false
     */
    fun useExternalStorage(useExternalStorage: Boolean): RoomBackup {
        this.useExternalStorage = useExternalStorage
        return this
    }

    /**
     * Set max backup files count
     * if fileCount is > maxFileCount the oldest backup file will be deleted
     * is for both internal and external storage
     *
     *
     * @param maxFileCount Int, default = null
     */
    fun maxFileCount(maxFileCount: Int): RoomBackup {
        this.maxFileCount = maxFileCount
        return this
    }

    /**
     * Init vars, and return true if no error occurred
     */
    private fun initRoomBackup(): Boolean {
        if (context == null) {
            if (enableLogDebug) Log.d(TAG, "context is missing")
            onCompleteListener?.onComplete(false, "context is missing")
            //        throw IllegalArgumentException("context is not initialized")
            return false
        }
        if (roomDatabase == null) {
            if (enableLogDebug) Log.d(TAG, "roomDatabase is missing")
            onCompleteListener?.onComplete(false, "roomDatabase is missing")
            //       throw IllegalArgumentException("roomDatabase is not initialized")
            return false
        }

        dbName = roomDatabase!!.openHelper.databaseName
        INTERNAL_BACKUP_PATH = Paths.get("${context!!.filesDir}/databasebackup/")
        TEMP_BACKUP_PATH = Paths.get("${context!!.filesDir}/databasebackup-temp/")
        TEMP_BACKUP_FILE = Paths.get("$TEMP_BACKUP_PATH/tempbackup.sqlite3")
        EXTERNAL_BACKUP_PATH = Paths.get(Environment.getExternalStorageDirectory().absolutePath + "/MyTracksDB")
        DATABASE_FILE = Paths.get(context!!.getDatabasePath(dbName).toURI())

        //Create internal and temp backup directory if does not exist
        try {
            Files.createDirectory(INTERNAL_BACKUP_PATH)
            Files.createDirectory(TEMP_BACKUP_PATH)
        } catch (e: FileAlreadyExistsException) {
        } catch (e: IOException) {
        }

        if (enableLogDebug) {
            Log.d(TAG, "DatabaseName: $dbName")
            Log.d(TAG, "Database Location: $DATABASE_FILE")
            Log.d(TAG, "INTERNAL_BACKUP_PATH: $INTERNAL_BACKUP_PATH")
            Log.d(TAG, "EXTERNAL_BACKUP_PATH: $EXTERNAL_BACKUP_PATH")
        }
        return true

    }

    /**
     * Start Backup process, and set onComplete Listener to success, if no error occurred, else onComplete Listener success is false and error message is passed
     */
    fun backup() {
        if (enableLogDebug) Log.d(TAG, "Starting Backup ...")
        val success = initRoomBackup()
        if (!success) return

        //Close the database
        roomDatabase!!.close()

        //Create name for backup file, if no custom name is set: Database name + currentTime + .sqlite3
        var filename = if (customBackupFileName == null) "$dbName-${getTime()}.sqlite3" else customBackupFileName as String
        //Add .aes extension to filename, if file is encrypted

        //Path to save current database
        val backuppath = if (useExternalStorage) Paths.get("$EXTERNAL_BACKUP_PATH/$filename") else Paths.get("$INTERNAL_BACKUP_PATH/$filename")

        //Copy current database to save location (/files dir)
        Files.copy(DATABASE_FILE, backuppath, StandardCopyOption.REPLACE_EXISTING)

        if (enableLogDebug) Log.d(TAG, "Saved to: $backuppath")

        //If maxFileCount is set and is reached, delete oldest file
        if (maxFileCount != null) {
            val deleted = deleteOldBackup()
            if (!deleted) return
        }

        onCompleteListener?.onComplete(true, "success")
    }

    /**
     * @return current time formatted as String
     */
    private fun getTime(): String {

        val currentTime = LocalDateTime.now()

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss")

        return currentTime.format(formatter)

    }

    /**
     * If maxFileCount is set, and reached, all old files will be deleted
     *
     * @return true if old files deleted or nothing to do
     */
    private fun deleteOldBackup(): Boolean {
        //Path of Backup Directory
        val backupDirectory = if (useExternalStorage) File("$EXTERNAL_BACKUP_PATH/") else File(INTERNAL_BACKUP_PATH.toUri())

        //All Files in an Array of type File
        val arrayOfFiles = backupDirectory.listFiles()

        //If array is null or empty nothing to do and return
        if (arrayOfFiles.isNullOrEmpty()) {
            if (enableLogDebug) Log.d(TAG, "")
            onCompleteListener?.onComplete(false, "maxFileCount: Failed to get list of backups")
            return false
        } else if (arrayOfFiles.size > maxFileCount!!) {
            //Sort Array: lastModified
            Arrays.sort(arrayOfFiles, Comparator.comparingLong { obj: File -> obj.lastModified() })

            //Get count of files to delete
            val fileCountToDelete = arrayOfFiles.size - maxFileCount!!

            for (i in 1..fileCountToDelete) {
                //Delete all old files (i-1 because array starts a 0)
                Files.delete(arrayOfFiles[i - 1].toPath())

                if (enableLogDebug) Log.d(TAG, "maxFileCount reached: ${arrayOfFiles[i - 1]} deleted")
            }
        }
        return true
    }

    /**
     * Start Restore process, and set onComplete Listener to success, if no error occurred, else onComplete Listener success is false and error message is passed
     * this function shows a list of all available backup files in a MaterialAlertDialog
     * and calls restoreSelectedFile(filename) to restore selected file
     */
    fun restore(filename: String) {
        if (enableLogDebug) Log.d(TAG, "Starting Restore ...")
        val success = initRoomBackup()
        if (!success) return

        //Path of Backup Directory
        val backupDirectory = if (useExternalStorage) Paths.get("$EXTERNAL_BACKUP_PATH/") else INTERNAL_BACKUP_PATH

        //All Files in an Array of type File
        val path = Paths.get("$backupDirectory/$filename.sqlite3")

        if (enableLogDebug) Log.d(TAG, "Restore selected file...")
        //Close the database
        roomDatabase!!.close()

        if (!Files.exists(path)) {
            onCompleteListener?.onComplete(false, "File $path does not exist")
            return
        }

        //Copy back database and replace current database
        Files.copy(path, DATABASE_FILE, StandardCopyOption.REPLACE_EXISTING)
        if (enableLogDebug) Log.d(TAG, "Restored File: $path")
        onCompleteListener?.onComplete(true, "success")
    }

}