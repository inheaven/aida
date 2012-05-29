DELIMITER $$

DROP FUNCTION IF EXISTS `getTradeBalance` $$
CREATE FUNCTION `getTradeBalance` (alphaTraderId BIGINT, startDate DATETIME, endDate DATETIME) RETURNS INT
BEGIN

  DECLARE sellCount, buyCount INT;
  DECLARE balance DECIMAL(15,6);

  select count(*) into sellCount from alpha_trader_data where alpha_trader_id = alphaTraderId and `order` = 'SELL'
          and reply_code = 3 and `date` between startDate and endDate;

  select count(*) into buyCount from alpha_trader_data where alpha_trader_id = alphaTraderId and `order` = 'BUY'
          and reply_code = 3 and `date` between startDate and endDate;

  select (select sum(s.price*s.quantity) FROM (select price, quantity from alpha_trader_data where alpha_trader_id = alphaTraderId and `order` = 'SELL'
          and reply_code = 3 and `date` between startDate and endDate order by `date` limit buyCount) as s)
  -
  (select sum(b.price*b.quantity) FROM (select price, quantity from alpha_trader_data where alpha_trader_id = alphaTraderId and `order` = 'BUY'
  and reply_code = 3 and `date` between startDate and endDate order by `date` limit sellCount) as b) into balance;

  return balance;

END $$

DELIMITER ;

DELIMITER $$