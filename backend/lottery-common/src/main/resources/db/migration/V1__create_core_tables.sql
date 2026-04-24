create table lottery_prize
(
    id               bigserial primary key,
    prize_code       varchar(64)    not null unique,
    prize_name       varchar(128)   not null,
    prize_level      varchar(32)    not null,
    prize_level_sort integer        not null default 999,
    probability      numeric(10, 6) not null,
    total_stock      integer        not null default 0,
    available_stock  integer        not null default 0,
    prize_desc       varchar(255),
    prize_image      varchar(255),
    status           smallint       not null default 1,
    sort             integer        not null default 0,
    created_by       varchar(64)             default 'system',
    updated_by       varchar(64)             default 'system',
    created_at       timestamp      not null default current_timestamp,
    updated_at       timestamp      not null default current_timestamp,
    deleted          boolean        not null default false,
    constraint chk_lottery_prize_probability check (probability >= 0 and probability <= 1),
    constraint chk_lottery_prize_total_stock check (total_stock >= 0),
    constraint chk_lottery_prize_available_stock check (available_stock >= 0 and available_stock <= total_stock),
    constraint chk_lottery_prize_status check (status in (0, 1))
);

create index idx_lottery_prize_status on lottery_prize (status);
create index idx_lottery_prize_level_sort on lottery_prize (prize_level_sort);
create index idx_lottery_prize_sort on lottery_prize (sort);
create index idx_lottery_prize_deleted on lottery_prize (deleted);

create table user_account
(
    id                bigserial primary key,
    username          varchar(32)  not null unique,
    nickname          varchar(64)  not null,
    email             varchar(128) not null unique,
    mobile            varchar(16),
    password          varchar(255),
    enabled           boolean      not null default true,
    auth_source       varchar(32)  not null default 'LOCAL',
    keycloak_subject  varchar(128),
    keycloak_username varchar(64),
    created_at        timestamp    not null default current_timestamp,
    updated_at        timestamp    not null default current_timestamp,
    deleted           boolean      not null default false
);

create unique index uk_user_account_mobile on user_account (mobile) where mobile is not null;
create unique index uk_user_account_keycloak_subject on user_account (keycloak_subject) where keycloak_subject is not null;
create index idx_user_account_enabled on user_account (enabled);
create index idx_user_account_deleted on user_account (deleted);

create table sys_role
(
    id         bigserial primary key,
    role_code  varchar(64) not null unique,
    role_name  varchar(64) not null,
    role_type  varchar(32) not null default 'BUSINESS',
    status     smallint    not null default 1,
    sort       integer     not null default 0,
    remark     varchar(255),
    created_at timestamp   not null default current_timestamp,
    updated_at timestamp   not null default current_timestamp,
    deleted    boolean     not null default false,
    constraint chk_sys_role_status check (status in (0, 1))
);

create index idx_sys_role_status on sys_role (status);
create index idx_sys_role_deleted on sys_role (deleted);

create table sys_menu
(
    id         bigserial primary key,
    menu_code  varchar(64)  not null unique,
    menu_name  varchar(64)  not null,
    parent_id  bigint,
    path       varchar(128) not null,
    route_name varchar(64)  not null,
    component  varchar(128) not null,
    menu_type  varchar(32)  not null default 'MENU',
    icon       varchar(64),
    sort       integer      not null default 0,
    status     smallint     not null default 1,
    visible    boolean      not null default true,
    remark     varchar(255),
    created_at timestamp    not null default current_timestamp,
    updated_at timestamp    not null default current_timestamp,
    deleted    boolean      not null default false,
    constraint chk_sys_menu_status check (status in (0, 1))
);

create index idx_sys_menu_parent_id on sys_menu (parent_id);
create index idx_sys_menu_status on sys_menu (status);
create index idx_sys_menu_deleted on sys_menu (deleted);

create table user_role_rel
(
    id         bigserial primary key,
    user_id    bigint    not null,
    role_id    bigint    not null,
    created_at timestamp not null default current_timestamp,
    unique (user_id, role_id)
);

create index idx_user_role_rel_user_id on user_role_rel (user_id);
create index idx_user_role_rel_role_id on user_role_rel (role_id);

create table role_menu_rel
(
    id         bigserial primary key,
    role_id    bigint    not null,
    menu_id    bigint    not null,
    created_at timestamp not null default current_timestamp,
    unique (role_id, menu_id)
);

create index idx_role_menu_rel_role_id on role_menu_rel (role_id);
create index idx_role_menu_rel_menu_id on role_menu_rel (menu_id);

create table lottery_draw_record
(
    id               bigserial primary key,
    record_no        varchar(64) not null unique,
    user_id          bigint      not null,
    prize_id         bigint,
    prize_code       varchar(64),
    prize_name       varchar(128),
    prize_level      varchar(32),
    prize_level_sort integer,
    hit_probability  numeric(10, 6),
    draw_status      smallint    not null,
    draw_remark      varchar(255),
    request_no       varchar(64),
    trace_id         varchar(64),
    created_at       timestamp   not null default current_timestamp,
    updated_at       timestamp   not null default current_timestamp,
    deleted          boolean     not null default false,
    constraint chk_lottery_draw_record_status check (draw_status in (0, 1, 2))
);

create index idx_lottery_draw_record_user_id on lottery_draw_record (user_id);
create index idx_lottery_draw_record_prize_id on lottery_draw_record (prize_id);
create index idx_lottery_draw_record_draw_status on lottery_draw_record (draw_status);
create index idx_lottery_draw_record_created_at on lottery_draw_record (created_at desc);
create index idx_lottery_draw_record_request_no on lottery_draw_record (request_no);
create index idx_lottery_draw_record_deleted on lottery_draw_record (deleted);

alter table sys_menu
    add constraint fk_sys_menu_parent_id
        foreign key (parent_id) references sys_menu (id);

alter table user_role_rel
    add constraint fk_user_role_rel_user_id
        foreign key (user_id) references user_account (id);

alter table user_role_rel
    add constraint fk_user_role_rel_role_id
        foreign key (role_id) references sys_role (id);

alter table role_menu_rel
    add constraint fk_role_menu_rel_role_id
        foreign key (role_id) references sys_role (id);

alter table role_menu_rel
    add constraint fk_role_menu_rel_menu_id
        foreign key (menu_id) references sys_menu (id);

alter table lottery_draw_record
    add constraint fk_lottery_draw_record_user_id
        foreign key (user_id) references user_account (id);

alter table lottery_draw_record
    add constraint fk_lottery_draw_record_prize_id
        foreign key (prize_id) references lottery_prize (id);
