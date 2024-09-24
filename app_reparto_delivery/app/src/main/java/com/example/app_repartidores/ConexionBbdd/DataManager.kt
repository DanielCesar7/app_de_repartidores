package com.example.app_repartidores.ConexionBbdd


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

    fun addPedido(usuarioID: Int, calle: String, modoDePago: String, precio: Double, numeroTelefono: String, fechaString: String) {

        val db = dbHelper.writableDatabase
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Convertir la cadena de fecha a Date
        val fecha = dateFormat.parse(fechaString)

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_CALLE, calle)
            put(DatabaseHelper.COLUMN_MODO_DE_PAGO, modoDePago)
            put(DatabaseHelper.COLUMN_PRECIO, precio)
            put(DatabaseHelper.COLUMN_NUMERO_DE_TELEFONO, numeroTelefono)
            put(DatabaseHelper.COLUMN_FECHA, fecha?.time) // Guardar la fecha como milisegundos
            put(DatabaseHelper.COLUMN_USUARIO_ID, usuarioID) // Guardar el nombre del usuario
            put(DatabaseHelper.COLUMN_ESTADO, 0)
        }

        // Insertar el nuevo pedido en la tabla
        db.insert(DatabaseHelper.TABLE_NAME_PEDIDO, null, values)
        db.close()
    }

    fun eliminarPedido(
        usuarioID: Int, // Agregamos el nombre del usuario
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

                // Verificar si se han obtenido bien las variables
                Log.d("EliminarPedido", "Calle: $calle, ModoDePago: $modoDePago, FechaMillis: $fechaMillis, Usuario: $usuarioID")

                // Definir la condición para la eliminación (WHERE)
                val selection = """
                ${DatabaseHelper.COLUMN_CALLE} = ? AND 
                ${DatabaseHelper.COLUMN_MODO_DE_PAGO} = ? AND 
                ${DatabaseHelper.COLUMN_FECHA} = ? AND 
                ${DatabaseHelper.COLUMN_USUARIO_ID} = ?
            """
                val selectionArgs = arrayOf(calle, modoDePago, fechaMillis.toString(), usuarioID.toString())

                // Actualizar el pedido para marcarlo como tachado
                val values = ContentValues().apply {
                    put(DatabaseHelper.COLUMN_ESTADO, 1) // Marcamos el pedido como eliminado
                }

                val rowsUpdated = db.update(DatabaseHelper.TABLE_NAME_PEDIDO, values, selection, selectionArgs)
                Log.d("EliminarPedido", "Filas actualizadas: $rowsUpdated")

                db.close()
            } catch (e: Exception) {
                // Loggear cualquier error
                Log.e("EliminarPedido", "Error eliminando el pedido", e)
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
        estado: Boolean = false // Nuevo parámetro para incluir pedidos tachados
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
            DatabaseHelper.COLUMN_ESTADO // Incluimos la columna Estado
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
            condiciones.add("LOWER(${DatabaseHelper.COLUMN_CALLE}) LIKE ?")
            argumentos.add("%${calle.toLowerCase()}%")
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

                // Convertir la fecha de milisegundos a formato legible
                val date = Date(fechaMillis)
                val formattedFecha = dateFormat.format(date)

                // Crear un objeto Pedido con los valores obtenidos
                val pedido = Pedido(idPedido, callePedido, modoDePagoPedido, precioPedido, numeroTelefono, formattedFecha, estado)
                pedidos.add(pedido)
            } while (cursor.moveToNext())
        }

        // Cerrar el cursor y la base de datos
        cursor.close()
        db.close()

        return pedidos
    }


    fun editarPedido(
        usuarioID: Int,  // Usuario propietario del pedido
        calle: String,   // Calle donde se realizó el pedido
        modoDePago: String,  // Modo de pago utilizado para identificar el pedido
        nuevoPrecio: Double?,  // Puede ser null
        nuevoNumeroTelefono: String?,  // Puede ser null
        nuevaFechaString: String?  // Puede ser null, la nueva fecha en formato de cadena
    ): Boolean {
        val db = dbHelper.writableDatabase
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Normalizar el valor de la calle (eliminando guiones, puntos, y haciendo insensible a mayúsculas)
        fun normalizarCalle(calle: String): String {
            return calle
                .toLowerCase()  // Convertir a minúsculas
                .replace(Regex("[\\s.-]"), "")  // Eliminar espacios, puntos, y guiones
        }

        // Normalizar la calle proporcionada
        val calleNormalizada = normalizarCalle(calle)

        // Primero, obtenemos los valores actuales del pedido para el usuario y con la combinación de calle y modo de pago
        val cursor = db.query(
            DatabaseHelper.TABLE_NAME_PEDIDO,
            arrayOf(DatabaseHelper.COLUMN_PRECIO, DatabaseHelper.COLUMN_NUMERO_DE_TELEFONO, DatabaseHelper.COLUMN_FECHA),
            "LOWER(REPLACE(REPLACE(REPLACE(${DatabaseHelper.COLUMN_CALLE}, ' ', ''), '.', ''), '-', '')) = ? AND ${DatabaseHelper.COLUMN_MODO_DE_PAGO} = ? AND ${DatabaseHelper.COLUMN_USUARIO_ID} = ?",
            arrayOf(calleNormalizada, modoDePago, usuarioID.toString()),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            // Obtener los valores actuales de la base de datos
            val precioActual = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRECIO))
            val numeroTelefonoActual = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NUMERO_DE_TELEFONO))
            val fechaActual = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FECHA)) // Guardado como milisegundos

            // Verificar que el nuevo número de teléfono no sea nulo o vacío
            val numeroTelefonoFinal = if (!nuevoNumeroTelefono.isNullOrBlank()) nuevoNumeroTelefono else numeroTelefonoActual

            // Verificar que el nuevo precio no sea nulo o 0.0; si lo es, usar el precio actual
            val precioFinal = if (nuevoPrecio != null && nuevoPrecio != 0.0) nuevoPrecio else precioActual

            // Verificar y parsear la nueva fecha, si no es válida, mantener la fecha actual
            val fechaFinal: Long = if (!nuevaFechaString.isNullOrBlank()) {
                try {
                    val nuevaFecha = dateFormat.parse(nuevaFechaString)
                    nuevaFecha?.time ?: fechaActual // Si la fecha es nula, usar la fecha actual
                } catch (e: ParseException) {
                    fechaActual // Si no se puede parsear, usar la fecha actual
                }
            } else {
                fechaActual // Si no se proporcionó una nueva fecha, mantener la actual
            }

            // Crear un objeto ContentValues para almacenar los nuevos valores o mantener los actuales
            val values = ContentValues().apply {
                put(DatabaseHelper.COLUMN_PRECIO, precioFinal)  // Usar el nuevo precio si no es nulo y no es 0.0, o el valor actual
                put(DatabaseHelper.COLUMN_NUMERO_DE_TELEFONO, numeroTelefonoFinal)  // Usar el nuevo teléfono o mantener el actual si está vacío
                put(DatabaseHelper.COLUMN_FECHA, fechaFinal)  // Usar la nueva fecha o mantener la actual
            }

            // Actualizar el pedido usando la calle normalizada, modo de pago y usuarioID
            val rowsAffected = db.update(
                DatabaseHelper.TABLE_NAME_PEDIDO,
                values,
                "LOWER(REPLACE(REPLACE(REPLACE(${DatabaseHelper.COLUMN_CALLE}, ' ', ''), '.', ''), '-', '')) = ? AND ${DatabaseHelper.COLUMN_MODO_DE_PAGO} = ? AND ${DatabaseHelper.COLUMN_USUARIO_ID} = ?",
                arrayOf(calleNormalizada, modoDePago, usuarioID.toString())  // Usamos la calle, modo de pago, y usuarioID como criterios de búsqueda
            )

            cursor.close()  // Cerrar el cursor
            db.close()  // Cerrar la base de datos
            return rowsAffected > 0 // Retorna true si se actualizaron filas, false si no
        } else {
            cursor.close()  // Cerrar el cursor si no se encontraron resultados
            db.close()  // Cerrar la base de datos
            return false  // No se encontró ningún pedido que coincida
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

