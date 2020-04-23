create table okex_info
(
  id bigint auto_increment
    primary key,
  account_id bigint null,
  currency varchar(16) not null,
  balance decimal(19,8) null,
  profit decimal(19,8) null,
  margin_cash decimal(19,8) null,
  created timestamp(3) default CURRENT_TIMESTAMP(3) not null
)
;

create index okex_info__created
  on okex_info (created)
;

create index okex_info__currency
  on okex_info (currency)
;

create table okex_order
(
  id bigint auto_increment
    primary key,
  strategy_id bigint null,
  avg_price double(19,8) null,
  cl_order_id varchar(32) null,
  commission double(19,8) null,
  total_qty int null,
  currency varchar(16) not null,
  exec_id varchar(32) null,
  order_id varchar(32) null,
  qty int null,
  status varchar(16) null,
  type varchar(16) null,
  price double(19,8) null,
  side varchar(4) not null,
  symbol varchar(16) not null,
  text varchar(64) null,
  tx_time datetime null,
  exec_type varchar(16) null,
  leaves_qty int null,
  margin_ratio int null,
  created timestamp(3) default CURRENT_TIMESTAMP(3) not null,
  closed datetime(3) null
)
;

create index okex_order__cl_order_id
  on okex_order (cl_order_id)
;

create index okex_order__currency
  on okex_order (currency)
;

create index okex_order__order_id
  on okex_order (order_id)
;

create index okex_order__status
  on okex_order (status)
;

create index okex_order__symbol
  on okex_order (symbol)
;

create index okex_order__side
  on okex_order (side)
;

create index okex_order__created
  on okex_order (created)
;

create table okex_position
(
  id int auto_increment
    primary key,
  account_id bigint null,
  currency varchar(16) not null,
  avg_price double(19,8) null,
  qty int null,
  price double(19,8) null,
  symbol varchar(16) not null,
  profit double(19,8) null,
  frozen double(19,8) null,
  margin_cash double(19,8) null,
  position_id varchar(32) null,
  type varchar(16) not null,
  evening_up double(19,8) null,
  created timestamp(3) default CURRENT_TIMESTAMP(3) not null
)
;

create index okex_position__created
  on okex_position (created)
;

create index okex_position__currency
  on okex_position (currency)
;

create index okex_position__symbol
  on okex_position (symbol)
;

create table okex_trade
(
  id bigint auto_increment
    primary key,
  currency varchar(16) not null,
  orig_time datetime(3) not null,
  symbol varchar(16) not null,
  order_id bigint not null,
  side varchar(4) not null,
  price double(19,8) not null,
  qty int null,
  created timestamp(3) default CURRENT_TIMESTAMP(3) not null
)
;

create index okex_trade__created
  on okex_trade (created)
;

create index okex_trade__currency
  on okex_trade (currency)
;

create index okex_trade__side
  on okex_trade (side)
;

create index okex_trade__symbol
  on okex_trade (symbol)
;

