create table projects (
    id bigserial primary key,
    platform varchar(40) not null,
    external_id varchar(120) not null,
    title varchar(500) not null,
    description text not null,
    price numeric(12, 2),
    published_at timestamptz,
    category varchar(40) not null,
    complexity varchar(40) not null,
    estimated_hours integer,
    automation_percent integer,
    skill_match_percent integer,
    risk_percent integer,
    score numeric(5, 2) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uk_projects_platform_external_id unique (platform, external_id)
);

create table project_technologies (
    project_id bigint not null references projects (id) on delete cascade,
    technology varchar(80) not null,
    primary key (project_id, technology)
);

create index idx_projects_score on projects (score desc);
create index idx_projects_category on projects (category);
