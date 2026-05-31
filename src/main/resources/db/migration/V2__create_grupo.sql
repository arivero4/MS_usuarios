-- ============================================================
-- V2: Creación de tablas GRUPOS, PRIVILEGIOS y relaciones N:M
-- Migración de Flyway - Compatible con Oracle 10g
-- Depende de V1 (tabla USUARIO debe existir)
-- ============================================================

-- ── Secuencias ────────────────────────────────────────────────────────────────

-- Secuencia para IDs de grupos (roles).
CREATE SEQUENCE SEQ_GRUPOS
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- Secuencia para IDs de privilegios (permisos).
CREATE SEQUENCE SEQ_PRIVILEGIOS
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- ── Tabla GRUPOS ──────────────────────────────────────────────────────────────
-- Representa roles del sistema: ADMINISTRADOR, PROPIETARIO, PRODUCTOR, ASISTENTE_TECNICO.
-- Mapeada por: usuarios.infrastructure.adapter.out.persistence.entity.GrupoEntity
-- Modelo de dominio: usuarios.domain.model.Grupo

CREATE TABLE GRUPOS (
    -- Clave primaria generada por SEQ_GRUPOS via trigger.
    ID          NUMBER(19)    NOT NULL,

    -- Nombre único del grupo/rol. Convenio: MAYUSCULAS (ej: ADMINISTRADOR, PRODUCTOR).
    -- Este nombre se incluye como claim "grupos" en el JWT.
    NOMBRE      VARCHAR2(100) NOT NULL,

    -- Descripción del propósito del grupo. Campo opcional.
    DESCRIPCION VARCHAR2(500),

    -- Estado del grupo (mismos valores que USUARIO.ESTADO).
    -- Un grupo INACTIVO no puede asignarse a nuevos usuarios.
    ESTADO      VARCHAR2(20)  NOT NULL,

    CONSTRAINT PK_GRUPOS       PRIMARY KEY (ID),
    CONSTRAINT UK_GRUPOS_NOMBRE UNIQUE (NOMBRE),
    CONSTRAINT CK_GRUPOS_ESTADO CHECK (ESTADO IN ('ACTIVO','INACTIVO','SUSPENDIDO','BLOQUEADO'))
);

-- Trigger para auto-incremento del ID de grupos.
CREATE OR REPLACE TRIGGER TRG_GRUPOS_ID
    BEFORE INSERT ON GRUPOS
    FOR EACH ROW
BEGIN
    IF :NEW.ID IS NULL THEN
        SELECT SEQ_GRUPOS.NEXTVAL INTO :NEW.ID FROM DUAL;
    END IF;
END;
/

-- ── Tabla PRIVILEGIOS ─────────────────────────────────────────────────────────
-- Catálogo de permisos/acciones que pueden asignarse a grupos.
-- Mapeada por: usuarios.infrastructure.adapter.out.persistence.entity.PrivilegioEntity
-- Modelo de dominio: usuarios.domain.model.Privilegio

CREATE TABLE PRIVILEGIOS (
    ID          NUMBER(19)    NOT NULL,

    -- Código único del privilegio. Convenio: RECURSO_ACCION (ej: USR_CREAR, GRP_ADMIN).
    CODIGO      VARCHAR2(50)  NOT NULL,

    -- Nombre legible del privilegio (ej: "Crear Usuario").
    NOMBRE      VARCHAR2(100) NOT NULL,

    -- Descripción del permiso que otorga.
    DESCRIPCION VARCHAR2(500),

    -- Verbo HTTP habilitado: GET, POST, PUT, PATCH, DELETE, ALL.
    ACCION      VARCHAR2(100) NOT NULL,

    -- Ruta del recurso protegido (ej: /api/v1/usuarios).
    RECURSO     VARCHAR2(200) NOT NULL,

    CONSTRAINT PK_PRIVILEGIOS       PRIMARY KEY (ID),
    CONSTRAINT UK_PRIVILEGIOS_CODIGO UNIQUE (CODIGO)
);

-- Trigger para auto-incremento del ID de privilegios.
CREATE OR REPLACE TRIGGER TRG_PRIVILEGIOS_ID
    BEFORE INSERT ON PRIVILEGIOS
    FOR EACH ROW
BEGIN
    IF :NEW.ID IS NULL THEN
        SELECT SEQ_PRIVILEGIOS.NEXTVAL INTO :NEW.ID FROM DUAL;
    END IF;
END;
/

-- ── Tabla GRUPOS_USUARIO (N:M) ────────────────────────────────────────────────
-- Tabla de relación muchos-a-muchos entre usuarios y grupos.
-- Un usuario puede pertenecer a varios grupos; un grupo puede tener varios usuarios.
-- Gestionada directamente por UsuarioRepositoryAdapter (no hay entidad JPA).

CREATE TABLE GRUPOS_USUARIO (
    -- FK al usuario. CASCADE DELETE no está definido (se elimina manualmente en el repo).
    ID_USUARIO  NUMBER(19) NOT NULL,
    -- FK al grupo.
    ID_GRUPO    NUMBER(19) NOT NULL,

    -- Clave primaria compuesta: evita duplicados en la asignación usuario-grupo.
    CONSTRAINT PK_GRUPOS_USUARIO PRIMARY KEY (ID_USUARIO, ID_GRUPO),
    CONSTRAINT FK_GU_USUARIO FOREIGN KEY (ID_USUARIO) REFERENCES USUARIO(ID_USUARIO),
    CONSTRAINT FK_GU_GRUPO   FOREIGN KEY (ID_GRUPO)   REFERENCES GRUPOS(ID)
);

-- ── Tabla PRIVILEGIO_GRUPO (N:M) ──────────────────────────────────────────────
-- Tabla de relación muchos-a-muchos entre grupos y privilegios.
-- Un grupo puede tener varios privilegios; un privilegio puede asignarse a varios grupos.

CREATE TABLE PRIVILEGIO_GRUPO (
    ID_PRIVILEGIO NUMBER(19) NOT NULL,
    ID_GRUPO      NUMBER(19) NOT NULL,

    CONSTRAINT PK_PRIVILEGIO_GRUPO  PRIMARY KEY (ID_PRIVILEGIO, ID_GRUPO),
    CONSTRAINT FK_PG_PRIVILEGIO FOREIGN KEY (ID_PRIVILEGIO) REFERENCES PRIVILEGIOS(ID),
    CONSTRAINT FK_PG_GRUPO      FOREIGN KEY (ID_GRUPO)      REFERENCES GRUPOS(ID)
);

-- ── Datos iniciales del catálogo de privilegios ───────────────────────────────
-- Se insertan en la migración para que estén disponibles desde el primer arranque.
-- Gestionados por PrivilegioRepositoryAdapter (solo lectura en runtime).

INSERT INTO PRIVILEGIOS (CODIGO, NOMBRE, DESCRIPCION, ACCION, RECURSO)
    VALUES ('USR_CREAR',    'Crear Usuario',        'Permite crear nuevos usuarios en el sistema',        'POST',   '/api/v1/usuarios');
INSERT INTO PRIVILEGIOS (CODIGO, NOMBRE, DESCRIPCION, ACCION, RECURSO)
    VALUES ('USR_LEER',     'Leer Usuarios',        'Permite consultar la lista de usuarios',             'GET',    '/api/v1/usuarios');
INSERT INTO PRIVILEGIOS (CODIGO, NOMBRE, DESCRIPCION, ACCION, RECURSO)
    VALUES ('USR_EDITAR',   'Editar Usuario',       'Permite modificar datos de usuarios existentes',     'PUT',    '/api/v1/usuarios');
INSERT INTO PRIVILEGIOS (CODIGO, NOMBRE, DESCRIPCION, ACCION, RECURSO)
    VALUES ('USR_ELIMINAR', 'Eliminar Usuario',     'Permite eliminar usuarios del sistema',              'DELETE', '/api/v1/usuarios');
INSERT INTO PRIVILEGIOS (CODIGO, NOMBRE, DESCRIPCION, ACCION, RECURSO)
    VALUES ('GRP_ADMIN',    'Administrar Grupos',   'Permite gestionar grupos y sus privilegios',         'ALL',    '/api/v1/grupos');

-- ── Grupos iniciales del sistema ──────────────────────────────────────────────
-- Se crean los grupos base del sistema TerraIca.
-- Los demás grupos (PROPIETARIO, PRODUCTOR, ASISTENTE_TECNICO) se crean via API.

INSERT INTO GRUPOS (NOMBRE, DESCRIPCION, ESTADO)
    VALUES ('ADMINISTRADOR',  'Grupo con acceso total al sistema TerraIca', 'ACTIVO');
INSERT INTO GRUPOS (NOMBRE, DESCRIPCION, ESTADO)
    VALUES ('PROPIETARIO',    'Propietario de predios agricolas',            'ACTIVO');
INSERT INTO GRUPOS (NOMBRE, DESCRIPCION, ESTADO)
    VALUES ('PRODUCTOR',      'Productor agricola gestiona lugares y lotes', 'ACTIVO');
INSERT INTO GRUPOS (NOMBRE, DESCRIPCION, ESTADO)
    VALUES ('ASISTENTE_TECNICO', 'Asistente tecnico fitosanitario del ICA', 'ACTIVO');

COMMIT;
