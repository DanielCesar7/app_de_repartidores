package com.example.app_repartidores.ConexionBbdd

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.app_repartidores.R
import androidx.appcompat.app.AlertDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GestorPedidos : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestor_pedidos)

        // Obtener referencias a los TextViews
        val pedidosEnEfectivo = findViewById<TextView>(R.id.PedidosEnEfectivo)
        val pedidosConTarjeta = findViewById<TextView>(R.id.PedidosConTarjeta)
        val totalEfectivoTextView = findViewById<TextView>(R.id.totalPedidosEnEfectivo)
        val totalTarjetaTextView = findViewById<TextView>(R.id.totalPedidosConTarjeta)
        val numeroTotalPedidosEnEfectivo = findViewById<TextView>(R.id.NumeroTotalPedidosEnEfectivo)
        val numeroTotalPedidosConTarjeta = findViewById<TextView>(R.id.numeroTotalPedidosConTarjeta)
        val pedidoTotal = findViewById<TextView>(R.id.NumeroTotalPedidos)
        val dineroTotal = findViewById<TextView>(R.id.NumeroTotalDinero)

        // Inicializar el DataManager
        val dataManager = DataManager(this)

        // Variables para sumar el total de efectivo y tarjeta y contar pedidos
        var totalEfectivo = 0.0
        var totalTarjeta = 0.0
        var totalPedidosEfectivo = 0
        var totalPedidosTarjeta = 0

        // Recuperar el nombre del usuario desde el Intent
        val usuarioID = intent.getIntExtra("usuarioID", -1)

        // Recuperar los criterios de búsqueda del Intent
        val direccion = intent.getStringExtra("direccion")
        val modoDePago = intent.getStringExtra("modoDePago")
        val precio = intent.getSerializableExtra("precio") as? Double
        val telefono = intent.getStringExtra("telefono")
        val fecha = intent.getStringExtra("fecha")
        val estado = intent.getBooleanExtra("estado", false) // Filtro de estado

        // Recuperar pedidos filtrados desde la base de datos
        val pedidosFiltrados = dataManager.verPedidos(
            usuarioID, direccion, modoDePago, precio, telefono, fecha, estado
        )

        val pedidosEnEfectivoTexto = SpannableStringBuilder()
        val pedidosConTarjetaTexto = SpannableStringBuilder()

        // Formato de fecha
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())


        // Mostrar los pedidos filtrados
        for (pedido in pedidosFiltrados) {
            val fechaFormateada = try {
                dateFormat.format(Date(pedido.fecha.toLong()))
            } catch (e: Exception) {
                pedido.fecha
            }

            val detallePedido = """
            Dirección: ${pedido.calle}
            Precio: ${pedido.precio}€
            Teléfono: ${pedido.numeroTelefono}
            Fecha: $fechaFormateada
            """.trimIndent()

            // Verificar si el pedido está eliminado o activo
            if (pedido.estado == 1) {
                // Crear SpannableString para el texto del pedido
                val spannableDetallePedido = SpannableString(detallePedido)

                // Aplicar el color rojo al texto del pedido eliminado
                spannableDetallePedido.setSpan(
                    ForegroundColorSpan(Color.rgb(255, 0, 0)), // Rojo fuerte
                    0, detallePedido.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // Si el pedido es en efectivo
                if (pedido.modoDePago == "efectivo") {
                    totalEfectivo += pedido.precio
                    totalPedidosEfectivo++
                    pedidosEnEfectivoTexto.append(spannableDetallePedido)
                    pedidosEnEfectivoTexto.append("\n\n")
                }
                // Si el pedido es con tarjeta
                else if (pedido.modoDePago == "tarjeta") {
                    totalTarjeta += pedido.precio
                    totalPedidosTarjeta++
                    pedidosConTarjetaTexto.append(spannableDetallePedido)
                    pedidosConTarjetaTexto.append("\n\n")
                }
            } else {
                // Si el pedido está activo, sumamos a los totales y mostramos en negro (por defecto)
                if (pedido.modoDePago == "efectivo") {
                    totalEfectivo += pedido.precio
                    totalPedidosEfectivo++
                    pedidosEnEfectivoTexto.append(detallePedido).append("\n\n")
                } else if (pedido.modoDePago == "tarjeta") {
                    totalTarjeta += pedido.precio
                    totalPedidosTarjeta++
                    pedidosConTarjetaTexto.append(detallePedido).append("\n\n")
                }
            }
        }

        // Mostrar los pedidos en los TextViews correspondientes
        pedidosEnEfectivo.text = pedidosEnEfectivoTexto
        pedidosConTarjeta.text = pedidosConTarjetaTexto

        // Redondear los totales de efectivo y tarjeta a dos decimales
        val totalEfectivoRedondeado = String.format("%.2f", totalEfectivo)
        val totalTarjetaRedondeado = String.format("%.2f", totalTarjeta)

        // Actualizar los totales de efectivo y tarjeta
        totalEfectivoTextView.text = "Total en efectivo: $totalEfectivoRedondeado €"
        totalTarjetaTextView.text = "Total con tarjeta: $totalTarjetaRedondeado €"

        // Actualizar el número total de pedidos
        numeroTotalPedidosEnEfectivo.text = "Pedido total: $totalPedidosEfectivo"
        numeroTotalPedidosConTarjeta.text = "Pedido total: $totalPedidosTarjeta"

        // Calcular el total combinado de pedidos y dinero
        val totalPedidos = totalPedidosEfectivo + totalPedidosTarjeta
        val totalDinero = totalEfectivo + totalTarjeta

        // Actualizar los TextViews de suma total
        pedidoTotal.text = "Total de pedidos: $totalPedidos"
        dineroTotal.text = "Total del dinero: ${String.format("%.2f", totalDinero)} €"

        val botonPrueba = findViewById<Button>(R.id.btnPrueba)

        botonPrueba.setOnClickListener {
            // Acciones del botón, si es necesario
        }
    }
}








