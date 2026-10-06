
-- 01_esquema.sql  |  Esquema de Gestión de turnos y atención al usuario

BEGIN;

--Usuarios internos (agente, coordinador, administrador técnico)

CREATE TABLE usuarios_internos (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre_completo VARCHAR(120) NOT NULL,
    correo          VARCHAR(150) NOT NULL,
    rol             VARCHAR(20)  NOT NULL,
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_usuarios_internos_rol
        CHECK (rol IN ('AGENTE', 'COORDINADOR', 'ADMINISTRADOR'))
);
CREATE UNIQUE INDEX uq_usuarios_internos_correo ON usuarios_internos (lower(correo));

-- Solicitantes (personas que piden turno)

CREATE TABLE solicitantes (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tipo_documento   VARCHAR(15)  NOT NULL,
    numero_documento VARCHAR(30)  NOT NULL,
    nombre_completo  VARCHAR(120) NOT NULL,
    correo           VARCHAR(150),
    telefono         VARCHAR(20),
    fecha_creacion   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_solicitantes_tipo_documento
        CHECK (tipo_documento IN ('CC', 'TI', 'CE', 'PASAPORTE')),
    CONSTRAINT uq_solicitantes_documento UNIQUE (tipo_documento, numero_documento)
);


-- Servicios (orientación, recepción de documentos, soporte, trámites)

CREATE TABLE servicios (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo               VARCHAR(10)  NOT NULL,
    nombre               VARCHAR(100) NOT NULL,
    descripcion          VARCHAR(255),
    activo               BOOLEAN      NOT NULL DEFAULT TRUE,
    hora_apertura        TIME         NOT NULL,
    hora_cierre          TIME         NOT NULL,
    fecha_creacion       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_servicios_codigo UNIQUE (codigo),
    CONSTRAINT uq_servicios_nombre UNIQUE (nombre),
    CONSTRAINT ck_servicios_codigo_formato CHECK (codigo ~ '^[A-Z]{2,10}$'),
    CONSTRAINT ck_servicios_horario CHECK (hora_cierre > hora_apertura)   -- RN-02
);


-- Prioridades autorizables. "nivel" mayor = se atiende primero.

CREATE TABLE prioridades (
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre                VARCHAR(50)  NOT NULL,
    descripcion           VARCHAR(255),
    nivel                 INTEGER      NOT NULL DEFAULT 0,
    requiere_autorizacion BOOLEAN      NOT NULL DEFAULT TRUE,
    activa                BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_prioridades_nombre UNIQUE (nombre),
    CONSTRAINT ck_prioridades_nivel CHECK (nivel >= 0)
);


-- Módulos de atención. El agente solo se asigna mientras está abierto.

CREATE TABLE modulos (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo         VARCHAR(10)  NOT NULL,
    nombre         VARCHAR(100) NOT NULL,
    estado         VARCHAR(10)  NOT NULL DEFAULT 'CERRADO',
    agente_id      BIGINT,
    fecha_creacion TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_modulos_codigo UNIQUE (codigo),
    CONSTRAINT ck_modulos_estado CHECK (estado IN ('ABIERTO', 'PAUSADO', 'CERRADO')),
    CONSTRAINT ck_modulos_agente CHECK (estado = 'CERRADO' OR agente_id IS NOT NULL),
    CONSTRAINT fk_modulos_agente FOREIGN KEY (agente_id)
        REFERENCES usuarios_internos (id)
);
CREATE INDEX ix_modulos_agente ON modulos (agente_id);


-- Servicios habilitados por módulo (N:M)                     RN-06

CREATE TABLE modulo_servicio (
    modulo_id   BIGINT NOT NULL,
    servicio_id BIGINT NOT NULL,
    PRIMARY KEY (modulo_id, servicio_id),
    CONSTRAINT fk_modulo_servicio_modulo FOREIGN KEY (modulo_id)
        REFERENCES modulos (id) ON DELETE CASCADE,
    CONSTRAINT fk_modulo_servicio_servicio FOREIGN KEY (servicio_id)
        REFERENCES servicios (id)
);
CREATE INDEX ix_modulo_servicio_servicio ON modulo_servicio (servicio_id);

-- Secuencia de turnos por servicio y fecha operativa

CREATE TABLE secuencias_turno (
    servicio_id        BIGINT  NOT NULL,
    fecha_operativa    DATE    NOT NULL,
    ultimo_consecutivo INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (servicio_id, fecha_operativa),
    CONSTRAINT fk_secuencias_servicio FOREIGN KEY (servicio_id)
        REFERENCES servicios (id),
    CONSTRAINT ck_secuencias_consecutivo CHECK (ultimo_consecutivo >= 0)
);

-- Turnos. "codigo" = PREFIJO-yyMMdd-consecutivo (ej. ORI-261005-007),

CREATE TABLE turnos (
    id                     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo                 VARCHAR(25) NOT NULL,
    servicio_id            BIGINT      NOT NULL,
    solicitante_id         BIGINT      NOT NULL,
    prioridad_id           BIGINT      NOT NULL,
    autorizado_por_id      BIGINT,
    fecha_operativa        DATE        NOT NULL,
    consecutivo            INTEGER     NOT NULL,
    estado                 VARCHAR(15) NOT NULL DEFAULT 'EN_ESPERA',
    modulo_id              BIGINT,
    agente_id              BIGINT,
    fecha_emision          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_turnos_codigo UNIQUE (codigo),
    CONSTRAINT uq_turnos_secuencia UNIQUE (servicio_id, fecha_operativa, consecutivo),   -- RN-01
    CONSTRAINT ck_turnos_consecutivo CHECK (consecutivo > 0),
    CONSTRAINT ck_turnos_estado CHECK (estado IN                                         -- RN-04
        ('EN_ESPERA', 'LLAMADO', 'EN_ATENCION', 'FINALIZADO', 'CANCELADO', 'NO_PRESENTE')),
    CONSTRAINT ck_turnos_asignacion CHECK (                                              -- RN-09
        estado NOT IN ('LLAMADO', 'EN_ATENCION')
        OR (modulo_id IS NOT NULL AND agente_id IS NOT NULL)),
    CONSTRAINT fk_turnos_servicio     FOREIGN KEY (servicio_id)       REFERENCES servicios (id),
    CONSTRAINT fk_turnos_solicitante  FOREIGN KEY (solicitante_id)    REFERENCES solicitantes (id),
    CONSTRAINT fk_turnos_prioridad    FOREIGN KEY (prioridad_id)      REFERENCES prioridades (id),
    CONSTRAINT fk_turnos_autorizado   FOREIGN KEY (autorizado_por_id) REFERENCES usuarios_internos (id),
    CONSTRAINT fk_turnos_modulo       FOREIGN KEY (modulo_id)         REFERENCES modulos (id),
    CONSTRAINT fk_turnos_agente       FOREIGN KEY (agente_id)         REFERENCES usuarios_internos (id)
);

-- RN-03: un solicitante no tiene dos turnos activos para el mismo servicio
CREATE UNIQUE INDEX uq_turnos_activo_solicitante_servicio
    ON turnos (solicitante_id, servicio_id)
    WHERE estado IN ('EN_ESPERA', 'LLAMADO', 'EN_ATENCION');

-- RN-07: un módulo no tiene dos atenciones activas a la vez
CREATE UNIQUE INDEX uq_turnos_activo_modulo
    ON turnos (modulo_id)
    WHERE estado IN ('LLAMADO', 'EN_ATENCION');

-- RN-08: selección del siguiente turno (prioridad y orden de emisión)
CREATE INDEX ix_turnos_fila
    ON turnos (servicio_id, fecha_operativa, prioridad_id, fecha_emision)
    WHERE estado = 'EN_ESPERA';

CREATE INDEX ix_turnos_solicitante ON turnos (solicitante_id);
CREATE INDEX ix_turnos_prioridad   ON turnos (prioridad_id);
CREATE INDEX ix_turnos_fecha_estado ON turnos (fecha_operativa, estado);

-- 9. Historial de turnos (solo se inserta, nunca se edita)  RN-09, RN-12

CREATE TABLE historial_turnos (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    turno_id        BIGINT       NOT NULL,
    estado_anterior VARCHAR(15),
    estado_nuevo    VARCHAR(15)  NOT NULL,
    fecha_evento    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modulo_id       BIGINT,
    usuario_id      BIGINT,
    observacion     VARCHAR(255),
    CONSTRAINT ck_historial_estado_anterior CHECK (estado_anterior IS NULL OR estado_anterior IN
        ('EN_ESPERA', 'LLAMADO', 'EN_ATENCION', 'FINALIZADO', 'CANCELADO', 'NO_PRESENTE')),
    CONSTRAINT ck_historial_estado_nuevo CHECK (estado_nuevo IN
        ('EN_ESPERA', 'LLAMADO', 'EN_ATENCION', 'FINALIZADO', 'CANCELADO', 'NO_PRESENTE')),
    CONSTRAINT fk_historial_turno   FOREIGN KEY (turno_id)   REFERENCES turnos (id),
    CONSTRAINT fk_historial_modulo  FOREIGN KEY (modulo_id)  REFERENCES modulos (id),
    CONSTRAINT fk_historial_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios_internos (id)
);
CREATE INDEX ix_historial_turno  ON historial_turnos (turno_id, fecha_evento);
CREATE INDEX ix_historial_estado ON historial_turnos (estado_nuevo, fecha_evento);


-- Vista de tiempos calculados desde el historial               RN-12

CREATE VIEW v_turnos_tiempos AS
SELECT t.id                AS turno_id,
       t.servicio_id,
       t.fecha_operativa,
       t.estado,
       min(h.fecha_evento) FILTER (WHERE h.estado_nuevo = 'EN_ESPERA')   AS emitido_en,
       min(h.fecha_evento) FILTER (WHERE h.estado_nuevo = 'LLAMADO')     AS llamado_en,
       min(h.fecha_evento) FILTER (WHERE h.estado_nuevo = 'EN_ATENCION') AS inicio_atencion_en,
       min(h.fecha_evento) FILTER (WHERE h.estado_nuevo = 'FINALIZADO')  AS fin_atencion_en,
       min(h.fecha_evento) FILTER (WHERE h.estado_nuevo = 'LLAMADO')
         - min(h.fecha_evento) FILTER (WHERE h.estado_nuevo = 'EN_ESPERA')   AS tiempo_espera,
       min(h.fecha_evento) FILTER (WHERE h.estado_nuevo = 'FINALIZADO')
         - min(h.fecha_evento) FILTER (WHERE h.estado_nuevo = 'EN_ATENCION') AS tiempo_atencion
FROM turnos t
LEFT JOIN historial_turnos h ON h.turno_id = t.id
GROUP BY t.id, t.servicio_id, t.fecha_operativa, t.estado;

COMMIT;
