package com.ptsmods.chattix.util;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import net.minecraft.network.chat.ChatDecorator;

@UtilityClass
public class MixinHelper {
    /**
     * Serves as an override for the decorator used when
     * resolving message arguments.
     */
    @Getter @Setter
    private static ChatDecorator decoratorOverride;
}
