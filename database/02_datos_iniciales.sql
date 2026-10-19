-- 02_datos_iniciales.sql  |  Datos de referencia y de demostración

BEGIN;

INSERT INTO prioridades (nombre, descripcion, nivel, requiere_autorizacion) VALUES
    ('NORMAL',        'Atención por orden de llegada',        0,  FALSE),
    ('PREFERENCIAL',  'Adulto mayor, gestante o movilidad reducida', 10, TRUE),
    ('URGENTE',       'Autorizada por coordinación',          20, TRUE);

INSERT INTO usuarios_internos (nombre_completo, correo, rol) VALUES
    ('Coordinador Demo',   'coordinador@entidad.test', 'COORDINADOR'),
    ('Agente Uno',         'agente1@entidad.test',     'AGENTE'),
    ('Agente Dos',         'agente2@entidad.test',     'AGENTE'),
    ('Administrador Demo', 'admin@entidad.test',       'ADMINISTRADOR');

INSERT INTO servicios (codigo, nombre, descripcion, hora_apertura, hora_cierre) VALUES
    ('ORI', 'Orientación',             'Información general al público',      '08:00', '17:00'),
    ('DOC', 'Recepción de documentos', 'Radicación y entrega de documentos', '08:00', '16:00'),
    ('SOP', 'Soporte',                 'Soporte a usuarios',                  '08:00', '17:00'),
    ('TRI', 'Trámites internos',       'Trámites administrativos',            '09:00', '16:00');

INSERT INTO modulos (codigo, nombre) VALUES
    ('M1', 'Módulo 1'),
    ('M2', 'Módulo 2'),
    ('M3', 'Módulo 3');

-- RN-06: servicios habilitados por módulo
INSERT INTO modulo_servicio (modulo_id, servicio_id)
SELECT m.id, s.id
FROM (VALUES ('M1','ORI'), ('M1','SOP'),
             ('M2','DOC'), ('M2','TRI'),
             ('M3','ORI'), ('M3','DOC'), ('M3','SOP')) AS v(modulo, servicio)
JOIN modulos   m ON m.codigo = v.modulo
JOIN servicios s ON s.codigo = v.servicio;

COMMIT;
