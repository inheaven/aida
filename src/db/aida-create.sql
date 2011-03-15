DROP TABLE IF EXISTS `all_trades`;
CREATE TABLE  `all_trades` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `transaction_id` bigint(20) unsigned NOT NULL,
  `date` varchar(10) NOT NULL,
  `time` time NOT NULL,
  `symbol` varchar(8) NOT NULL,
  `price` decimal(15,6) NOT NULL,
  `quantity` int(10) unsigned NOT NULL,
  `volume` decimal(15,2) NOT NULL,
  `transaction` varchar(10) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `key_transaction` (`transaction_id`),
  KEY `key_date_time` (`date`,`time`),
  KEY `key_symbol` (`symbol`)
) ENGINE=InnoDB DEFAULT CHARSET=cp1251;

create table `quotes`(
   `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT ,
   `symbol` varchar(10) NOT NULL ,
   `date` datetime NOT NULL ,
   `open` decimal(15,6) NOT NULL ,
   `high` decimal(15,6) NOT NULL ,
   `low` decimal(15,6) NOT NULL ,
   `close` decimal(15,6) NOT NULL ,
   `volume` decimal(15,2) NOT NULL ,
   PRIMARY KEY (`id`),
   UNIQUE KEY `key_symbol_date` (`symbol`, `date`),
 )  Engine=InnoDB DEFAULT charset=cp1251;


DELIMITER $$
    CREATE TRIGGER `create_quotes` AFTER INSERT
    ON `all_trades`
    FOR EACH ROW BEGIN
	set @date = TIMESTAMP(STR_TO_DATE(new.date, '%e.%c.%Y'), DATE_FORMAT(new.time, '%H:%i:00'));	
		
	if (select count(*) > 0 from `quotes_1min` where `date` = @date and `symbol` = new.symbol) then
	    update `quotes_1min`  set `low` = if(`low` < new.price,`low`, new.price), `high` = if(`high` > new.price, `high`, new.price), 
	        `close` = new.price, `volume` = `volume` + new.volume where `date` = @date and `symbol` = new.symbol;		
	else
	    insert into `quotes_1min` (`symbol`, `date`, `open`, `low`, `high`, `close`, `volume`) 
	        values (new.symbol, @date, new.price, new.price, new.price, new.price, new.volume);		
	end if;
    END$$
DELIMITER ;