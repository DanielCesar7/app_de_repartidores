package com.example.deliveryease.ConexionBbdd

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 4
        private const val DATABASE_NAME = "DeeliveryEaseeeee.db"

        const val TABLE_NAME = "usuario"
        const val COLUMN_ID = "IDUsuario"
        const val COLUMN_NOMBRE= "Nombre"
        const val COLUMN_PRIMERAPELLIDO = "PrimerApellido"
        const val COLUMN_CONTRASENIA = "Contrasenia"
        const val COLUMN_NUMEROTELEFONO = "NumeroTelefono"
        const val COLUMN_CORREOELECTRONICO = "CorreoElectronico"
        const val COLUMN_CENTRO = "Centro"

        const val TABLE_NAME_PEDIDO=  "pedido"
        const val COLUMN_ID_PEDIDO = "IDPedido"
        const val COLUMN_CALLE = "Calle"
        const val COLUMN_MODO_DE_PAGO = "ModoDePago"
        const val COLUMN_PRECIO = "Precio"
        const val COLUMN_NUMERO_DE_TELEFONO = "Telefono"
        const val COLUMN_FECHA = "Fecha"
        const val COLUMN_USUARIO_ID = "UsuarioID"
        const val COLUMN_ESTADO = "Estado"
        const val COLUMN_PROPINA = "Propina"
        const val COLUMN_CONTENIDO_PEDIDO = "ContenidoPedido"

    } // co

    //creamos el metodo oncreate que crea la tabla
    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_TABLE = "CREATE TABLE $TABLE_NAME" +
                "($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_NOMBRE TEXT, $COLUMN_PRIMERAPELLIDO TEXT," +
                " $COLUMN_CONTRASENIA TEXT, $COLUMN_NUMEROTELEFONO TEXT, $COLUMN_CORREOELECTRONICO TEXT, $COLUMN_CENTRO TEXT)"
        db.execSQL(CREATE_TABLE)

        val CREATE_TABLE_PEDIDO = "CREATE TABLE $TABLE_NAME_PEDIDO" +
                "($COLUMN_ID_PEDIDO INTEGER PRIMARY KEY, $COLUMN_CALLE TEXT, $COLUMN_MODO_DE_PAGO TEXT," +
                " $COLUMN_PRECIO DOUBLE, $COLUMN_NUMERO_DE_TELEFONO TEXT, $COLUMN_FECHA DATE," +
                " $COLUMN_USUARIO_ID INTEGER, $COLUMN_ESTADO  DEFAULT 0, $COLUMN_PROPINA DOUBLE,"+
                " $COLUMN_CONTENIDO_PEDIDO TEXT,"+
                " FOREIGN KEY($COLUMN_USUARIO_ID) REFERENCES $TABLE_NAME($COLUMN_ID))"  // Relacionar con la tabla usuario
        db.execSQL(CREATE_TABLE_PEDIDO)
    }

    //creamos el metodo que permite eliminar la table y Volver a crearla
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Añadir la columna Estado a la tabla pedido si no existe
            try {
                db.execSQL("ALTER TABLE $TABLE_NAME_PEDIDO ADD COLUMN $COLUMN_ESTADO INTEGER DEFAULT 0")
            } catch (e: Exception) {
                e.printStackTrace() // Si la columna ya existe o si hay algún error, manejar la excepción
            }
        }
        if (oldVersion < 3) {
            // Añadir la columna Propina a la tabla pedido
            try {
                db.execSQL("ALTER TABLE $TABLE_NAME_PEDIDO ADD COLUMN $COLUMN_PROPINA DOUBLE DEFAULT 0")
            } catch (e: Exception) {
                e.printStackTrace() // Manejar la excepción si hay un error
            }

            // Añadir la columna ContenidoPedido a la tabla pedido
            try {
                db.execSQL("ALTER TABLE $TABLE_NAME_PEDIDO ADD COLUMN $COLUMN_CONTENIDO_PEDIDO TEXT")
            } catch (e: Exception) {
                e.printStackTrace() // Manejar la excepción si hay un error
            }
        }

            // Esta es la nueva sección para la actualización de los valores NULL
        if (oldVersion < 4) {
            try {
                // Actualizamos los valores NULL solo si la versión es menor a 4
                db.execSQL("UPDATE $TABLE_NAME_PEDIDO SET $COLUMN_PROPINA = 0 WHERE $COLUMN_PROPINA IS NULL")
                db.execSQL("UPDATE $TABLE_NAME_PEDIDO SET $COLUMN_CONTENIDO_PEDIDO = 'Sin contenido' WHERE $COLUMN_CONTENIDO_PEDIDO IS NULL")
            } catch (e: Exception) {
                e.printStackTrace() // Manejar la excepción si hay un error durante la actualización
            }
        }
    }
} // class