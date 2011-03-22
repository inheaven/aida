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
  primary key (`id`),
  key `key_transaction` (`transaction_id`),
  key `key_date_time` (`date`,`time`),
  key `key_symbol` (`symbol`)
) engine=innodb default charset=cp1251;

drop table if exists `quotes`;
create table `quotes`(
   `id` bigint unsigned not null auto_increment,
   `symbol` varchar(10) not null,
   `date` datetime not null,
   `open` decimal(15,6) not null,
   `high` decimal(15,6) not null,
   `low` decimal(15,6) not null,
   `close` decimal(15,6) not null,
   `volume` decimal(15,2) not null,
   primary key (`id`),
   unique key `key_symbol_date` (`symbol`, `date`)
 )  engine=innodb default charset=cp1251;

drop trigger if exists `create_quotes`;
delimiter $$
    create trigger `create_quotes` after insert
    on `all_trades`
    for each row begin
	set @date = timestamp(str_to_date(new.date, '%e.%c.%y'), date_format(new.time, '%h:%i:00'));	
		
	if (select count(*) > 0 from `quotes_1min` where `date` = @date and `symbol` = new.symbol) then
	    update `quotes_1min`  set `low` = if(`low` < new.price,`low`, new.price), `high` = if(`high` > new.price, `high`, new.price), 
	        `close` = new.price, `volume` = `volume` + new.volume where `date` = @date and `symbol` = new.symbol;		
	else
	    insert into `quotes_1min` (`symbol`, `date`, `open`, `low`, `high`, `close`, `volume`) 
	        values (new.symbol, @date, new.price, new.price, new.price, new.price, new.volume);		
	end if;
    end$$
delimiter ;

drop table if exists `vector_forecast`;
create table `vector_forecast`(
   `id` bigint unsigned not null auto_increment,
   `symbol` varchar(10) not null,
   `interval` varchar(10) not null,
   `n` int(10) not null,
   `l` int(10) not null,
   `p` int(10) not null,
   `m` int(10) not null,
   `created` datetime not null,
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
  `type` varchar(10) not null,
  primary key (`id`),
  unique key `key_unique` (`vector_forecast_id`, `date`, `index`)
) engine=innodb default charset=cp1251;

drop table if exists `alpha_oracle`;
create table `alpha_oracle`(
  `id` bigint unsigned not null auto_increment,
  `vector_forecast_id` bigint unsigned not null,
  `created` datetime not null,
  primary key (`id`)
) engine=innodb default charset=cp1251;

drop table if exists `alpha_oracle_data`;
create table `alpha_oracle_data`(
  `id` bigint unsigned not null auto_increment,
  `alpha_oracle_id` bigint unsigned not null,
  `date` datetime not null,
  `price` decimal(15,6) not null,
  `prediction` varchar(10) not null,
  `predicted` datetime not null,
  primary key (`id`),
  unique key `key_unique` (`alpha_oracle_id`, `date`)
) engine=innodb default charset=cp1251;
