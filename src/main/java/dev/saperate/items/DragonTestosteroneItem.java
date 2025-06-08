package dev.saperate.items;

import com.iafenvoy.iceandfire.entity.EntityDragonBase;
import dev.saperate.effects.DragonHRTEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class DragonTestosteroneItem extends Item {
    public DragonTestosteroneItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if(entity instanceof EntityDragonBase dragon  && !dragon.isMale()){
            dragon.addStatusEffect(new StatusEffectInstance(
                    DragonHRTEffects.TESTOSTERONE_EFFECT,
                    48000,
                    0,
                    false,
                    true,
                    true)
            );
            return ActionResult.CONSUME;
        }
        return ActionResult.FAIL;
    }
}
