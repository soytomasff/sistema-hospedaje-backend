--
-- PostgreSQL database dump
--

\restrict gwrl4pIHxZ9afLDvhgpFYwu5scpccNcCrx17mCSMWbp7DuR4HfO2X04qKI61IoW

-- Dumped from database version 16.13
-- Dumped by pg_dump version 16.13

-- Started on 2026-05-18 13:34:43

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
-- TOC entry 4950 (class 0 OID 0)
-- Dependencies: 215
-- Name: habitacion_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.habitacion_id_seq OWNED BY public.habitacion.id;


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
-- TOC entry 4951 (class 0 OID 0)
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
-- TOC entry 4952 (class 0 OID 0)
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
-- TOC entry 4953 (class 0 OID 0)
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
-- TOC entry 4954 (class 0 OID 0)
-- Dependencies: 219
-- Name: registro_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.registro_id_seq OWNED BY public.registro.id;


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
-- TOC entry 4955 (class 0 OID 0)
-- Dependencies: 225
-- Name: usuario_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.usuario_id_seq OWNED BY public.usuario.id;


--
-- TOC entry 4760 (class 2604 OID 16471)
-- Name: habitacion id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.habitacion ALTER COLUMN id SET DEFAULT nextval('public.habitacion_id_seq'::regclass);


--
-- TOC entry 4761 (class 2604 OID 16441)
-- Name: huesped id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.huesped ALTER COLUMN id SET DEFAULT nextval('public.huesped_id_seq'::regclass);


--
-- TOC entry 4764 (class 2604 OID 16587)
-- Name: movimiento_inventario id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimiento_inventario ALTER COLUMN id SET DEFAULT nextval('public.movimiento_inventario_id_seq'::regclass);


--
-- TOC entry 4763 (class 2604 OID 16488)
-- Name: producto id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.producto ALTER COLUMN id SET DEFAULT nextval('public.producto_id_seq'::regclass);


--
-- TOC entry 4762 (class 2604 OID 16610)
-- Name: registro id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.registro ALTER COLUMN id SET DEFAULT nextval('public.registro_id_seq'::regclass);


--
-- TOC entry 4766 (class 2604 OID 16650)
-- Name: usuario id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario ALTER COLUMN id SET DEFAULT nextval('public.usuario_id_seq'::regclass);


--
-- TOC entry 4934 (class 0 OID 16400)
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
17	307	Matrimonial	1	30.00	LIBRE	HAB-11
19	309	Matrimonial	1	20.00	LIBRE	HAB-12
21	310	Matrimonial	1	30.00	LIBRE	HAB-13
23	311	Matrimonial	1	25.00	LIBRE	HAB-14
13	305	Doble	2	30.00	OCUPADO	HAB-9
\.


--
-- TOC entry 4936 (class 0 OID 16409)
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
\.


--
-- TOC entry 4942 (class 0 OID 16459)
-- Dependencies: 224
-- Data for Name: movimiento_inventario; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.movimiento_inventario (id, producto_id, tipo_movimiento, cantidad, fecha, observacion) FROM stdin;
1	1	SALIDA	2	2026-04-11 12:16:20.704428	Uso por Semana Santa
2	2	ENTRADA	10	2026-04-11 12:16:20.704428	Lavado y reposicion
3	4	DA¥ADO	1	2026-04-11 12:16:20.704428	Control remoto no funciona
\.


--
-- TOC entry 4940 (class 0 OID 16435)
-- Dependencies: 222
-- Data for Name: producto; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.producto (id, nombre, categoria, stock, stock_minimo, precio, descripcion, unidad, estado, ubicacion, codigo) FROM stdin;
1	Frazada plaza y media	Ropa de cama	20	5	45.00	Frazada extra para fechas festivas	unidad	RESERVA	Almacen principal	TEMP-1
2	Toalla blanca	Ba¤o	35	10	18.00	Toalla de uso diario para huespedes	unidad	DISPONIBLE	Lavanderia	TEMP-2
3	Sabana matrimonial	Ropa de cama	18	5	35.00	Sabana blanca para cama matrimonial	unidad	DISPONIBLE	Almacen principal	TEMP-3
4	Control remoto TV	Habitacion	8	2	25.00	Control remoto para televisores de habitaciones	unidad	EN_USO	Recepcion	TEMP-4
8	Silla y mesa 	Habitacion	20	45	32.00	Cuidar	20	EN_USO	Habitacion	TEMP-5
\.


--
-- TOC entry 4938 (class 0 OID 16418)
-- Dependencies: 220
-- Data for Name: registro; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.registro (id, huesped_id, habitacion_id, fecha_ingreso, hora_ingreso, fecha_salida, hora_salida, dni, estado, hora_entrada, nombre_huesped, numero_cuarto, precio_cuarto, procedencia, precio) FROM stdin;
2	4	2	2026-05-08	13:51:00	2026-05-09	11:00:00	\N	\N	\N	\N	\N	\N	\N	30.00
9	2	4	2026-05-13	17:06:00	2026-05-14	11:00:00	\N	\N	\N	\N	\N	\N	\N	25.00
4	5	4	2026-05-08	16:07:00	2026-05-09	16:07:00	\N	\N	\N	\N	\N	\N	\N	25.00
12	9	13	2026-05-18	08:46:00	2026-05-19	11:00:00	\N	\N	\N	\N	\N	\N	\N	30.00
\.


--
-- TOC entry 4944 (class 0 OID 16642)
-- Dependencies: 226
-- Data for Name: usuario; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.usuario (id, username, password, rol) FROM stdin;
1	jefe	1234	JEFE
2	Ulises Quispe Mamani	1234	EMPLEADO
\.


--
-- TOC entry 4956 (class 0 OID 0)
-- Dependencies: 215
-- Name: habitacion_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.habitacion_id_seq', 24, true);


--
-- TOC entry 4957 (class 0 OID 0)
-- Dependencies: 217
-- Name: huesped_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.huesped_id_seq', 10, true);


--
-- TOC entry 4958 (class 0 OID 0)
-- Dependencies: 223
-- Name: movimiento_inventario_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.movimiento_inventario_id_seq', 3, true);


--
-- TOC entry 4959 (class 0 OID 0)
-- Dependencies: 221
-- Name: producto_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.producto_id_seq', 8, true);


--
-- TOC entry 4960 (class 0 OID 0)
-- Dependencies: 219
-- Name: registro_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.registro_id_seq', 13, true);


--
-- TOC entry 4961 (class 0 OID 0)
-- Dependencies: 225
-- Name: usuario_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.usuario_id_seq', 2, true);


--
-- TOC entry 4768 (class 2606 OID 16485)
-- Name: habitacion habitacion_numero_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.habitacion
    ADD CONSTRAINT habitacion_numero_key UNIQUE (numero);


--
-- TOC entry 4770 (class 2606 OID 16473)
-- Name: habitacion habitacion_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.habitacion
    ADD CONSTRAINT habitacion_pkey PRIMARY KEY (id);


--
-- TOC entry 4772 (class 2606 OID 16455)
-- Name: huesped huesped_dni_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.huesped
    ADD CONSTRAINT huesped_dni_key UNIQUE (dni);


--
-- TOC entry 4774 (class 2606 OID 16443)
-- Name: huesped huesped_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.huesped
    ADD CONSTRAINT huesped_pkey PRIMARY KEY (id);


--
-- TOC entry 4782 (class 2606 OID 16589)
-- Name: movimiento_inventario movimiento_inventario_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimiento_inventario
    ADD CONSTRAINT movimiento_inventario_pkey PRIMARY KEY (id);


--
-- TOC entry 4778 (class 2606 OID 16490)
-- Name: producto producto_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.producto
    ADD CONSTRAINT producto_pkey PRIMARY KEY (id);


--
-- TOC entry 4776 (class 2606 OID 16612)
-- Name: registro registro_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.registro
    ADD CONSTRAINT registro_pkey PRIMARY KEY (id);


--
-- TOC entry 4780 (class 2606 OID 16608)
-- Name: producto unique_codigo; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.producto
    ADD CONSTRAINT unique_codigo UNIQUE (codigo);


--
-- TOC entry 4784 (class 2606 OID 16652)
-- Name: usuario usuario_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_pkey PRIMARY KEY (id);


--
-- TOC entry 4786 (class 2606 OID 16661)
-- Name: usuario usuario_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_username_key UNIQUE (username);


--
-- TOC entry 4789 (class 2606 OID 16596)
-- Name: movimiento_inventario movimiento_inventario_producto_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.movimiento_inventario
    ADD CONSTRAINT movimiento_inventario_producto_id_fkey FOREIGN KEY (producto_id) REFERENCES public.producto(id);


--
-- TOC entry 4787 (class 2606 OID 16619)
-- Name: registro registro_habitacion_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.registro
    ADD CONSTRAINT registro_habitacion_id_fkey FOREIGN KEY (habitacion_id) REFERENCES public.habitacion(id);


--
-- TOC entry 4788 (class 2606 OID 16630)
-- Name: registro registro_huesped_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.registro
    ADD CONSTRAINT registro_huesped_id_fkey FOREIGN KEY (huesped_id) REFERENCES public.huesped(id);


-- Completed on 2026-05-18 13:34:43

--
-- PostgreSQL database dump complete
--

\unrestrict gwrl4pIHxZ9afLDvhgpFYwu5scpccNcCrx17mCSMWbp7DuR4HfO2X04qKI61IoW

