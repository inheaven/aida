DELIMITER $$

DROP PROCEDURE IF EXISTS `aida`.`alpha_traider_proc` $$
CREATE PROCEDURE `aida`.`alpha_traider_proc` ()
BEGIN
  DECLARE vf_id INT DEFAULT 14;

  DECLARE ao_id INT;

  DECLARE done INT DEFAULT 0;
  DECLARE d DATETIME;

  DECLARE a1,a2,a3,a4,p,f1,f2,f2,f4 FLOAT;


  DECLARE cur1 CURSOR FOR
    SELECT
      `date`, `close`
    FROM
      vector_forecast_data
    where
      `index` = 0 and vector_forecast_id = vf_id limit 10;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

  insert into `alpha_oracle` (`contract`, `vector_forecast_id`) value ('GAZP', vf_id);
  SET ao_id = LAST_INSERT_ID();

  OPEN cur1;

  read_loop: LOOP
    FETCH cur1 INTO d, p;

    IF done THEN
      LEAVE read_loop;
    END IF;

  SELECT `close` INTO a1 from `vector_forecast_data` where `index` = -1 and `now` = d and vector_forecast_id = vf_id;
  SELECT `close` INTO a2 from `vector_forecast_data` where `index` = -2 and `now` = d and vector_forecast_id = vf_id;
  SELECT `close` INTO a3 from `vector_forecast_data` where `index` = -3 and `now` = d and vector_forecast_id = vf_id;
  SELECT `close` INTO a4 from `vector_forecast_data` where `index` = -4 and `now` = d and vector_forecast_id = vf_id;

  SELECT `close` INTO f1 from `vector_forecast_data` where `index` = 1 and `now` = d and vector_forecast_id = vf_id;
  SELECT `close` INTO f2 from `vector_forecast_data` where `index` = 2 and `now` = d and vector_forecast_id = vf_id;
  SELECT `close` INTO f3 from `vector_forecast_data` where `index` = 3 and `now` = d and vector_forecast_id = vf_id;
  SELECT `close` INTO f4 from `vector_forecast_data` where `index` = 4 and `now` = d and vector_forecast_id = vf_id;

  IF p > a1 and a1 > a2 and a2 > a3 and a3 > a4 and p > f1 and f1 > f2 and f2 > f3 and f3 > f4 THEN
    insert into
      `alpha_oracle_data` (`alpha_oracle_id`, `date`, `order`, `price`)
    value
      (ao_id, d, 'SELL', p);
  END IF;

  IF p < a1 and a1 < a2 and a2 < a3 and a3 < a4 and p < f1 and f1 < f2 and f2 < f3 and f3 < f4 THEN
    insert into
      `alpha_oracle_data` (`alpha_oracle_id`, `date`, `order`, `price`)
    value
      (ao_id, d, 'BUY', p);
  END IF;

  END LOOP;

  CLOSE cur1;

END $$

DELIMITER ;