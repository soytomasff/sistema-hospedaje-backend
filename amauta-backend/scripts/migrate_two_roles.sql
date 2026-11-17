-- Ejecutar solo despues de revisar roles_preflight.sql y hacer un respaldo de PostgreSQL.
-- La transaccion conserva usuarios, contrasenas y toda la historia; guarda el rol anterior por id.
BEGIN;
LOCK TABLE public.usuario IN ACCESS EXCLUSIVE MODE;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM public.usuario WHERE UPPER(BTRIM(rol)) NOT IN
        ('ADMIN', 'JEFE', 'ADMINISTRADOR', 'RECEPCION', 'RECEPCION_CAJA',
         'RECEPCIONISTA', 'LIMPIEZA', 'PERSONAL_LIMPIEZA')) THEN
        RAISE EXCEPTION 'Hay usuarios con roles sin clasificar. Revise roles_preflight.sql antes de migrar.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM public.usuario WHERE UPPER(BTRIM(rol)) IN
        ('ADMIN', 'JEFE', 'ADMINISTRADOR')) THEN
        RAISE EXCEPTION 'No hay ninguna cuenta administrativa; revise los usuarios antes de migrar.';
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS public.usuario_rol_migracion_20261117 (
    usuario_id bigint PRIMARY KEY,
    username varchar(255) NOT NULL,
    rol_anterior varchar(255) NOT NULL,
    fecha_migracion timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO public.usuario_rol_migracion_20261117 (usuario_id, username, rol_anterior)
SELECT id, username, rol FROM public.usuario
ON CONFLICT (usuario_id) DO NOTHING;

UPDATE public.usuario
SET rol = CASE
    WHEN UPPER(BTRIM(rol)) IN ('ADMIN', 'JEFE', 'ADMINISTRADOR') THEN 'ADMINISTRADOR'
    ELSE 'RECEPCIONISTA'
END;

ALTER TABLE public.usuario DROP CONSTRAINT IF EXISTS usuario_rol_dos_roles;
ALTER TABLE public.usuario ADD CONSTRAINT usuario_rol_dos_roles
    CHECK (rol IN ('ADMINISTRADOR', 'RECEPCIONISTA'));
COMMIT;

SELECT id, username, rol FROM public.usuario ORDER BY id;
