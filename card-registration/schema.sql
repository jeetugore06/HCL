-- =============================================================================
--  scoring-service :: schema  (creditcards_scoring_db)
--  Notes:
--   * customer_reference is a logical reference to customer-service's customer
--     UUID. NO foreign key — services don't share schemas.
-- =============================================================================
CREATE DATABASE IF NOT EXISTS creditcards_scoring_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE creditcards_scoring_db;

DROP TABLE IF EXISTS `credit_score`;

CREATE TABLE `credit_score` (
  `id`                    BIGINT       AUTO_INCREMENT NOT NULL,
  `customer_reference`    CHAR(36)     NOT NULL,
  `score`                 INT          NOT NULL,
  `created_at`            DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at`            DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                       ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  CONSTRAINT `uk_credit_score_customer_ref` UNIQUE (`customer_reference`)
) ENGINE = InnoDB;
