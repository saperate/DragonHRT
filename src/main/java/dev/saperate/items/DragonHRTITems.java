package dev.saperate.items;

import dev.saperate.DragonHRT;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class DragonHRTITems {
    public static final DragonEstrogenItem DRAGON_ESTROGEN_ITEM = (DragonEstrogenItem)
            registerItem("dragon_estrogen",
                    new DragonEstrogenItem(new FabricItemSettings().maxCount(16)));

    public static final DragonTestosteroneItem DRAGON_TESTOSTERONE_ITEM = (DragonTestosteroneItem)
            registerItem("dragon_testosterone",
                    new DragonTestosteroneItem(new FabricItemSettings().maxCount(16)));

    public static final HormoneBlockerItem HORMONE_BLOCKER_ITEM = (HormoneBlockerItem)
            registerItem("hormone_blocker",
                    new HormoneBlockerItem(new FabricItemSettings().maxCount(16)));

    public static void register() {}

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(DragonHRT.MODID, name), item);
    }
}
