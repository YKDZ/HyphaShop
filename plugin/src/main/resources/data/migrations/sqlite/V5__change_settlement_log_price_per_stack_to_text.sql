-- 1. 删除依赖于 hyphashop_log_product 的视图
DROP VIEW IF EXISTS hyphashop_product_history;

-- 2. 重命名原表
ALTER TABLE hyphashop_log_product
    RENAME TO hyphashop_log_product_old;

-- 3. 创建新表结构 (price_per_stack 变为 TEXT)
CREATE TABLE IF NOT EXISTS hyphashop_log_product
(
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    log_id          INTEGER NOT NULL,
    shop_id         TEXT    NOT NULL,
    product_id      TEXT    NOT NULL,
    product_amount  INTEGER NOT NULL,
    ordered_stack   INTEGER NOT NULL,
    price_per_stack TEXT    NOT NULL,
    FOREIGN KEY (log_id) REFERENCES hyphashop_settlement_log (id) ON DELETE CASCADE,
    FOREIGN KEY (shop_id) REFERENCES hyphashop_shop (id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES hyphashop_product (id) ON DELETE CASCADE
);

-- 4. 迁移数据
INSERT INTO hyphashop_log_product (id,
                                   log_id,
                                   shop_id,
                                   product_id,
                                   product_amount,
                                   ordered_stack,
                                   price_per_stack)
SELECT id,
       log_id,
       shop_id,
       product_id,
       product_amount,
       ordered_stack,
       -- 转换为 JSON: {"prices": {"VAULT": old_value}}
       '{"prices":{"VAULT":' || price_per_stack || '}}'
FROM hyphashop_log_product_old;

-- 5. 删除旧表
DROP TABLE hyphashop_log_product_old;

-- 6. 重新创建视图
CREATE VIEW IF NOT EXISTS hyphashop_product_history
    (product_id, shop_id, type, history_stack, history_amount)
AS
SELECT hyphashop_log_product.product_id,
       hyphashop_log_product.shop_id,
       hyphashop_settlement_log.type,
       SUM(hyphashop_log_product.ordered_stack)                                        AS history_stack,
       SUM(hyphashop_log_product.product_amount * hyphashop_log_product.ordered_stack) AS history_amount
FROM hyphashop_log_product
         INNER JOIN
     hyphashop_settlement_log ON hyphashop_log_product.log_id = hyphashop_settlement_log.id
GROUP BY hyphashop_log_product.product_id,
         hyphashop_log_product.shop_id,
         hyphashop_settlement_log.type;
