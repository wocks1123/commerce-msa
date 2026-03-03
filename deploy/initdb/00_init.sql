-- =============================================================
-- commerce-msa PostgreSQL 초기화 스크립트
-- docker-entrypoint-initdb.d 에 의해 컨테이너 최초 기동 시 실행됨
-- (볼륨이 비어있을 때만 실행 → 재실행하려면 docker compose down -v)
-- =============================================================

-- -------------------------------------------------------------
-- Schema 생성
-- -------------------------------------------------------------
CREATE SCHEMA IF NOT EXISTS "order";
CREATE SCHEMA IF NOT EXISTS product;
CREATE SCHEMA IF NOT EXISTS inventory;
CREATE SCHEMA IF NOT EXISTS payment;
