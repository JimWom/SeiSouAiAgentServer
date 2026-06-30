create table if not exists chat_sessions (
    id varchar(36) primary key,
    visitor_id varchar(128) not null,
    status varchar(32) not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table if not exists chat_messages (
    id varchar(36) primary key,
    session_id varchar(36) not null,
    role varchar(32) not null,
    content clob not null,
    created_at timestamp not null,
    constraint fk_chat_messages_session
        foreign key (session_id) references chat_sessions(id)
);

create index if not exists idx_chat_messages_session_created
    on chat_messages(session_id, created_at);

create table if not exists skill_definitions (
    id varchar(36) primary key,
    code varchar(80) not null unique,
    name varchar(120) not null,
    description varchar(500) not null,
    instruction clob not null,
    enabled boolean not null,
    created_at timestamp not null
);
