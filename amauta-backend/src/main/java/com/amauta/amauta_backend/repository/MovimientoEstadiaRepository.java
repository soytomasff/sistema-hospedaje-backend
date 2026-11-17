package com.amauta.amauta_backend.repository;
import com.amauta.amauta_backend.entity.MovimientoEstadia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MovimientoEstadiaRepository extends JpaRepository<MovimientoEstadia, Long> {
    List<MovimientoEstadia> findByRegistroIdOrderByIdAsc(Long registroId);
    List<MovimientoEstadia> findByHabitacionIdOrderByIdAsc(Long habitacionId);
}
