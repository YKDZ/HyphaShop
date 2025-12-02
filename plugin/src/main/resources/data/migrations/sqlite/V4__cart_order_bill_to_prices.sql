UPDATE hyphashop_profile
SET cart_order = json_set(
        json_remove(cart_order, '$.bill'),
        '$.prices',
        json_extract(cart_order, '$.bill')
                 )
WHERE json_extract(cart_order, '$.bill') IS NOT NULL;