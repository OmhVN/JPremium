package com.community.jpremium.bungee.command;

import com.community.jpremium.bungee.JPremium;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class BungeeReloadCommand
extends Command {
    private final JPremium plugin;

    public BungeeReloadCommand(JPremium jPremium) {
        super("jreload", "jpremium.command.reload");
        this.plugin = jPremium;
    }

    @Override
    public void execute(CommandSender commandSender, String[] arguments) {
        try {
            this.plugin.getConfig().reload();
            this.plugin.getMessagesConfig().reload();
            this.plugin.loadWeakPasswordSet();
            this.plugin.loadRecoveryTemplate();
            if (this.plugin.initializeAccessTokenIfNeeded()) {
                commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&8[&a&l\u00bb&8] &7JPremium configuration files have been reloaded! Please notice that some features require server re-join or server restart to reload!")));
            }
        }
        catch (Exception exception) {
            commandSender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&8[&c&l\u00bb&8] &7Could not reload JPremium configuration files! Please go to the BungeeCord console for more information!")));
            exception.printStackTrace();
        }
    }
}
