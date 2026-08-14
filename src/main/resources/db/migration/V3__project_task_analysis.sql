create table project_task_analyses (
    id bigserial primary key,
    project_id bigint not null references projects (id) on delete cascade,
    requirements text not null,
    questions text not null,
    risks text not null,
    implementation_plan text not null,
    acceptance_criteria text not null,
    analyzer varchar(80) not null,
    created_at timestamptz not null
);

create index idx_project_task_analyses_project_created_at
    on project_task_analyses (project_id, created_at desc);
