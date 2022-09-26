package com.ptsmods.chattix.forge;

import dev.architectury.platform.forge.EventBuses;
import com.ptsmods.chattix.Chattix;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Chattix.MOD_ID)
public class ChattixForge {
    public ChattixForge() {
        EventBuses.registerModEventBus(Chattix.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        Chattix.init();
    }
}
