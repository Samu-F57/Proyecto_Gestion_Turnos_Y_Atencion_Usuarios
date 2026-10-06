-- =====================================================================
-- 00_crear_base.sql  |  Crear la base de datos (ejecutar como superusuario)
-- Uso:  psql -U postgres -f database/00_crear_base.sql
-- =====================================================================
-- Nombre en minúsculas: en PostgreSQL un nombre sin comillas se pliega a
-- minúsculas, así "GestionTurnos" no coincidiría con la URL JDBC.
CREATE DATABASE gestion_turnos_db
    ENCODING 'UTF8'
    TEMPLATE template0;

-- Verificar la conexión
\l
