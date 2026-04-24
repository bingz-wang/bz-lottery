create table if not exists lottery_system_config
(
    config_key
    varchar
(
    64
) primary key,
    config_value varchar
(
    128
) not null,
    config_desc varchar
(
    255
),
    updated_at timestamp not null default current_timestamp,
    updated_by varchar
(
    64
) default 'system'
    );

insert into lottery_system_config (config_key, config_value, config_desc, updated_at, updated_by)
values ('LOTTERY_DRAW_STRATEGY', 'GUARANTEE_LADDER', 'Global lottery draw strategy', current_timestamp,
        'system') on conflict (config_key) do
update set
    config_value = excluded.config_value,
    config_desc = excluded.config_desc,
    updated_at = excluded.updated_at,
    updated_by = excluded.updated_by;
