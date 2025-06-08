package dev.saperate.effects;

import com.iafenvoy.iceandfire.entity.EntityDragonBase;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.util.ActionResult;

public class EstrogenEffect extends StatusEffect {
    protected EstrogenEffect() {
        super(StatusEffectCategory.NEUTRAL, 0xfed3ff);
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        if(entity instanceof EntityDragonBase dragon){
            dragon.setGender(false);
        }
    }
}
