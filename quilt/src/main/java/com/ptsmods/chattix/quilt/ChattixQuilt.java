package com.ptsmods.chattix.quilt;

import com.ptsmods.chattix.fabriclike.ChattixFabricLike;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

@SuppressWarnings("unused") // It is used
public class ChattixQuilt implements ModInitializer {
    @Override
    public void onInitialize(ModContainer mod) {
        ChattixFabricLike.init();
    }
}
