package com.adrian.tienda.zapatillas.constantesSQL;

public class ConstantesSQL {

    public static final String SQL_OBTENER_ZAPATILLAS_JSON =
            "select tz.id as ID, " +
                    "tz.modelo as MODELO, " +
                    "tz.marca as MARCA, " +
                    "tz.precio as PRECIO, " +
                    "tz.color as COLOR, " +
                    "tz.talla as TALLA, " +
                    "tz.descripcion as DESCRIPCION, " +
                    "tz.sku as SKU, " +
                    "c.nombre as CATEGORIA " +
                    "from tabla_zapatillas as tz, categoria as c " +
                    "where tz.categoria_id = c.id";

    public static final String SQL_OBTENER_ZAPATILLA_JSON =
            "select tz.id as ID, " +
                    "tz.modelo as MODELO, " +
                    "tz.marca as MARCA, " +
                    "tz.precio as PRECIO, " +
                    "tz.color as COLOR, " +
                    "tz.talla as TALLA, " +
                    "tz.descripcion as DESCRIPCION, " +
                    "tz.sku as SKU, " +
                    "c.nombre as CATEGORIA " +
                    "from tabla_zapatillas as tz, categoria as c " +
                    "where tz.categoria_id = c.id and tz.id = :id";

    public static final String SQL_OBTENER_PRODUCTOS_CARRITO_JSON =
            "select tz.id as zapatilla_id, " +
                    "tz.modelo as MODELO, " +
                    "tz.marca as MARCA, " +
                    "tz.precio as PRECIO, " +
                    "pc.cantidad as CANTIDAD " +
                    "from tabla_zapatillas as tz, producto_carrito as pc " +
                    "where tz.id = pc.zapatilla_id and pc.carrito_id = :carrito_id";

    public static final String SQL_BORRAR_PRODUCTOS_CARRITO =
            "delete from producto_carrito where carrito_id = :carrito_id";
    ;

}