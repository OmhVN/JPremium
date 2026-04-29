package com.community.jpremium.proxy.api.event.bungee;

import com.community.jpremium.proxy.api.user.User;
import java.util.Objects;
import java.util.Optional;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Event;

public class UserEvent
extends Event {
    private final User userProfile;
    private final CommandSender commandSender;

    public User getUser() {
        return this.userProfile;
    }

    public Optional<CommandSender> getCommandSender() {
        return Optional.ofNullable(this.commandSender);
    }

    public UserEvent(User userProfile, CommandSender commandSender) {
        Objects.requireNonNull(userProfile, "userProfile");
        this.userProfile = userProfile;
        this.commandSender = commandSender;
    }

    public static class FailedLogin
    extends UserEvent {
        private final Reason reason;

        public Reason getReason() {
            return this.reason;
        }

        public FailedLogin(User user, CommandSender commandSender, Reason reason) {
            super(user, commandSender);
            this.reason = reason;
        }

        public static enum Reason {
            WRONG_PASSWORD,
            WRONG_TOTP,
            TIMEOUT;

        }
    }

    public static class DeactivateSecondFactor
    extends UserEvent {
        public DeactivateSecondFactor(User user, CommandSender commandSender) {
            super(user, commandSender);
        }
    }

    public static class ActivateSecondFactor
    extends UserEvent {
        public ActivateSecondFactor(User user, CommandSender commandSender) {
            super(user, commandSender);
        }
    }

    public static class RequestSecondFactor
    extends UserEvent {
        public RequestSecondFactor(User user, CommandSender commandSender) {
            super(user, commandSender);
        }
    }

    public static class RecoveryPassword
    extends UserEvent {
        public RecoveryPassword(User user, CommandSender commandSender) {
            super(user, commandSender);
        }
    }

    public static class ChangeEmailAddress
    extends UserEvent {
        public ChangeEmailAddress(User user, CommandSender commandSender) {
            super(user, commandSender);
        }
    }

    public static class DestroySession
    extends UserEvent {
        public DestroySession(User user, CommandSender commandSender) {
            super(user, commandSender);
        }
    }

    public static class StartSession
    extends UserEvent {
        public StartSession(User user, CommandSender commandSender) {
            super(user, commandSender);
        }
    }

    public static class CreatePassword
    extends UserEvent {
        public CreatePassword(User user, CommandSender commandSender) {
            super(user, commandSender);
        }
    }

    public static class ChangePassword
    extends UserEvent {
        public ChangePassword(User user, CommandSender commandSender) {
            super(user, commandSender);
        }
    }

    public static class Cracked
    extends UserEvent {
        public Cracked(User user, CommandSender commandSender) {
            super(user, commandSender);
        }
    }

    public static class Premium
    extends UserEvent {
        public Premium(User user, CommandSender commandSender) {
            super(user, commandSender);
        }
    }

    public static class Unregister
    extends UserEvent {
        public Unregister(User user, CommandSender commandSender) {
            super(user, commandSender);
        }
    }

    public static class Register
    extends UserEvent {
        public Register(User user, CommandSender commandSender) {
            super(user, commandSender);
        }
    }

    public static class Login
    extends UserEvent {
        public Login(User user, CommandSender commandSender) {
            super(user, commandSender);
        }
    }
}
