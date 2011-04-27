drop table if exists `all_trades`;
create table  `all_trades` (
  `id` bigint(20) unsigned not null auto_increment,
  `transaction_id` bigint(20) unsigned not null,
  `date` varchar(10) not null,
  `time` time not null,
  `symbol` varchar(8) not null,
  `price` decimal(15,6) not null,
  `quantity` int(10) unsigned not null,
  `volume` decimal(15,2) not null,
  `transaction` varchar(10) not null,
  `created` timestamp default current_timestamp,
  primary key (`id`),
  key `key_transaction` (`transaction_id`),
  key `key_date_time` (`date`,`time`),
  key `key_symbol` (`symbol`)
) engine=innodb default charset=cp1251;

drop table if exists `quotes_1min`;
create table `quotes_1min`(
   `id` bigint unsigned not null auto_increment,
   `symbol` varchar(10) not null,
   `date` datetime not null,
   `open` decimal(15,6) not null,
   `high` decimal(15,6) not null,
   `low` decimal(15,6) not null,
   `close` decimal(15,6) not null,
   `volume` decimal(15,2) not null,
   `created` timestamp default current_timestamp,
   primary key (`id`),
   unique key `key_symbol_date` (`symbol`, `date`)
 ) engine=innodb default charset=cp1251;

drop table if exists `current`;
create table `current`(
   `id` bigint unsigned not null auto_increment,
   `instrument` varchar(64) not null,
   `symbol` varchar(10) not null,
   `date` varchar(10),
   `price` decimal(15,6) not null,
   `mean` decimal(15,6),
   `bid` decimal(15,6),
   `ask` decimal(15,6),
   `rate` decimal(2,2),
   `created` timestamp default current_timestamp,
   primary key (`id`),
   unique key `key_symbol_date` (`symbol`, `date`)
 ) engine=innodb default charset=cp1251;

DELIMITER $$
DROP TRIGGER /*!50032 IF EXISTS */ `create_quotes`$$
CREATE
    /*!50017 DEFINER = 'root'@'localhost' */
    TRIGGER `create_quotes` AFTER INSERT ON `all_trades`
    FOR EACH ROW begin
	set @date = timestamp(str_to_date(new.date, '%d.%m.%Y'), date_format(new.time, '%H:%i:00'));

	if (select count(*) > 0 from `quotes_1min` where `date` = @date and `symbol` = new.symbol) then
	    update `quotes_1min`  set `low` = if(`low` < new.price,`low`, new.price), `high` = if(`high` > new.price, `high`, new.price),
	        `close` = new.price, `volume` = `volume` + new.volume where `date` = @date and `symbol` = new.symbol;
	else
	    insert into `quotes_1min` (`symbol`, `date`, `open`, `low`, `high`, `close`, `volume`)
	        values (new.symbol, @date, new.price, new.price, new.price, new.price, new.volume);
	end if;
    end;
$$
DELIMITER ;

drop table if exists `vector_forecast`;
create table `vector_forecast`(
   `id` bigint unsigned not null auto_increment,
   `symbol` varchar(10) not null,
   `interval` varchar(10) not null,
   `n` int(10) not null,
   `l` int(10) not null,
   `p` int(10) not null,
   `m` int(10) not null,
   `created` timestamp default current_timestamp,
   primary key (`id`),
   unique key `key_unique` (`symbol`, `interval`, `n`, `l`, `p`, `m`)
) engine=innodb default charset=cp1251;

drop table if exists `vector_forecast_data`;
create table `vector_forecast_data`(
  `id` bigint unsigned not null auto_increment,
  `vector_forecast_id` bigint unsigned not null,
  `date` datetime not null,
  `index` int(10) not null,
  `index_date` datetime not null,
  `price` decimal(15,6) not null,
  `type` varchar(10),
  `created` timestamp default current_timestamp,
  primary key (`id`),
  unique key `key_unique` (`vector_forecast_id`, `date`, `index`)
) engine=innodb default charset=cp1251;

drop table if exists `alpha_oracle`;
create table `alpha_oracle`(
  `id` bigint unsigned not null auto_increment,
  `vector_forecast_id` bigint unsigned not null,
  `price_type` varchar(10) not null,
  `stop_price` decimal(15,6) not null,
  `stop_type` varchar(10) not null,
  `stop_factor` decimal(15,6) not null,
  `stop_count` int(10) not null,
  `max_stop_count` int(10) not null,
  `score` decimal(15,2) not null,
  `prediction` varchar(10),
  `status` varchar(45) not null,
  `created` timestamp default current_timestamp,
  primary key (`id`),
  key `key_vector_forecast` (`vector_forecast_id`),
  constraint `fk_vector_forecast` foreign key (`vector_forecast_id`) references `vector_forecast` (`id`)
) engine=innodb default charset=cp1251;

drop table if exists `alpha_oracle_data`;
create table `alpha_oracle_data`(
  `id` bigint unsigned not null auto_increment,
  `alpha_oracle_id` bigint unsigned not null,
  `date` datetime not null,
  `price` decimal(15,6) not null,
  `prediction` varchar(10) not null,
  `created` timestamp default current_timestamp,
  primary key (`id`),
  unique key `key_unique` (`alpha_oracle_id`, `date`)
) engine=innodb default charset=cp1251;

drop table if exists `alpha_trader`;
create table `alpha_trader`(
  `id` bigint unsigned not null auto_increment,
  `alpha_oracle_id` bigint unsigned not null,
  `future_symbol` varchar(10) not null,
  `symbol` varchar(10) not null,
  `price` decimal(15,6),
  `quantity` int not null,
  `order_quantity` int not null,
  `balance` decimal(15,2),
  `created` timestamp default current_timestamp,
  primary key (`id`)
) engine=innodb default charset=cp1251;

drop table if exists `alpha_trader_data`;
create table `alpha_trader_data`(
  `id` bigint unsigned not null auto_increment,
  `alpha_trader_id` bigint unsigned not null,
  `date` datetime not null,
  `price` decimal(15,6) not null,
  `order` varchar(10) not null,
  `order_num` bigint unsigned,
  `result` int,
  `reply_code` int,
  `result_message` varchar(255),
  `error_message` varchar(255),
  `predicted` datetime not null,
  `created` timestamp default current_timestamp,
  primary key (`id`)
) engine=innodb default charset=cp1251;
