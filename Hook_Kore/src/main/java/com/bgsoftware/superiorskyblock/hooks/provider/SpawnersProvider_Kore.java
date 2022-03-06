package com.bgsoftware.superiorskyblock.hooks.provider;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.golfing8.kore.FactionsKore;
import com.golfing8.kore.expansionstacker.features.SpawnerStackingFeature;
import com.golfing8.kore.object.StackedSpawner;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public final class SpawnersProvider_Kore implements SpawnersProvider_AutoDetect {
    private final SpawnerStackingFeature feature = FactionsKore.get().getFeature(SpawnerStackingFeature.class);

    public SpawnersProvider_Kore() {
        SuperiorSkyblockPlugin.log("Using Kore as a spawner provider.");
    }

    @Override
    public Pair<Integer, String> getSpawner(Location loc) {
        Preconditions.checkNotNull(loc, "location parameter cannot be null.");

        int blockCount = -1;
        if (Bukkit.isPrimaryThread()) {
            StackedSpawner sSpawner = feature.getStackedSpawner(loc);
            blockCount = sSpawner != null ? sSpawner.getStackSize() : 1;
        }

        return new Pair<>(blockCount, null);
    }

    @Override
    public String getSpawnerType(ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "itemStack parameter cannot be null.");
        return itemStack.getItemMeta().getLore().get(0).replaceAll("§e", "");
    }

}
