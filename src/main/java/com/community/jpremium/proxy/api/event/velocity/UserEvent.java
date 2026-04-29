package com.community.jpremium.proxy.api.event.velocity;

import com.community.jpremium.proxy.api.user.User;
import com.velocitypowered.api.command.CommandSource;
import java.util.Objects;
import java.util.Optional;

public class UserEvent {
    private final User userProfile;
    private final CommandSource commandSource;

    public User getUser() {
        return this.userProfile;
    }

    public Optional<CommandSource> getCommandSource() {
        return Optional.ofNullable(this.commandSource);
    }

    public UserEvent(User userProfile, CommandSource commandSource) {
        Objects.requireNonNull(userProfile, "userProfile");
        this.userProfile = userProfile;
        this.commandSource = commandSource;
    }

    public static class FailedLogin
    extends UserEvent {
        private final Reason reason;

        public Reason getReason() {
            return this.reason;
        }

        public FailedLogin(User user, CommandSource commandSource, Reason reason) {
            super(user, commandSource);
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
        public DeactivateSecondFactor(User user, CommandSource commandSource) {
            super(user, commandSource);
        }
    }

    public static class ActivateSecondFactor
    extends UserEvent {
        public ActivateSecondFactor(User user, CommandSource commandSource) {
            super(user, commandSource);
        }
    }

    public static class RequestSecondFactor
    extends UserEvent {
        public RequestSecondFactor(User user, CommandSource commandSource) {
            super(user, commandSource);
        }
    }

    public static class RecoveryPassword
    extends UserEvent {
        public RecoveryPassword(User user, CommandSource commandSource) {
            super(user, commandSource);
        }
    }

    public static class ChangeEmailAddress
    extends UserEvent {
        public ChangeEmailAddress(User user, CommandSource commandSource) {
            super(user, commandSource);
        }
    }

    public static class DestroySession
    extends UserEvent {
        public DestroySession(User user, CommandSource commandSource) {
            super(user, commandSource);
        }
    }

    public static class StartSession
    extends UserEvent {
        public StartSession(User user, CommandSource commandSource) {
            super(user, commandSource);
        }
    }

    public static class CreatePassword
    extends UserEvent {
        public CreatePassword(User user, CommandSource commandSource) {
            super(user, commandSource);
        }
    }

    public static class ChangePassword
    extends UserEvent {
        public ChangePassword(User user, CommandSource commandSource) {
            super(user, commandSource);
        }
    }

    public static class Cracked
    extends UserEvent {
        public Cracked(User user, CommandSource commandSource) {
            super(user, commandSource);
        }
    }

    public static class Premium
    extends UserEvent {
        public Premium(User user, CommandSource commandSource) {
            super(user, commandSource);
        }
    }

    public static class Unregister
    extends UserEvent {
        public Unregister(User user, CommandSource commandSource) {
            super(user, commandSource);
        }
    }

    public static class Register
    extends UserEvent {
        public Register(User user, CommandSource commandSource) {
            super(user, commandSource);
        }
    }

    public static class Login
    extends UserEvent {
        public Login(User user, CommandSource commandSource) {
            super(user, commandSource);
        }
    }
}
