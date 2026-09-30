-- ============================================
-- V1 : Creation du schema Spoony
-- ============================================

-- 1. users
CREATE TABLE users (
    id              UUID PRIMARY KEY,
    email           VARCHAR(255) UNIQUE NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL
);

-- 2. base_tasks (catalogue de taches predefinies)
CREATE TABLE base_tasks (
    id UUID PRIMARY KEY,
    task_key VARCHAR(100) NOT NULL UNIQUE,  -- "tasks.household.groceries"
    spoon_cost SMALLINT NOT NULL CHECK(spoon_cost BETWEEN 1 AND 5),
    importance VARCHAR(10) NOT NULL CHECK(importance IN ('LOW', 'MEDIUM', 'HIGH')),
    category VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- 3. user_tasks
CREATE TABLE user_tasks (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    spoon_cost      SMALLINT NOT NULL DEFAULT 2 CHECK (spoon_cost >= 1 AND spoon_cost <= 5),
    importance      VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    category        VARCHAR(50),
    notes           TEXT,
    due_date        DATE NOT NULL DEFAULT CURRENT_DATE,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    completed_at    TIMESTAMP,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL
);

-- 4. daily_energy
CREATE TABLE daily_energy (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date            DATE NOT NULL,
    spoons          SMALLINT NOT NULL CHECK (spoons >= 0 AND spoons <= 12),
    spoons_used     SMALLINT NOT NULL DEFAULT 0,
    end_of_day_mood VARCHAR(50),
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL,
    UNIQUE (user_id, date)
);

-- 5. user_task_logs
CREATE TABLE user_task_logs (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user_task_id    UUID NOT NULL REFERENCES user_tasks(id) ON DELETE CASCADE,
    date            DATE NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    completed_at    TIMESTAMP,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL,
    UNIQUE (user_task_id, date)
);

-- 6. benevolent_messages
CREATE TABLE benevolent_messages (
    id              UUID PRIMARY KEY,
    context         VARCHAR(50) NOT NULL,
    message_key     VARCHAR(100) NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL
);
