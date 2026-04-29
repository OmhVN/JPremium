package com.community.jpremium.velocity.command;

import com.community.jpremium.common.util.ProfileDataUtils;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import java.util.Optional;
import java.util.UUID;

public abstract class AbstractVelocityForceUserCommand
extends AbstractVelocityCommand {
    protected AbstractVelocityForceUserCommand(JPremiumVelocity jPremiumVelocity, String commandName) {
        super(jPremiumVelocity, commandName, "jpremium.command." + commandName);
    }

    @Override
    public void executeCommand(CommandSource commandSource, String[] arguments) {
        if (arguments.length < 1) {
            this.executeForTarget(commandSource, null, arguments);
        } else {
            try {
                Optional<UserProfileData> targetUser;
                String targetIdentifier = arguments[0];
                Optional<UUID> parsedUniqueId = ProfileDataUtils.parseOptionalUuid(targetIdentifier);
                if (parsedUniqueId.isPresent()) {
                    UUID uniqueId = parsedUniqueId.get();
                    Optional<Player> proxyPlayer = this.proxyServer.getPlayer(uniqueId);
                    targetUser = proxyPlayer.isEmpty() ? this.userRepository.findByUniqueId(uniqueId) : this.plugin.getOnlineUserRegistry().findByUniqueId(uniqueId);
                } else {
                    Optional<Player> proxyPlayer = this.proxyServer.getPlayer(targetIdentifier);
                    targetUser = proxyPlayer.isEmpty() ? this.userRepository.findByNickname(targetIdentifier) : this.plugin.getOnlineUserRegistry().findByUniqueId(proxyPlayer.get().getUniqueId());
                }
                if (targetUser.isPresent() && this.isNonConsoleSource(commandSource) && this.isProtectedAccount(targetUser.get())) {
                    this.messageService.sendMessage(commandSource, "forceForbiddenProtectedAccount");
                    return;
                }
                this.executeForTarget(commandSource, targetUser.orElse(null), arguments);
            }
            catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    private boolean isNonConsoleSource(CommandSource commandSource) {
        return !(commandSource instanceof ConsoleCommandSource);
    }

    private boolean isProtectedAccount(UserProfileData userProfile) {
        return this.config.getStringList("protectedAccounts").contains(userProfile.getUniqueId().toString());
    }

    public void executeForUserApi(UserProfileData userProfile, String ... arguments) {
        this.executeForTarget(null, userProfile, ProfileDataUtils.prepend(arguments, userProfile.getLastNickname()));
    }

    public abstract void executeForTarget(CommandSource commandSource, UserProfileData userProfile, String ... arguments);
}

