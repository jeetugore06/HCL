-- =============================================================================
--  Z Bank :: Credit Card Application :: MySQL DDL  (single shared database)
--  Target: MySQL 8.x, InnoDB
--
--  Architecture decisions baked into this schema:
--    * SINGLE shared database for all 4 services (creditcards_db).
--    * Customer is "earned" — `customer` row is created ONLY when an
--      application is APPROVED by card-service. A REJECTED application
--      never produces a customer.
--    * registration-service owns `credit_card_application`. It collects
--      the form data; the form fields live here until approval.
--    * scoring-service owns `credit_score`. Keyed by application_reference,
--      because the customer doesn't exist yet at scoring time.
--    * card-service owns `customer`, `credit_card`, `card_audit_log`.
--      On approval it copies the form data from `credit_card_application`
--      into `customer`, then issues the card.
-- =============================================================================

CREATE DATABASE IF NOT EXISTS creditcards_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE creditcards_db;

-- Drop in dependency-safe order (helpful if re-running for a fresh demo) ------
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS card_audit_log;
DROP TABLE IF EXISTS credit_card;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS credit_score;
DROP TABLE IF EXISTS credit_card_application;
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- credit_card_application   (owned by registration-service)
--   * Form data lands here directly from POST /apply.
--   * status state machine: SUBMITTED -> PROCESSING -> APPROVED | REJECTED
--   * `credit_score` column is populated by registration-service after
--     calling scoring-service via REST.
-- =============================================================================
CREATE TABLE `credit_card_application` (
  `id`                    BIGINT          AUTO_INCREMENT NOT NULL,
  `application_reference` CHAR(36)        NOT NULL,
  `first_name`            VARCHAR(50)     NOT NULL,
  `last_name`             VARCHAR(50)     NOT NULL,
  `email`                 VARCHAR(120)    NOT NULL,
  `phone`                 VARCHAR(20)     NOT NULL,
  `date_of_birth`         DATE            NOT NULL,
  `annual_salary`         DECIMAL(15,2)   NOT NULL,
  `employer_name`         VARCHAR(120)    NULL,
  `employment_type`       VARCHAR(20)     NOT NULL,
  `document_type`         VARCHAR(20)     NOT NULL,
  `document_id`           VARCHAR(40)     NOT NULL,
  `credit_score`          INT             NULL,
  `status`                VARCHAR(30)     NOT NULL,
  `decision_reason`       VARCHAR(255)    NULL,
  `created_at`            DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at`            DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                          ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  CONSTRAINT `uk_application_reference` UNIQUE (`application_reference`),
  CONSTRAINT `uk_application_document`  UNIQUE (`document_type`, `document_id`)
) ENGINE = InnoDB;

CREATE INDEX `idx_application_status_created`
  ON `credit_card_application` (`status` ASC, `created_at` ASC);

-- =============================================================================
-- credit_score   (owned by scoring-service)
--   * One score per application attempt — keyed by application_reference,
--     because a customer record does NOT exist at scoring time.
-- =============================================================================
CREATE TABLE `credit_score` (
  `id`                    BIGINT          AUTO_INCREMENT NOT NULL,
  `application_reference` CHAR(36)        NOT NULL,
  `score`                 INT             NOT NULL,
  `created_at`            DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at`            DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                          ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  CONSTRAINT `uk_credit_score_application_ref` UNIQUE (`application_reference`)
) ENGINE = InnoDB;

-- =============================================================================
-- customer   (owned by card-service)
--   * Created ONLY when card-service approves an application.
--   * Form data is copied from credit_card_application into this row at
--     approval time, so the customer record is self-sufficient afterward.
-- =============================================================================
CREATE TABLE `customer` (
  `id`                    BIGINT          AUTO_INCREMENT NOT NULL,
  `customer_reference`    CHAR(36)        NOT NULL,
  `application_reference` CHAR(36)        NOT NULL,
  `first_name`            VARCHAR(50)     NOT NULL,
  `last_name`             VARCHAR(50)     NOT NULL,
  `email`                 VARCHAR(120)    NOT NULL,
  `phone`                 VARCHAR(20)     NOT NULL,
  `date_of_birth`         DATE            NOT NULL,
  `annual_salary`         DECIMAL(15,2)   NOT NULL,
  `employer_name`         VARCHAR(120)    NULL,
  `employment_type`       VARCHAR(20)     NOT NULL,
  `document_type`         VARCHAR(20)     NOT NULL,
  `document_id`           VARCHAR(40)     NOT NULL,
  `created_at`            DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at`            DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                          ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  CONSTRAINT `uk_customer_reference`     UNIQUE (`customer_reference`),
  CONSTRAINT `uk_customer_application`   UNIQUE (`application_reference`),
  CONSTRAINT `uk_customer_email`         UNIQUE (`email`),
  CONSTRAINT `uk_customer_document`      UNIQUE (`document_type`, `document_id`),
  CONSTRAINT `fk_customer_application`
    FOREIGN KEY (`application_reference`)
    REFERENCES `credit_card_application` (`application_reference`)
    ON DELETE RESTRICT ON UPDATE NO ACTION
) ENGINE = InnoDB;

-- =============================================================================
-- credit_card   (owned by card-service)
--   * One card per APPROVED application (uk_credit_card_application).
--   * `version` supports JPA @Version optimistic locking on PIN updates.
--   * `pin_hash` stores BCrypt-hashed PIN (60 chars).
-- =============================================================================
CREATE TABLE `credit_card` (
  `id`              BIGINT          AUTO_INCREMENT NOT NULL,
  `card_number`     CHAR(16)        NOT NULL,
  `customer_id`     BIGINT          NOT NULL,
  `application_id`  BIGINT          NOT NULL,
  `card_type`       VARCHAR(20)     NOT NULL,
  `credit_limit`    DECIMAL(19,4)   NOT NULL,
  `pin_hash`        VARCHAR(60)     NOT NULL,
  `pin_status`      VARCHAR(20)     NOT NULL,
  `version`         BIGINT          NOT NULL DEFAULT 0,
  `created_at`      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at`      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                    ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  CONSTRAINT `uk_credit_card_number`      UNIQUE (`card_number`),
  CONSTRAINT `uk_credit_card_application` UNIQUE (`application_id`),
  CONSTRAINT `fk_credit_card_customer`
    FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`)
    ON DELETE RESTRICT ON UPDATE NO ACTION,
  CONSTRAINT `fk_credit_card_application`
    FOREIGN KEY (`application_id`) REFERENCES `credit_card_application` (`id`)
    ON DELETE RESTRICT ON UPDATE NO ACTION
) ENGINE = InnoDB;

CREATE INDEX `idx_credit_card_customer`
  ON `credit_card` (`customer_id` ASC);

-- =============================================================================
-- card_audit_log   (owned by card-service)
--   * Append-only audit ledger.
--   * event_type: CARD_ISSUED, PIN_GENERATED, PIN_CHANGED.
-- =============================================================================
CREATE TABLE `card_audit_log` (
  `id`            BIGINT          AUTO_INCREMENT NOT NULL,
  `card_id`       BIGINT          NOT NULL,
  `event_type`    VARCHAR(30)     NOT NULL,
  `event_payload` VARCHAR(500)    NULL,
  `created_at`    DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_card_audit_card`
    FOREIGN KEY (`card_id`) REFERENCES `credit_card` (`id`)
    ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE = InnoDB;

CREATE INDEX `idx_card_audit_card_event`
  ON `card_audit_log` (`card_id` ASC, `event_type` ASC, `created_at` ASC);
