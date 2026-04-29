package com.community.jpremium.bungee.command;

import com.community.jpremium.bungee.service.BungeeMessageService;
import com.community.jpremium.storage.UserProfileRepository;
import com.community.jpremium.bungee.service.BungeeServerRoutingService;
import com.community.jpremium.common.config.BungeeConfigService;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.resolver.Resolver;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;

public abstract class AbstractBungeeCommand
extends Command {
    protected final JPremium plugin;
    protected final ProxyServer proxyServer;
    protected final BungeeConfigService config;
    protected final UserProfileRepository userRepository;
    protected final BungeeMessageService messageService;
    protected final BungeeServerRoutingService routingService;
    protected final Resolver profileResolver;

    protected AbstractBungeeCommand(JPremium jPremium, String commandName, String permission) {
        super(commandName, permission, jPremium.getCommandAliases(commandName));
        this.plugin = jPremium;
        this.proxyServer = jPremium.getProxy();
        this.config = jPremium.getConfig();
        this.userRepository = jPremium.getUserRepository();
        this.messageService = jPremium.getMessageService();
        this.routingService = jPremium.getRoutingService();
        this.profileResolver = jPremium.getProfileResolver();
    }

    public void execute(CommandSender commandSender, String[] arguments) {
        this.plugin.runAsync(() -> {
            try {
                this.executeCommand(commandSender, arguments);
            }
            catch (Throwable throwable) {
                this.plugin.getLogger().severe("An error occurred during processing %s command for %s".formatted(this.getName(), commandSender.getName()));
                throwable.printStackTrace();
            }
        });
    }

    public abstract void executeCommand(CommandSender commandSender, String[] arguments);
}

