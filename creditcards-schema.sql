CREATE TABLE `card_audit_log` ( 
  `id` BIGINT AUTO_INCREMENT NOT NULL,
  `card_id` BIGINT NOT NULL,
  `event_type` VARCHAR(30) NOT NULL,
  `event_payload` VARCHAR(500) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ,
   PRIMARY KEY (`id`)
)
ENGINE = InnoDB;
CREATE TABLE `credit_card` ( 
  `id` BIGINT AUTO_INCREMENT NOT NULL,
  `card_number` CHAR(16) NOT NULL,
  `application_id` BIGINT NOT NULL,
  `card_type` VARCHAR(20) NOT NULL,
  `credit_limit` DECIMAL(19,4) NOT NULL,
  `pin_hash` VARCHAR(60) NOT NULL,
  `pin_status` VARCHAR(20) NOT NULL,
  `version` BIGINT NOT NULL DEFAULT 0 ,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ,
  `customer_id` BIGINT NOT NULL,
   PRIMARY KEY (`id`),
  CONSTRAINT `uk_credit_card_application` UNIQUE (`application_id`),
  CONSTRAINT `uk_credit_card_number` UNIQUE (`card_number`)
)
ENGINE = InnoDB;
CREATE TABLE `credit_card_application` ( 
  `id` BIGINT AUTO_INCREMENT NOT NULL,
  `application_reference` CHAR(36) NOT NULL,
  `customer_id` BIGINT NOT NULL,
  `credit_score` INT NOT NULL,
  `status` VARCHAR(30) NOT NULL,
  `decision_reason` VARCHAR(255) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ,
   PRIMARY KEY (`id`),
  CONSTRAINT `uk_application_reference` UNIQUE (`application_reference`)
)
ENGINE = InnoDB;
CREATE TABLE `credit_score` ( 
  `id` BIGINT AUTO_INCREMENT NOT NULL,
  `customer_id` BIGINT NOT NULL,
  `score` INT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ,
   PRIMARY KEY (`id`),
  CONSTRAINT `uk_credit_score_customer` UNIQUE (`customer_id`)
)
ENGINE = InnoDB;
CREATE TABLE `customer` ( 
  `id` BIGINT AUTO_INCREMENT NOT NULL,
  `customer_reference` CHAR(36) NOT NULL,
  `first_name` VARCHAR(50) NOT NULL,
  `last_name` VARCHAR(50) NOT NULL,
  `email` VARCHAR(120) NOT NULL,
  `phone` VARCHAR(20) NOT NULL,
  `date_of_birth` DATE NOT NULL,
  `annual_salary` DECIMAL(15,2) NOT NULL,
  `employer_name` VARCHAR(120) NULL,
  `employment_type` VARCHAR(20) NOT NULL,
  `document_type` VARCHAR(20) NOT NULL,
  `document_id` VARCHAR(40) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ,
   PRIMARY KEY (`id`),
  CONSTRAINT `uk_customer_reference` UNIQUE (`customer_reference`),
  CONSTRAINT `uk_customer_email` UNIQUE (`email`),
  CONSTRAINT `uk_customer_document` UNIQUE (`document_type`, `document_id`)
)
ENGINE = InnoDB;
CREATE INDEX `idx_card_audit_card_event` 
ON `card_audit_log` (
  `card_id` ASC,
  `event_type` ASC,
  `created_at` ASC
);
CREATE INDEX `FK_credit_card_customer_id` 
ON `credit_card` (
  `customer_id` ASC
);
CREATE INDEX `idx_application_customer_status` 
ON `credit_card_application` (
  `customer_id` ASC,
  `status` ASC,
  `created_at` ASC
);
ALTER TABLE `card_audit_log` ADD CONSTRAINT `fk_card_audit_card` FOREIGN KEY (`card_id`) REFERENCES `credit_card` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE `credit_card` ADD CONSTRAINT `fk_credit_card_application` FOREIGN KEY (`application_id`) REFERENCES `credit_card_application` (`id`) ON DELETE RESTRICT ON UPDATE NO ACTION;
ALTER TABLE `credit_card` ADD CONSTRAINT `fk_credit_card_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`) ON DELETE RESTRICT ON UPDATE NO ACTION;
ALTER TABLE `credit_card` ADD CONSTRAINT `FK_credit_card_customer_id` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `credit_card_application` ADD CONSTRAINT `fk_application_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`) ON DELETE RESTRICT ON UPDATE NO ACTION;
ALTER TABLE `credit_score` ADD CONSTRAINT `fk_credit_score_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;
