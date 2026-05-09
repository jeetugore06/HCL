-- =============================================================================
--  card-service :: schema  (creditcards_card_db)
--  Notes:
--   * customer_reference is a logical reference to customer-service's customer
--     UUID. NO foreign key — services don't share schemas.
-- =============================================================================
CREATE DATABASE IF NOT EXISTS creditcards_card_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE creditcards_card_db;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `card_audit_log`;
DROP TABLE IF EXISTS `credit_card`;
DROP TABLE IF EXISTS `credit_card_application`;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `credit_card_application` (
  `id`                    BIGINT          AUTO_INCREMENT NOT NULL,
  `application_reference` CHAR(36)        NOT NULL,
  `customer_reference`    CHAR(36)        NOT NULL,
  `credit_score`          INT             NOT NULL,
  `status`                VARCHAR(30)     NOT NULL,
  `decision_reason`       VARCHAR(255)    NULL,
  `created_at`            DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at`            DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                          ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  CONSTRAINT `uk_application_reference` UNIQUE (`application_reference`)
) ENGINE = InnoDB;

CREATE INDEX `idx_application_customer_status`
  ON `credit_card_application` (`customer_reference` ASC, `status` ASC, `created_at` ASC);

CREATE TABLE `credit_card` (
  `id`              BIGINT          AUTO_INCREMENT NOT NULL,
  `card_number`     CHAR(16)        NOT NULL,
  `application_id`  BIGINT          NOT NULL,
  `customer_reference` CHAR(36)     NOT NULL,
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
  CONSTRAINT `fk_credit_card_application`
    FOREIGN KEY (`application_id`) REFERENCES `credit_card_application` (`id`)
    ON DELETE RESTRICT ON UPDATE NO ACTION
) ENGINE = InnoDB;

CREATE INDEX `idx_credit_card_customer_ref`
  ON `credit_card` (`customer_reference` ASC);

CREATE TABLE `card_audit_log` (
  `id`            BIGINT       AUTO_INCREMENT NOT NULL,
  `card_id`       BIGINT       NOT NULL,
  `event_type`    VARCHAR(30)  NOT NULL,
  `event_payload` VARCHAR(500) NULL,
  `created_at`    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_card_audit_card`
    FOREIGN KEY (`card_id`) REFERENCES `credit_card` (`id`)
    ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE = InnoDB;

CREATE INDEX `idx_card_audit_card_event`
  ON `card_audit_log` (`card_id` ASC, `event_type` ASC, `created_at` ASC);
