package com.community.jpremium.velocity.command;

import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import java.util.Locale;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public abstract class AbstractVelocityPlayerCommand
extends AbstractVelocityCommand {
    private final Component playerOnlyMessage;

    protected AbstractVelocityPlayerCommand(JPremiumVelocity jPremiumVelocity, String commandName) {
        super(jPremiumVelocity, commandName, null);
        this.playerOnlyMessage = LegacyComponentSerializer.legacyAmpersand().deserialize("&cThat command can be only executed by a player! Alternative command: /force" + this.commandName.toLowerCase(Locale.ROOT));
    }

    @Override
    public void executeCommand(CommandSource commandSource, String[] arguments) {
        if (commandSource instanceof Player) {
            Player player = (Player)commandSource;
            Optional<UserProfileData> optional = this.plugin.getOnlineUserRegistry().findByUniqueId(player.getUniqueId());
            if (optional.isEmpty()) {
                this.plugin.getLogger().warning("Could not execute command %s for player %s due to missing user!".formatted(this.getCommandName(), player.getUsername()));
                return;
            }
            this.executeForPlayer(player, optional.get(), arguments);
        } else {
            commandSource.sendMessage(this.playerOnlyMessage);
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

    public abstract void executeForPlayer(Player player, UserProfileData userProfile, String[] arguments);
}

