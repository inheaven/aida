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
  level_lot DECIMAL(19,8) NOT NULL,
  level_spread DECIMAL(19,8) NOT NULL,
  level_size INT NOT NULL,
  KEY `key_account_id` (account_id),
  FOREIGN KEY (account_id) REFERENCES account (id) ON DELETE CASCADE
);

