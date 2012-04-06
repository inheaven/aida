DELIMITER $$

USE `aida`$$

CREATE
  DEFINER=`root`@`localhost`
TRIGGER `aida`.`create_quotes`
AFTER INSERT ON `aida`.`all_trades`
FOR EACH ROW
begin

-- 5 sec

  set @date_5sec = date_add(timestamp(new.date, date_format(new.time, '%H:%i:00')),
  interval extract(second from (new.time div 5)*5) second);

  if (select count(*) > 0 from `quotes_5sec` where `date` = @date_5sec and `symbol` = new.symbol) then
  update `quotes_5sec`  set `low` = if(`low` < new.price,`low`, new.price), `high` = if(`high` > new.price, `high`, new.price),
          `close` = new.price, `volume` = `volume` + new.volume where `date` = @date_5sec and `symbol` = new.symbol;
    else
    insert into `quotes_5sec` (`symbol`, `date`, `open`, `low`, `high`, `close`, `volume`)
            values (new.symbol, @date_5sec, new.price, new.price, new.price, new.price, new.volume);
  end if;

-- 1min

  set @date = timestamp(new.date, date_format(new.time, '%H:%i:00'));

  if (select count(*) > 0 from `quotes_1min` where `date` = @date and `symbol` = new.symbol) then
  update `quotes_1min`  set `low` = if(`low` < new.price,`low`, new.price), `high` = if(`high` > new.price, `high`, new.price),
          `close` = new.price, `volume` = `volume` + new.volume where `date` = @date and `symbol` = new.symbol;
    else
    insert into `quotes_1min` (`symbol`, `date`, `open`, `low`, `high`, `close`, `volume`)
            values (new.symbol, @date, new.price, new.price, new.price, new.price, new.volume);
  end if;
end$$


-- all_trades_session

DELIMITER $$

USE `aida`$$

CREATE
  DEFINER=`root`@`localhost`
TRIGGER `aida`.`copy_to_all_trades`
AFTER INSERT ON `aida`.`all_trades_session`
FOR EACH ROW
begin

  insert ignore into `all_trades` (`transaction_id`, `date`, `time`, `symbol`, `price`, `quantity`, `volume`, `transaction`)
          values (new.`transaction_id`, str_to_date(new.date, '%d.%m.%Y'), new.`time`, new.`symbol`, new.`price`, new.`quantity`, new.`volume`, new.`transaction`);
end$$




DROP TRIGGER /*!50032 IF EXISTS */ `gzm1_quotes`$$

CREATE
TRIGGER `gzm1_quotes` AFTER UPDATE ON `current`
FOR EACH ROW BEGIN
  if (new.symbol = 'GZM1') then

  set @date1 = timestamp(str_to_date(new.date, '%d.%m.%Y'), date_format(new.time, '%H:%i:00'));

  if (select count(*) > 0 from `quotes_1min` where `date` = @date1 and `symbol` = new.symbol) then
  update `quotes_1min`  set `low` = if(`low` < new.price,`low`, new.price), `high` = if(`high` > new.price, `high`, new.price),
          `close` = new.price, `volume` = new.volume where `date` = @date1 and `symbol` = new.symbol;
    else
    insert into `quotes_1min` (`symbol`, `date`, `open`, `low`, `high`, `close`, `volume`)
            values (new.symbol, @date1, new.price, new.price, new.price, new.price, new.volume);
  end if;

  end if;
END;
$$

DELIMITER ;