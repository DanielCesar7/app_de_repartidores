package com.example.deliveryease.ConexionBbdd


import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.text.ParseException

class DataManager(context: Context) { // class


    /*creamos una instancia de DatabaseHelper y le añadimos el contexto, es decir acceso a recursos, base de datos, etc */
    private val dbHelper = DatabaseHelper(context)

    fun addData(Nombre: String, PrimerApellido: String, Contrasenia: String, NumeroTelefono: String, CorreoElectronico:String, Centro:String) {
        val db = dbHelper.writableDatabase   //usamos el método par //escribir en la bbdd

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_NOMBRE, Nombre)
            put(DatabaseHelper.COLUMN_PRIMERAPELLIDO, PrimerApellido)
            put(DatabaseHelper.COLUMN_CONTRASENIA, Contrasenia)
            put(DatabaseHelper.COLUMN_NUMEROTELEFONO, NumeroTelefono)
            put(DatabaseHelper.COLUMN_CORREOELECTRONICO, CorreoElectronico)
            put(DatabaseHelper.COLUMN_CENTRO, Centro)
        }
        db.insert(DatabaseHelper.TABLE_NAME, null, values)
        db.close()
    }


    //rawQuery crea una consulta y la devuelve en un cursor
    @SuppressLint("Range")
    fun getAllData(context: Context): String {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_NAME}", null)
        val data = StringBuilder()

        while (cursor.moveToNext()) {
            val Nombre = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOMBRE))
            val PrimerApellido = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PRIMERAPELLIDO))
            val Contrasenia = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CONTRASENIA))
            val NumeroTelefono = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NUMEROTELEFONO))
            val CorreoElectronico = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CORREOELECTRONICO))
            val Centro = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CENTRO))

            data.append("Nombre --> $Nombre\n")
            data.append("Primer Apellido --> $PrimerApellido\n")
            data.append("Contraseña --> $Contrasenia\n")
            data.append("Numero de telefono --> $NumeroTelefono\n")
            data.append("Correo eletrónico --> $CorreoElectronico\n")
            data.append("Centro --> $Centro\n")
            data.append("\n")
        } // while

        cursor.close()
        db.close()

        if (data.isEmpty()) {
            return "No hay datos en la base de datos"
        }

        return data.toString()
    }

    fun addPedido(usuarioID: Int, calle: String, modoDePago: String, precio: Double, numeroTelefono: String, fechaString: String, contenidoPedido: String, propina: Double?) {

        val db = dbHelper.writableDatabase
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Convertir la cadena de fecha a Date
        val fecha = dateFormat.parse(fechaString)

        // Validar que el precio sea mayor a 0
        if (precio <= 0) {
            throw IllegalArgumentException("El precio debe ser mayor a cero")
        }

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_CALLE, calle)
            put(DatabaseHelper.COLUMN_MODO_DE_PAGO, modoDePago)
            put(DatabaseHelper.COLUMN_PRECIO, precio)
            put(DatabaseHelper.COLUMN_NUMERO_DE_TELEFONO, numeroTelefono)
            put(DatabaseHelper.COLUMN_FECHA, fecha?.time) // Guardar la fecha como milisegundos
            put(DatabaseHelper.COLUMN_USUARIO_ID, usuarioID) // Guardar el nombre del usuario
            put(DatabaseHelper.COLUMN_ESTADO, 0)
            put(DatabaseHelper.COLUMN_CONTENIDO_PEDIDO, contenidoPedido)

            // Añadir propina solo si no es null o mayor que 0
            if (propina != null && propina > 0) {
                put(DatabaseHelper.COLUMN_PROPINA, propina)
            }
        }

        // Insertar el nuevo pedido en la tabla
        db.insert(DatabaseHelper.TABLE_NAME_PEDIDO, null, values)
        db.close()
    }

    fun normalizarCalle(calle: String): String {
        return calle
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .replace("Á", "a")
            .replace("É", "e")
            .replace("Í", "i")
            .replace("Ó", "o")
            .replace("Ú", "u")
            .replace("[.-]".toRegex(), " ")  // Reemplaza '.' y '-' por espacios
            .replace("\\s+".toRegex(), " ")  // Reemplaza múltiples espacios por uno solo
            .trim()                          // Elimina espacios en blanco al inicio y al final
            .lowercase(Locale.getDefault())  // Convertir a minúsculas
    }

    @SuppressLint("Range")
    fun eliminarPedido(
        usuarioID: Int,
        calle: String,
        modoDePago: String,
        fechaString: String
    ) {
        // Ejecutar la eliminación en un hilo secundario
        Thread {
            try {
                val db = dbHelper.writableDatabase
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                // Convertir la cadena de fecha a Date
                val fecha = dateFormat.parse(fechaString)

                // Convertir la fecha a milisegundos (como está almacenada en la base de datos)
                val fechaMillis = fecha?.time

                // Normalizar la calle: eliminamos puntos y guiones y convertimos a minúsculas
                val calleNormalizada = normalizarCalle(calle)

                // Verificar si se han obtenido bien las variables
                Log.d("EliminarPedido", "Calle normalizada: $calleNormalizada, ModoDePago: $modoDePago, FechaMillis: $fechaMillis, Usuario: $usuarioID")

                // Consultar los pedidos que coinciden con los criterios
                val query = """
                SELECT ${DatabaseHelper.COLUMN_ID_PEDIDO}, ${DatabaseHelper.COLUMN_ESTADO}
                FROM ${DatabaseHelper.TABLE_NAME_PEDIDO}
                WHERE LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(${DatabaseHelper.COLUMN_CALLE}, 'á', 'a'),'é', 'e'),'í', 'i'),'ó', 'o'),'ú', 'u'),'.', ' '),'-', ' ')) = ? AND
                ${DatabaseHelper.COLUMN_MODO_DE_PAGO} = ? AND 
                ${DatabaseHelper.COLUMN_FECHA} = ? AND 
                ${DatabaseHelper.COLUMN_USUARIO_ID} = ?
                """

                val cursor = db.rawQuery(query, arrayOf(calleNormalizada, modoDePago, fechaMillis.toString(), usuarioID.toString()))

                // Obtener todos los pedidos que coinciden
                val pedidosDuplicados = mutableListOf<Int>()
                var pedidosActivos = 0 // Contamos los pedidos que aún no han sido eliminados
                while (cursor.moveToNext()) {
                    val pedidoId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID_PEDIDO))
                    val estado = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ESTADO))

                    if (estado == 0) {
                        pedidosActivos++ // Contamos solo los pedidos activos (no eliminados)
                        pedidosDuplicados.add(pedidoId)
                    }
                }

                // Si hay más de un pedido activo, eliminamos todos excepto uno
                if (pedidosActivos > 1) {
                    for (i in 1 until pedidosActivos) {
                        val pedidoId = pedidosDuplicados[i] // Empezamos desde el segundo pedido
                        val values = ContentValues().apply {
                            put(DatabaseHelper.COLUMN_ESTADO, 1) // Marcamos el pedido como eliminado
                        }
                        db.update(
                            DatabaseHelper.TABLE_NAME_PEDIDO,
                            values,
                            "${DatabaseHelper.COLUMN_ID_PEDIDO} = ?",
                            arrayOf(pedidoId.toString())
                        )
                    }
                    Log.d("EliminarPedido", "Se eliminaron ${pedidosActivos - 1} pedidos repetidos, 1 fue conservado")
                }
                // Si solo hay un pedido activo, se eliminará si se vuelve a ejecutar
                else if (pedidosActivos == 1) {
                    val pedidoId = pedidosDuplicados[0]
                    val values = ContentValues().apply {
                        put(DatabaseHelper.COLUMN_ESTADO, 1) // Marcamos el pedido como eliminado
                    }
                    db.update(
                        DatabaseHelper.TABLE_NAME_PEDIDO,
                        values,
                        "${DatabaseHelper.COLUMN_ID_PEDIDO} = ?",
                        arrayOf(pedidoId.toString())
                    )
                    Log.d("EliminarPedido", "El último pedido activo ha sido eliminado")
                }

                cursor.close()
                db.close()
            } catch (e: Exception) {
                // Loggear cualquier error
                Log.e("EliminarPedido", "Error eliminando pedidos", e)
            }
        }.start()
    }


    fun verPedidos(
        usuarioID: Int?,
        calle: String? = null,
        modoDePago: String? = null,
        precio: Double? = null,
        telefono: String? = null,
        fechaString: String? = null,
        estado: Boolean = false, // Nuevo parámetro para incluir pedidos tachados
        propina: Double? = null,
        contenidoPedido: String? = null
    ): List<Pedido> {
        val db = dbHelper.readableDatabase
        val pedidos = mutableListOf<Pedido>()

        // Definir el formato de fecha
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Definir las columnas que queremos obtener de la base de datos
        val columnas = arrayOf(
            DatabaseHelper.COLUMN_ID_PEDIDO,
            DatabaseHelper.COLUMN_CALLE,
            DatabaseHelper.COLUMN_MODO_DE_PAGO,
            DatabaseHelper.COLUMN_PRECIO,
            DatabaseHelper.COLUMN_NUMERO_DE_TELEFONO,
            DatabaseHelper.COLUMN_FECHA,
            DatabaseHelper.COLUMN_USUARIO_ID,
            DatabaseHelper.COLUMN_ESTADO, // Incluimos la columna Estado
            DatabaseHelper.COLUMN_CONTENIDO_PEDIDO,
            DatabaseHelper.COLUMN_PROPINA
        )

        // Crear una lista para las condiciones (selection) y los argumentos (selectionArgs)
        val condiciones = mutableListOf<String>()
        val argumentos = mutableListOf<String>()

        // Condición para filtrar por usuarioID si se proporciona
        if (usuarioID != null) {
            condiciones.add("${DatabaseHelper.COLUMN_USUARIO_ID} = ?")
            argumentos.add(usuarioID.toString())
        }

        // Si se especifica una calle, buscar tanto en pedidos tachados como activos
        if (!calle.isNullOrEmpty()) {
            // Eliminar tildes y convertir a minúsculas
            val calleSinTildes = normalizarCalle(calle)

            // En la consulta SQL, también eliminar tildes y convertir a minúsculas
            condiciones.add("""
            LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(${DatabaseHelper.COLUMN_CALLE}, 'á', 'a'), 'é', 'e'), 'í', 'i'), 'ó', 'o'), 'ú', 'u'), '.', ' '), '-', ' ')) LIKE ?
        """.trimIndent())

            // Agregar la calle sin tildes como argumento de búsqueda
            argumentos.add("%$calleSinTildes%")
        }

        // Condición para buscar solo pedidos activos o solo eliminados
        if (estado) {
            // Si estado es true, buscar solo pedidos tachados (eliminados)
            condiciones.add("${DatabaseHelper.COLUMN_ESTADO} = 1")
        } else {
            // Si estado es false, buscar solo pedidos activos
            condiciones.add("${DatabaseHelper.COLUMN_ESTADO} = 0")
        }

        // Si se especifica un modo de pago, agregar la condición para filtrar por el modo de pago
        if (!modoDePago.isNullOrEmpty()) {
            condiciones.add("${DatabaseHelper.COLUMN_MODO_DE_PAGO} = ?")
            argumentos.add(modoDePago)
        }

        // Si se especifica un precio, agregar la condición para filtrar por el precio con una tolerancia
        if (precio != null) {
            val tolerancia = 0.01
            condiciones.add("${DatabaseHelper.COLUMN_PRECIO} BETWEEN ? AND ?")
            argumentos.add((precio - tolerancia).toString())
            argumentos.add((precio + tolerancia).toString())
        }

        // Si se especifica un teléfono, agregar la condición para filtrar por teléfono
        if (!telefono.isNullOrEmpty()) {
            condiciones.add("REPLACE(${DatabaseHelper.COLUMN_NUMERO_DE_TELEFONO}, ' ', '') LIKE ?")
            argumentos.add("%${telefono.replace(" ", "")}%")
        }

        // Si se especifica una fecha, convertir la cadena a milisegundos y agregar la condición
        if (!fechaString.isNullOrEmpty()) {
            try {
                val fecha = dateFormat.parse(fechaString)
                if (fecha != null) {
                    val fechaMillis = fecha.time
                    condiciones.add("${DatabaseHelper.COLUMN_FECHA} = ?")
                    argumentos.add(fechaMillis.toString()) // Convertimos la fecha a milisegundos para la búsqueda
                }
            } catch (e: Exception) {
                Log.e("verPedidos", "Error parsing fecha: $fechaString", e)
            }
        }

        // Si se especifica contenidoPedido, agregar la condición para filtrar por contenido
       /* if (!contenidoPedido.isNullOrEmpty()) {
            condiciones.add("LOWER(${DatabaseHelper.COLUMN_CONTENIDO_PEDIDO}) LIKE ?")
            argumentos.add("%${contenidoPedido.toLowerCase()}%")
        }

        // Si se especifica propina, agregar la condición para filtrar por la propina con una tolerancia
        if (propina != null) {
            val tolerancia = 0.01
            condiciones.add("${DatabaseHelper.COLUMN_PROPINA} BETWEEN ? AND ?")
            argumentos.add((propina - tolerancia).toString())
            argumentos.add((propina + tolerancia).toString())
        }*/

        // Crear la consulta condicional
        val selection = if (condiciones.isNotEmpty()) condiciones.joinToString(" AND ") else null
        val selectionArgs = if (argumentos.isNotEmpty()) argumentos.toTypedArray() else null

        // Ejecutar la consulta
        val cursor = db.query(
            DatabaseHelper.TABLE_NAME_PEDIDO,
            columnas,
            selection,
            selectionArgs,
            null,  // groupBy
            null,  // having
            null   // orderBy
        )

        // Recorrer el cursor y añadir los pedidos a la lista
        if (cursor.moveToFirst()) {
            do {
                val idPedido = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID_PEDIDO))
                val callePedido = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CALLE))
                val modoDePagoPedido = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MODO_DE_PAGO))
                val precioPedido = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRECIO))
                val numeroTelefono = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NUMERO_DE_TELEFONO))
                val fechaMillis = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FECHA))
                val estado = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ESTADO)) // Obtener el estado
                val contenidoPedido = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONTENIDO_PEDIDO)) ?: "Sin contenido"
                val propina = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROPINA)) ?: 0.0

                // Convertir la fecha de milisegundos a formato legible
                val date = Date(fechaMillis)
                val formattedFecha = dateFormat.format(date)

                // Crear un objeto Pedido con los valores obtenidos
                val pedido = Pedido(idPedido, callePedido, modoDePagoPedido, precioPedido, numeroTelefono, formattedFecha, estado,
                    contenidoPedido, propina)
                pedidos.add(pedido)
            } while (cursor.moveToNext())
        }

        // Cerrar el cursor y la base de datos
        cursor.close()
        db.close()

        return pedidos
    }

    fun editarPedido(
        usuarioID: Int,
        calle: String,
        modoDePago: String,
        nuevoPrecio: Double?,
        nuevoNumeroTelefono: String?,
        nuevaFechaString: String?,
        nuevoContenidoPedido: String?,
        nuevaPropina: Double?
    ): Boolean {
        val db = dbHelper.writableDatabase
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        return try {
            // Normalizar la calle proporcionada
            val calleNormalizada = normalizarCalle(calle)

            val query = """
            SELECT ${DatabaseHelper.COLUMN_PRECIO}, 
                   ${DatabaseHelper.COLUMN_NUMERO_DE_TELEFONO}, 
                   ${DatabaseHelper.COLUMN_FECHA}, 
                   ${DatabaseHelper.COLUMN_CONTENIDO_PEDIDO}, 
                   ${DatabaseHelper.COLUMN_PROPINA}
            FROM ${DatabaseHelper.TABLE_NAME_PEDIDO}
            WHERE LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(${DatabaseHelper.COLUMN_CALLE}, 'á', 'a'), 'é', 'e'), 'í', 'i'), 'ó', 'o'), 'ú', 'u'), '-', ' '), '.', ' ')) = ? 
            AND ${DatabaseHelper.COLUMN_MODO_DE_PAGO} = ? 
            AND ${DatabaseHelper.COLUMN_USUARIO_ID} = ?
        """

            // Primero, obtenemos los valores actuales del pedido para el usuario y con la combinación de calle y modo de pago
            val cursor = db.rawQuery(query, arrayOf(calleNormalizada, modoDePago, usuarioID.toString()))

            if (cursor.moveToFirst()) {
                val precioActual = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRECIO))
                val numeroTelefonoActual = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NUMERO_DE_TELEFONO))
                val fechaActual = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FECHA))
                val contenidoPedidoActual = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONTENIDO_PEDIDO))
                val propinaActual = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROPINA))

                // Verificar los valores nulos
                val numeroTelefonoFinal = nuevoNumeroTelefono.takeUnless { it.isNullOrBlank() } ?: numeroTelefonoActual
                val precioFinal = nuevoPrecio.takeUnless { it == null || it == 0.0 } ?: precioActual
                val contenidoPedidoFinal = nuevoContenidoPedido.takeUnless { it.isNullOrBlank() } ?: contenidoPedidoActual
                val propinaFinal = nuevaPropina ?: propinaActual

                val fechaFinal = try {
                    nuevaFechaString?.let { dateFormat.parse(it)?.time } ?: fechaActual
                } catch (e: ParseException) {
                    fechaActual
                }

                val values = ContentValues().apply {
                    put(DatabaseHelper.COLUMN_PRECIO, precioFinal)
                    put(DatabaseHelper.COLUMN_NUMERO_DE_TELEFONO, numeroTelefonoFinal)
                    put(DatabaseHelper.COLUMN_FECHA, fechaFinal)
                    put(DatabaseHelper.COLUMN_PROPINA, propinaFinal)
                    put(DatabaseHelper.COLUMN_CONTENIDO_PEDIDO, contenidoPedidoFinal)
                }

                val rowsAffected = db.update(
                    DatabaseHelper.TABLE_NAME_PEDIDO,
                    values,
                    """
                LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(${DatabaseHelper.COLUMN_CALLE}, 'á', 'a'), 'é', 'e'), 'í', 'i'), 'ó', 'o'), 'ú', 'u'), '-', ' '), '.', ' ')) = ? 
                AND ${DatabaseHelper.COLUMN_MODO_DE_PAGO} = ? 
                AND ${DatabaseHelper.COLUMN_FECHA} = ? 
                AND ${DatabaseHelper.COLUMN_USUARIO_ID} = ?
                """,
                    arrayOf(calleNormalizada, modoDePago, fechaFinal.toString() ,usuarioID.toString())
                )

                cursor.close()
                db.close()

                rowsAffected > 0 // Retorna true si se actualizaron filas
            } else {
                cursor.close()
                db.close()
                false // No se encontró ningún pedido que coincida
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false // Retorna false si ocurrió una excepción
        }
    }

    fun verifyUser(nombre: String, contrasenia: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_NAME} WHERE ${DatabaseHelper.COLUMN_NOMBRE} = ? AND ${DatabaseHelper.COLUMN_CONTRASENIA} = ?", arrayOf(nombre, contrasenia))
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    @SuppressLint("Range")
    fun getCentroUsuario(nombreUsuario: String): String? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT ${DatabaseHelper.COLUMN_CENTRO} FROM ${DatabaseHelper.TABLE_NAME} WHERE ${DatabaseHelper.COLUMN_NOMBRE} = ?", arrayOf(nombreUsuario))

        var centroUsuario: String? = null
        if (cursor.moveToFirst()) {
            centroUsuario = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CENTRO))
        }

        cursor.close()
        db.close()

        return centroUsuario
    }

    //Elimina todos los usuarios
    /*fun eliminateData() {
        val db = dbHelper.writableDatabase
        db.delete(DatabaseHelper.TABLE_NAME, null, null)
    }// fun*/

    // Función para obtener el ID del usuario a partir de su nombre
    fun getUsuarioID(nombre: String): Int? {
        val db = dbHelper.readableDatabase

        val columnas = arrayOf(DatabaseHelper.COLUMN_ID)
        val selection = "${DatabaseHelper.COLUMN_NOMBRE} = ?"
        val selectionArgs = arrayOf(nombre)

        val cursor = db.query(
            DatabaseHelper.TABLE_NAME,
            columnas,
            selection,
            selectionArgs,
            null,  // groupBy
            null,  // having
            null   // orderBy
        )

        var usuarioID: Int? = null
        if (cursor.moveToFirst()) {
            usuarioID = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
        }

        cursor.close()
        db.close()

        return usuarioID
    }

}

