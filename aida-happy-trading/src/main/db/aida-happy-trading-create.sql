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

  KEY `key_closed` (closed),
  KEY `key_type` (type),
  KEY `key_closed` (closed),
  KEY `key_status` (status),
  UNIQUE KEY `key_order_id` (order_id),
  KEY `key_strategy_id` (strategy_id),
  FOREIGN KEY (strategy_id) REFERENCES strategy (id)
);

CREATE TABLE `user_info`
(
  id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  account_id BIGINT NOT NULL,
  currency VARCHAR(32) NOT NULL,
  account_rights DECIMAL(19,8) NOT NULL,
  keep_deposit DECIMAL(19,8) NOT NULL,
  profit_real DECIMAL(19,8) NOT NULL,
  profit_unreal DECIMAL(19,8) NOT NULL,
  risk_rate DECIMAL(19,8) NOT NULL,
  created TIMESTAMP NOT NULL DEFAULT NOW(),
  KEY `key_created` (created),
  KEY `key_account_id` (account_id),
  FOREIGN KEY (account_id) REFERENCES account (id)
);

CREATE TABLE `user_info_total`
(
  id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  account_id BIGINT NOT NULL,
  spot_total DECIMAL(19,8) NOT NULL,
  futures_total DECIMAL(19,8) NOT NULL,
  spot_volume DECIMAL(19,8) NOT NULL,
  futures_volume DECIMAL(19,8) NOT NULL,
  ltc_price DECIMAL(19,8) NOT NULL,
  btc_price DECIMAL(19,8) NOT NULL,
  created TIMESTAMP NOT NULL DEFAULT NOW(),
  KEY `key_created` (created),
  KEY `key_account_id` (account_id),
  FOREIGN KEY (account_id) REFERENCES account (id)
);

CREATE TABLE `trade`
(
  id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  trade_id VARCHAR(32) NOT NULL,
  exchange_type VARCHAR(8) NOT NULL,
  symbol VARCHAR(8) NOT NULL,
  symbol_type VARCHAR(10),
  order_type VARCHAR(10),
  price DECIMAL(19,8) NOT NULL,
  amount DECIMAL(19,8) NOT NULL,
  time VARCHAR(8),
  created TIMESTAMP NOT NULL DEFAULT NOW(),
  UNIQUE KEY `key_trade_id` (trade_id),
  KEY `key_exchange_type` (exchange_type),
  KEY `key_symbol` (symbol),
  KEY `key_symbol_type` (symbol_type),
  KEY `key_order_type` (order_type),
  KEY `key_price` (price),
  KEY `key_created` (created)
);

CREATE TABLE `depth`
(
  id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  exchange_type VARCHAR(8) NOT NULL,
  symbol VARCHAR(8) NOT NULL,
  symbol_type VARCHAR(10),
  bid DECIMAL(19,8) NOT NULL,
  ask DECIMAL(19,8) NOT NULL,
  bid_json LONGTEXT NOT NULL,
  ask_json LONGTEXT NOT NULL,
  time TIMESTAMP NOT NULL DEFAULT NOW(),
  created TIMESTAMP NOT NULL DEFAULT NOW(),
  KEY `key_exchange_type` (exchange_type),
  KEY `key_symbol` (symbol),
  KEY `key_symbol_type` (symbol_type),
  KEY `key_bid` (bid),
  KEY `key_ask` (ask),
  KEY `key_created` (created)
);

