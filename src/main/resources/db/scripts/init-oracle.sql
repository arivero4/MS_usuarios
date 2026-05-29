-- ============================================================
-- Script de inicialización para Oracle 10g
-- Ejecutar como DBA (SYS o SYSTEM) antes de arrancar la app
-- ============================================================

-- Crear tablespace para el microservicio
CREATE TABLESPACE TS_MS_USUARIOS
    DATAFILE 'ts_ms_usuarios.dbf' SIZE 50M
    AUTOEXTEND ON NEXT 10M MAXSIZE 500M;

-- Crear usuario de base de datos
CREATE USER ms_usuarios IDENTIFIED BY ms_usuarios_pass
    DEFAULT TABLESPACE TS_MS_USUARIOS
    TEMPORARY TABLESPACE TEMP
    QUOTA UNLIMITED ON TS_MS_USUARIOS;

-- Permisos mínimos necesarios
GRANT CREATE SESSION     TO ms_usuarios;
GRANT CREATE TABLE       TO ms_usuarios;
GRANT CREATE SEQUENCE    TO ms_usuarios;
GRANT CREATE TRIGGER     TO ms_usuarios;
GRANT CREATE VIEW        TO ms_usuarios;
GRANT CREATE INDEX       TO ms_usuarios;

-- Verificar conexión
-- CONNECT ms_usuarios/ms_usuarios_pass@//localhost:1521/ORCL
