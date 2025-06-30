package cn.encmys.ykdz.forest.hyphashop.var.extractor;

import cn.encmys.ykdz.forest.hyphashop.api.utils.StringUtils;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarExtractor;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarInjectorContext;
import cn.encmys.ykdz.forest.hyphashop.product.BundleProduct;
import org.jetbrains.annotations.NotNull;

public class BundleProductExtractor implements VarExtractor {
    @Override
    public void extract(@NotNull VarInjectorContext ctx) {
        final BundleProduct product = ctx.get(BundleProduct.class);

        if (product == null) return;

        ctx.putVar("bundle_contents", () -> StringUtils.wrapToScriptObject(product.getBundleContents()));
    }
}
