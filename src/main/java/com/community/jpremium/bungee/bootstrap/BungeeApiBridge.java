package com.community.jpremium.bungee.bootstrap;

import com.community.jpremium.bungee.command.BungeeForceChangeEmailAddressCommand;
import com.community.jpremium.bungee.command.BungeeForceChangePasswordCommand;
import com.community.jpremium.bungee.command.BungeeForceCrackedCommand;
import com.community.jpremium.bungee.command.BungeeForceCreatePasswordCommand;
import com.community.jpremium.bungee.command.BungeeForceDestroySessionCommand;
import com.community.jpremium.bungee.command.BungeeForceLoginCommand;
import com.community.jpremium.bungee.command.BungeeForcePremiumCommand;
import com.community.jpremium.bungee.command.BungeeForcePurgeUserProfileCommand;
import com.community.jpremium.bungee.command.BungeeForceRegisterCommand;
import com.community.jpremium.bungee.command.BungeeForceStartSessionCommand;
import com.community.jpremium.bungee.command.BungeeForceUnregisterCommand;
import com.community.jpremium.security.PasswordHashService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.App;
import com.community.jpremium.proxy.api.user.User;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class BungeeApiBridge
implements App {
    private final JPremium plugin;
    private final BungeeForceLoginCommand forceLoginCommand;
    private final BungeeForceRegisterCommand forceRegisterCommand;
    private final BungeeForceUnregisterCommand forceUnregisterCommand;
    private final BungeeForceChangePasswordCommand forceChangePasswordCommand;
    private final BungeeForceCreatePasswordCommand forceCreatePasswordCommand;
    private final BungeeForcePremiumCommand forcePremiumCommand;
    private final BungeeForceCrackedCommand forceCrackedCommand;
    private final BungeeForceStartSessionCommand forceStartSessionCommand;
    private final BungeeForceDestroySessionCommand forceDestroySessionCommand;
    private final BungeeForceChangeEmailAddressCommand forceChangeEmailAddressCommand;
    private final BungeeForcePurgeUserProfileCommand forcePurgeProfileCommand;

    public BungeeApiBridge(JPremium plugin) {
        this.plugin = plugin;
        this.forceLoginCommand = new BungeeForceLoginCommand(plugin);
        this.forceRegisterCommand = new BungeeForceRegisterCommand(plugin);
        this.forceUnregisterCommand = new BungeeForceUnregisterCommand(plugin);
        this.forceChangePasswordCommand = new BungeeForceChangePasswordCommand(plugin);
        this.forceCreatePasswordCommand = new BungeeForceCreatePasswordCommand(plugin);
        this.forcePremiumCommand = new BungeeForcePremiumCommand(plugin);
        this.forceCrackedCommand = new BungeeForceCrackedCommand(plugin);
        this.forceStartSessionCommand = new BungeeForceStartSessionCommand(plugin);
        this.forceDestroySessionCommand = new BungeeForceDestroySessionCommand(plugin);
        this.forceChangeEmailAddressCommand = new BungeeForceChangeEmailAddressCommand(plugin);
        this.forcePurgeProfileCommand = new BungeeForcePurgeUserProfileCommand(plugin);
    }

    private static UserProfileData requireUserProfile(User user) {
        return (UserProfileData)Objects.requireNonNull(user, "user");
    }

    @Override
    public Set<? extends User> getUserProfiles() {
        return this.plugin.getOnlineUserRegistry().getOnlineProfiles();
    }

    @Override
    public Optional<? extends User> getUserProfileByUniqueId(UUID uniqueId) {
        return this.plugin.getOnlineUserRegistry().findByUniqueId(Objects.requireNonNull(uniqueId, "uniqueId"));
    }

    @Override
    public Optional<? extends User> getUserProfileByPremiumId(UUID premiumId) {
        return this.plugin.getOnlineUserRegistry().findByPremiumId(Objects.requireNonNull(premiumId, "premiumId"));
    }

    @Override
    public Optional<? extends User> getUserProfileByNickname(String nickname) {
        return this.plugin.getOnlineUserRegistry().findByNickname(Objects.requireNonNull(nickname, "nickname"));
    }

    @Override
    public Optional<? extends User> fetchUserProfileByUniqueId(UUID uniqueId) {
        return this.plugin.getUserRepository().findByUniqueId(Objects.requireNonNull(uniqueId, "uniqueId"));
    }

    @Override
    public Optional<? extends User> fetchUserProfileByPremiumId(UUID premiumId) {
        return this.plugin.getUserRepository().findByPremiumId(Objects.requireNonNull(premiumId, "premiumId"));
    }

    @Override
    public Optional<? extends User> fetchUserProfileByNickname(String nickname) {
        return this.plugin.getUserRepository().findByNickname(Objects.requireNonNull(nickname, "nickname"));
    }

    @Override
    public void forceLogin(User user) {
        this.forceLoginCommand.executeForUserApi(requireUserProfile(user));
    }

    @Override
    public void forceRegister(User user, String plainPassword) {
        this.forceRegisterCommand.executeForUserApi(requireUserProfile(user), plainPassword);
    }

    @Override
    public void forceUnregister(User user) {
        this.forceUnregisterCommand.executeForUserApi(requireUserProfile(user));
    }

    @Override
    public void forceChangePassword(User user, String plainPassword) {
        this.forceChangePasswordCommand.executeForUserApi(requireUserProfile(user), plainPassword);
    }

    @Override
    public void forceCreatePassword(User user, String plainPassword) {
        this.forceCreatePasswordCommand.executeForUserApi(requireUserProfile(user), plainPassword);
    }

    @Override
    public void forcePremium(User user) {
        this.forcePremiumCommand.executeForUserApi(requireUserProfile(user));
    }

    @Override
    public void forceCracked(User user) {
        this.forceCrackedCommand.executeForUserApi(requireUserProfile(user));
    }

    @Override
    public void forceStartSession(User user) {
        this.forceStartSessionCommand.executeForUserApi(requireUserProfile(user));
    }

    @Override
    public void forceDestroySession(User user) {
        this.forceDestroySessionCommand.executeForUserApi(requireUserProfile(user));
    }

    @Override
    public void forceChangeEmailAddress(User user, String emailAddress) {
        this.forceChangeEmailAddressCommand.executeForUserApi(requireUserProfile(user), emailAddress);
    }

    @Override
    public void forcePurgeUserProfile(User user) {
        this.forcePurgeProfileCommand.executeForUserApi(requireUserProfile(user));
    }

    @Override
    public void forceRecoveryPassword(User user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean validatePassword(User user, String plainPassword) {
        Objects.requireNonNull(user, "user");
        Objects.requireNonNull(plainPassword, "password");
        if (user.hasHashedPassword()) {
            return PasswordHashService.verifyPassword(plainPassword, user.getHashedPassword());
        }
        throw new IllegalStateException("This user does not have a password!");
    }

    @Override
    public boolean validateSecondFactorCode(User user, int verificationCode) {
        Objects.requireNonNull(user, "user");
        if (user.hasVerificationToken()) {
            return this.plugin.getGoogleAuthenticator().authorize(user.getVerificationToken(), verificationCode);
        }
        throw new IllegalStateException("This user does not have a second factor secret!");
    }
}

