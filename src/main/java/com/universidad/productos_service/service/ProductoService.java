package com.universidad.productos_service.service;

import com.universidad.productos_service.domain.Producto;

public interface ProductoService {

    /**
     * Crea un nuevo producto con las validaciones de negocio aplicadas.
     * @param nombre Nombre del producto (no puede ser nulo o vacío)
     * @param precio Precio del producto (debe ser mayor a cero)
     * @param stock  Cantidad en inventario (no puede ser negativo)
     * @return El producto persistido con su ID asignado
     */
    Producto crear(String nombre, Double precio, Integer stock);

    /**
     * Busca un producto por su identificador único.
     * @param id Identificador del producto
     * @return El producto encontrado
     * @throws RuntimeException si el producto no existe
     */
    Producto buscarPorId(Long id);

    /**
     * Actualiza el stock de un producto existente.
     * @param id         Identificador del producto
     * @param nuevoStock Nuevo valor de stock (no puede ser negativo)
     * @return El producto actualizado
     */
    Producto actualizarStock(Long id, Integer nuevoStock);

    /**
     * Elimina un producto por su ID.
     * @param id Identificador del producto a eliminar
     * @throws RuntimeException si el producto no existe
     */
    void eliminar(Long id);
}