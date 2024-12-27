package com.example.deliveryease.Clases

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.deliveryease.ConexionBbdd.DataManager

import com.example.deliveryease.R
import java.util.Calendar

class Registro_de_pedidos : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_logeado)

        val textViewData = findViewById<TextView>(R.id.textViewData)

        val dataManager = DataManager(this)

        val nombreUsuario = intent.getStringExtra("nombreUsuario")
        val centroUsuario = nombreUsuario?.let { dataManager.getCentroUsuario(it) }
        val usuarioID = intent.getIntExtra("usuarioID", -1)


        if (nombreUsuario != null && centroUsuario != null) {
            textViewData.text = "¡¡Bienvenido, $nombreUsuario!!\nCentro: $centroUsuario"
        }

        val dirrecion = findViewById<EditText>(R.id.etDirrecion)
        val modoDePago = findViewById<Spinner>(R.id.spinnerModoDePago)
        val precio = findViewById<EditText>(R.id.etPrecio)
        val telefono = findViewById<EditText>(R.id.etTelefono)
        val fecha = findViewById<DatePicker>(R.id.datePicker)
        val contenidoPedido = findViewById<EditText>(R.id.etContenidoPedido)
        val propina =findViewById<EditText>(R.id.etPropina)

        val btnRegistro = findViewById<Button>(R.id.btnRegistro)

        btnRegistro.setOnClickListener {
            val direccionText = dirrecion.text.toString().trim()
            val modoDePagoText = modoDePago.selectedItem.toString().trim()
            val precioText = precio.text.toString().trim()
            val telefonoText = telefono.text.toString().trim()
            val contenidoPedidoText= contenidoPedido.text.toString().trim()
            val propinaText= propina.text.toString().trim()

            // Validar que los campos no estén vacíos
            if (contenidoPedidoText.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa algun pedido.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validar que los campos no estén vacíos
            if (direccionText.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa la dirección.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (modoDePagoText.isEmpty()) {
                Toast.makeText(this, "Por favor, selecciona un modo de pago.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (precioText.isEmpty() || precioText.toDoubleOrNull() == null || precioText.toDouble() <= 0.0) {
                Toast.makeText(this, "Por favor, ingresa un precio válido.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (telefonoText.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa un número de teléfono.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Verificar que el número contenga solo dígitos o solo guiones
            val regexSoloNumeros = Regex("^[0-9]+$")   // Solo permite números
            val regexSoloGuiones = Regex("^-+$")        // Solo permite guiones

            // Verificar si es un número de teléfono con exactamente 9 dígitos
            if (regexSoloNumeros.matches(telefonoText)) {
                if (telefonoText.length != 9) {
                    Toast.makeText(this, "El número debe contener exactamente 9 dígitos.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            } else if (!regexSoloGuiones.matches(telefonoText)) {
                Toast.makeText(this, "El número solo puede contener dígitos o guiones", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            // Obtener fecha del DatePicker
            val day = fecha.dayOfMonth
            val month = fecha.month + 1 // Enero es 0, por eso sumamos 1
            val year = fecha.year
            val fechaString = String.format("%02d/%02d/%04d", day, month, year)

            // Validar y convertir el campo propina
            val propinas: Double? = if (propinaText.isEmpty()) {
                null // Si el campo está vacío, asignar null
            } else {
                propinaText.toDoubleOrNull() // Intentar convertir a Double
            }

            // Si propinaText no puede convertirse a Double y no es nulo, mostrar error
            if (propinaText.isNotEmpty() && propina == null) {
                Toast.makeText(this, "Por favor, ingresa una propina válida.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            /*val mensajePrueba=
                    "Fecha: $fechaString"*/

            // Llamada a la función que agrega el pedido

            dataManager.addPedido(usuarioID, direccionText, modoDePagoText, precioText.toDouble(), telefonoText, fechaString, contenidoPedidoText,  propinas)
            Toast.makeText(this, "Registro con éxito", Toast.LENGTH_SHORT).show()

            // Limpiar los campos después de registrar el pedido
            contenidoPedido.text.clear()
            dirrecion.text.clear()
            modoDePago.setSelection(0)
            precio.text.clear()
            telefono.text.clear()
            propina.text.clear()
            // modoDePago.setSelection(0) // Restablecer el Spinner a la primera opción
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH) + 1 // Enero es 0, sumamos 1 para trabajar con tu lógica
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
            // Restablecer el DatePicker a la fecha actual
            fecha.updateDate(currentYear, currentMonth - 1, currentDay)
        }

        val btnEliminar = findViewById<Button>(R.id.btnEliminarPedido)

        btnEliminar.setOnClickListener {

            if (dirrecion.text.isNullOrEmpty()) {
                Toast.makeText(this, "Por favor, introduce una dirección.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Obtener los datos del usuario
            val direccionText = dirrecion.text.toString()
            val modoDePagoText = modoDePago.selectedItem.toString()

            // Obtener fecha del DatePicker
            val day = fecha.dayOfMonth
            val month = fecha.month + 1 // Enero es 0, por eso sumamos 1
            val year = fecha.year
            val fechaString = "$day/$month/$year"

            dataManager.eliminarPedido(usuarioID, direccionText, modoDePagoText, fechaString)
            Toast.makeText(this, "¡Le has dado al botón eliminar!", Toast.LENGTH_SHORT).show()

            // Limpiar los campos
            contenidoPedido.text.clear()
            dirrecion.text.clear()
            modoDePago.setSelection(0)
            precio.text.clear()
            telefono.text.clear()
            propina.text.clear()
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH) + 1 // Enero es 0, sumamos 1 para trabajar con tu lógica
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
            // Restablecer el DatePicker a la fecha actual
            fecha.updateDate(currentYear, currentMonth - 1, currentDay)
        }

        val btnVer = findViewById<Button>(R.id.btnVerPedido)
        val checkBoxFecha = findViewById<CheckBox>(R.id.chkIncluirFecha)
        val checkBoxEstado = findViewById<CheckBox>(R.id.checkBoxEstado)
        val checkBoxPropina = findViewById<CheckBox>(R.id.chkActivarPropina)

        btnVer.setOnClickListener {

            // Obtener los datos del usuario
            val contenidoPedidoText= contenidoPedido.text.toString()
            val direccionText = dirrecion.text.toString()
            val modoDePagoText = modoDePago.selectedItem.toString()
            val precioText = precio.text.toString().trim()
            val telefonoText = telefono.text.toString().trim()
            val propinaText = propina.text.toString().trim()

            // Verificar si el CheckBox de desactivar fecha está activado
            val fechaString = if (checkBoxFecha.isChecked) {
                // Si está marcado, no utilizar la fecha
                null
            } else {
                // Obtener fecha del DatePicker solo si no está marcado
                val day = fecha.dayOfMonth
                val month = fecha.month + 1 // Enero es 0, por eso sumamos 1
                val year = fecha.year
                String.format("%02d/%02d/%04d", day, month, year) // Formatear día y mes con dos dígitos
            }

            // Convertir el precio a Double si no está vacío
            val precioDouble = if (precioText.isNotEmpty()) {
                try {
                    precioText.toDouble() // Intentar convertir el texto a Double
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "El precio debe ser un número válido", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener // Detener la ejecución si no es un número válido
                }
            } else {
                null // Si el campo está vacío, establecer null
            }

            // Convertir la propina a Double si no está vacío
            val propinaDouble = if (propinaText.isNotEmpty()) {
                try {
                    propinaText.toDouble() // Intentar convertir el texto a Double
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "La propina debe ser un número válido", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener // Detener la ejecución si no es un número válido
                }
            } else {
                null // Si el campo está vacío, establecer null
            }

            val estado = checkBoxEstado.isChecked

            // Crear un Intent para pasar los criterios de filtrado a GestorPedidos
            val intent = Intent(this, GestorPedidos::class.java)

            // Pasar los criterios de búsqueda al Intent
            if (!contenidoPedidoText.isNullOrEmpty()) intent.putExtra("contenidoPedido", contenidoPedidoText)
            intent.putExtra("direccion", direccionText)
            intent.putExtra("modoDePago", modoDePagoText)
            if (fechaString != null) intent.putExtra("fecha", fechaString)
            if (!telefonoText.isNullOrEmpty()) intent.putExtra("telefono", telefonoText)
            intent.putExtra("estado", estado) // Pasar el estado
            // Solo pasar el precio si no es null
            if (precioDouble != null) {
                intent.putExtra("precio", precioDouble)
            }
            // Pasar la propina al Intent solo si es válida

            // Solo pasar la propina si no es null
            if (propinaDouble != null) {
                intent.putExtra("propina", propinaDouble)
            }

            // Solo pasar la propina si no es null
            if (propinaDouble != null) {
                intent.putExtra("propina", propinaDouble)
            }

            // Verificar si se ha activado el filtro de propina (CheckBox)
            if (checkBoxPropina.isChecked) {
                intent.putExtra("filtroPropina", true) // Enviar un flag indicando que queremos filtrar por propina
            }

            intent.putExtra("usuarioID", usuarioID) // Pasa el id del usuario



            // Iniciar la actividad GestorPedidos
            startActivity(intent)

            /*val verRegistro = """
                Dirección: $direccionText
                Modo de Pago: $modoDePagoText
                Precio: $precioText
                Teléfono: $telefonoText
                Fecha: ${fechaString ?: "No especificada"}
                Estado: $estado
                """*/
            Toast.makeText(this, "Le has dado al botón ver", Toast.LENGTH_SHORT).show()

            contenidoPedido.text.clear() // Limpiar el campo contenidoPedido
            dirrecion.text.clear()
            modoDePago.setSelection(0)
            precio.text.clear()
            telefono.text.clear()
            propina.text.clear() // Limpiar el campo propina
            // Restablecer el DatePicker a la fecha actual si no se ha deshabilitado la fecha
            if (!checkBoxFecha.isChecked) {
                // Obtener la fecha actual del sistema
                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                val currentMonth = calendar.get(Calendar.MONTH) // Nota: Enero es 0, no sumes 1
                val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

                // Restablecer el DatePicker a la fecha actual
                fecha.updateDate(currentYear, currentMonth, currentDay)
            }
        }

        val btnEditar = findViewById<Button>(R.id.btnModificar)

        btnEditar.setOnClickListener {
            // Obtener los valores de los campos
            val contenidoPedidoText= contenidoPedido.text.toString()
            val direccionText = dirrecion.text.toString()
            val modoDePagoText = modoDePago.selectedItem.toString()
            val precioText = precio.text.toString().toDoubleOrNull() ?: 0.0
            val telefonoText = telefono.text.toString()
            val propinaText = propina.text.toString().trim()

            // Convertir la propina a Double, si es vacía, dejarla como null
            val propinaDouble = if (propinaText.isNotEmpty()) propinaText.toDoubleOrNull() else null

            // Obtener fecha del DatePicker
            val day = fecha.dayOfMonth
            val month = fecha.month + 1 // Enero es 0, por eso sumamos 1
            val year = fecha.year
            val fechaString = "$day/$month/$year"

            if (dirrecion.text.isNullOrEmpty()) {
                Toast.makeText(this, "Por favor, introduce una dirección.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (modoDePagoText.isEmpty()) {
                Toast.makeText(this, "Por favor, introduce una modo de pago.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Llamar a la función addPedido con los valores obtenidos
            if (nombreUsuario != null) {
                dataManager.editarPedido(usuarioID, direccionText, modoDePagoText, precioText, telefonoText, fechaString, contenidoPedidoText, propinaDouble)
                Toast.makeText(this, "¡Le has dado al botón editar!", Toast.LENGTH_SHORT).show()
            }else {
                // Manejar el caso cuando nombreUsuario es null
                Toast.makeText(this, "Nombre de usuario no disponible", Toast.LENGTH_SHORT).show()
            }


            // Limpiar los campos
            contenidoPedido.text.clear()
            dirrecion.text.clear()
            modoDePago.setSelection(0)
            precio.text.clear()
            telefono.text.clear()
            propina.text.clear()
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH) + 1 // Enero es 0, sumamos 1 para trabajar con tu lógica
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
            // Restablecer el DatePicker a la fecha actual
            fecha.updateDate(currentYear, currentMonth - 1, currentDay)
        }
    }
}