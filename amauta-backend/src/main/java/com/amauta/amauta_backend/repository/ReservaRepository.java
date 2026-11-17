package com.amauta.amauta_backend.repository;

import com.amauta.amauta_backend.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reserva r where r.id = :id")
    Optional<Reserva> buscarParaActualizar(@Param("id") Long id);

    @Query("""
        select count(r) from Reserva r
        where r.habitacion.id = :habitacionId
          and r.id <> :excluirId
          and upper(coalesce(r.estado, '')) <> 'CANCELADO'
          and r.fechaIngreso < :salida
          and r.fechaSalida > :ingreso
        """)
    long contarCruces(@Param("habitacionId") Long habitacionId,
                      @Param("ingreso") LocalDate ingreso,
                      @Param("salida") LocalDate salida,
                      @Param("excluirId") Long excluirId);
}
