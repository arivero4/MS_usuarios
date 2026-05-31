-- ============================================================
-- V1: Creación de la tabla USUARIO y su secuencia
-- Migración de Flyway - Compatible con Oracle 10g
-- Nota: Oracle 10g no soporta columnas IDENTITY (se usa secuencia + trigger)
-- ============================================================

-- Secuencia para generar IDs únicos incrementales.
-- NOCACHE: no pre-genera valores (evita gaps en el ID cuando el servidor reinicia).
-- NOCYCLE: no reinicia al llegar al máximo (evita duplicados).
CREATE SEQUENCE SEQ_USUARIO
    START WITH 100          -- Empieza en 100 para dejar espacio a datos de seeding manual
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- Tabla principal de usuarios del sistema TerraIca.
-- Equivale al modelo de dominio: usuarios.domain.model.Usuario
-- Mapeada por: usuarios.infrastructure.adapter.out.persistence.entity.UsuarioEntity
CREATE TABLE USUARIO (
    -- Clave primaria. Asignada por SEQ_USUARIO via trigger TRG_USUARIO_ID.
    ID_USUARIO               NUMBER(19)    NOT NULL,

    -- Cédula o NIT del usuario. UNIQUE garantiza que no haya duplicados.
    -- Mapeado al campo numeroIdentificacion del dominio.
    NUMERO_IDENTIFICACION    VARCHAR2(30)  NOT NULL,

    -- Nombre completo del usuario (nombre + apellidos).
    NOMBRE                   VARCHAR2(100) NOT NULL,

    -- Correo electrónico. Actúa como nombre de usuario para el login.
    -- Debe ser único (UK_USUARIO_CORREO).
    CORREO                   VARCHAR2(100) NOT NULL,

    -- Teléfono de contacto. Campo opcional (NULL permitido).
    TELEFONO                 VARCHAR2(20),

    -- Hash BCrypt de 60 caracteres de la contraseña del usuario.
    -- Nunca se almacena la contraseña en texto plano.
    PASSWORD                 VARCHAR2(255) NOT NULL,

    -- Número de tarjeta profesional del ICA. Opcional.
    -- Usado principalmente por Asistentes Técnicos Fitosanitarios.
    NUM_TARJETA_PROFESIONAL  VARCHAR2(50),

    -- Estado del ciclo de vida del usuario. CHECK constraint garantiza integridad.
    -- Valores: ACTIVO (operativo) | INACTIVO (dado de baja) |
    --          SUSPENDIDO (bloqueado temporal) | BLOQUEADO (bloqueado permanente)
    ESTADO                   VARCHAR2(30)  NOT NULL,

    -- Timestamp de creación del registro. Asignado por UsuarioService.crearUsuario().
    FECHA_CREACION           DATE          NOT NULL,

    -- Timestamp de la última modificación. Actualizado en cada operación de escritura.
    FECHA_ACTUALIZACION      DATE,

    -- Restricciones de integridad:
    CONSTRAINT PK_USUARIO        PRIMARY KEY (ID_USUARIO),
    CONSTRAINT UK_USUARIO_CORREO UNIQUE (CORREO),
    CONSTRAINT UK_USUARIO_NUM_ID UNIQUE (NUMERO_IDENTIFICACION),
    CONSTRAINT CK_USUARIO_ESTADO CHECK (ESTADO IN ('ACTIVO','INACTIVO','SUSPENDIDO','BLOQUEADO'))
);

-- Trigger que asigna automáticamente el ID desde la secuencia antes de cada INSERT.
-- Oracle 10g no tiene columnas IDENTITY, por eso se usa este patrón secuencia+trigger.
-- El IF :NEW.ID_USUARIO IS NULL permite insertar IDs explícitos si se necesita.
CREATE OR REPLACE TRIGGER TRG_USUARIO_ID
    BEFORE INSERT ON USUARIO
    FOR EACH ROW
BEGIN
    IF :NEW.ID_USUARIO IS NULL THEN
        SELECT SEQ_USUARIO.NEXTVAL INTO :NEW.ID_USUARIO FROM DUAL;
    END IF;
END;
/

-- Índices para acelerar las búsquedas más frecuentes:
-- El repositorio UsuarioRepositoryAdapter busca por CORREO (login, JwtAuthFilter)
-- y por ESTADO (listados filtrados).
CREATE INDEX IDX_USUARIO_CORREO ON USUARIO(CORREO);
CREATE INDEX IDX_USUARIO_ESTADO ON USUARIO(ESTADO);
