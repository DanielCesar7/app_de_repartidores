package com.example.app_repartidores.ConexionBbdd

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel

class DataTransferManager (private val context: Context) {

    companion object {
        private const val DATABASE_NAME = "DeeliveryEaseeeee.db"
    }

    fun exportDatabase(): Boolean {
        val dbFile = context.getDatabasePath(DATABASE_NAME)
        val exportDir = File(Environment.getExternalStorageDirectory(), "DeeliveryEaseBackup")

        if (!exportDir.exists()) {
            if (!exportDir.mkdirs()) {
                return false
            }
        }

        val destFile = File(exportDir, DATABASE_NAME)

        return try {
            val src: FileChannel = FileInputStream(dbFile).channel
            val dst: FileChannel = FileOutputStream(destFile).channel
            dst.transferFrom(src, 0, src.size())
            src.close()
            dst.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun importDatabaseFromAssets(): Boolean {
        // Nombre del archivo de la base de datos en la carpeta assets
        val assetDbFileName = "DeeliveryEaseeeee.db"

        // Obtener la ruta de la base de datos interna de la app
        val dbFile = context.getDatabasePath(DATABASE_NAME)

        return try {
            // Obtener acceso al AssetManager para leer el archivo en assets
            val assetManager = context.assets
            val inputStream = assetManager.open(assetDbFileName)

            // Crear un canal de destino para copiar el archivo a la base de datos interna
            val outputStream = FileOutputStream(dbFile)

            // Copiar los datos desde el archivo de assets al archivo de la base de datos
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

            // Cerrar los streams
            inputStream.close()
            outputStream.flush()
            outputStream.close()

            true // La importación ha sido exitosa
        } catch (e: IOException) {
            e.printStackTrace()
            false // Error al importar la base de datos
        }
    }
}