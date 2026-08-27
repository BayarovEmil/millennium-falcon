CREATE TABLE life_areas (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name       TEXT    NOT NULL,
    color      TEXT    NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uq_life_areas_name UNIQUE (name)
);

CREATE TABLE plan_periods (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type       VARCHAR(10) NOT NULL CHECK (type IN ('MONTH', 'WEEK', 'DAY')),
    start_date DATE        NOT NULL,
    end_date   DATE        NOT NULL,
    note       TEXT,
    parent_id  BIGINT REFERENCES plan_periods (id),
    CONSTRAINT uq_plan_periods_type_start UNIQUE (type, start_date),
    CONSTRAINT ck_plan_periods_date_order CHECK (end_date >= start_date)
);

CREATE INDEX idx_plan_periods_parent_id ON plan_periods (parent_id);
CREATE INDEX idx_plan_periods_start_end ON plan_periods (start_date, end_date);

CREATE TABLE goals (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title           TEXT        NOT NULL,
    description     TEXT,
    type            VARCHAR(10) NOT NULL CHECK (type IN ('MILESTONE', 'HABIT')),
    life_area_id    BIGINT      NOT NULL REFERENCES life_areas (id),
    period_id       BIGINT      NOT NULL REFERENCES plan_periods (id),
    target_count    INTEGER,
    current_count   INTEGER,
    status          VARCHAR(10) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'DONE', 'DROPPED')),
    target_per_week INTEGER,
    archived_at     TIMESTAMPTZ
);

CREATE INDEX idx_goals_life_area_id ON goals (life_area_id);
CREATE INDEX idx_goals_period_id ON goals (period_id);
CREATE INDEX idx_goals_status ON goals (status);

CREATE TABLE tasks (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title        TEXT    NOT NULL,
    period_id    BIGINT  NOT NULL REFERENCES plan_periods (id),
    goal_id      BIGINT REFERENCES goals (id),
    done         BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMPTZ,
    sort_order   INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_tasks_period_id ON tasks (period_id);
CREATE INDEX idx_tasks_goal_id ON tasks (goal_id);

CREATE TABLE habit_entries (
    id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    goal_id BIGINT  NOT NULL REFERENCES goals (id),
    date    DATE    NOT NULL,
    done    BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_habit_entries_goal_date UNIQUE (goal_id, date)
);

CREATE INDEX idx_habit_entries_goal_id ON habit_entries (goal_id);
