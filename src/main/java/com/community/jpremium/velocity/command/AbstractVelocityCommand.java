package com.community.jpremium.velocity.command;

import com.community.jpremium.velocity.service.VelocityMessageService;
import com.community.jpremium.storage.UserProfileRepository;
import com.community.jpremium.velocity.service.VelocityServerRoutingService;
import com.community.jpremium.common.config.VelocityConfigService;
import com.community.jpremium.proxy.api.resolver.Resolver;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.identity.Identity;

public abstract class AbstractVelocityCommand
implements SimpleCommand {
    protected final JPremiumVelocity plugin;
    protected final ProxyServer proxyServer;
    protected final VelocityConfigService config;
    protected final UserProfileRepository userRepository;
    protected final VelocityMessageService messageService;
    protected final VelocityServerRoutingService routingService;
    protected final Resolver profileResolver;
    protected final String commandName;
    protected final String permission;

    public String getCommandName() {
        return this.commandName;
    }

    public String getPermission() {
        return this.permission;
    }

    public void register() {
        this.proxyServer.getCommandManager().register(this.commandName, this, this.plugin.getCommandAliases(this.commandName));
    }

    protected AbstractVelocityCommand(JPremiumVelocity jPremiumVelocity, String commandName, String permission) {
        this.plugin = jPremiumVelocity;
        this.proxyServer = jPremiumVelocity.getProxyServer();
        this.config = jPremiumVelocity.getConfig();
        this.userRepository = jPremiumVelocity.getUserRepository();
        this.messageService = jPremiumVelocity.getMessageService();
        this.routingService = jPremiumVelocity.getRoutingService();
        this.profileResolver = jPremiumVelocity.getProfileResolver();
        this.commandName = commandName;
        this.permission = permission;
    }

    public void executeAsync(SimpleCommand.Invocation invocation) {
        this.plugin.runAsync(() -> {
            try {
                this.executeCommand(invocation.source(), (String[])invocation.arguments());
            }
            catch (Throwable throwable) {
                this.plugin.getLogger().severe("An error occurred during processing %s command for %s".formatted(this.commandName, invocation.source().get(Identity.NAME)));
                throwable.printStackTrace();
            }
        });
    }

    public boolean checkPermission(SimpleCommand.Invocation invocation) {
        return this.permission == null || invocation.source().hasPermission(this.permission);
    }

    public abstract void executeCommand(CommandSource commandSource, String[] arguments);

    @Override
    public boolean hasPermission(SimpleCommand.Invocation invocation) {
        return this.checkPermission(invocation);
    }

    @Override
    public void execute(SimpleCommand.Invocation invocation) {
        this.executeAsync(invocation);
    }
}
