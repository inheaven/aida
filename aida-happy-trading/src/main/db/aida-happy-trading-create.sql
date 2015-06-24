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
  api_key VARCHAR(255) NOT NULL,
  exchange_type VARCHAR(255) NOT NULL,
  secret_key VARCHAR(255) NOT NULL,
  client_id BIGINT,
  KEY `key_client_id` (client_id),
  FOREIGN KEY (client_id) REFERENCES client (id)
);
