package com.ptsmods.chattix.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hjson.JsonObject;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public class JoinLeaveConfig {
    public static final JoinLeaveConfig DEFAULT = new JoinLeaveConfig(true, "<yellow>%name% has joined the game</yellow>",
            "<yellow>%name% has joined the game (was %old_name%)</yellow>", "<yellow>%name% has left the game</yellow>");
    private final boolean enabled;
    private final String joinFormat, joinChangedNameFormat, leaveFormat;

    public static JoinLeaveConfig fromJson(JsonObject object) {
        return new JoinLeaveConfig(object.getBoolean("enabled", true),
                object.getString("join_format", DEFAULT.getJoinFormat()),
                object.getString("join_changed_name", DEFAULT.getJoinChangedNameFormat()),
                object.getString("leave_format", DEFAULT.getLeaveFormat()));
    }
}
