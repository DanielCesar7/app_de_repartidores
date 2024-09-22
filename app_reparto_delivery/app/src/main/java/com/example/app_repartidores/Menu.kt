package com.example.app_repartidores

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
import com.example.app_repartidores.ConexionBbdd.DataManager
import com.example.app_repartidores.ConexionBbdd.Logeado
import com.example.app_repartidores.ConexionBbdd.Registro

class Menu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu)
        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etPass = findViewById<EditText>(R.id.etPass)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnCreacionUsuario = findViewById<Button>(R.id.btnCreacionUsuario)

        btnCreacionUsuario.setOnClickListener {
            val intent = Intent(this, Registro::class.java)
            startActivity(intent)
            Toast.makeText(this, "¡Le has dado al botón nuevo usuario!", Toast.LENGTH_SHORT).show()
            Log.d(ContentValues.TAG, "Botón nuevo usuario funcionando correctamente");
        } // btn

        btnLogin.setOnClickListener {
            val nombre = etNombre.text.toString()
            val contrasenia = etPass.text.toString()

            if (nombre.isEmpty() || contrasenia.isEmpty()) {
                Toast.makeText(this, "Por favor, ingrese todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                val dataManager = DataManager(this)
                val isUserValid = dataManager.verifyUser(nombre, contrasenia)

                if (isUserValid) {

                    // Recuperar el centro del usuario desde la base de datos
                    val centroUsuario = dataManager.getCentroUsuario(nombre)

                    // Obtener los pedidos del usuario
                    //val pedidos = dataManager.getPedidosDeUsuario(nombre)

                    // Convertir la lista de pedidos en un ArrayList para pasarlo como extra
                    //val pedidosArrayList = ArrayList(pedidos) // Convertimos la lista en ArrayList para que sea serializable

                    val usuarioID = dataManager.getUsuarioID(nombre)

                    val intent = Intent(this, Logeado::class.java)
                    intent.putExtra("nombreUsuario", nombre)
                    intent.putExtra("centroUsuario", centroUsuario)
                    intent.putExtra("usuarioID", usuarioID)
                    //intent.putExtra("pedidos", pedidosArrayList) // Pasamos la lista de pedidos como extra

                    startActivity(intent)
                    Toast.makeText(this, "¡Te has logeado!", Toast.LENGTH_SHORT).show()
                    Log.d(ContentValues.TAG, "Botón login funcionando correctamente")
                } else {
                    Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}