-- Batch meta tables
CREATE TABLE BATCH_JOB_INSTANCE  (
   JOB_INSTANCE_ID NUMBER(19,0)  NOT NULL PRIMARY KEY ,
   VERSION NUMBER(19,0) ,
   JOB_NAME VARCHAR2(100) NOT NULL,
   JOB_KEY VARCHAR2(32) NOT NULL,
   constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
) ;

CREATE TABLE BATCH_JOB_EXECUTION  (
   JOB_EXECUTION_ID NUMBER(19,0)  NOT NULL PRIMARY KEY ,
   VERSION NUMBER(19,0)  ,
   JOB_INSTANCE_ID NUMBER(19,0) NOT NULL,
   CREATE_TIME TIMESTAMP NOT NULL,
   START_TIME TIMESTAMP DEFAULT NULL ,
   END_TIME TIMESTAMP DEFAULT NULL ,
   STATUS VARCHAR2(10) ,
   EXIT_CODE VARCHAR2(2500) ,
   EXIT_MESSAGE VARCHAR2(2500) ,
   LAST_UPDATED TIMESTAMP,
   JOB_CONFIGURATION_LOCATION VARCHAR(2500) NULL,
   constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
   references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) ;

CREATE TABLE BATCH_JOB_EXECUTION_PARAMS  (
   JOB_EXECUTION_ID NUMBER(19,0) NOT NULL ,
   TYPE_CD VARCHAR2(6) NOT NULL ,
   KEY_NAME VARCHAR2(100) NOT NULL ,
   STRING_VAL VARCHAR2(250) ,
   DATE_VAL TIMESTAMP DEFAULT NULL ,
   LONG_VAL NUMBER(19,0) ,
   DOUBLE_VAL NUMBER ,
   IDENTIFYING CHAR(1) NOT NULL ,
   constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
   references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;

CREATE TABLE BATCH_STEP_EXECUTION  (
   STEP_EXECUTION_ID NUMBER(19,0)  NOT NULL PRIMARY KEY ,
   VERSION NUMBER(19,0) NOT NULL,
   STEP_NAME VARCHAR2(100) NOT NULL,
   JOB_EXECUTION_ID NUMBER(19,0) NOT NULL,
   START_TIME TIMESTAMP NOT NULL ,
   END_TIME TIMESTAMP DEFAULT NULL ,
   STATUS VARCHAR2(10) ,
   COMMIT_COUNT NUMBER(19,0) ,
   READ_COUNT NUMBER(19,0) ,
   FILTER_COUNT NUMBER(19,0) ,
   WRITE_COUNT NUMBER(19,0) ,
   READ_SKIP_COUNT NUMBER(19,0) ,
   WRITE_SKIP_COUNT NUMBER(19,0) ,
   PROCESS_SKIP_COUNT NUMBER(19,0) ,
   ROLLBACK_COUNT NUMBER(19,0) ,
   EXIT_CODE VARCHAR2(2500) ,
   EXIT_MESSAGE VARCHAR2(2500) ,
   LAST_UPDATED TIMESTAMP,
   constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID)
   references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;

CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT  (
   STEP_EXECUTION_ID NUMBER(19,0) NOT NULL PRIMARY KEY,
   SHORT_CONTEXT VARCHAR2(2500) NOT NULL,
   SERIALIZED_CONTEXT CLOB ,
   constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID)
   references BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
) ;

CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT  (
   JOB_EXECUTION_ID NUMBER(19,0) NOT NULL PRIMARY KEY,
   SHORT_CONTEXT VARCHAR2(2500) NOT NULL,
   SERIALIZED_CONTEXT CLOB ,
   constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
   references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;

CREATE SEQUENCE BATCH_STEP_EXECUTION_SEQ START WITH 0 MINVALUE 0 MAXVALUE 9223372036854775807 NOCYCLE;
CREATE SEQUENCE BATCH_JOB_EXECUTION_SEQ START WITH 0 MINVALUE 0 MAXVALUE 9223372036854775807 NOCYCLE;
CREATE SEQUENCE BATCH_JOB_SEQ START WITH 0 MINVALUE 0 MAXVALUE 9223372036854775807 NOCYCLE;

--LOL Sequences
CREATE SEQUENCE SEQ_LOL2DATA;
--LOL Tables
CREATE TABLE TBLLOL2DATA_1 (
   cs_id NUMBER(19,0) DEFAULT SEQ_LOL2DATA.nextval NOT NULL PRIMARY KEY,
   mappingid NUMBER(19,0) NOT NULL,
   stichtag TIMESTAMP NOT NULL,
   importdatum TIMESTAMP NOT NULL,
   mitgliedsnummer VARCHAR2(1024) NOT NULL,
   mandant VARCHAR2(512),
   buchungskreis VARCHAR2(512),
   bobiknummer VARCHAR2(1024) NOT NULL,
   debitornummer VARCHAR2(1024),
   name_1 VARCHAR2(2048),
   name_2 VARCHAR2(2048),
   name_3 VARCHAR2(2048),
   name_4 VARCHAR2(2048),
   plz VARCHAR2(256),
   ort VARCHAR2(256),
   land VARCHAR2(256),
   inso VARCHAR2(256),
   lolIndex NUMBER(19,0),
   branchenBezeichnung VARCHAR2(1024),
   branchenCode VARCHAR2(256),
   branchenArt VARCHAR2(256),
   branchenLand VARCHAR2(256),
   anzahlBelLiefer_2 NUMBER(19,0),
   anzahlBelPoolOhneLiefer_2 NUMBER(19,0),
   anzahlBelLiefer_4 NUMBER(19,0),
   anzahlBelPoolOhneLiefer_4 NUMBER(19,0),
   anzahlBelLiefer_8 NUMBER(19,0),
   anzahlBelPoolOhneLiefer_8 NUMBER(19,0),
   anzahlBelLiefer_12 NUMBER(19,0),
   anzahlBelPoolOhneLiefer_12 NUMBER(19,0),
   anzahlBelBranche NUMBER(19,0),
   anzahlCrefosBranche NUMBER(19,0),
   betragLiefer_2 NUMBER(19,3),
   betragLiefer_2_w VARCHAR2(128),
   betragPoolOhneLiefer_2 NUMBER(19, 3),
   betragPoolOhneLiefer_2_w VARCHAR2(128),
   betragLiefer_4 NUMBER(19,3),
   betragLiefer_4_w VARCHAR2(128),
   betragPoolOhneLiefer_4 NUMBER(19, 3),
   betragPoolOhneLiefer_4_w VARCHAR2(128),
   betragLiefer_8 NUMBER(19,3),
   betragLiefer_8_w VARCHAR2(128),
   betragPoolOhneLiefer_8 NUMBER(19,3),
   betragPoolOhneLiefer_8_w VARCHAR2(128),
   betragLiefer_12 NUMBER(19,3),
   betragLiefer_12_w VARCHAR2(128),
   betragPoolOhneLiefer_12 NUMBER(19,3),
   betragPoolOhneLiefer_12_w VARCHAR2(128),
   betragsvolumenOP NUMBER(19,3),
   betragsvolumenOP_w VARCHAR2(128),
   betragBranche NUMBER(19,3),
   betragBranche_w VARCHAR2(128),
   tageSollLiefer_2 NUMBER(19,0),
   tageSollPoolOhneLiefer_2 NUMBER(19,0),
   tageIstLiefer_2 NUMBER(19,0),
   tageIstPoolOhneLiefer_2 NUMBER(19,0),
   tageDiffLiefer_2 NUMBER(19,0),
   tageDiffPoolOhneLiefer_2 NUMBER(19,0),
   tageSollLiefer_4 NUMBER(19,0),
   tageSollPoolOhneLiefer_4 NUMBER(19,0),
   tageIstLiefer_4 NUMBER(19,0),
   tageIstPoolOhneLiefer_4 NUMBER(19,0),
   tageDiffLiefer_4 NUMBER(19,0),
   tageDiffPoolOhneLiefer_4 NUMBER(19,0),
   tageSollLiefer_8 NUMBER(19,0),
   tageSollPoolOhneLiefer_8 NUMBER(19,0),
   tageIstLiefer_8 NUMBER(19,0),
   tageIstPoolOhneLiefer_8 NUMBER(19,0),
   tageDiffLiefer_8 NUMBER(19,0),
   tageDiffPoolOhneLiefer_8 NUMBER(19,0),
   tageSollLiefer_12 NUMBER(19,0),
   tageSollPoolOhneLiefer_12 NUMBER(19,0),
   tageIstLiefer_12 NUMBER(19,0),
   tageIstPoolOhneLiefer_12 NUMBER(19,0),
   tageDiffLiefer_12 NUMBER(19,0),
   tageDiffPoolOhneLiefer_12 NUMBER(19,0),
   tageSollBranche NUMBER(19,0),
   tageIstBranche NUMBER(19,0),
   tageDiffBranche NUMBER(19,0),
   filename VARCHAR2(1024) NOT NULL,
   businesscontact NUMBER(1) DEFAULT 0 NOT NULL
);
COMMIT;

CREATE INDEX TBLLOL2DATA_1_MAP ON TBLLOL2DATA_1 (mappingid);
CREATE INDEX TBLLOL2DATA_1_STT ON TBLLOL2DATA_1 (stichtag);
CREATE INDEX TBLLOL2DATA_1_CRN ON TBLLOL2DATA_1 (bobiknummer);
CREATE INDEX TBLLOL2DATA_1_DRN ON TBLLOL2DATA_1 (debitornummer);
CREATE INDEX TBLLOL2DATA_1_IND ON TBLLOL2DATA_1 (lolindex);
CREATE INDEX TBLLOL2DATA_1_FLN ON TBLLOL2DATA_1 (filename);
CREATE BITMAP INDEX TBLLOL2DATA_1_BC ON TBLLOL2DATA_1 (businesscontact);
CREATE INDEX TBLLOL2DATA_1_GRP ON TBLLOL2DATA_1 (mappingid,bobiknummer,TRUNC(STICHTAG, 'MM'));
COMMIT;

CREATE TABLE TBLLOL2DATA_2 AS (SELECT * FROM TBLLOL2DATA_1);
ALTER TABLE TBLLOL2DATA_2 MODIFY CS_ID DEFAULT SEQ_LOL2DATA.nextval PRIMARY KEY;
COMMIT;

CREATE INDEX TBLLOL2DATA_2_MAP ON TBLLOL2DATA_2 (mappingid);
CREATE INDEX TBLLOL2DATA_2_STT ON TBLLOL2DATA_2 (stichtag);
CREATE INDEX TBLLOL2DATA_2_CRN ON TBLLOL2DATA_2 (bobiknummer);
CREATE INDEX TBLLOL2DATA_2_DRN ON TBLLOL2DATA_2 (debitornummer);
CREATE INDEX TBLLOL2DATA_2_IND ON TBLLOL2DATA_2 (lolindex);
CREATE INDEX TBLLOL2DATA_2_FLN ON TBLLOL2DATA_2 (filename);
CREATE BITMAP INDEX TBLLOL2DATA_2_BC ON TBLLOL2DATA_2 (businesscontact);
CREATE INDEX TBLLOL2DATA_2_GRP ON TBLLOL2DATA_2 (mappingid,bobiknummer,TRUNC(STICHTAG, 'MM'));
COMMIT;

CREATE OR REPLACE SYNONYM TBLLOL2DATA FOR TBLLOL2DATA_1;
COMMIT;

CREATE MATERIALIZED VIEW VIEW_LOL2_TRENDANALYSE_1
PARALLEL 12
REFRESH FORCE ON DEMAND WITH PRIMARY KEY
USING ENFORCED CONSTRAINTS ENABLE QUERY REWRITE AS (
    SELECT
        m.cs_id,
        m.stichtag,
        m.mappingid,
        m.bobiknummer,
        nvl2(nullif(average, 0), round(m.lolindex - average, 2), 0) as trendanalyse
    FROM
        TBLLOL2DATA_1 M
        LEFT JOIN (
            SELECT   av.mappingid,
                     av.bobiknummer,
                     Avg(av.lolindex) as average,
                     crt.som as stichtag
            FROM
                TBLLOL2DATA_1 av,
                 (
                  SELECT   sq.mappingid,
                           sq.bobiknummer,
                           trunc(sq.stichtag, 'MM') as som
                  FROM     TBLLOL2DATA_1 sq
                  GROUP BY sq.mappingid,
                           sq.bobiknummer,
                           trunc(sq.stichtag, 'MM')) crt
            WHERE    av.mappingid = crt.mappingid
            AND      av.bobiknummer = crt.bobiknummer
            AND      av.stichtag >= add_months(crt.som, -3)
            AND      av.stichtag < crt.som
            AND      av.lolindex >= 100 and av.lolindex <= 600
            GROUP BY av.mappingid,
                     av.bobiknummer,
                     crt.som
                     ) SS
    ON  ss.mappingid = m.mappingid
    AND    ss.bobiknummer = M.bobiknummer
    AND    TRUNC(m.stichtag, 'MM') = ss.stichtag);
COMMIT;
CREATE UNIQUE INDEX VIEW_TREND_1_CID ON VIEW_LOL2_TRENDANALYSE_1 (cs_id);
CREATE INDEX VIEW_TREND_1_STG ON VIEW_LOL2_TRENDANALYSE_1 (stichtag);
CREATE INDEX VIEW_TREND_1_CRNR ON VIEW_LOL2_TRENDANALYSE_1 (bobiknummer);
CREATE INDEX VIEW_TREND_1_MAP ON VIEW_LOL2_TRENDANALYSE_1 (mappingid);
CREATE INDEX VIEW_TREND_1_GRP ON VIEW_LOL2_TRENDANALYSE_1 (mappingid,bobiknummer,stichtag);
COMMIT;

CREATE MATERIALIZED VIEW VIEW_LOL2_TRENDANALYSE_2
PARALLEL 12
REFRESH FORCE ON DEMAND WITH PRIMARY KEY
USING ENFORCED CONSTRAINTS ENABLE QUERY REWRITE AS (
    SELECT
        m.cs_id,
        m.stichtag,
        m.mappingid,
        m.bobiknummer,
        nvl2(nullif(average, 0), round(m.lolindex - average, 2), 0) as trendanalyse
    FROM
        TBLLOL2DATA_2 M
        LEFT JOIN (
            SELECT   av.mappingid,
                     av.bobiknummer,
                     Avg(av.lolindex) as average,
                     crt.som as stichtag
            FROM
                TBLLOL2DATA_2 av,
                 (
                  SELECT   sq.mappingid,
                           sq.bobiknummer,
                           trunc(sq.stichtag, 'MM') as som
                  FROM     TBLLOL2DATA_2 sq
                  GROUP BY sq.mappingid,
                           sq.bobiknummer,
                           trunc(sq.stichtag, 'MM')) crt
            WHERE    av.mappingid = crt.mappingid
            AND      av.bobiknummer = crt.bobiknummer
            AND      av.stichtag >= add_months(crt.som, -3)
            AND      av.stichtag < crt.som
            AND      av.lolindex >= 100 and av.lolindex <= 600
            GROUP BY av.mappingid,
                     av.bobiknummer,
                     crt.som
                     ) SS
    ON  ss.mappingid = m.mappingid
    AND    ss.bobiknummer = M.bobiknummer
    AND    TRUNC(m.stichtag, 'MM') = ss.stichtag);
COMMIT;
CREATE UNIQUE INDEX VIEW_TREND_2_CID ON VIEW_LOL2_TRENDANALYSE_2 (cs_id);
CREATE INDEX VIEW_TREND_2_STG ON VIEW_LOL2_TRENDANALYSE_2 (stichtag);
CREATE INDEX VIEW_TREND_2_CRNR ON VIEW_LOL2_TRENDANALYSE_2 (bobiknummer);
CREATE INDEX VIEW_TREND_2_MAP ON VIEW_LOL2_TRENDANALYSE_2 (mappingid);
CREATE INDEX VIEW_TREND_2_GRP ON VIEW_LOL2_TRENDANALYSE_2 (mappingid,bobiknummer,stichtag);
COMMIT;

CREATE OR REPLACE SYNONYM VIEW_LOL2_TRENDANALYSE FOR VIEW_LOL2_TRENDANALYSE_1;

COMMIT;
