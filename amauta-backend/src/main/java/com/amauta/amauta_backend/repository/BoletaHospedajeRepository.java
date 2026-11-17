package com.amauta.amauta_backend.repository;

import com.amauta.amauta_backend.entity.BoletaHospedaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BoletaHospedajeRepository extends JpaRepository<BoletaHospedaje, Long> {
    boolean existsByRegistroId(Long registroId);

    // Serializa la numeracion entre todas las instancias sobre PostgreSQL.
    @Query(value = "SELECT pg_advisory_xact_lock(820260928)", nativeQuery = true)
    Object bloquearNumeracion();

    @Query("SELECT COALESCE(MAX(b.numero), 0) FROM BoletaHospedaje b")
    Long obtenerUltimoNumero();
}
