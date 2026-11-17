package com.amauta.amauta_backend.repository;

import com.amauta.amauta_backend.entity.Habitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from Habitacion h where h.id = :id")
    Optional<Habitacion> buscarParaActualizar(@Param("id") Long id);
}
