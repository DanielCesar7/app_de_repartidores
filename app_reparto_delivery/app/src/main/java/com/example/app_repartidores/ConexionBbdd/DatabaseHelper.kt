package com.example.app_repartidores.ConexionBbdd

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
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
                " $COLUMN_USUARIO_ID INTEGER, FOREIGN KEY($COLUMN_USUARIO_ID) REFERENCES $TABLE_NAME($COLUMN_ID))"  // Relacionar con la tabla usuario
        db.execSQL(CREATE_TABLE_PEDIDO)
    }

    //creamos el metodo que permite eliminar la table y Volver a crearla
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_PEDIDO")
        onCreate(db)
    } // fun

} // class