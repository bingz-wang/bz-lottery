
### 1.flyway_schema_history(无注释)

|       列名       |     数据类型      | 是否为空  |  默认值  |  主键   | 备注  |
| :------------: | :-----------: | :---: | :---: | :---: | :-: |
| installed_rank |     int4      | false |       | true  |     |
|    version     |  varchar(50)  | true  |       | false |     |
|  description   | varchar(200)  | false |       | false |     |
|      type      |  varchar(20)  | false |       | false |     |
|     script     | varchar(1000) | false |       | false |     |
|    checksum    |     int4      | true  |       | false |     |
|  installed_by  | varchar(100)  | false |       | false |     |
|  installed_on  |   timestamp   | false | now() | false |     |
| execution_time |     int4      | false |       | false |     |
|    success     |     bool      | false |       | false |     |

#### 索引信息

|索引名称|索引字段|索引类型|是否唯一|
|:-----:|:-----:|:-----:|:-----:|
|flyway_schema_history_pk| installed_rank| btree| true|
|flyway_schema_history_s_idx| success| btree| false|

### 2.lottery_draw_record(无注释)

|列名|数据类型|是否为空|默认值|主键|备注|
|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|
|id| int8| false| nextval('lottery_draw_record_id_seq'::regclass)| true| |
|record_no| varchar(64)| false| | false| |
|user_id| int8| false| | false| |
|prize_id| int8| true| | false| |
|prize_code| varchar(64)| true| | false| |
|prize_name| varchar(128)| true| | false| |
|prize_level| varchar(32)| true| | false| |
|prize_level_sort| int4| true| | false| |
|hit_probability| numeric(10,6)| true| | false| |
|draw_status| int2| false| | false| |
|draw_remark| varchar(255)| true| | false| |
|request_no| varchar(64)| true| | false| |
|trace_id| varchar(64)| true| | false| |
|created_at| timestamp| false| CURRENT_TIMESTAMP| false| |
|updated_at| timestamp| false| CURRENT_TIMESTAMP| false| |
|deleted| bool| false| false| false| |

#### 索引信息

|索引名称|索引字段|索引类型|是否唯一|
|:-----:|:-----:|:-----:|:-----:|
|lottery_draw_record_pkey| id| btree| true|
|lottery_draw_record_record_no_key| record_no| btree| true|
|idx_lottery_draw_record_user_id| user_id| btree| false|
|idx_lottery_draw_record_prize_id| prize_id| btree| false|
|idx_lottery_draw_record_draw_status| draw_status| btree| false|
|idx_lottery_draw_record_created_at| created_at| btree| false|
|idx_lottery_draw_record_request_no| request_no| btree| false|
|idx_lottery_draw_record_deleted| deleted| btree| false|

### 3.lottery_prize(无注释)

|列名|数据类型|是否为空|默认值|主键|备注|
|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|
|id| int8| false| nextval('lottery_prize_id_seq'::regclass)| true| |
|prize_code| varchar(64)| false| | false| |
|prize_name| varchar(128)| false| | false| |
|prize_level| varchar(32)| false| | false| |
|prize_level_sort| int4| false| 999| false| |
|probability| numeric(10,6)| false| | false| |
|total_stock| int4| false| 0| false| |
|available_stock| int4| false| 0| false| |
|prize_desc| varchar(255)| true| | false| |
|prize_image| varchar(255)| true| | false| |
|status| int2| false| 1| false| |
|sort| int4| false| 0| false| |
|created_by| varchar(64)| true| 'system'::character varying| false| |
|updated_by| varchar(64)| true| 'system'::character varying| false| |
|created_at| timestamp| false| CURRENT_TIMESTAMP| false| |
|updated_at| timestamp| false| CURRENT_TIMESTAMP| false| |
|deleted| bool| false| false| false| |

#### 索引信息

|索引名称|索引字段|索引类型|是否唯一|
|:-----:|:-----:|:-----:|:-----:|
|lottery_prize_pkey| id| btree| true|
|lottery_prize_prize_code_key| prize_code| btree| true|
|idx_lottery_prize_status| status| btree| false|
|idx_lottery_prize_level_sort| prize_level_sort| btree| false|
|idx_lottery_prize_sort| sort| btree| false|
|idx_lottery_prize_deleted| deleted| btree| false|

### 4.lottery_system_config(无注释)

|列名|数据类型|是否为空|默认值|主键|备注|
|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|
|config_key| varchar(64)| false| | true| |
|config_value| varchar(128)| false| | false| |
|config_desc| varchar(255)| true| | false| |
|updated_at| timestamp| false| CURRENT_TIMESTAMP| false| |
|updated_by| varchar(64)| true| 'system'::character varying| false| |

#### 索引信息

|索引名称|索引字段|索引类型|是否唯一|
|:-----:|:-----:|:-----:|:-----:|
|lottery_system_config_pkey| config_key| btree| true|

### 5.role_menu_rel(无注释)

|列名|数据类型|是否为空|默认值|主键|备注|
|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|
|id| int8| false| nextval('role_menu_rel_id_seq'::regclass)| true| |
|role_id| int8| false| | false| |
|menu_id| int8| false| | false| |
|created_at| timestamp| false| CURRENT_TIMESTAMP| false| |

#### 索引信息

|索引名称|索引字段|索引类型|是否唯一|
|:-----:|:-----:|:-----:|:-----:|
|role_menu_rel_pkey| id| btree| true|
|role_menu_rel_role_id_menu_id_key| role_id,menu_id| btree| true|
|idx_role_menu_rel_role_id| role_id| btree| false|
|idx_role_menu_rel_menu_id| menu_id| btree| false|

### 6.sys_menu(无注释)

|列名|数据类型|是否为空|默认值|主键|备注|
|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|
|id| int8| false| nextval('sys_menu_id_seq'::regclass)| true| |
|menu_code| varchar(64)| false| | false| |
|menu_name| varchar(64)| false| | false| |
|parent_id| int8| true| | false| |
|path| varchar(128)| false| | false| |
|route_name| varchar(64)| false| | false| |
|component| varchar(128)| false| | false| |
|menu_type| varchar(32)| false| 'MENU'::character varying| false| |
|icon| varchar(64)| true| | false| |
|sort| int4| false| 0| false| |
|status| int2| false| 1| false| |
|visible| bool| false| true| false| |
|remark| varchar(255)| true| | false| |
|created_at| timestamp| false| CURRENT_TIMESTAMP| false| |
|updated_at| timestamp| false| CURRENT_TIMESTAMP| false| |
|deleted| bool| false| false| false| |

#### 索引信息

|索引名称|索引字段|索引类型|是否唯一|
|:-----:|:-----:|:-----:|:-----:|
|sys_menu_pkey| id| btree| true|
|sys_menu_menu_code_key| menu_code| btree| true|
|idx_sys_menu_parent_id| parent_id| btree| false|
|idx_sys_menu_status| status| btree| false|
|idx_sys_menu_deleted| deleted| btree| false|

### 7.sys_role(无注释)

|列名|数据类型|是否为空|默认值|主键|备注|
|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|
|id| int8| false| nextval('sys_role_id_seq'::regclass)| true| |
|role_code| varchar(64)| false| | false| |
|role_name| varchar(64)| false| | false| |
|role_type| varchar(32)| false| 'BUSINESS'::character varying| false| |
|status| int2| false| 1| false| |
|sort| int4| false| 0| false| |
|remark| varchar(255)| true| | false| |
|created_at| timestamp| false| CURRENT_TIMESTAMP| false| |
|updated_at| timestamp| false| CURRENT_TIMESTAMP| false| |
|deleted| bool| false| false| false| |

#### 索引信息

|索引名称|索引字段|索引类型|是否唯一|
|:-----:|:-----:|:-----:|:-----:|
|sys_role_pkey| id| btree| true|
|sys_role_role_code_key| role_code| btree| true|
|idx_sys_role_status| status| btree| false|
|idx_sys_role_deleted| deleted| btree| false|

### 8.user_account(无注释)

|列名|数据类型|是否为空|默认值|主键|备注|
|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|
|id| int8| false| nextval('user_account_id_seq'::regclass)| true| |
|username| varchar(32)| false| | false| |
|nickname| varchar(64)| false| | false| |
|email| varchar(128)| false| | false| |
|mobile| varchar(16)| true| | false| |
|password| varchar(255)| true| | false| |
|enabled| bool| false| true| false| |
|auth_source| varchar(32)| false| 'LOCAL'::character varying| false| |
|keycloak_subject| varchar(128)| true| | false| |
|keycloak_username| varchar(64)| true| | false| |
|created_at| timestamp| false| CURRENT_TIMESTAMP| false| |
|updated_at| timestamp| false| CURRENT_TIMESTAMP| false| |
|deleted| bool| false| false| false| |

#### 索引信息

|索引名称|索引字段|索引类型|是否唯一|
|:-----:|:-----:|:-----:|:-----:|
|user_account_pkey| id| btree| true|
|user_account_username_key| username| btree| true|
|user_account_email_key| email| btree| true|
|uk_user_account_mobile| mobile| btree| true|
|uk_user_account_keycloak_subject| keycloak_subject| btree| true|
|idx_user_account_enabled| enabled| btree| false|
|idx_user_account_deleted| deleted| btree| false|

### 9.user_role_rel(无注释)

|列名|数据类型|是否为空|默认值|主键|备注|
|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|
|id| int8| false| nextval('user_role_rel_id_seq'::regclass)| true| |
|user_id| int8| false| | false| |
|role_id| int8| false| | false| |
|created_at| timestamp| false| CURRENT_TIMESTAMP| false| |

#### 索引信息

|索引名称|索引字段|索引类型|是否唯一|
|:-----:|:-----:|:-----:|:-----:|
|user_role_rel_pkey| id| btree| true|
|user_role_rel_user_id_role_id_key| user_id,role_id| btree| true|
|idx_user_role_rel_user_id| user_id| btree| false|
|idx_user_role_rel_role_id| role_id| btree| false|
