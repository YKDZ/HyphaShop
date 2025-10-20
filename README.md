# HyphaShop

English | [简体中文](README_ZH_CN.md)

# Intro

A SpigotMC plugin inspired by [DailyShop](https://github.com/divios/DailyShop). Pursuing ultra customizability and
efficiency.

## Todo

- [x] Dynamic pricing based on market feedback
- [ ] BE GUI
- [x] Total market volume
- [x] Transaction log
- [x] SQL Support
- [x] Random amount and amount-based price
- [x] List product by condition
- [ ] Discount
- [ ] Manually specifying restock results
- [x] Transition limit
- [x] Cart
- [x] Merchant
- [x] Buy more
- [x] More gui type
- [ ] Cart Collection & Order again
- [x] Gui icon condition
- [x] Folia Support
- [ ] Document

## API

### Maven

```
<repositories>
    <repository>
        <id>jitpack</id>
        <url>https://jitpack.io/</url>
    </repository>
</repositories>
```

```
<dependencies>
    <dependency>
        <groupId>cn.encmys</groupId>
        <artifactId>HyphaShop</artifactId>
        <version>{VERSION}</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### Gradle (Kotlin)

```
repositories {
    maven("https://jitpack.io/")
}
```

```
dependencies {
    compileOnly("cn.encmys:HyphaShop:{VERSION}")
}
```

### Example Usage

```java
public class MyPlugin extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        // Get a shop instance by shop id
        Shop shop = DailyShop.SHOP_FACTORY.getShop("black_market");
        // Restock a shop
        shop.getShopStocker().stock();
        // Modify balance of shop in merchant mode
        if (shop.getShopCashier().isMerchant()) {
            // Increase balance by 100
            shop.getShopCashier().modifyBalance(100);
        }

        // Get a product by product id
        Product product = DailyShop.PRODUCT_FACTORY.getProduct("DIAMOND_ORE");
        // Restock a product
        product.getProductStock().stock();
    }
}
```

## Thanks to

- [InvUI](https://github.com/NichtStudioCode/InvUI)
- [CommandAPI](https://github.com/JorelAli/CommandAPI)
- [ItemsLangAPI](https://github.com/Rubix327/ItemsLangAPI)

that make this plugin possible.
