package com.community.jpremium.bungee.command;

import com.community.jpremium.bungee.JPremium;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.PluginDescription;

public class BungeeInfoCommand
extends Command {
    private final BaseComponent[] infoMessage;

    public BungeeInfoCommand(JPremium plugin) {
        super("jbungee", "", "jpremium");
        PluginDescription pluginDescription = plugin.getDescription();
        String version = pluginDescription.getVersion();
        String message = String.format(ChatColor.translateAlternateColorCodes('&', "&8[&a&l\u00bb&8] &7JPremium &a%s &7by &aJakubson"), version);
        this.infoMessage = TextComponent.fromLegacyText(message);
    }

    @Override
    public void execute(CommandSender commandSender, String[] arguments) {
        commandSender.sendMessage(this.infoMessage);
    }
}

