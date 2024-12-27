package com.example.deliveryease.Clases

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.deliveryease.ConexionBbdd.DataManager
import com.example.deliveryease.R

class Creacion_De_Nuevo_Usuario : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_nuevo_usuario)

        var dataManager = DataManager(this)

        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etPrimerApellido = findViewById<EditText>(R.id.etPrimerApellido)
        val etTelefono = findViewById<EditText>(R.id.etTelefono)
        val etCorreo = findViewById<EditText>(R.id.etCorreo) // Corrección aquí
        val etPass = findViewById<EditText>(R.id.etPass)
        val btnRegistro = findViewById<Button>(R.id.btnRegistro)
        val btnVerDatos = findViewById<Button>(R.id.btnVerDatos)
        val etCentro = findViewById<EditText>(R.id.etCentro)

        btnRegistro.setOnClickListener {

            val nombre = etNombre.text.toString().trim()
            val primerApellido = etPrimerApellido.text.toString().trim()
            val contrasenia = etPass.text.toString().trim()
            val numeroTelefono = etTelefono.text.toString().trim()
            val correoElectronico = etCorreo.text.toString().trim()
            val centro = etCentro.text.toString().trim()

            if (nombre.isEmpty() || primerApellido.isEmpty() || contrasenia.isEmpty() || numeroTelefono.isEmpty() || correoElectronico.isEmpty() || centro.isEmpty()) {
                Toast.makeText(this, "Por favor, ingrese todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                // Guardar el centro en SharedPreferences
                val sharedPreferences = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("centroUsuario", centro)
                editor.apply()

                dataManager.addData(nombre, primerApellido, contrasenia, numeroTelefono, correoElectronico, centro)
                Toast.makeText(this, "Se agregó a la base de datos todos los valores de: $nombre, $primerApellido", Toast.LENGTH_SHORT).show()
                etNombre.text.clear()
                etPrimerApellido.text.clear()
                etPass.text.clear()
                etTelefono.text.clear()
                etCorreo.text.clear() // Corrección aquí
                etCentro.text.clear()

                val intent = Intent(this, com.example.deliveryease.Principal::class.java)
                startActivity(intent)
                Toast.makeText(this, "¡Registro del nuevo usuario hecho correctamente!", Toast.LENGTH_SHORT).show()
                Log.d(ContentValues.TAG, "Botón registro funcionando correctamente")
            }
        }

        btnVerDatos.setOnClickListener {

            val intent = Intent(this, Mostrar_Registro_Y_Exportacion_E_Importacion_De_Datos::class.java)
            startActivity(intent)
            Toast.makeText(this, "¡Has ido a la página para comprobar tu registro!", Toast.LENGTH_SHORT).show()
            Log.d(ContentValues.TAG, "Botón ver datos funcionando correctamente")
        }
    }
}