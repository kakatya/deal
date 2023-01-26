
-- liquibase formatted sql

-- changeset kakatya:1674674689063-1
CREATE SEQUENCE  IF NOT EXISTS "change_id" AS bigint START WITH 1 INCREMENT BY 50 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1;

-- changeset kakatya:1674674689063-2
CREATE SEQUENCE  IF NOT EXISTS "client_id" AS bigint START WITH 1 INCREMENT BY 50 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1;

-- changeset kakatya:1674674689063-3
CREATE SEQUENCE  IF NOT EXISTS "credit_status_id" AS bigint START WITH 1 INCREMENT BY 50 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1;

-- changeset kakatya:1674674689063-4
CREATE SEQUENCE  IF NOT EXISTS "employment_position_id" AS bigint START WITH 1 INCREMENT BY 50 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1;

-- changeset kakatya:1674674689063-5
CREATE SEQUENCE  IF NOT EXISTS "employment_status_id" AS bigint START WITH 1 INCREMENT BY 50 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1;

-- changeset kakatya:1674674689063-6
CREATE SEQUENCE  IF NOT EXISTS "gender_id" AS bigint START WITH 1 INCREMENT BY 50 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1;

-- changeset kakatya:1674674689063-7
CREATE SEQUENCE  IF NOT EXISTS "hibernate_sequence" AS bigint START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1;

-- changeset kakatya:1674674689063-8
CREATE SEQUENCE  IF NOT EXISTS "marital_status_id" AS bigint START WITH 1 INCREMENT BY 50 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1;

-- changeset kakatya:1674674689063-9
CREATE SEQUENCE  IF NOT EXISTS "status_id" AS bigint START WITH 1 INCREMENT BY 50 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1;

-- changeset kakatya:1674674689063-10
CREATE TABLE "application" ("application_id" BIGINT NOT NULL, "applied_offer" JSONB, "creation_date" TIMESTAMP WITHOUT TIME ZONE, "ses_code" VARCHAR(255), "sign_date" TIMESTAMP WITHOUT TIME ZONE, "status" VARCHAR(255), "status_history" JSONB, "client_id" BIGINT, "credit_id" BIGINT, CONSTRAINT "application_pkey" PRIMARY KEY ("application_id"));

-- changeset kakatya:1674674689063-11
CREATE TABLE "client" ("client_id" BIGINT NOT NULL, "account" VARCHAR(255), "birth_date" date NOT NULL, "dependent_amount" INTEGER, "email" VARCHAR(255) NOT NULL, "employment" JSONB, "first_name" VARCHAR(30) NOT NULL, "gender" VARCHAR(255), "last_name" VARCHAR(30) NOT NULL, "marital_status" VARCHAR(255), "middle_name" VARCHAR(30), "passport" JSONB, CONSTRAINT "client_pkey" PRIMARY KEY ("client_id"));

-- changeset kakatya:1674674689063-12
CREATE TABLE "credit" ("credit_id" BIGINT NOT NULL, "amount" numeric(19, 2), "credit_status" VARCHAR(255), "insurance_enable" BOOLEAN, "monthly_payment" numeric(19, 2), "payment_schedule" JSONB, "psk" numeric(19, 2), "rate" numeric(19, 2), "salary_client" BOOLEAN, "term" INTEGER NOT NULL, CONSTRAINT "credit_pkey" PRIMARY KEY ("credit_id"));

-- changeset kakatya:1674674689063-13
ALTER TABLE "application" ADD CONSTRAINT "fksf5y1x7pt40gk6tvecgimmw7p" FOREIGN KEY ("client_id") REFERENCES "client" ("client_id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset kakatya:1674674689063-14
ALTER TABLE "application" ADD CONSTRAINT "fktivv032krrnua6gl76ey13mjm" FOREIGN KEY ("credit_id") REFERENCES "credit" ("credit_id") ON UPDATE NO ACTION ON DELETE NO ACTION;

