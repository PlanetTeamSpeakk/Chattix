package com.ptsmods.chattix.mixin;

import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Style.class)
public interface MixinStyleAccessor {
    @Accessor Boolean getBold();
    @Accessor Boolean getItalic();
    @Accessor Boolean getUnderlined();
    @Accessor Boolean getStrikethrough();
    @Accessor Boolean getObfuscated();
}
