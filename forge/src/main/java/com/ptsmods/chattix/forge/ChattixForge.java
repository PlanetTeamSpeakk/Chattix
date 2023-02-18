package com.ptsmods.chattix.forge;

import com.ptsmods.chattix.Chattix;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;

import java.util.HashMap;
import java.util.Map;

@Mod(Chattix.MOD_ID)
public class ChattixForge {
    private static final Map<String, PermissionNode<?>> permissionNodes = new HashMap<>();
    private static boolean registeredPermissions = false;

    public ChattixForge() {
        EventBuses.registerModEventBus(Chattix.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        MinecraftForge.EVENT_BUS.addListener(this::onPermissionGather);
        Chattix.init();
    }

    // They are of type Boolean, but they have to be of a wildcard type,
    // so I can pass permissionNodes.values() in onPermissionGather.
    @SuppressWarnings("unchecked")
    public static PermissionNode<Boolean> getPermission(String node) {
        return (PermissionNode<Boolean>) permissionNodes.get(node);
    }

    public static void registerPermission(String permission, boolean defaultValue) {
        if (registeredPermissions) return;

        permissionNodes.put(permission, new PermissionNode<>(Chattix.MOD_ID, permission.startsWith("chattix.") ?
                permission.substring(9) : permission, PermissionTypes.BOOLEAN, (player, uuid, permissionDynamicContext) -> defaultValue));
    }

    public void onPermissionGather(PermissionGatherEvent.Nodes event) {
        event.addNodes(permissionNodes.values());
        registeredPermissions = true;
    }
}
