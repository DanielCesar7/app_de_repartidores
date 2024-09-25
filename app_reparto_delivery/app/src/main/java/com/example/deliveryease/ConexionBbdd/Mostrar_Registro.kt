package com.example.deliveryease.ConexionBbdd

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.deliveryease.R

class Mostrar_Registro : AppCompatActivity() {

    private val REQUEST_WRITE_STORAGE = 112

    // Variable para saber si estamos importando o exportando
    private var isImporting = false

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mostrar_registro)

        var dataManager = DataManager(this)
        var tvMuestraDatos = findViewById<TextView>(R.id.tvMuestraDatos)
        val btnVerDatos = findViewById<Button>(R.id.btnVerDatos)
        val btnExportar = findViewById<Button>(R.id.btnExportar)
        val btnImportar= findViewById<Button>(R.id.btnImportar)

        btnVerDatos.setOnClickListener {
            // estamos instanciando la clase datamanager
            val nombres = dataManager.getAllData(this)
            tvMuestraDatos.text = nombres //nos muestra los nombres que hay en la tabla
        }

        btnExportar.setOnClickListener {
            isImporting = false // Estamos exportando
            checkPermission()
        }

        btnImportar.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirmación de importación")
            builder.setMessage("¿Estás seguro de que deseas importar la base de datos? Si la base de datos está desactualizada, podrías perder todos los datos actuales.")

            // Botón "Aceptar"
            builder.setPositiveButton("Aceptar") { dialog, which ->
                // Usuario acepta la importación
                isImporting = true // Estamos importando
                checkPermission()  // Aquí llamas a tu función para iniciar el proceso
            }

            // Botón "Cancelar"
            builder.setNegativeButton("Cancelar") { dialog, which ->
                // Usuario cancela la importación
                dialog.dismiss()  // Cierra el diálogo sin hacer nada
            }

            // Muestra el diálogo
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }
    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

                // Solicitar permiso de escritura en almacenamiento externo
                requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_STORAGE)
            } else {
                // Si ya tienes el permiso, procede a la operación (importación o exportación)
                proceedWithOperation()
            }
        } else {
            // Si la versión de Android es menor a M, no es necesario pedir permisos
            proceedWithOperation()
        }
    }

    // Manejo del resultado de la solicitud de permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_WRITE_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // El permiso fue concedido, puedes proceder con la operación
                    proceedWithOperation()
                } else {
                    // El permiso fue denegado
                    Toast.makeText(this, "Permiso denegado. No se puede realizar la operación.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Decide si importar o exportar la base de datos
    private fun proceedWithOperation() {
        val dataTransferManager = DataTransferManager(this)
        if (isImporting) {
            if (dataTransferManager.importDatabaseFromAssets()) {
                Toast.makeText(this, "Base de datos importada correctamente", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error al importar la base de datos", Toast.LENGTH_SHORT).show()
            }
        } else {
            exportarBBDD(dataTransferManager)
        }
    }

    // Método para exportar la base de datos
    private fun exportarBBDD(dataTransferManager: DataTransferManager) {
        if (dataTransferManager.exportDatabase()) {
            Toast.makeText(this, "Base de datos exportada correctamente", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error al exportar la base de datos", Toast.LENGTH_SHORT).show()
        }
    }
}