# Database Migrations — CTTest v2

Base de datos: **MySQL 8.x** / `utf8mb4_0900_ai_ci` / InnoDB
ORM: Hibernate 6 con `ddl-auto: validate` (producción)
Esquema de referencia: `legacy/src/main/webapp/resources/scripts/demo.sql`

> ⚠️ Los tests con H2 usan `create-drop`, no se ven afectados por estos scripts.

---

## Lote 1 — Inmediatos (compatibles sin cambios Java)

```sql
-- 1. Optimistic locking: columna version para @Version en StudentTest
ALTER TABLE `student_test`
  ADD COLUMN `version` bigint NOT NULL DEFAULT 0
  AFTER `score`;

-- 2. Índice compuesto para consultas frecuentes por estudiante + estado
CREATE INDEX `idx_stt_student_status`
  ON `student_test` (`student_id`, `status_id`);

-- 3. Precisión de score: decimal(4,2) → decimal(5,2) (soporta >100%)
ALTER TABLE `student_test`
  MODIFY `score` decimal(5,2) DEFAULT NULL;

-- 4. Índice único en pin de test_instance
ALTER TABLE `test_instance`
  ADD UNIQUE INDEX `idx_test_instance_pin` (`pin`);
```

---

## Lote 2 — Integridad y auditoría (CHECK + auditoría)

```sql
-- 4. CHECK constraints para columnas enum (sin cambios Java)
ALTER TABLE `test`
  ADD CONSTRAINT `ck_test_type`   CHECK (`type_id`   IN (1, 2)),
  ADD CONSTRAINT `ck_test_status` CHECK (`status_id` IN (1, 2, 3));

ALTER TABLE `test_instance`
  ADD CONSTRAINT `ck_ti_status` CHECK (`status_id` IN (1, 2));

ALTER TABLE `student_test`
  ADD CONSTRAINT `ck_stt_status` CHECK (`status_id` IN (1, 2, 3, 4));

ALTER TABLE `student`
  ADD CONSTRAINT `ck_gender` CHECK (`gender_type` IN (0, 1, 2, 9));

ALTER TABLE `image`
  ADD CONSTRAINT `ck_mime` CHECK (`mime_type` IN (1, 2, 3));

ALTER TABLE `question`
  ADD CONSTRAINT `ck_layout` CHECK (`layout_type_id` IN (1, 2, 3));

-- 5. Limpiar índice redundante en student_answer
DROP INDEX `idx_stn_ans_idx` ON `student_answer`;

-- 6. Ampliar question.question a TEXT
ALTER TABLE `question` MODIFY `question` text DEFAULT NULL;
```

---

## Lote 3 — Optimizaciones de esquema (requieren coordinar con código)

```sql
-- 7. ON DELETE CASCADE en FK de cascada (cuando se elimine test/question)
ALTER TABLE `question` DROP FOREIGN KEY `fk_tst_idx2`;
ALTER TABLE `question` ADD CONSTRAINT `fk_tst_idx2`
  FOREIGN KEY (`test_id`) REFERENCES `test` (`id`) ON DELETE CASCADE;

ALTER TABLE `answer` DROP FOREIGN KEY `fk_qtn_idx`;
ALTER TABLE `answer` ADD CONSTRAINT `fk_qtn_idx`
  FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE;

-- 8. Columnas materializadas para countTotal (requiere triggers o lógica app)
ALTER TABLE `question` ADD COLUMN `count_total` int NOT NULL DEFAULT 0;
ALTER TABLE `answer`   ADD COLUMN `count_total` int NOT NULL DEFAULT 0;
```

---

## Lote 4 — Refactor mayor (requiere cambios en entidades JPA)

```sql
-- 9. Migrar rel_user_student_group a PK compuesta (opcional)
ALTER TABLE `rel_user_student_group` DROP PRIMARY KEY,
  DROP INDEX `uq_usr_stn_grp_idx`;
ALTER TABLE `rel_user_student_group` ADD PRIMARY KEY (`user_id`, `student_group_id`);
ALTER TABLE `rel_user_student_group` DROP COLUMN `id`;

-- 10. Comprimir tabla image (alternativa menos invasiva que migrar a archivos)
ALTER TABLE `image` ROW_FORMAT=COMPRESSED;
```

---

## Notas de implementación

- **Ejecución**: `mysql -h localhost -P 8033 -u root -p < DB_MIGRATIONS.md` (extrayendo el lote deseado).
- **Rollback**: Cada ALTER es incremental. Si algo falla, el cambio no se aplica (modalidad DDL transaccional en MySQL 8+ para DDL compatibles).
- **Validación post-migración**: La aplicación arranca con `ddl-auto: validate`. Si el esquema no coincide con las entidades, Hibernate lanza error al iniciar.
- **Versiones**: Numerar los scripts como `V1__`, `V2__` si en el futuro se adopta Flyway/Liquibase.
