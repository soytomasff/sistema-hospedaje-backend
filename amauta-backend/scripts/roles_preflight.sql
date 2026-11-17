-- Ejecutar antes de la migracion y revisar cada cuenta con su responsable.
SELECT id, username, rol FROM public.usuario ORDER BY rol, id;
SELECT UPPER(BTRIM(rol)) AS rol_actual, COUNT(*) AS usuarios
FROM public.usuario GROUP BY UPPER(BTRIM(rol)) ORDER BY rol_actual;
-- ADMIN, JEFE y ADMINISTRADOR -> ADMINISTRADOR.
-- RECEPCION, RECEPCION_CAJA, RECEPCIONISTA, LIMPIEZA y PERSONAL_LIMPIEZA -> RECEPCIONISTA.
-- Cualquier otro rol (incluido EMPLEADO) requiere clasificacion manual antes de migrar.
