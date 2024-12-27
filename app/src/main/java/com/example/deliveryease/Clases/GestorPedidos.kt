package com.example.deliveryease.Clases

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.deliveryease.ConexionBbdd.CustomZoomScrollView
import com.example.deliveryease.ConexionBbdd.DataManager
import com.example.deliveryease.R
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
        val propinaEfectivoTextView = findViewById<TextView>(R.id.propinaEfectivo)
        val propinaTarjetaTextView  = findViewById<TextView>(R.id.propinaTarjeta)
        val numeroTotalPropinaTextView = findViewById<TextView>(R.id.numeroTotalPropina)

        val activarZoom= findViewById<Button>(R.id.buttonActivarZoom)
        val desactivarZoom= findViewById<Button>(R.id.buttonDesactivarZoom)
        // Variables para los ScrollViews personalizados
        val scrollViewEfectivo = findViewById<CustomZoomScrollView>(R.id.scrollViewPedidosEnEfectivo)
        val scrollViewTarjeta = findViewById<CustomZoomScrollView>(R.id.scrollViewPedidosConTarjeta)

        // Estado inicial al cargar la vista (Zoom desactivado y scroll activado)
        scrollViewEfectivo.setZoomEnabled(false)  // Desactivar zoom
        scrollViewTarjeta.setZoomEnabled(false)   // Desactivar zoom

        scrollViewEfectivo.isVerticalScrollBarEnabled = true  // Activar scroll
        scrollViewTarjeta.isVerticalScrollBarEnabled = true   // Activar scroll

        activarZoom.setOnClickListener {
            // Activar zoom
            scrollViewEfectivo.setZoomEnabled(true)
            scrollViewTarjeta.setZoomEnabled(true)

            // Desactivar scroll
            scrollViewEfectivo.isVerticalScrollBarEnabled = false
            scrollViewTarjeta.isVerticalScrollBarEnabled = false
        }

        // Función para desactivar el zoom y activar el scroll
        desactivarZoom.setOnClickListener {
            // Desactivar zoom
            scrollViewEfectivo.setZoomEnabled(false)
            scrollViewTarjeta.setZoomEnabled(false)

            // Activar scroll
            scrollViewEfectivo.isVerticalScrollBarEnabled = true
            scrollViewTarjeta.isVerticalScrollBarEnabled = true
        }

        // Inicializar el DataManager
        val dataManager = DataManager(this)

        // Variables para sumar el total de efectivo y tarjeta y contar pedidos
        var totalEfectivo = 0.0
        var totalTarjeta = 0.0
        var totalPedidosEfectivo = 0
        var totalPedidosTarjeta = 0

        // Variables para propinas
        var totalPropinaEfectivo = 0.0
        var totalPropinaTarjeta = 0.0
        var totalPropinas = 0.0

        // Recuperar el nombre del usuario desde el Intent
        val usuarioID = intent.getIntExtra("usuarioID", -1)

        // Recuperar los criterios de búsqueda del Intent
        val direccion = intent.getStringExtra("direccion")
        val modoDePago = intent.getStringExtra("modoDePago")
        val precio = intent.getSerializableExtra("precio",) as? Double
        val telefono = intent.getStringExtra("telefono")
        val fecha = intent.getStringExtra("fecha")
        val estado = intent.getBooleanExtra("estado", false) // Filtro de estado
        val propina = intent.getSerializableExtra("propina") as? Double
        val contenidoPedido = intent.getStringExtra("contenidoPedido")

        // Recuperar el flag del filtro de propina desde el Intent
        val filtroPropina = intent.getBooleanExtra("filtroPropina", false)

        // Recuperar pedidos filtrados desde la base de datos
        val pedidosFiltrados = dataManager.verPedidos(
            usuarioID, direccion, modoDePago, precio, telefono, fecha, estado, propina, contenidoPedido
        )

        val pedidosEnEfectivoTexto = SpannableStringBuilder()
        val pedidosConTarjetaTexto = SpannableStringBuilder()

        // Formato de fecha
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())


        // Mostrar los pedidos filtrados
        for (pedido in pedidosFiltrados) {

            // Si el filtro de propina está activado, solo mostrar los pedidos con propina mayor a 0
            if (filtroPropina && (pedido.propina == null || pedido.propina <= 0.0)) {
                // Si el filtro de propina está activado y el pedido no tiene propina o es <= 0, lo ignoramos
                continue
            }

            val fechaFormateada = try {
                dateFormat.format(Date(pedido.fecha.toLong()))
            } catch (e: Exception) {
                pedido.fecha
            }

            // Si 'contenidoPedido' o 'propina' son null, asignamos un valor por defecto
            val contenidoPedidoText = pedido.contenidoPedido ?: "Sin contenido"
            val propinaText = if (pedido.propina != null) "${pedido.propina}€" else "No especificada"

            val numeroTelefonoFormateado = pedido.numeroTelefono.replace(Regex("(\\d{3})(\\d{2})(\\d{2})(\\d{2})"), "$1 $2 $3 $4")

            val detallePedido = """
            Pedido: $contenidoPedidoText    
            Dirección: ${pedido.calle}
            Precio: ${pedido.precio}€
            Teléfono: $numeroTelefonoFormateado
            Propina: $propinaText
            Fecha: $fechaFormateada
            """.trimIndent().replace("[ ]+".toRegex(), " ") // Aplicar también limpieza al resto del texto

            // Para aplicar estilos a campos específicos
            val spannableDetallePedido = SpannableString(detallePedido)

            // Aplicar negritas a los campos que quieras resaltar
            val camposResaltados = listOf("Pedido", "Dirección", "Precio", "Teléfono", "Propina", "Fecha")
            for (campo in camposResaltados) {
                val start = detallePedido.indexOf(campo)
                if (start >= 0) {
                    spannableDetallePedido.setSpan(
                        StyleSpan(Typeface.BOLD),
                        start, start + campo.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }

            // Puedes añadir un separador visual entre pedidos para que se vean más ordenados
            val separador = "\n-----------------------------\n"

            // Verificar si el pedido está eliminado o activo
            if (pedido.estado == 1) {

                // Aplicar el color rojo al texto del pedido eliminado
                spannableDetallePedido.setSpan(
                    ForegroundColorSpan(Color.rgb(255, 0, 0)), // Rojo fuerte
                    0, detallePedido.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // Si el pedido es en efectivo
                if (pedido.modoDePago.toLowerCase().equals("efectivo")) {
                    totalEfectivo += pedido.precio
                    totalPedidosEfectivo++
                    totalPropinaEfectivo += pedido.propina
                    pedidosEnEfectivoTexto.append(spannableDetallePedido.trim())
                    pedidosEnEfectivoTexto.append(separador)
                }
                // Si el pedido es con tarjeta
                else if (pedido.modoDePago.toLowerCase().equals("tarjeta".trim())) {
                    totalTarjeta += pedido.precio
                    totalPedidosTarjeta++
                    totalPropinaTarjeta += pedido.propina
                    pedidosConTarjetaTexto.append(spannableDetallePedido)
                    pedidosConTarjetaTexto.append(separador)
                }
            } else {

                // Si el pedido está activo, sumamos a los totales y mostramos en negro (por defecto)
                if (pedido.modoDePago.toLowerCase().equals("efectivo")) {
                    totalEfectivo += pedido.precio
                    totalPedidosEfectivo++
                    totalPropinaEfectivo += pedido.propina
                    pedidosEnEfectivoTexto.append(spannableDetallePedido)
                    pedidosEnEfectivoTexto.append(separador)
                } else if (pedido.modoDePago.toLowerCase().equals("tarjeta")) {
                    totalTarjeta += pedido.precio
                    totalPedidosTarjeta++
                    totalPropinaTarjeta += pedido.propina
                    pedidosConTarjetaTexto.append(spannableDetallePedido)
                    pedidosConTarjetaTexto.append(separador)
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

        // Calcular el total de propinas
        totalPropinas = totalPropinaEfectivo + totalPropinaTarjeta

        // Actualizar los TextViews de propinas
        propinaEfectivoTextView.text = "Propina en efectivo: ${String.format("%.2f", totalPropinaEfectivo)} €"
        propinaTarjetaTextView.text = "Propina con tarjeta: ${String.format("%.2f", totalPropinaTarjeta)} €"
        numeroTotalPropinaTextView.text = "Total propinas: ${String.format("%.2f", totalPropinas)} €"

        val botonPrueba = findViewById<Button>(R.id.btnPrueba)

        botonPrueba.setOnClickListener {
            // Acciones del botón, si es necesario
        }
    }
}








