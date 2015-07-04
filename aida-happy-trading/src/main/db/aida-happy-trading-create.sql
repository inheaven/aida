CREATE TABLE client
(
  id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  login VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL
);

CREATE TABLE account
(
  id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  api_key VARCHAR(255) NOT NULL,
  exchange_type VARCHAR(255) NOT NULL,
  secret_key VARCHAR(255) NOT NULL,
  client_id BIGINT,
  KEY `key_client_id` (client_id),
  FOREIGN KEY (client_id) REFERENCES client (id)
);

CREATE TABLE strategy
(
  id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  type VARCHAR(255) NOT NULL,
  account_id BIGINT NOT NULL,
  symbol VARCHAR(255) NOT NULL,
  symbol_type VARCHAR(255),
  level_lot DECIMAL(19,8) NOT NULL,
  level_spread DECIMAL(19,8) NOT NULL,
  level_size INT NOT NULL,
  active BOOLEAN NOT NULL,
  session_start DATETIME NOT NULL,
  KEY `key_account_id` (account_id),
  FOREIGN KEY (account_id) REFERENCES account (id)
);

CREATE TABLE `order`
(
  id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  order_id VARCHAR(32) NOT NULL,
  strategy_id BIGINT NOT NULL,
  exchange_type VARCHAR(32) NOT NULL,
  type VARCHAR(32) NOT NULL,
  symbol VARCHAR(32) NOT NULL,
  symbol_type VARCHAR(32),
  price DECIMAL(19,8) NOT NULL,
  amount DECIMAL(19,8) NOT NULL,
  filled_amount DECIMAL(19,8),
  avg_price DECIMAL(19,8),
  fee DECIMAL(19,8),
  created TIMESTAMP NOT NULL DEFAULT NOW(),
  open TIMESTAMP NULL,
  closed TIMESTAMP NULL,
  status VARCHAR(32) NOT NULL,
  KEY `key_strategy_id` (strategy_id),
  FOREIGN KEY (strategy_id) REFERENCES strategy (id),
  KEY `key_closed` (closed)
);


