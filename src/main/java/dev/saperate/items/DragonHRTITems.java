package dev.saperate.items;

import dev.saperate.DragonHRT;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
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

    public static  final ItemGroup DRAGONHRT_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(DRAGON_ESTROGEN_ITEM))
            .displayName(Text.of("DragonHRT"))
            .entries((context,entries) -> {
                entries.add(DRAGON_ESTROGEN_ITEM);
                entries.add(DRAGON_TESTOSTERONE_ITEM);
                entries.add(HORMONE_BLOCKER_ITEM);
            }).build();
    
    public static void register() {
        Registry.register(Registries.ITEM_GROUP, Identifier.of(DragonHRT.MODID, "group"), DRAGONHRT_GROUP);
    }

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(DragonHRT.MODID, name), item);
    }
}
