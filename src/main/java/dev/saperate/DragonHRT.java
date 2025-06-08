package dev.saperate;

import dev.saperate.effects.DragonHRTEffects;
import dev.saperate.items.DragonHRTITems;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DragonHRT implements ModInitializer {
	public static final String MODID = "dragonhrt";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {
		LOGGER.info("Hello from DragonHRT! :3");
		DragonHRTITems.register();
		DragonHRTEffects.register();
	}
}