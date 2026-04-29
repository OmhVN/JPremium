package com.community.jpremium.velocity.command;

import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.RawCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class VelocityReloadCommand
implements RawCommand {
    private final JPremiumVelocity plugin;

    public VelocityReloadCommand(JPremiumVelocity jPremiumVelocity) {
        this.plugin = jPremiumVelocity;
    }

    public void executeCommand(RawCommand.Invocation invocation) {
        try {
            this.plugin.getConfig().reload();
            this.plugin.getMessagesConfig().reload();
            this.plugin.loadWeakPasswordSet();
            this.plugin.loadRecoveryTemplate();
            if (this.plugin.initializeAccessTokenIfNeeded()) {
                invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&8[&a&l\u00bb&8] &7JPremium configuration files have been reloaded! Please notice that some features require server re-join or server restart to reload!"));
            }
        }
        catch (Exception exception) {
            invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&8[&c&l\u00bb&8] &7Could not reload JPremium configuration files! Please go to the BungeeCord console for more information!"));
            exception.printStackTrace();
        }
    }

    public boolean checkPermission(RawCommand.Invocation invocation) {
        return invocation.source().hasPermission("jpremium.command.reload");
    }

    @Override
    public boolean hasPermission(RawCommand.Invocation invocation) {
        return this.checkPermission(invocation);
    }

    @Override
    public void execute(RawCommand.Invocation invocation) {
        this.executeCommand(invocation);
    }
}
