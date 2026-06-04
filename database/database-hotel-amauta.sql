--
-- PostgreSQL database dump
--

\restrict zGgfpoATjlTZZUZundbv1TirZEozbNbQ9yqRjXvuqEpIcmTchhPWrbHgaLoLCiH

-- Dumped from database version 16.13
-- Dumped by pg_dump version 16.13

-- Started on 2026-06-04 17:05:56

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 232 (class 1259 OID 17951)
-- Name: boleta_hospedaje; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.boleta_hospedaje (
    id bigint NOT NULL,
    registro_id bigint NOT NULL,
    serie character varying(255) DEFAULT 'B001'::character varying,
    numero bigint NOT NULL,
    nombre_huesped character varying(255),
    dni character varying(255),
    numero_habitacion character varying(255),
    fecha_ingreso date,
    fecha_salida date,
    noches integer,
    precio_noche numeric(38,2),
    subtotal numeric(38,2),
    igv numeric(38,2),
    total numeric(38,2),
    metodo_pago character varying(255),
    fecha_emision timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    estado character varying(255) DEFAULT 'PAGADO'::character varying
);


ALTER TABLE public.boleta_hospedaje OWNER TO postgres;

--
-- TOC entry 231 (class 1259 OID 17950)
-- Name: boleta_hospedaje_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.boleta_hospedaje_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.boleta_hospedaje_id_seq OWNER TO postgres;

--
-- TOC entry 5107 (class 0 OID 0)
-- Dependencies: 231
-- Name: boleta_hospedaje_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.boleta_hospedaje_id_seq OWNED BY public.boleta_hospedaje.id;


--
-- TOC entry 238 (class 1259 OID 18004)
-- Name: catalogo_item_habitacion; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.catalogo_item_habitacion (
    id bigint NOT NULL,
    nombre character varying(100) NOT NULL,
    categoria character varying(100),
    descripcion character varying(255)
);


ALTER TABLE public.catalogo_item_habitacion OWNER TO postgres;

--
-- TOC entry 237 (class 1259 OID 18003)
-- Name: catalogo_item_habitacion_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.catalogo_item_habitacion_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.catalogo_item_habitacion_id_seq OWNER TO postgres;

--
-- TOC entry 5108 (class 0 OID 0)
-- Dependencies: 237
-- Name: catalogo_item_habitacion_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.catalogo_item_habitacion_id_seq OWNED BY public.catalogo_item_habitacion.id;


--
-- TOC entry 244 (class 1259 OID 18056)
-- Name: cierre_caja; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cierre_caja (
    id bigint NOT NULL,
    fecha date NOT NULL,
    total_hospedaje numeric(10,2) DEFAULT 0,
    total_vitrina numeric(10,2) DEFAULT 0,
    total_ingresos numeric(10,2) DEFAULT 0,
    total_egresos numeric(10,2) DEFAULT 0,
    total_final numeric(10,2) DEFAULT 0,
    efectivo numeric(10,2) DEFAULT 0,
    yape numeric(10,2) DEFAULT 0,
    plin numeric(10,2) DEFAULT 0,
    tarjeta numeric(10,2) DEFAULT 0,
    transferencia numeric(10,2) DEFAULT 0,
    cerrado boolean DEFAULT true,
    fecha_cierre timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    usuario character varying(100),
    observacion character varying(255)
);


ALTER TABLE public.cierre_caja OWNER TO postgres;

--
-- TOC entry 243 (class 1259 OID 18055)
-- Name: cierre_caja_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.cierre_caja_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.cierre_caja_id_seq OWNER TO postgres;

--
-- TOC entry 5109 (class 0 OID 0)
-- Dependencies: 243
-- Name: cierre_caja_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.cierre_caja_id_seq OWNED BY public.cierre_caja.id;


--
-- TOC entry 246 (class 1259 OID 18077)
-- Name: egreso_caja; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.egreso_caja (
    id bigint NOT NULL,
    fecha timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    descripcion character varying(255) NOT NULL,
    metodo_pago character varying(50) DEFAULT 'EFECTIVO'::character varying,
    monto numeric(10,2) NOT NULL,
    observacion character varying(255)
);


ALTER TABLE public.egreso_caja OWNER TO postgres;

--
-- TOC entry 245 (class 1259 OID 18076)
-- Name: egreso_caja_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.egreso_caja_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.egreso_caja_id_seq OWNER TO postgres;

--
-- TOC entry 5110 (class 0 OID 0)
-- Dependencies: 245
-- Name: egreso_caja_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.egreso_caja_id_seq OWNED BY public.egreso_caja.id;


--
-- TOC entry 216 (class 1259 OID 16400)
-- Name: habitacion; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.habitacion (
    id bigint NOT NULL,
    numero character varying(255),
    tipo character varying(255),
    capacidad integer,
    precio numeric(38,2),
    estado character varying(255),
    codigo character varying(255)
);


ALTER TABLE public.habitacion OWNER TO postgres;

--
-- TOC entry 215 (class 1259 OID 16399)
-- Name: habitacion_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.habitacion_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.habitacion_id_seq OWNER TO postgres;

--
-- TOC entry 5111 (class 0 OID 0)
-- Dependencies: 215
-- Name: habitacion_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.habitacion_id_seq OWNED BY public.habitacion.id;


--
-- TOC entry 240 (class 1259 OID 18013)
-- Name: habitacion_item; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.habitacion_item (
    id bigint NOT NULL,
    habitacion_id bigint NOT NULL,
    item_id bigint NOT NULL,
    cantidad integer DEFAULT 1 NOT NULL,
    estado character varying(50) DEFAULT 'BUENO'::character varying,
    observacion character varying(255)
);


ALTER TABLE public.habitacion_item OWNER TO postgres;

--
-- TOC entry 239 (class 1259 OID 18012)
-- Name: habitacion_item_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.habitacion_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.habitacion_item_id_seq OWNER TO postgres;

--
-- TOC entry 5112 (class 0 OID 0)
-- Dependencies: 239
-- Name: habitacion_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.habitacion_item_id_seq OWNED BY public.habitacion_item.id;


--
-- TOC entry 242 (class 1259 OID 18034)
-- Name: habitacion_servicio; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.habitacion_servicio (
    id bigint NOT NULL,
    habitacion_id bigint NOT NULL,
    cable boolean DEFAULT false,
    wifi boolean DEFAULT false,
    ducha_caliente boolean DEFAULT false,
    bano_privado boolean DEFAULT false,
    tv_smart boolean DEFAULT false,
    ventilador boolean DEFAULT false,
    aire_acondicionado boolean DEFAULT false,
    observacion character varying(255),
    tipo_bano character varying(50) DEFAULT 'COMPARTIDO'::character varying
);


ALTER TABLE public.habitacion_servicio OWNER TO postgres;

--
-- TOC entry 241 (class 1259 OID 18033)
-- Name: habitacion_servicio_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.habitacion_servicio_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.habitacion_servicio_id_seq OWNER TO postgres;

--
-- TOC entry 5113 (class 0 OID 0)
-- Dependencies: 241
-- Name: habitacion_servicio_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.habitacion_servicio_id_seq OWNED BY public.habitacion_servicio.id;


--
-- TOC entry 218 (class 1259 OID 16409)
-- Name: huesped; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.huesped (
    id bigint NOT NULL,
    nombre character varying(255),
    dni character varying(255),
    procedencia character varying(255),
    telefono character varying(255),
    apellido character varying(255),
    pais character varying(255),
    profesion character varying(255)
);


ALTER TABLE public.huesped OWNER TO postgres;

--
-- TOC entry 217 (class 1259 OID 16408)
-- Name: huesped_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.huesped_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.huesped_id_seq OWNER TO postgres;

--
-- TOC entry 5114 (class 0 OID 0)
-- Dependencies: 217
-- Name: huesped_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.huesped_id_seq OWNED BY public.huesped.id;


--
-- TOC entry 224 (class 1259 OID 16459)
-- Name: movimiento_inventario; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.movimiento_inventario (
    id bigint NOT NULL,
    producto_id bigint NOT NULL,
    tipo_movimiento character varying(255) NOT NULL,
    cantidad integer NOT NULL,
    fecha timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    observacion character varying(255)
);


ALTER TABLE public.movimiento_inventario OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 16458)
-- Name: movimiento_inventario_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.movimiento_inventario_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.movimiento_inventario_id_seq OWNER TO postgres;

--
-- TOC entry 5115 (class 0 OID 0)
-- Dependencies: 223
-- Name: movimiento_inventario_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.movimiento_inventario_id_seq OWNED BY public.movimiento_inventario.id;


--
-- TOC entry 222 (class 1259 OID 16435)
-- Name: producto; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.producto (
    id bigint NOT NULL,
    nombre character varying(255),
    categoria character varying(255),
    stock integer,
    stock_minimo integer,
    precio numeric(38,2),
    descripcion character varying(255),
    unidad character varying(255),
    estado character varying(255),
    ubicacion character varying(255),
    codigo character varying(255) NOT NULL
);


ALTER TABLE public.producto OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 16434)
-- Name: producto_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.producto_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.producto_id_seq OWNER TO postgres;

--
-- TOC entry 5116 (class 0 OID 0)
-- Dependencies: 221
-- Name: producto_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.producto_id_seq OWNED BY public.producto.id;


--
-- TOC entry 220 (class 1259 OID 16418)
-- Name: registro; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.registro (
    id bigint NOT NULL,
    huesped_id bigint,
    habitacion_id bigint,
    fecha_ingreso date,
    hora_ingreso time without time zone,
    fecha_salida date,
    hora_salida time without time zone,
    dni character varying(255),
    estado character varying(255),
    hora_entrada timestamp(6) without time zone,
    nombre_huesped character varying(255),
    numero_cuarto character varying(255),
    precio_cuarto double precision,
    procedencia character varying(255),
    precio numeric(38,2)
);


ALTER TABLE public.registro OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 16417)
-- Name: registro_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.registro_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.registro_id_seq OWNER TO postgres;

--
-- TOC entry 5117 (class 0 OID 0)
-- Dependencies: 219
-- Name: registro_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.registro_id_seq OWNED BY public.registro.id;


--
-- TOC entry 228 (class 1259 OID 16663)
-- Name: reserva; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.reserva (
    id bigint NOT NULL,
    huesped_id bigint,
    habitacion_id bigint,
    fecha_reserva timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    fecha_ingreso date,
    fecha_salida date,
    estado character varying(255)
);


ALTER TABLE public.reserva OWNER TO postgres;

--
-- TOC entry 227 (class 1259 OID 16662)
-- Name: reserva_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.reserva_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.reserva_id_seq OWNER TO postgres;

--
-- TOC entry 5118 (class 0 OID 0)
-- Dependencies: 227
-- Name: reserva_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.reserva_id_seq OWNED BY public.reserva.id;


--
-- TOC entry 236 (class 1259 OID 17984)
-- Name: stock_ubicacion; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.stock_ubicacion (
    id bigint NOT NULL,
    producto_id bigint NOT NULL,
    ubicacion_id bigint NOT NULL,
    stock integer DEFAULT 0
);


ALTER TABLE public.stock_ubicacion OWNER TO postgres;

--
-- TOC entry 235 (class 1259 OID 17983)
-- Name: stock_ubicacion_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.stock_ubicacion_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.stock_ubicacion_id_seq OWNER TO postgres;

--
-- TOC entry 5119 (class 0 OID 0)
-- Dependencies: 235
-- Name: stock_ubicacion_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.stock_ubicacion_id_seq OWNED BY public.stock_ubicacion.id;


--
-- TOC entry 234 (class 1259 OID 17974)
-- Name: ubicacion_inventario; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ubicacion_inventario (
    id bigint NOT NULL,
    nombre character varying(100) NOT NULL,
    descripcion character varying(255),
    estado character varying(50) DEFAULT 'ACTIVO'::character varying
);


ALTER TABLE public.ubicacion_inventario OWNER TO postgres;

--
-- TOC entry 233 (class 1259 OID 17973)
-- Name: ubicacion_inventario_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.ubicacion_inventario_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.ubicacion_inventario_id_seq OWNER TO postgres;

--
-- TOC entry 5120 (class 0 OID 0)
-- Dependencies: 233
-- Name: ubicacion_inventario_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.ubicacion_inventario_id_seq OWNED BY public.ubicacion_inventario.id;


--
-- TOC entry 226 (class 1259 OID 16642)
-- Name: usuario; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.usuario (
    id bigint NOT NULL,
    username character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    rol character varying(255) NOT NULL
);


ALTER TABLE public.usuario OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 16641)
-- Name: usuario_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.usuario_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.usuario_id_seq OWNER TO postgres;

--
-- TOC entry 5121 (class 0 OID 0)
-- Dependencies: 225
-- Name: usuario_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.usuario_id_seq OWNED BY public.usuario.id;


--
-- TOC entry 230 (class 1259 OID 17937)
-- Name: venta_producto; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.venta_producto (
    id bigint NOT NULL,
    producto_id bigint NOT NULL,
    cantidad integer NOT NULL,
    precio_unitario numeric(38,2) NOT NULL,
    subtotal numeric(38,2) NOT NULL,
    metodo_pago character varying(255) NOT NULL,
    fecha timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    observacion character varying(255)
);


ALTER TABLE public.venta_producto OWNER TO postgres;

--
-- TOC entry 229 (class 1259 OID 17936)
-- Name: venta_producto_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.venta_producto_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.venta_producto_id_seq OWNER TO postgres;

--
-- TOC entry 5122 (class 0 OID 0)
-- Dependencies: 229
-- Name: venta_producto_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.venta_producto_id_seq OWNED BY public.venta_producto.id;


--
-- TOC entry 247 (class 1259 OID 18087)
-- Name: vista_caja_diaria; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.vista_caja_diaria AS
 SELECT r.fecha_ingreso AS fecha,
    COALESCE(to_char((r.hora_ingreso)::interval, 'HH24:MI'::text), to_char(r.hora_entrada, 'HH24:MI'::text), '00:00'::text) AS hora,
    'HOSPEDAJE'::text AS tipo,
    COALESCE(r.numero_cuarto, h.numero, '-'::character varying) AS habitacion,
    COALESCE(r.nombre_huesped, (concat(hu.nombre, ' ', hu.apellido))::character varying, 'Hu‚sped sin nombre'::character varying) AS detalle,
    'EFECTIVO'::character varying AS metodo_pago,
    (COALESCE((r.precio)::double precision, r.precio_cuarto, (h.precio)::double precision, (0)::double precision))::numeric AS ingreso,
    (0)::numeric AS egreso,
    ('Registro de estancia #'::text || r.id) AS observacion
   FROM ((public.registro r
     LEFT JOIN public.huesped hu ON ((hu.id = r.huesped_id)))
     LEFT JOIN public.habitacion h ON ((h.id = r.habitacion_id)))
  WHERE ((r.estado IS NULL) OR (upper((r.estado)::text) <> 'CANCELADO'::text))
UNION ALL
 SELECT (v.fecha)::date AS fecha,
    to_char(v.fecha, 'HH24:MI'::text) AS hora,
    'VENTA VITRINA'::text AS tipo,
    '-'::character varying AS habitacion,
    (((p.nombre)::text || ' x '::text) || v.cantidad) AS detalle,
    v.metodo_pago,
    v.subtotal AS ingreso,
    (0)::numeric AS egreso,
    COALESCE(v.observacion, ''::character varying) AS observacion
   FROM (public.venta_producto v
     JOIN public.producto p ON ((v.producto_id = p.id)))
UNION ALL
 SELECT (e.fecha)::date AS fecha,
    to_char(e.fecha, 'HH24:MI'::text) AS hora,
    'EGRESO'::text AS tipo,
    '-'::character varying AS habitacion,
    e.descripcion AS detalle,
    e.metodo_pago,
    (0)::numeric AS ingreso,
    e.monto AS egreso,
    COALESCE(e.observacion, ''::character varying) AS observacion
   FROM public.egreso_caja e;


ALTER VIEW public.vista_caja_diaria OWNER TO postgres;

--
-- TOC entry 4825 (class 2604 OID 17954)
-- Name: boleta_hospedaje id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.boleta_hospedaje ALTER COLUMN id SET DEFAULT nextval('public.boleta_hospedaje_id_seq'::regclass);


--
-- TOC entry 4833 (class 2604 OID 18007)
-- Name: catalogo_item_habitacion id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.catalogo_item_habitacion ALTER COLUMN id SET DEFAULT nextval('public.catalogo_item_habitacion_id_seq'::regclass);


--
-- TOC entry 4846 (class 2604 OID 18059)
-- Name: cierre_caja id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cierre_caja ALTER COLUMN id SET DEFAULT nextval('public.cierre_caja_id_seq'::regclass);


--
-- TOC entry 4859 (class 2604 OID 18080)
-- Name: egreso_caja id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.egreso_caja ALTER COLUMN id SET DEFAULT nextval('public.egreso_caja_id_seq'::regclass);


--
-- TOC entry 4814 (class 2604 OID 16471)
-- Name: habitacion id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.habitacion ALTER COLUMN id SET DEFAULT nextval('public.habitacion_id_seq'::regclass);


--
-- TOC entry 4834 (class 2604 OID 18016)
-- Name: habitacion_item id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.habitacion_item ALTER COLUMN id SET DEFAULT nextval('public.habitacion_item_id_seq'::regclass);


--
-- TOC entry 4837 (class 2604 OID 18037)
-- Name: habitacion_servicio id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.habitacion_servicio ALTER COLUMN id SET DEFAULT nextval('public.habitacion_servicio_id_seq'::regclass);


--
-- TOC entry 4815 (class 2604 OID 16441)
-- Name: huesped id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.huesped ALTER COLUMN id SET DEFAULT nextval('public.huesped_id_seq'::regclass);


--
-- TOC entry 4818 (class 2604 OID 16587)
-- Name: movimiento_inventario id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimiento_inventario ALTER COLUMN id SET DEFAULT nextval('public.movimiento_inventario_id_seq'::regclass);


--
-- TOC entry 4817 (class 2604 OID 16488)
-- Name: producto id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.producto ALTER COLUMN id SET DEFAULT nextval('public.producto_id_seq'::regclass);


--
-- TOC entry 4816 (class 2604 OID 16610)
-- Name: registro id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.registro ALTER COLUMN id SET DEFAULT nextval('public.registro_id_seq'::regclass);


--
-- TOC entry 4821 (class 2604 OID 16680)
-- Name: reserva id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reserva ALTER COLUMN id SET DEFAULT nextval('public.reserva_id_seq'::regclass);


--
-- TOC entry 4831 (class 2604 OID 17987)
-- Name: stock_ubicacion id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stock_ubicacion ALTER COLUMN id SET DEFAULT nextval('public.stock_ubicacion_id_seq'::regclass);


--
-- TOC entry 4829 (class 2604 OID 17977)
-- Name: ubicacion_inventario id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ubicacion_inventario ALTER COLUMN id SET DEFAULT nextval('public.ubicacion_inventario_id_seq'::regclass);


--
-- TOC entry 4820 (class 2604 OID 16650)
-- Name: usuario id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario ALTER COLUMN id SET DEFAULT nextval('public.usuario_id_seq'::regclass);


--
-- TOC entry 4823 (class 2604 OID 17940)
-- Name: venta_producto id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.venta_producto ALTER COLUMN id SET DEFAULT nextval('public.venta_producto_id_seq'::regclass);


--
-- TOC entry 5087 (class 0 OID 17951)
-- Dependencies: 232
-- Data for Name: boleta_hospedaje; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.boleta_hospedaje (id, registro_id, serie, numero, nombre_huesped, dni, numero_habitacion, fecha_ingreso, fecha_salida, noches, precio_noche, subtotal, igv, total, metodo_pago, fecha_emision, estado) FROM stdin;
1	2	B001	1	Eder Ulises Nieto Párraga	50505050	202	2026-05-08	2026-05-09	1	30.00	30.00	0.00	30.00	EFECTIVO	2026-05-31 12:14:05.660294	PAGADO
2	9	B001	2	Maria  perez lopez	45678912	204	2026-05-13	2026-05-14	1	25.00	25.00	0.00	25.00	YAPE	2026-05-31 12:15:01.966617	PAGADO
3	4	B001	3	Pepe Tinco	45201452	204	2026-05-08	2026-05-09	1	25.00	25.00	0.00	25.00	YAPE	2026-05-31 17:13:19.798691	PAGADO
\.


--
-- TOC entry 5093 (class 0 OID 18004)
-- Dependencies: 238
-- Data for Name: catalogo_item_habitacion; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.catalogo_item_habitacion (id, nombre, categoria, descripcion) FROM stdin;
1	TV	Electrodomestico	Televisor de habitacion
2	Mesa	Mueble	Mesa de habitacion
3	Silla	Mueble	Silla de habitacion
4	Cama	Mueble	Cama principal
5	Control remoto	Accesorio	Control del televisor
6	Frazada	Ropa de cama	Frazada asignada al cuarto
7	Sabana	Ropa de cama	Sabana asignada al cuarto
8	Toalla	Ropa de bano	Toalla asignada al huesped
9	Espejo	Mueble	Espejo de habitacion
10	Basurero	Accesorio	Basurero de habitacion
\.


--
-- TOC entry 5099 (class 0 OID 18056)
-- Dependencies: 244
-- Data for Name: cierre_caja; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.cierre_caja (id, fecha, total_hospedaje, total_vitrina, total_ingresos, total_egresos, total_final, efectivo, yape, plin, tarjeta, transferencia, cerrado, fecha_cierre, usuario, observacion) FROM stdin;
\.


--
-- TOC entry 5101 (class 0 OID 18077)
-- Dependencies: 246
-- Data for Name: egreso_caja; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.egreso_caja (id, fecha, descripcion, metodo_pago, monto, observacion) FROM stdin;
\.


--
-- TOC entry 5071 (class 0 OID 16400)
-- Dependencies: 216
-- Data for Name: habitacion; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.habitacion (id, numero, tipo, capacidad, precio, estado, codigo) FROM stdin;
3	203	Simple	1	20.00	OCUPADO	HAB-3
4	204	Matrimonial	2	25.00	OCUPADO	HAB-4
5	301	Doble	2	15.00	LIBRE	HAB-5
7	302	Doble	2	35.00	LIBRE	HAB-6
2	202	Matrimonial	2	30.00	LIBRE	HAB-2
1	201	Matrimonial	2	30.00	OCUPADO	HAB-1
9	303	Doble	2	30.00	LIBRE	HAB-7
11	304	Doble	2	30.00	LIBRE	HAB-8
15	306	Doble	2	45.00	LIBRE	HAB-10
19	309	Matrimonial	1	20.00	LIBRE	HAB-12
21	310	Matrimonial	1	30.00	LIBRE	HAB-13
13	305	Doble	2	30.00	OCUPADO	HAB-9
25	401	Simple	1	25.00	LIBRE	HAB-15
29	403	Doble	2	15.00	LIBRE	HAB-17
31	404	Matrimonial	1	25.00	LIBRE	HAB-18
33	405	Matrimonial	1	25.00	LIBRE	HAB-19
35	406	Doble	2	20.00	LIBRE	HAB-20
17	307	Matrimonial	1	30.00	OCUPADO	HAB-11
27	402	Simple	1	25.00	OCUPADO	HAB-16
23	311	Matrimonial	1	25.00	OCUPADO	HAB-14
\.


--
-- TOC entry 5095 (class 0 OID 18013)
-- Dependencies: 240
-- Data for Name: habitacion_item; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.habitacion_item (id, habitacion_id, item_id, cantidad, estado, observacion) FROM stdin;
57	23	9	1	BUENO	Equipamiento inicial
58	21	9	1	BUENO	Equipamiento inicial
59	19	9	1	BUENO	Equipamiento inicial
60	17	9	1	BUENO	Equipamiento inicial
61	1	9	1	BUENO	Equipamiento inicial
62	2	9	1	BUENO	Equipamiento inicial
63	4	9	1	BUENO	Equipamiento inicial
64	23	10	1	BUENO	Equipamiento inicial
65	21	10	1	BUENO	Equipamiento inicial
66	19	10	1	BUENO	Equipamiento inicial
67	17	10	1	BUENO	Equipamiento inicial
68	1	10	1	BUENO	Equipamiento inicial
69	2	10	1	BUENO	Equipamiento inicial
70	4	10	1	BUENO	Equipamiento inicial
79	3	9	1	BUENO	Equipamiento inicial
80	3	10	1	BUENO	Equipamiento inicial
13	2	2	1	BUENO	Actualizado desde sistema
27	2	4	1	BUENO	Actualizado desde sistema
34	2	5	1	BUENO	Actualizado desde sistema
41	2	6	1	BUENO	Actualizado desde sistema
48	2	7	2	BUENO	Actualizado desde sistema
55	2	8	2	BUENO	Actualizado desde sistema
71	3	1	1	BUENO	Actualizado desde sistema
72	3	2	1	BUENO	Actualizado desde sistema
74	3	4	1	BUENO	Actualizado desde sistema
75	3	5	1	BUENO	Actualizado desde sistema
76	3	6	1	BUENO	Actualizado desde sistema
77	3	7	1	BUENO	Actualizado desde sistema
78	3	8	1	BUENO	Actualizado desde sistema
7	4	1	1	BUENO	Actualizado desde sistema
14	4	2	1	BUENO	Actualizado desde sistema
28	4	4	1	BUENO	Actualizado desde sistema
35	4	5	1	BUENO	Actualizado desde sistema
42	4	6	1	BUENO	Actualizado desde sistema
49	4	7	2	BUENO	Actualizado desde sistema
56	4	8	2	BUENO	Actualizado desde sistema
86	5	1	1	BUENO	Actualizado desde sistema
19	1	3	1	BUENO	Actualizado desde sistema
26	1	4	1	BUENO	Actualizado desde sistema
33	1	5	1	BUENO	Actualizado desde sistema
40	1	6	1	BUENO	Actualizado desde sistema
47	1	7	1	BUENO	Actualizado desde sistema
8	23	2	1	BUENO	Actualizado desde sistema
85	7	1	1	BUENO	Actualizado desde sistema
12	1	2	1	BUENO	Actualizado desde sistema
91	7	2	1	BUENO	Actualizado desde sistema
97	7	3	2	BUENO	Actualizado desde sistema
5	1	1	1	BUENO	Actualizado desde sistema
15	23	3	1	BUENO	Actualizado desde sistema
22	23	4	1	BUENO	Actualizado desde sistema
36	23	6	1	BUENO	Actualizado desde sistema
43	23	7	1	BUENO	Actualizado desde sistema
50	23	8	0	BUENO	Actualizado desde sistema
2	21	1	1	BUENO	Actualizado desde sistema
1	23	1	1	BUENO	Actualizado desde sistema
9	21	2	1	BUENO	Actualizado desde sistema
16	21	3	1	BUENO	Actualizado desde sistema
30	21	5	1	BUENO	Actualizado desde sistema
37	21	6	1	BUENO	Actualizado desde sistema
44	21	7	2	BUENO	Actualizado desde sistema
51	21	8	1	BUENO	Actualizado desde sistema
10	19	2	1	BUENO	Actualizado desde sistema
17	19	3	1	BUENO	Actualizado desde sistema
31	19	5	1	BUENO	Actualizado desde sistema
38	19	6	1	BUENO	Actualizado desde sistema
45	19	7	1	BUENO	Actualizado desde sistema
52	19	8	0	BUENO	Actualizado desde sistema
4	17	1	1	BUENO	Actualizado desde sistema
3	19	1	1	BUENO	Actualizado desde sistema
11	17	2	1	BUENO	Actualizado desde sistema
18	17	3	2	BUENO	Actualizado desde sistema
32	17	5	1	BUENO	Actualizado desde sistema
39	17	6	1	BUENO	Actualizado desde sistema
46	17	7	2	BUENO	Actualizado desde sistema
53	17	8	0	BUENO	Actualizado desde sistema
82	15	1	1	BUENO	Actualizado desde sistema
88	15	2	1	BUENO	Actualizado desde sistema
81	13	1	1	BUENO	Actualizado desde sistema
87	13	2	1	BUENO	Actualizado desde sistema
93	13	3	2	BUENO	Actualizado desde sistema
83	11	1	1	BUENO	Actualizado desde sistema
89	11	2	1	BUENO	Actualizado desde sistema
95	11	3	2	BUENO	Actualizado desde sistema
84	9	1	1	BUENO	Actualizado desde sistema
96	9	3	2	BUENO	Actualizado desde sistema
129	13	9	1	BUENO	Equipamiento inicial
130	15	9	1	BUENO	Equipamiento inicial
131	11	9	1	BUENO	Equipamiento inicial
132	9	9	1	BUENO	Equipamiento inicial
133	7	9	1	BUENO	Equipamiento inicial
134	5	9	1	BUENO	Equipamiento inicial
135	13	10	1	BUENO	Equipamiento inicial
136	15	10	1	BUENO	Equipamiento inicial
137	11	10	1	BUENO	Equipamiento inicial
138	9	10	1	BUENO	Equipamiento inicial
139	7	10	1	BUENO	Equipamiento inicial
140	5	10	1	BUENO	Equipamiento inicial
6	2	1	1	BUENO	Actualizado desde sistema
20	2	3	2	BUENO	Actualizado desde sistema
73	3	3	1	BUENO	Actualizado desde sistema
21	4	3	2	BUENO	Actualizado desde sistema
92	5	2	1	BUENO	Actualizado desde sistema
98	5	3	2	BUENO	Actualizado desde sistema
104	5	4	2	BUENO	Actualizado desde sistema
110	5	5	1	BUENO	Actualizado desde sistema
116	5	6	2	BUENO	Actualizado desde sistema
122	5	7	2	BUENO	Actualizado desde sistema
128	5	8	2	BUENO	Actualizado desde sistema
90	9	2	1	BUENO	Actualizado desde sistema
103	7	4	2	BUENO	Actualizado desde sistema
109	7	5	1	BUENO	Actualizado desde sistema
115	7	6	2	BUENO	Actualizado desde sistema
121	7	7	2	BUENO	Actualizado desde sistema
127	7	8	2	BUENO	Actualizado desde sistema
54	1	8	0	BUENO	Actualizado desde sistema
29	23	5	1	BUENO	Actualizado desde sistema
23	21	4	1	BUENO	Actualizado desde sistema
24	19	4	1	BUENO	Actualizado desde sistema
25	17	4	1	BUENO	Actualizado desde sistema
94	15	3	2	BUENO	Actualizado desde sistema
100	15	4	2	BUENO	Actualizado desde sistema
106	15	5	1	BUENO	Actualizado desde sistema
112	15	6	2	BUENO	Actualizado desde sistema
118	15	7	2	BUENO	Actualizado desde sistema
124	15	8	0	BUENO	Actualizado desde sistema
99	13	4	2	BUENO	Actualizado desde sistema
105	13	5	1	BUENO	Actualizado desde sistema
111	13	6	2	BUENO	Actualizado desde sistema
117	13	7	2	BUENO	Actualizado desde sistema
123	13	8	0	BUENO	Actualizado desde sistema
101	11	4	2	BUENO	Actualizado desde sistema
107	11	5	1	BUENO	Actualizado desde sistema
113	11	6	2	BUENO	Actualizado desde sistema
119	11	7	2	BUENO	Actualizado desde sistema
125	11	8	0	BUENO	Actualizado desde sistema
102	9	4	2	BUENO	Actualizado desde sistema
108	9	5	1	BUENO	Actualizado desde sistema
114	9	6	2	BUENO	Actualizado desde sistema
120	9	7	2	BUENO	Actualizado desde sistema
126	9	8	0	BUENO	Actualizado desde sistema
285	25	1	1	BUENO	Actualizado desde sistema
286	25	2	1	BUENO	Actualizado desde sistema
287	25	3	1	BUENO	Actualizado desde sistema
288	25	4	1	BUENO	Actualizado desde sistema
289	25	5	1	BUENO	Actualizado desde sistema
290	25	6	1	BUENO	Actualizado desde sistema
291	25	7	1	BUENO	Actualizado desde sistema
292	25	8	0	BUENO	Actualizado desde sistema
293	27	1	1	BUENO	Actualizado desde sistema
294	27	2	1	BUENO	Actualizado desde sistema
295	27	3	1	BUENO	Actualizado desde sistema
296	27	4	1	BUENO	Actualizado desde sistema
297	27	5	1	BUENO	Actualizado desde sistema
298	27	6	1	BUENO	Actualizado desde sistema
299	27	7	1	BUENO	Actualizado desde sistema
300	27	8	0	BUENO	Actualizado desde sistema
309	31	1	1	BUENO	Actualizado desde sistema
310	31	2	1	BUENO	Actualizado desde sistema
311	31	3	1	BUENO	Actualizado desde sistema
312	31	4	1	BUENO	Actualizado desde sistema
313	31	5	1	BUENO	Actualizado desde sistema
314	31	6	1	BUENO	Actualizado desde sistema
315	31	7	1	BUENO	Actualizado desde sistema
316	31	8	0	BUENO	Actualizado desde sistema
301	29	1	1	BUENO	Actualizado desde sistema
302	29	2	1	BUENO	Actualizado desde sistema
303	29	3	1	BUENO	Actualizado desde sistema
304	29	4	2	BUENO	Actualizado desde sistema
305	29	5	1	BUENO	Actualizado desde sistema
306	29	6	2	BUENO	Actualizado desde sistema
307	29	7	2	BUENO	Actualizado desde sistema
308	29	8	0	BUENO	Actualizado desde sistema
325	33	1	1	BUENO	Actualizado desde sistema
326	33	2	1	BUENO	Actualizado desde sistema
327	33	3	1	BUENO	Actualizado desde sistema
328	33	4	1	BUENO	Actualizado desde sistema
329	33	5	1	BUENO	Actualizado desde sistema
330	33	6	1	BUENO	Actualizado desde sistema
331	33	7	1	BUENO	Actualizado desde sistema
332	33	8	0	BUENO	Actualizado desde sistema
341	35	1	1	BUENO	Actualizado desde sistema
342	35	2	1	BUENO	Actualizado desde sistema
343	35	3	1	BUENO	Actualizado desde sistema
344	35	4	1	BUENO	Actualizado desde sistema
345	35	5	1	BUENO	Actualizado desde sistema
346	35	6	1	BUENO	Actualizado desde sistema
347	35	7	1	BUENO	Actualizado desde sistema
348	35	8	0	BUENO	Actualizado desde sistema
\.


--
-- TOC entry 5097 (class 0 OID 18034)
-- Dependencies: 242
-- Data for Name: habitacion_servicio; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.habitacion_servicio (id, habitacion_id, cable, wifi, ducha_caliente, bano_privado, tv_smart, ventilador, aire_acondicionado, observacion, tipo_bano) FROM stdin;
5	2	t	t	t	t	f	f	f	Pendiente de revisar	PRIVADO
1	3	t	t	f	f	f	f	f	Pendiente de revisar	COMPARTIDO
2	4	t	t	t	t	f	f	f	Pendiente de revisar	PRIVADO
3	5	t	t	f	f	f	f	f	Bano compartido con ventilador	COMPARTIDO
4	7	t	t	t	f	t	f	f	Pendiente de revisar	COMPARTIDO
6	1	t	t	t	t	f	f	f	Baño privado, ducha caliente.	PRIVADO
13	23	t	t	t	t	f	f	f	Pendiente de revisar	PRIVADO
12	21	t	t	t	t	f	f	f	Pendiente de revisar	PRIVADO
11	19	t	t	f	f	f	f	f	Pendiente de revisar	COMPARTIDO
10	17	t	t	t	t	f	f	f	Pendiente de revisar	PRIVADO
9	15	t	t	t	t	f	f	f	Pendiente de revisar	PRIVADO
14	13	t	t	f	f	f	f	f	Pendiente de revisar	COMPARTIDO
8	11	t	t	f	f	f	f	f	Pendiente de revisar	COMPARTIDO
7	9	t	t	f	f	f	f	f	Pendiente de revisar	COMPARTIDO
47	25	t	t	t	t	f	f	f		PRIVADO
48	27	t	t	t	t	f	f	f		PRIVADO
50	31	t	t	t	t	f	f	f		PRIVADO
49	29	t	t	f	f	f	f	f		COMPARTIDO
52	33	t	t	t	t	f	f	f		PRIVADO
54	35	t	t	t	t	f	f	f		PRIVADO
\.


--
-- TOC entry 5073 (class 0 OID 16409)
-- Dependencies: 218
-- Data for Name: huesped; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.huesped (id, nombre, dni, procedencia, telefono, apellido, pais, profesion) FROM stdin;
1	Juan 	78451236	Cusco	987654321	Nieto Párraga	Perú	comerciante
2	Maria 	45678912	Lima	912345678	perez lopez	Perú	comerciante
4	Eder Ulises	50505050	Huancavelica	928887229	Nieto Párraga	Perú	Tecnico
5	Pepe	45201452	Lima	987456321	Tinco	Peru	Comerciante
9	Paolo	41225551	Ayacucho 	9999999999	Dabid	Peru	Tecnico
7	Brayan	45678967	PIURA	878546123	Palomino	PERU	Comerciante
11	franco	45782565	LIMA		gomez rojas	Perú	COMERCIANTE
13	Alfred	72745207	Ayacucho 		Rua Human	Perú	Profesor
14	Paolo Dabid	60862792	Ayacucho 		Jose Romani	Perú	Tecnico
\.


--
-- TOC entry 5079 (class 0 OID 16459)
-- Dependencies: 224
-- Data for Name: movimiento_inventario; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.movimiento_inventario (id, producto_id, tipo_movimiento, cantidad, fecha, observacion) FROM stdin;
1	1	SALIDA	1	2026-05-31 15:09:05.655675	Venta de vitrina - EFECTIVO
2	6	SALIDA	1	2026-06-01 11:17:43.91131	Venta de vitrina - YAPE
\.


--
-- TOC entry 5077 (class 0 OID 16435)
-- Dependencies: 222
-- Data for Name: producto; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.producto (id, nombre, categoria, stock, stock_minimo, precio, descripcion, unidad, estado, ubicacion, codigo) FROM stdin;
5	Galletas	Snacks	40	10	2.00	Paquete de galletas para venta	unidad	DISPONIBLE	Recepcion	PROD-005
11	Cepillo dental	Aseo personal	40	10	2.00	Cepillo dental individual	unidad	DISPONIBLE	Recepcion	PROD-011
8	Peine	Aseo personal	30	10	1.50	Peine personal para huesped	unidad	DISPONIBLE	Almacen	PROD-008
10	Papel higienico	Habitacion	100	25	1.20	Rollo de papel higienico para habitaciones	unidad	DISPONIBLE	Almacen	PROD-010
3	Sporade	Bebidas	30	8	3.00	Bebida rehidratante Sporade	unidad	DISPONIBLE	Recepcion	PROD-003
1	Agua mineral	Bebidas	49	10	2.00	Botella de agua mineral personal	unidad	DISPONIBLE	Recepcion	PROD-001
4	Volt	Bebidas	25	8	2.50	Bebida energetica Volt	unidad	DISPONIBLE	Recepcion	PROD-004
9	Preservativo	Aseo personal	35	10	5.00	Preservativo para venta a huespedes	unidad	DISPONIBLE	Recepcion	PROD-009
2	Gaseosa personal	Bebidas	40	10	2.00	Gaseosa personal para venta a huespedes	unidad	DISPONIBLE	Recepcion	PROD-002
7	Jabon de cara	Aseo personal	20	20	1.00	Jabon de tocador para habitacion	unidad	DISPONIBLE	Almacen	PROD-007
12	Pasta dental 	Aseo personal	40	10	2.50	Pasta dental personal	unidad	DISPONIBLE	Recepcion	PROD-012
20	Frazada plaza y media	Ropa de cama	33	5	45.00	Frazada para habitaciones	unidad	DISPONIBLE	Varias ubicaciones	INV-001
21	Sabana matrimonial	Ropa de cama	47	8	35.00	Sabana para habitaciones matrimoniales	unidad	DISPONIBLE	Varias ubicaciones	INV-002
6	Shampoo sachet	Aseo personal	59	15	1.50	Shampoo personal para huesped	unidad	DISPONIBLE	Almacen	PROD-006
\.


--
-- TOC entry 5075 (class 0 OID 16418)
-- Dependencies: 220
-- Data for Name: registro; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.registro (id, huesped_id, habitacion_id, fecha_ingreso, hora_ingreso, fecha_salida, hora_salida, dni, estado, hora_entrada, nombre_huesped, numero_cuarto, precio_cuarto, procedencia, precio) FROM stdin;
2	4	2	2026-05-08	13:51:00	2026-05-09	11:00:00	50505050	\N	\N	Eder Ulises Nieto Párraga	202	30	\N	30.00
9	2	4	2026-05-13	17:06:00	2026-05-14	11:00:00	45678912	\N	\N	Maria  perez lopez	204	25	\N	25.00
4	5	4	2026-05-08	16:07:00	2026-05-09	16:07:00	45201452	\N	\N	Pepe Tinco	204	25	\N	25.00
12	9	13	2026-05-18	08:46:00	2026-05-19	11:00:00	41225551	\N	\N	Paolo Dabid	305	30	\N	30.00
14	11	17	2026-06-01	10:55:00	2026-06-02	11:00:00	\N	\N	\N	\N	\N	\N	\N	30.00
16	13	27	2026-06-01	11:16:00	2026-06-02	11:17:00	\N	\N	\N	\N	\N	\N	\N	25.00
18	14	23	2026-06-02	15:50:00	2026-06-03	11:00:00	\N	\N	\N	\N	\N	\N	\N	25.00
\.


--
-- TOC entry 5083 (class 0 OID 16663)
-- Dependencies: 228
-- Data for Name: reserva; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.reserva (id, huesped_id, habitacion_id, fecha_reserva, fecha_ingreso, fecha_salida, estado) FROM stdin;
10	1	1	2026-05-20 16:08:41.982224	2026-05-20	2026-05-20	CONFIRMADO
11	1	1	2026-05-20 16:08:42.695082	2026-05-20	2026-05-20	CONFIRMADO
\.


--
-- TOC entry 5091 (class 0 OID 17984)
-- Dependencies: 236
-- Data for Name: stock_ubicacion; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.stock_ubicacion (id, producto_id, ubicacion_id, stock) FROM stdin;
7	20	1	20
8	20	2	3
9	20	3	10
10	21	1	30
11	21	2	5
12	21	3	12
\.


--
-- TOC entry 5089 (class 0 OID 17974)
-- Dependencies: 234
-- Data for Name: ubicacion_inventario; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.ubicacion_inventario (id, nombre, descripcion, estado) FROM stdin;
1	Almacen principal	Lugar principal para frazadas, sabanas, almohadas y productos grandes	ACTIVO
2	Administracion	Area donde se guardan controles, llaves y articulos administrativos	ACTIVO
3	Habitaciones	Articulos asignados o usados dentro de habitaciones	ACTIVO
4	Recepcion	Area de atencion y entrega de productos o llaves	ACTIVO
\.


--
-- TOC entry 5081 (class 0 OID 16642)
-- Dependencies: 226
-- Data for Name: usuario; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.usuario (id, username, password, rol) FROM stdin;
1	jefe	1234	JEFE
2	Ulises Quispe Mamani	1234	EMPLEADO
\.


--
-- TOC entry 5085 (class 0 OID 17937)
-- Dependencies: 230
-- Data for Name: venta_producto; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.venta_producto (id, producto_id, cantidad, precio_unitario, subtotal, metodo_pago, fecha, observacion) FROM stdin;
1	1	1	2.50	2.50	EFECTIVO	2026-05-31 15:09:05.655675	
2	6	1	1.50	1.50	YAPE	2026-06-01 11:17:43.91131	
\.


--
-- TOC entry 5123 (class 0 OID 0)
-- Dependencies: 231
-- Name: boleta_hospedaje_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.boleta_hospedaje_id_seq', 3, true);


--
-- TOC entry 5124 (class 0 OID 0)
-- Dependencies: 237
-- Name: catalogo_item_habitacion_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.catalogo_item_habitacion_id_seq', 20, true);


--
-- TOC entry 5125 (class 0 OID 0)
-- Dependencies: 243
-- Name: cierre_caja_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.cierre_caja_id_seq', 1, false);


--
-- TOC entry 5126 (class 0 OID 0)
-- Dependencies: 245
-- Name: egreso_caja_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.egreso_caja_id_seq', 1, false);


--
-- TOC entry 5127 (class 0 OID 0)
-- Dependencies: 215
-- Name: habitacion_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.habitacion_id_seq', 36, true);


--
-- TOC entry 5128 (class 0 OID 0)
-- Dependencies: 239
-- Name: habitacion_item_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.habitacion_item_id_seq', 348, true);


--
-- TOC entry 5129 (class 0 OID 0)
-- Dependencies: 241
-- Name: habitacion_servicio_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.habitacion_servicio_id_seq', 54, true);


--
-- TOC entry 5130 (class 0 OID 0)
-- Dependencies: 217
-- Name: huesped_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.huesped_id_seq', 14, true);


--
-- TOC entry 5131 (class 0 OID 0)
-- Dependencies: 223
-- Name: movimiento_inventario_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.movimiento_inventario_id_seq', 2, true);


--
-- TOC entry 5132 (class 0 OID 0)
-- Dependencies: 221
-- Name: producto_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.producto_id_seq', 21, true);


--
-- TOC entry 5133 (class 0 OID 0)
-- Dependencies: 219
-- Name: registro_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.registro_id_seq', 18, true);


--
-- TOC entry 5134 (class 0 OID 0)
-- Dependencies: 227
-- Name: reserva_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.reserva_id_seq', 11, true);


--
-- TOC entry 5135 (class 0 OID 0)
-- Dependencies: 235
-- Name: stock_ubicacion_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.stock_ubicacion_id_seq', 12, true);


--
-- TOC entry 5136 (class 0 OID 0)
-- Dependencies: 233
-- Name: ubicacion_inventario_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.ubicacion_inventario_id_seq', 4, true);


--
-- TOC entry 5137 (class 0 OID 0)
-- Dependencies: 225
-- Name: usuario_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.usuario_id_seq', 2, true);


--
-- TOC entry 5138 (class 0 OID 0)
-- Dependencies: 229
-- Name: venta_producto_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.venta_producto_id_seq', 2, true);


--
-- TOC entry 4887 (class 2606 OID 17959)
-- Name: boleta_hospedaje boleta_hospedaje_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.boleta_hospedaje
    ADD CONSTRAINT boleta_hospedaje_pkey PRIMARY KEY (id);


--
-- TOC entry 4897 (class 2606 OID 18011)
-- Name: catalogo_item_habitacion catalogo_item_habitacion_nombre_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.catalogo_item_habitacion
    ADD CONSTRAINT catalogo_item_habitacion_nombre_key UNIQUE (nombre);


--
-- TOC entry 4899 (class 2606 OID 18009)
-- Name: catalogo_item_habitacion catalogo_item_habitacion_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.catalogo_item_habitacion
    ADD CONSTRAINT catalogo_item_habitacion_pkey PRIMARY KEY (id);


--
-- TOC entry 4909 (class 2606 OID 18075)
-- Name: cierre_caja cierre_caja_fecha_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cierre_caja
    ADD CONSTRAINT cierre_caja_fecha_key UNIQUE (fecha);


--
-- TOC entry 4911 (class 2606 OID 18073)
-- Name: cierre_caja cierre_caja_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cierre_caja
    ADD CONSTRAINT cierre_caja_pkey PRIMARY KEY (id);


--
-- TOC entry 4913 (class 2606 OID 18086)
-- Name: egreso_caja egreso_caja_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.egreso_caja
    ADD CONSTRAINT egreso_caja_pkey PRIMARY KEY (id);


--
-- TOC entry 4901 (class 2606 OID 18022)
-- Name: habitacion_item habitacion_item_habitacion_id_item_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.habitacion_item
    ADD CONSTRAINT habitacion_item_habitacion_id_item_id_key UNIQUE (habitacion_id, item_id);


--
-- TOC entry 4903 (class 2606 OID 18020)
-- Name: habitacion_item habitacion_item_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.habitacion_item
    ADD CONSTRAINT habitacion_item_pkey PRIMARY KEY (id);


--
-- TOC entry 4863 (class 2606 OID 16485)
-- Name: habitacion habitacion_numero_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.habitacion
    ADD CONSTRAINT habitacion_numero_key UNIQUE (numero);


--
-- TOC entry 4865 (class 2606 OID 16473)
-- Name: habitacion habitacion_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.habitacion
    ADD CONSTRAINT habitacion_pkey PRIMARY KEY (id);


--
-- TOC entry 4905 (class 2606 OID 18048)
-- Name: habitacion_servicio habitacion_servicio_habitacion_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.habitacion_servicio
    ADD CONSTRAINT habitacion_servicio_habitacion_id_key UNIQUE (habitacion_id);


--
-- TOC entry 4907 (class 2606 OID 18046)
-- Name: habitacion_servicio habitacion_servicio_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.habitacion_servicio
    ADD CONSTRAINT habitacion_servicio_pkey PRIMARY KEY (id);


--
-- TOC entry 4867 (class 2606 OID 16455)
-- Name: huesped huesped_dni_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.huesped
    ADD CONSTRAINT huesped_dni_key UNIQUE (dni);


--
-- TOC entry 4869 (class 2606 OID 16443)
-- Name: huesped huesped_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.huesped
    ADD CONSTRAINT huesped_pkey PRIMARY KEY (id);


--
-- TOC entry 4877 (class 2606 OID 16589)
-- Name: movimiento_inventario movimiento_inventario_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimiento_inventario
    ADD CONSTRAINT movimiento_inventario_pkey PRIMARY KEY (id);


--
-- TOC entry 4873 (class 2606 OID 16490)
-- Name: producto producto_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.producto
    ADD CONSTRAINT producto_pkey PRIMARY KEY (id);


--
-- TOC entry 4871 (class 2606 OID 16612)
-- Name: registro registro_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.registro
    ADD CONSTRAINT registro_pkey PRIMARY KEY (id);


--
-- TOC entry 4883 (class 2606 OID 16682)
-- Name: reserva reserva_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reserva
    ADD CONSTRAINT reserva_pkey PRIMARY KEY (id);


--
-- TOC entry 4893 (class 2606 OID 17990)
-- Name: stock_ubicacion stock_ubicacion_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stock_ubicacion
    ADD CONSTRAINT stock_ubicacion_pkey PRIMARY KEY (id);


--
-- TOC entry 4895 (class 2606 OID 17992)
-- Name: stock_ubicacion stock_ubicacion_producto_id_ubicacion_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stock_ubicacion
    ADD CONSTRAINT stock_ubicacion_producto_id_ubicacion_id_key UNIQUE (producto_id, ubicacion_id);


--
-- TOC entry 4889 (class 2606 OID 17982)
-- Name: ubicacion_inventario ubicacion_inventario_nombre_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ubicacion_inventario
    ADD CONSTRAINT ubicacion_inventario_nombre_key UNIQUE (nombre);


--
-- TOC entry 4891 (class 2606 OID 17980)
-- Name: ubicacion_inventario ubicacion_inventario_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ubicacion_inventario
    ADD CONSTRAINT ubicacion_inventario_pkey PRIMARY KEY (id);


--
-- TOC entry 4875 (class 2606 OID 16608)
-- Name: producto unique_codigo; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.producto
    ADD CONSTRAINT unique_codigo UNIQUE (codigo);


--
-- TOC entry 4879 (class 2606 OID 16652)
-- Name: usuario usuario_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_pkey PRIMARY KEY (id);


--
-- TOC entry 4881 (class 2606 OID 16661)
-- Name: usuario usuario_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_username_key UNIQUE (username);


--
-- TOC entry 4885 (class 2606 OID 17943)
-- Name: venta_producto venta_producto_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.venta_producto
    ADD CONSTRAINT venta_producto_pkey PRIMARY KEY (id);


--
-- TOC entry 4920 (class 2606 OID 17960)
-- Name: boleta_hospedaje boleta_hospedaje_registro_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.boleta_hospedaje
    ADD CONSTRAINT boleta_hospedaje_registro_id_fkey FOREIGN KEY (registro_id) REFERENCES public.registro(id);


--
-- TOC entry 4923 (class 2606 OID 18023)
-- Name: habitacion_item habitacion_item_habitacion_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.habitacion_item
    ADD CONSTRAINT habitacion_item_habitacion_id_fkey FOREIGN KEY (habitacion_id) REFERENCES public.habitacion(id);


--
-- TOC entry 4924 (class 2606 OID 18028)
-- Name: habitacion_item habitacion_item_item_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.habitacion_item
    ADD CONSTRAINT habitacion_item_item_id_fkey FOREIGN KEY (item_id) REFERENCES public.catalogo_item_habitacion(id);


--
-- TOC entry 4925 (class 2606 OID 18049)
-- Name: habitacion_servicio habitacion_servicio_habitacion_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.habitacion_servicio
    ADD CONSTRAINT habitacion_servicio_habitacion_id_fkey FOREIGN KEY (habitacion_id) REFERENCES public.habitacion(id);


--
-- TOC entry 4916 (class 2606 OID 16596)
-- Name: movimiento_inventario movimiento_inventario_producto_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimiento_inventario
    ADD CONSTRAINT movimiento_inventario_producto_id_fkey FOREIGN KEY (producto_id) REFERENCES public.producto(id);


--
-- TOC entry 4914 (class 2606 OID 16619)
-- Name: registro registro_habitacion_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.registro
    ADD CONSTRAINT registro_habitacion_id_fkey FOREIGN KEY (habitacion_id) REFERENCES public.habitacion(id);


--
-- TOC entry 4915 (class 2606 OID 16630)
-- Name: registro registro_huesped_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.registro
    ADD CONSTRAINT registro_huesped_id_fkey FOREIGN KEY (huesped_id) REFERENCES public.huesped(id);


--
-- TOC entry 4917 (class 2606 OID 16687)
-- Name: reserva reserva_habitacion_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reserva
    ADD CONSTRAINT reserva_habitacion_id_fkey FOREIGN KEY (habitacion_id) REFERENCES public.habitacion(id);


--
-- TOC entry 4918 (class 2606 OID 16696)
-- Name: reserva reserva_huesped_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reserva
    ADD CONSTRAINT reserva_huesped_id_fkey FOREIGN KEY (huesped_id) REFERENCES public.huesped(id);


--
-- TOC entry 4921 (class 2606 OID 17993)
-- Name: stock_ubicacion stock_ubicacion_producto_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stock_ubicacion
    ADD CONSTRAINT stock_ubicacion_producto_id_fkey FOREIGN KEY (producto_id) REFERENCES public.producto(id);


--
-- TOC entry 4922 (class 2606 OID 17998)
-- Name: stock_ubicacion stock_ubicacion_ubicacion_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stock_ubicacion
    ADD CONSTRAINT stock_ubicacion_ubicacion_id_fkey FOREIGN KEY (ubicacion_id) REFERENCES public.ubicacion_inventario(id);


--
-- TOC entry 4919 (class 2606 OID 17944)
-- Name: venta_producto venta_producto_producto_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.venta_producto
    ADD CONSTRAINT venta_producto_producto_id_fkey FOREIGN KEY (producto_id) REFERENCES public.producto(id);


-- Completed on 2026-06-04 17:05:56

--
-- PostgreSQL database dump complete
--

\unrestrict zGgfpoATjlTZZUZundbv1TirZEozbNbQ9yqRjXvuqEpIcmTchhPWrbHgaLoLCiH

