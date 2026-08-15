create table project_workspaces (
    id bigserial primary key,
    project_id bigint not null references projects (id) on delete cascade,
    task_analysis_id bigint references project_task_analyses (id) on delete set null,
    path varchar(1000) not null,
    created_at timestamptz not null
);

create index idx_project_workspaces_project_created_at
    on project_workspaces (project_id, created_at desc);
