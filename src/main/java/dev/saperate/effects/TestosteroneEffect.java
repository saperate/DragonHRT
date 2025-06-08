package dev.saperate.effects;

import com.iafenvoy.iceandfire.entity.EntityDragonBase;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class TestosteroneEffect extends StatusEffect {
    protected TestosteroneEffect() {
        super(StatusEffectCategory.NEUTRAL, 0xa6c6ff);
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        if(entity instanceof EntityDragonBase dragon){
            dragon.setGender(true);
        }
    }
}
