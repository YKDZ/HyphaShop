CREATE TABLE IF NOT EXISTS hyphashop_profile
(
    owner_uuid
    VARCHAR
(
    32
) PRIMARY KEY,
    shopping_modes TEXT NOT NULL,
    cart_order TEXT NOT NULL
    );

CREATE TABLE IF NOT EXISTS hyphashop_product
(
    id
    TEXT
    NOT
    NULL
    PRIMARY
    KEY,
    current_player_amount
    TEXT
    NOT
    NULL,
    current_global_amount
    INTEGER
    NOT
    NULL
);

CREATE TABLE IF NOT EXISTS hyphashop_shop
(
    id
    TEXT
    NOT
    NULL
    PRIMARY
    KEY,
    balance
    DOUBLE,
    cached_amounts
    TEXT
    NOT
    NULL,
    cached_prices
    TEXT
    NOT
    NULL,
    listed_products
    TEXT
    NOT
    NULL,
    last_restocking
    INTEGER
    NOT
    NULL
);

CREATE TABLE IF NOT EXISTS hyphashop_settlement_log
(
    id
    INTEGER
    PRIMARY
    KEY
    AUTOINCREMENT,
    customer_uuid
    TEXT
    NOT
    NULL,
    type
    TEXT
    NOT
    NULL,
    transition_time
    TIMESTAMP
    NOT
    NULL
);

CREATE TABLE IF NOT EXISTS hyphashop_log_product
(
    id
    INTEGER
    PRIMARY
    KEY
    AUTOINCREMENT,
    log_id
    INTEGER
    NOT
    NULL,
    shop_id
    TEXT
    NOT
    NULL,
    product_id
    TEXT
    NOT
    NULL,
    product_amount
    INTEGER
    NOT
    NULL,
    ordered_stack
    INTEGER
    NOT
    NULL,
    price_per_stack
    DOUBLE
    NOT
    NULL,
    FOREIGN
    KEY
(
    log_id
) REFERENCES hyphashop_settlement_log
(
    id
) ON DELETE CASCADE,
    FOREIGN KEY
(
    shop_id
) REFERENCES hyphashop_shop
(
    id
)
  ON DELETE CASCADE,
    FOREIGN KEY
(
    product_id
) REFERENCES hyphashop_product
(
    id
)
  ON DELETE CASCADE
    );

CREATE VIEW IF
            NOT EXISTS hyphashop_product_history
            (product_id, shop_id, type, history_stack, history_amount, history_total_price)
AS
SELECT hyphashop_log_product.product_id,
       hyphashop_log_product.shop_id,
       hyphashop_settlement_log.type,
       SUM(hyphashop_log_product.ordered_stack)                                         AS history_stack,
       SUM(hyphashop_log_product.product_amount * hyphashop_log_product.ordered_stack)  AS history_amount,
       SUM(hyphashop_log_product.ordered_stack * hyphashop_log_product.price_per_stack) AS history_total_price
FROM hyphashop_log_product
         INNER JOIN
     hyphashop_settlement_log ON hyphashop_log_product.log_id = hyphashop_settlement_log.id
GROUP BY hyphashop_log_product.product_id,
         hyphashop_log_product.shop_id,
         hyphashop_settlement_log.type;