-- 1. 重命名原表
ALTER TABLE hyphashop_shop RENAME TO hyphashop_shop_old;

-- 2. 创建新表结构 (使用新的 balances TEXT 列)
CREATE TABLE IF NOT EXISTS hyphashop_shop
(
    id TEXT NOT NULL PRIMARY KEY,
    balances TEXT, -- 新的 balances 列，存储 JSON 文本
    cached_amounts TEXT NOT NULL,
    cached_prices TEXT NOT NULL,
    listed_products TEXT NOT NULL,
    last_restocking INTEGER NOT NULL
);

-- 3. 迁移数据并进行 JSON 转换
-- 将旧表数据复制到新表，并把 balance DOUBLE 转换为 JSON 格式: {"VAULT": double }
INSERT INTO hyphashop_shop (
    id,
    balances,
    cached_amounts,
    cached_prices,
    listed_products,
    last_restocking
)
SELECT
    id,
    -- 转换为 JSON TEXT: '{"VAULT": ' || COALESCE(balance, 0.0) || '}'
    '{"VAULT": ' || COALESCE(balance, 0.0) || '}',
    cached_amounts,
    cached_prices,
    listed_products,
    last_restocking
FROM hyphashop_shop_old;

-- 4. 删除旧表
DROP TABLE hyphashop_shop_old;