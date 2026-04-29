package com.community.jpremium.bungee.command;

import com.community.jpremium.common.util.ProfileDataUtils;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.TabExecutor;

public abstract class AbstractBungeeForceUserCommand
extends AbstractBungeeCommand
implements TabExecutor {
    public AbstractBungeeForceUserCommand(JPremium jPremium, String commandName) {
        super(jPremium, commandName, "jpremium.command." + commandName);
    }

    @Override
    public void executeCommand(CommandSender commandSender, String[] arguments) {
        if (arguments.length < 1) {
            this.executeForTarget(commandSender, null, arguments);
        } else {
            try {
                Optional<UserProfileData> targetUser;
                String targetIdentifier = arguments[0];
                Optional<UUID> parsedUniqueId = ProfileDataUtils.parseOptionalUuid(targetIdentifier);
                if (parsedUniqueId.isPresent()) {
                    UUID uniqueId = parsedUniqueId.get();
                    ProxiedPlayer proxiedPlayer = this.proxyServer.getPlayer(uniqueId);
                    targetUser = proxiedPlayer == null ? this.userRepository.findByUniqueId(uniqueId) : this.plugin.getOnlineUserRegistry().findByUniqueId(uniqueId);
                } else {
                    ProxiedPlayer proxiedPlayer = this.proxyServer.getPlayer(targetIdentifier);
                    targetUser = proxiedPlayer == null ? this.userRepository.findByNickname(targetIdentifier) : this.plugin.getOnlineUserRegistry().findByUniqueId(proxiedPlayer.getUniqueId());
                }
                if (targetUser.isPresent() && this.isPlayerSender(commandSender) && this.isProtectedAccount(targetUser.get())) {
                    this.messageService.sendMessage(commandSender, "forceForbiddenProtectedAccount");
                    return;
                }
                this.executeForTarget(commandSender, targetUser.orElse(null), arguments);
            }
            catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    public Iterable<String> onTabComplete(CommandSender commandSender, String[] arguments) {
        if (arguments.length != 1) {
            return Collections.emptyList();
        }
        String prefix = arguments[0].toLowerCase();
        return this.proxyServer.getPlayers().stream().map(CommandSender::getName).filter(name -> name.toLowerCase().startsWith(prefix)).collect(Collectors.toList());
    }

    public void executeForUserApi(UserProfileData userProfile, String ... arguments) {
        this.executeForTarget(null, userProfile, ProfileDataUtils.prepend(arguments, userProfile.getLastNickname()));
    }

    public abstract void executeForTarget(CommandSender commandSender, UserProfileData userProfile, String ... arguments);

    private boolean isPlayerSender(CommandSender commandSender) {
        return commandSender instanceof ProxiedPlayer;
    }

    private boolean isProtectedAccount(UserProfileData userProfile) {
        return this.config.getStringList("protectedAccounts").contains(userProfile.getUniqueId().toString());
    }
}
