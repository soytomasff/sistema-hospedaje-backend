package com.amauta.amauta_backend.repository;

import com.amauta.amauta_backend.entity.Registro;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface RegistroRepository extends JpaRepository<Registro, Long> {
    @Query("select r.id from Registro r where r.fechaSalida is not null and r.horaSalida is null and r.salidaPrevista is null order by r.id")
    java.util.List<Long> idsSinSalidaPrevista();

    @Query("""
        select r.id from Registro r where (r.fechaSalida is null or r.horaSalida is null)
        and r.salidaPrevista is not null and r.salidaPrevista <= :ahora order by r.id
        """)
    java.util.List<Long> idsSalidasVencidas(@Param("ahora") java.time.LocalDateTime ahora);

    @Query("""
        select r from Registro r where (r.fechaSalida is null or r.horaSalida is null)
        and r.salidaPrevista is not null and r.salidaPrevista <= :ahora order by r.salidaPrevista, r.id
        """)
    java.util.List<Registro> salidasVencidas(@Param("ahora") java.time.LocalDateTime ahora);
    @Query("""        select r from Registro r where (r.fechaSalida is null or r.horaSalida is null)        and r.salidaPrevista is not null and r.salidaPrevista <= :limite order by r.salidaPrevista, r.id        """)    java.util.List<Registro> salidasParaAviso(@Param("limite") java.time.LocalDateTime limite);
    @Query("""
        select count(r) from Registro r where r.habitacionId = :habitacionId
        and (r.fechaSalida is null or r.horaSalida is null)
        and r.salidaPrevista is not null and r.salidaPrevista <= :ahora
        """)
    long contarSalidasVencidas(@Param("habitacionId") Long habitacionId,
                              @Param("ahora") java.time.LocalDateTime ahora);

    @Query("""
            select count(r) from Registro r
            where r.habitacionId = :habitacionId
              and (:excluirId is null or r.id <> :excluirId)
              and (r.fechaSalida is null or r.horaSalida is null)
            """)
    long contarActivos(@Param("habitacionId") Long habitacionId,
                       @Param("excluirId") Long excluirId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Registro r where r.id = :id")
    Optional<Registro> buscarParaActualizar(@Param("id") Long id);

    @Query("""
            select (count(r) > 0) from Registro r
            where r.habitacionId = :habitacionId
              and (:excluirId is null or r.id <> :excluirId)
              and (r.fechaSalida is null or r.horaSalida is null)
            """)
    boolean existeOtroActivo(@Param("habitacionId") Long habitacionId,
                            @Param("excluirId") Long excluirId);
}
