package com.community.jpremium.bungee.command;

import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import java.util.Optional;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public abstract class AbstractBungeePlayerCommand
extends AbstractBungeeCommand {
    private final BaseComponent playerOnlyMessage = TextComponent.fromLegacy("\u00a7cThat command can be only executed by a player! Alternative command: /force" + this.getName().toLowerCase());

    public AbstractBungeePlayerCommand(JPremium jPremium, String commandName) {
        super(jPremium, commandName, null);
    }

    @Override
    public void executeCommand(CommandSender commandSender, String[] arguments) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer proxiedPlayer = (ProxiedPlayer)commandSender;
            Optional<UserProfileData> optional = this.plugin.getOnlineUserRegistry().findByUniqueId(proxiedPlayer.getUniqueId());
            if (optional.isEmpty()) {
                this.plugin.getLogger().warning("Could not execute command %s for player %s due to missing user!".formatted(this.getName(), proxiedPlayer.getName()));
                return;
            }
            this.executeForPlayer(proxiedPlayer, optional.get(), arguments);
        } else {
            commandSender.sendMessage(this.playerOnlyMessage);
        }
    }

    protected void runOrQueueConfirmation(UserProfileData userProfile, String messagePath, Runnable runnable) {
        if (this.config.getBoolean("riskyCommandsConfirmation")) {
            this.messageService.sendMessageToUser(userProfile, messagePath);
            userProfile.setPendingConfirmationAction(runnable);
        } else {
            runnable.run();
        }
    }

    public abstract void executeForPlayer(ProxiedPlayer player, UserProfileData userProfile, String[] arguments);
}

