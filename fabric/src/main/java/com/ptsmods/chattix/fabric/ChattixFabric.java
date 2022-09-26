package com.ptsmods.chattix.fabric;

import com.ptsmods.chattix.fabriclike.ChattixFabricLike;
import net.fabricmc.api.ModInitializer;

public class ChattixFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ChattixFabricLike.init();

    }
}
