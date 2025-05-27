package com.example.timeblock.util

import android.content.Context
import java.io.File

fun exportDatabase(context: Context, dest: File) {
    val dbFile = context.getDatabasePath("timeblock_database")
    dbFile.copyTo(dest, overwrite = true)
}

fun importDatabase(context: Context, source: File) {
    val dbFile = context.getDatabasePath("timeblock_database")
    source.copyTo(dbFile, overwrite = true)
}
