package cn.encmys.ykdz.forest.hyphashop.scheduler;

import cn.encmys.ykdz.forest.hyphashop.HyphaShopImpl;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Scheduler {
    private static final GlobalRegionScheduler globalScheduler = HyphaShopImpl.INSTANCE.getServer().getGlobalRegionScheduler();
    private static final AsyncScheduler asyncScheduler = HyphaShopImpl.INSTANCE.getServer().getAsyncScheduler();

    public static @NotNull ScheduledTask runTaskLater(@NotNull Consumer<ScheduledTask> task, long delay) {
        return globalScheduler.runDelayed(HyphaShopImpl.INSTANCE, task, delay);
    }

    public static @NotNull ScheduledTask runTask(@NotNull Consumer<ScheduledTask> task) {
        return globalScheduler.run(HyphaShopImpl.INSTANCE, task);
    }

    public static @Nullable ScheduledTask runTaskAtFixedRate(@NotNull Consumer<ScheduledTask> task, long delay, long period) {
        if (delay < 0 || period < 0) {
            return null;
        }
        // delay 和 period 不能小于等于 0，故 + 1
        return globalScheduler.runAtFixedRate(HyphaShopImpl.INSTANCE, task, delay + 1, period + 1);
    }

    public static @Nullable ScheduledTask runAsyncTaskAtFixedRate(@NotNull Consumer<ScheduledTask> task, long delay, long period) {
        if (delay < 0 || period < 0) {
            return null;
        }
        // delay 和 period 不能小于等于 0，故 + 1
        // 以纳秒为单位以消除这一单位的影响，形参单位则为 ticks
        return asyncScheduler.runAtFixedRate(HyphaShopImpl.INSTANCE, task, delay / 20 * 1_000_000_000 + 1, period / 20 * 1_000_000_000 + 1, TimeUnit.NANOSECONDS);
    }

    public static @NotNull ScheduledTask runAsyncTask(@NotNull Consumer<ScheduledTask> task) {
        return asyncScheduler.runNow(HyphaShopImpl.INSTANCE, task);
    }
}
