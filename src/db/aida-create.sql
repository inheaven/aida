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
   KEY `key_date` (`date`),
   KEY `key_symbol` (`symbol`)
 )  Engine=InnoDB DEFAULT charset=cp1251;