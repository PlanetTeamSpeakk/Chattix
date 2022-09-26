package com.ptsmods.chattix.mixin;

import com.ptsmods.chattix.config.Config;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServer.class)
public class MixinDedicatedServer {

    @Inject(at = @At("RETURN"), method = "previewsChat", cancellable = true)
    private void previewsChat(CallbackInfoReturnable<Boolean> cbi) {
        if (Config.getInstance().getFormattingConfig().isEnabled()) cbi.setReturnValue(true);
    }
}
