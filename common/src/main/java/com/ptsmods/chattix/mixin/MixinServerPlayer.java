package com.ptsmods.chattix.mixin;

import com.ptsmods.chattix.util.ServerPlayerAddon;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Getter @Setter
@Mixin(ServerPlayer.class)
public class MixinServerPlayer implements ServerPlayerAddon {
    private @Unique boolean firstTimePlaying;
}
