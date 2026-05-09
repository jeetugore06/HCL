-- =============================================================================
--  card-service :: schema  (creditcards_card_db)
--  Notes:
--   * application_reference is a logical reference to application-service's
--     application UUID.
--   * No foreign keys between microservices.
--   * Sensitive fields are encrypted before DB persistence.
-- =============================================================================

CREATE DATABASE IF NOT EXISTS creditcards_card_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE creditcards_card_db;

DROP TABLE IF EXISTS `credit_card`;

CREATE TABLE `credit_card` (

                               `id`                          BIGINT       AUTO_INCREMENT NOT NULL,

                               `application_reference`       CHAR(36)     NOT NULL,

                               `card_type`                   VARCHAR(30)  NOT NULL,

                               `card_limit`                  DECIMAL(12,2) NOT NULL,

                               `card_number_encrypted`       VARCHAR(500) NOT NULL,

                               `masked_card_number`          VARCHAR(25)  NOT NULL,

                               `pin_encrypted`               VARCHAR(500) NOT NULL,

                               `status`                      VARCHAR(30)  NOT NULL,

                               `created_at`                  DATETIME(6)  NOT NULL
                                DEFAULT CURRENT_TIMESTAMP(6),

                               `updated_at`                  DATETIME(6)  NOT NULL
                                DEFAULT CURRENT_TIMESTAMP(6)
                                ON UPDATE CURRENT_TIMESTAMP(6),

                               PRIMARY KEY (`id`),

                               CONSTRAINT `uk_credit_card_app_ref`
                                   UNIQUE (`application_reference`)

) ENGINE = InnoDB;