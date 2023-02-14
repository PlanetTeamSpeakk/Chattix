package com.ptsmods.chattix.mixin;

import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ChatTypeDecoration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChatType.class)
public interface MixinChatTypeAccessor {
    @Accessor @Mutable
    void setChat(ChatTypeDecoration chat);
}
