package dev.saperate.effects;

import dev.saperate.DragonHRT;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class DragonHRTEffects {
    public static EstrogenEffect ESTROGEN_EFFECT = new EstrogenEffect();
    public static TestosteroneEffect TESTOSTERONE_EFFECT = new TestosteroneEffect();
    
    
    public static void register(){
        registerEffect("estrogen",ESTROGEN_EFFECT);
        registerEffect("testosterone",TESTOSTERONE_EFFECT);
    }
    
    private static void registerEffect(String identifier, StatusEffect statusEffect){
        Registry.register(Registries.STATUS_EFFECT, new Identifier(DragonHRT.MODID, identifier), statusEffect);
    }
}
