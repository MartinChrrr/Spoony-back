-- Preserve task history independently from the mutable task definition.
ALTER TABLE user_task_logs ADD COLUMN task_name_snapshot VARCHAR(255);
ALTER TABLE user_task_logs ADD COLUMN spoon_cost_snapshot SMALLINT;
ALTER TABLE user_task_logs ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- Existing logs still reference an existing task because the original foreign key
-- used ON DELETE CASCADE. The original historical value is unavailable, so the
-- current task definition is the best possible backfill.
UPDATE user_task_logs
SET task_name_snapshot = (
        SELECT user_tasks.name
        FROM user_tasks
        WHERE user_tasks.id = user_task_logs.user_task_id
    ),
    spoon_cost_snapshot = (
        SELECT user_tasks.spoon_cost
        FROM user_tasks
        WHERE user_tasks.id = user_task_logs.user_task_id
    );

ALTER TABLE user_task_logs ALTER COLUMN task_name_snapshot SET NOT NULL;
ALTER TABLE user_task_logs ALTER COLUMN spoon_cost_snapshot SET NOT NULL;

ALTER TABLE user_task_logs
    ADD CONSTRAINT ck_user_task_logs_spoon_cost_snapshot
    CHECK (spoon_cost_snapshot BETWEEN 1 AND 5);

-- Repair counters from the history that is still present before enforcing the
-- non-negative invariant. Logs lost through an old cascade cannot be recovered,
-- but they no longer leave stale spoons in daily_energy after this reconciliation.
UPDATE daily_energy
SET spoons_used = CAST(COALESCE((
    SELECT SUM(user_task_logs.spoon_cost_snapshot)
    FROM user_task_logs
    WHERE user_task_logs.user_id = daily_energy.user_id
      AND user_task_logs.date = daily_energy.date
      AND user_task_logs.status = 'COMPLETED'
), 0) AS SMALLINT);

ALTER TABLE daily_energy
    ADD CONSTRAINT ck_daily_energy_spoons_used_non_negative
    CHECK (spoons_used >= 0);
