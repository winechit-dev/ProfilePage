package com.catalin.profilepage

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.IOUtils

suspend fun Context.compressFile(file: File, width: Int = 800, height: Int = 450): File {
    return withContext(Dispatchers.IO) {
        Compressor.compress(this@compressFile, file) {
            default(
                width = width,
                height = height,
                format = Bitmap.CompressFormat.WEBP,
                quality = 70
            )
        }
    }
}

@SuppressLint("Recycle")
fun Uri.getFile(context: Context): File {
    val parcelFileDescriptor =
        context.contentResolver.openFileDescriptor(this, "r", null)
    var file: File? = null
    parcelFileDescriptor?.let {
        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        file = File(
            context.cacheDir,
            context.contentResolver.getFileName(this)
        )
        val outputStream = FileOutputStream(file)
        IOUtils.copy(inputStream, outputStream)
    }
    return file!!
}

fun ContentResolver.getFileName(fileUri: Uri): String {
    var name = ""
    val returnCursor = this.query(fileUri, null, null, null, null)
    if (returnCursor != null) {
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        name = returnCursor.getString(nameIndex)
        returnCursor.close()
    }
    return name
}