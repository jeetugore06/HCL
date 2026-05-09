-- =============================================================================
--  customer-service :: schema  (creditcards_customer_db)
-- =============================================================================
CREATE DATABASE IF NOT EXISTS creditcards_customer_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE creditcards_customer_db;

DROP TABLE IF EXISTS `customer`;

CREATE TABLE `customer` (
  `id`                  BIGINT          AUTO_INCREMENT NOT NULL,
  `customer_reference`  CHAR(36)        NOT NULL,
  `first_name`          VARCHAR(50)     NOT NULL,
  `last_name`           VARCHAR(50)     NOT NULL,
  `email`               VARCHAR(120)    NOT NULL,
  `phone`               VARCHAR(20)     NOT NULL,
  `date_of_birth`       DATE            NOT NULL,
  `annual_salary`       DECIMAL(15,2)   NOT NULL,
  `employer_name`       VARCHAR(120)    NULL,
  `employment_type`     VARCHAR(20)     NOT NULL,
  `document_type`       VARCHAR(20)     NOT NULL,
  `document_id`         VARCHAR(40)     NOT NULL,
  `created_at`          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at`          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                        ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  CONSTRAINT `uk_customer_reference` UNIQUE (`customer_reference`),
  CONSTRAINT `uk_customer_email`     UNIQUE (`email`),
  CONSTRAINT `uk_customer_document`  UNIQUE (`document_type`, `document_id`)
) ENGINE = InnoDB;
