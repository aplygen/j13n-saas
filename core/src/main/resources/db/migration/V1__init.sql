DROP SCHEMA IF EXISTS CORE CASCADE;
CREATE SCHEMA IF NOT EXISTS CORE;

CREATE OR REPLACE FUNCTION CORE.CORE_GET_CUSTOM_EPOCH() RETURNS BIGINT AS
$$
BEGIN
    RETURN 1672531200000; -- JANUARY 1, 2023 IN MILLISECONDS SINCE UNIX EPOCH
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION CORE.CORE_UPDATE_TIMESTAMP() RETURNS TRIGGER AS
$$
BEGIN
    NEW.UPDATED_AT = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE PLPGSQL;

CREATE SEQUENCE IF NOT EXISTS CORE.TABLE_ID_SEQ;

-- CREATE ID GENERATION FUNCTION
CREATE OR REPLACE FUNCTION CORE.CORE_NEXT_ID(OUT RESULT BIGINT) AS
$$
DECLARE
    OUR_EPOCH  BIGINT := CORE.CORE_GET_CUSTOM_EPOCH();
    SEQ_ID     BIGINT;
    NOW_MILLIS BIGINT;
    SHARD_ID   INT    := 1; -- USING A FIXED SHARD ID OF 1 FOR NOW
BEGIN
    SELECT NEXTVAL('CORE.TABLE_ID_SEQ') % 1024 INTO SEQ_ID;
    SELECT FLOOR(EXTRACT(EPOCH FROM CLOCK_TIMESTAMP()) * 1000) INTO NOW_MILLIS;
    RESULT := (NOW_MILLIS - OUR_EPOCH) << 23;
    RESULT := RESULT | (SHARD_ID << 10);
    RESULT := RESULT | (SEQ_ID);
END;
$$ LANGUAGE PLPGSQL;

CREATE TYPE CORE_USER_STATUS_CODE AS ENUM ('ACTIVE', 'INACTIVE', 'DELETED', 'LOCKED', 'PASSWORD_EXPIRED');

CREATE TABLE CORE.CORE_USERS
(
    ID                BIGINT                NOT NULL DEFAULT CORE.CORE_NEXT_ID(),
    USER_NAME         CHAR(320) UNIQUE      NOT NULL DEFAULT 'NONE',
    EMAIL_ID          VARCHAR(320) UNIQUE   NOT NULL DEFAULT 'NONE',
    DIAL_CODE         SMALLINT              NOT NULL DEFAULT 91,
    PHONE_NUMBER      CHAR(15),
    FIRST_NAME        VARCHAR(128),
    LAST_NAME         VARCHAR(128),
    MIDDLE_NAME       VARCHAR(128),
    LOCALE_CODE       VARCHAR(10),
    PASSWORD          VARCHAR(512)          NOT NULL,
    PASSWORD_HASHED   BOOLEAN                        DEFAULT TRUE,
    USER_STATUS_CODE  CORE_USER_STATUS_CODE NOT NULL DEFAULT 'ACTIVE',
    NO_FAILED_ATTEMPT SMALLINT                       DEFAULT 0,
    CREATED_AT        TIMESTAMP             NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT        TIMESTAMP             NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CREATED_BY        BIGINT,
    UPDATED_BY        BIGINT,
    PRIMARY KEY (ID)
);

DROP TRIGGER IF EXISTS UPDATE_CORE_USERS_TIMESTAMP ON CORE.CORE_USERS;

CREATE TRIGGER UPDATE_CORE_USERS_TIMESTAMP
    BEFORE UPDATE
    ON CORE.CORE_USERS
    FOR EACH ROW
EXECUTE FUNCTION CORE.CORE_UPDATE_TIMESTAMP();

DROP TABLE IF EXISTS CORE.CORE_AUTHORITIES;

CREATE TABLE CORE.CORE_AUTHORITIES
(
    ID         BIGINT             NOT NULL DEFAULT CORE.CORE_NEXT_ID(),
    NAME       VARCHAR(50) UNIQUE NOT NULL,
    CREATED_AT TIMESTAMP          NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CREATED_BY BIGINT,
    UPDATED_AT TIMESTAMP          NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UPDATED_BY BIGINT,
    PRIMARY KEY (ID)
);

DROP TRIGGER IF EXISTS UPDATE_CORE_AUTHORITIES_TIMESTAMP ON CORE.CORE_AUTHORITIES;

CREATE TRIGGER UPDATE_CORE_AUTHORITIES_TIMESTAMP
    BEFORE UPDATE
    ON CORE.CORE_AUTHORITIES
    FOR EACH ROW
EXECUTE FUNCTION CORE.CORE_UPDATE_TIMESTAMP();

DROP TABLE IF EXISTS CORE.CORE_USER_AUTHORITIES;

CREATE TABLE CORE.CORE_USER_AUTHORITIES
(
    ID           BIGINT    NOT NULL DEFAULT CORE.CORE_NEXT_ID(),
    USER_ID      BIGINT    NOT NULL,
    AUTHORITY_ID BIGINT    NOT NULL,
    CREATED_AT   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CREATED_BY   BIGINT,
    UPDATED_AT   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UPDATED_BY   BIGINT,
    PRIMARY KEY (ID),
    CONSTRAINT FK1_USER_AUTHORITIES_USER_ID_USERS_ID FOREIGN KEY (USER_ID) REFERENCES CORE.CORE_USERS (ID),
    CONSTRAINT FK2_USER_AUTHORITIES_USER_ID_AUTHORITIES_ID FOREIGN KEY (AUTHORITY_ID) REFERENCES CORE.CORE_AUTHORITIES (ID)
);

CREATE UNIQUE INDEX UK1_USER_AUTHORITIES_USER_ID_AUTHORITY_ID ON CORE.CORE_USER_AUTHORITIES (USER_ID, AUTHORITY_ID);

DROP TRIGGER IF EXISTS UPDATE_CORE_USER_AUTHORITIES_TIMESTAMP ON CORE.CORE_USER_AUTHORITIES;

CREATE TRIGGER UPDATE_CORE_USER_AUTHORITIES_TIMESTAMP
    BEFORE UPDATE
    ON CORE.CORE_USER_AUTHORITIES
    FOR EACH ROW
EXECUTE FUNCTION CORE.CORE_UPDATE_TIMESTAMP();

CREATE TYPE CORE_FILE_RESOURCE_TYPE AS ENUM ('STATIC', 'SECURED');
CREATE TYPE CORE_FILE_SYSTEM_TYPE AS ENUM ('FILE', 'DIRECTORY');

CREATE TABLE CORE.CORE_FILE_SYSTEM
(
    ID                 BIGINT                  NOT NULL DEFAULT CORE.CORE_NEXT_ID(),
    FILE_RESOURCE_TYPE CORE_FILE_RESOURCE_TYPE NOT NULL DEFAULT 'STATIC',
    USER_ID            BIGINT                  NOT NULL,
    NAME               VARCHAR(512)            NOT NULL,
    FILE_SYSTEM_TYPE   CORE_FILE_SYSTEM_TYPE   NOT NULL DEFAULT 'FILE',
    SIZE               BIGINT,
    PARENT_ID          BIGINT,
    CREATED_BY         BIGINT,
    CREATED_AT         TIMESTAMP               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UPDATED_BY         BIGINT,
    UPDATED_AT         TIMESTAMP               NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (ID),
    CONSTRAINT FK1_FILE_SYSTEM_PARENT_ID_SELF_REFERENCE FOREIGN KEY (PARENT_ID) REFERENCES CORE.CORE_FILE_SYSTEM (ID) ON DELETE CASCADE,
    CONSTRAINT FK2_FILE_SYSTEM_USER_ID_CORE_USERS_ID FOREIGN KEY (USER_ID) REFERENCES CORE.CORE_USERS (ID)
);

CREATE INDEX IDX1_FILE_SYSTEM_FILE_RESOURCE_TYPE_FILE_SYSTEM_TYPE_PARENT_ID ON CORE.CORE_FILE_SYSTEM (FILE_RESOURCE_TYPE, FILE_SYSTEM_TYPE, PARENT_ID);
CREATE INDEX IDX2_FILE_SYSTEM_USER_ID_FILE_RESOURCE_TYPE_FILE_SYSTEM_TYPE ON CORE.CORE_FILE_SYSTEM (USER_ID, FILE_RESOURCE_TYPE, FILE_SYSTEM_TYPE);
CREATE INDEX IDX3_FILE_SYSTEM_CODE_NAME ON CORE.CORE_FILE_SYSTEM (USER_ID, NAME);
CREATE INDEX IDX4_FILE_SYSTEM_USER_ID ON CORE.CORE_FILE_SYSTEM (USER_ID);

DROP TRIGGER IF EXISTS UPDATE_CORE_FILE_SYSTEM_TIMESTAMP ON CORE.CORE_FILE_SYSTEM;

CREATE TRIGGER UPDATE_CORE_FILE_SYSTEM_TIMESTAMP
    BEFORE UPDATE
    ON CORE.CORE_FILE_SYSTEM
    FOR EACH ROW
EXECUTE FUNCTION CORE.CORE_UPDATE_TIMESTAMP();
