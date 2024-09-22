package com.example.app_repartidores.ConexionBbdd

import java.io.Serializable


data class Pedido(
    val id: Int,
    val calle: String,
    val modoDePago: String,
    val precio: Double,
    val numeroTelefono: String,
    val fecha: String
): Serializable