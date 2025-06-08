package dev.saperate.items;

import com.iafenvoy.iceandfire.entity.EntityDragonBase;
import dev.saperate.effects.DragonHRTEffects;
import dev.saperate.utils.SapsUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class HormoneBlockerItem extends Item {
    public HormoneBlockerItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if(entity instanceof EntityDragonBase dragon && dragon.isOwner(user)){
            
            if (SapsUtils.safeHasStatusEffect(DragonHRTEffects.ESTROGEN_EFFECT, entity)) {
                entity.removeStatusEffect(DragonHRTEffects.ESTROGEN_EFFECT);
                dragon.setGender(true);
                return ActionResult.CONSUME;
            }
            
            if (SapsUtils.safeHasStatusEffect(DragonHRTEffects.TESTOSTERONE_EFFECT, entity)) {
                entity.removeStatusEffect(DragonHRTEffects.TESTOSTERONE_EFFECT);
                dragon.setGender(false);
                return ActionResult.CONSUME;
            }
            return ActionResult.FAIL;
        }
        return ActionResult.FAIL;
    }
}
