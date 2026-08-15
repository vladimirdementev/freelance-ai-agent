create table execution_runs (
    id bigserial primary key,
    project_id bigint not null references projects (id) on delete cascade,
    workspace_id bigint not null references project_workspaces (id) on delete cascade,
    status varchar(40) not null,
    prompt_path varchar(1000) not null,
    logs_path varchar(1000),
    result_path varchar(1000),
    summary text,
    created_at timestamptz not null,
    started_at timestamptz,
    finished_at timestamptz
);

create index idx_execution_runs_project_created_at
    on execution_runs (project_id, created_at desc);
