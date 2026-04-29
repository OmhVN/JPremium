package com.community.jpremium.velocity.command;

import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.plugin.PluginContainer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class VelocityInfoCommand
implements RawCommand {
    private final Component infoMessage;

    public VelocityInfoCommand(JPremiumVelocity plugin) {
        String version = ((PluginContainer)plugin.getProxyServer().getPluginManager().getPlugin("jpremium").orElseThrow()).getDescription().getVersion().orElse("<unknown>");
        String message = String.format("&8[&a&l\u00bb&8] &7JPremium &a%s &7by &aJakubson", version);
        this.infoMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    public void executeCommand(RawCommand.Invocation invocation) {
        invocation.source().sendMessage(this.infoMessage);
    }

    @Override
    public void execute(RawCommand.Invocation invocation) {
        this.executeCommand(invocation);
    }
}
