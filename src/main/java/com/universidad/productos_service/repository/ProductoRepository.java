package com.universidad.productos_service.repository;

import com.universidad.productos_service.domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    // Spring Data JPA provee automáticamente: save, findById, findAll, deleteById, etc.
}