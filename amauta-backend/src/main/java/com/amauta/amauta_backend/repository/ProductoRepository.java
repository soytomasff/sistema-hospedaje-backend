package com.amauta.amauta_backend.repository;

import com.amauta.amauta_backend.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select p from Producto p where p.id = :id")
    java.util.Optional<Producto> buscarParaActualizar(@org.springframework.data.repository.query.Param("id") Long id);
}
