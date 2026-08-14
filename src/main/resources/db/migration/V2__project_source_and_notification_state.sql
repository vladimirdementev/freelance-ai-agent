alter table projects
    add column source_url varchar(1000),
    add column source_category varchar(300),
    add column notified_at timestamptz;

create index idx_projects_notified_at on projects (notified_at);
