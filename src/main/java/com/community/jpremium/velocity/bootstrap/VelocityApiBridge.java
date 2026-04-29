package com.community.jpremium.velocity.bootstrap;

import com.community.jpremium.velocity.command.VelocityForcePremiumCommand;
import com.community.jpremium.velocity.command.VelocityForcePurgeUserProfileCommand;
import com.community.jpremium.velocity.command.VelocityForceRegisterCommand;
import com.community.jpremium.velocity.command.VelocityForceStartSessionCommand;
import com.community.jpremium.velocity.command.VelocityForceUnregisterCommand;
import com.community.jpremium.velocity.command.VelocityForceChangeEmailAddressCommand;
import com.community.jpremium.velocity.command.VelocityForceChangePasswordCommand;
import com.community.jpremium.velocity.command.VelocityForceCrackedCommand;
import com.community.jpremium.velocity.command.VelocityForceCreatePasswordCommand;
import com.community.jpremium.velocity.command.VelocityForceDestroySessionCommand;
import com.community.jpremium.velocity.command.VelocityForceLoginCommand;
import com.community.jpremium.security.PasswordHashService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.proxy.api.App;
import com.community.jpremium.proxy.api.user.User;
import com.community.jpremium.velocity.JPremiumVelocity;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class VelocityApiBridge
implements App {
    private final JPremiumVelocity plugin;
    private final VelocityForceLoginCommand forceLoginCommand;
    private final VelocityForceRegisterCommand forceRegisterCommand;
    private final VelocityForceUnregisterCommand forceUnregisterCommand;
    private final VelocityForceChangePasswordCommand forceChangePasswordCommand;
    private final VelocityForceCreatePasswordCommand forceCreatePasswordCommand;
    private final VelocityForcePremiumCommand forcePremiumCommand;
    private final VelocityForceCrackedCommand forceCrackedCommand;
    private final VelocityForceStartSessionCommand forceStartSessionCommand;
    private final VelocityForceDestroySessionCommand forceDestroySessionCommand;
    private final VelocityForceChangeEmailAddressCommand forceChangeEmailAddressCommand;
    private final VelocityForcePurgeUserProfileCommand forcePurgeProfileCommand;

    public VelocityApiBridge(JPremiumVelocity plugin) {
        this.plugin = plugin;
        this.forceLoginCommand = new VelocityForceLoginCommand(plugin);
        this.forceRegisterCommand = new VelocityForceRegisterCommand(plugin);
        this.forceUnregisterCommand = new VelocityForceUnregisterCommand(plugin);
        this.forceChangePasswordCommand = new VelocityForceChangePasswordCommand(plugin);
        this.forceCreatePasswordCommand = new VelocityForceCreatePasswordCommand(plugin);
        this.forcePremiumCommand = new VelocityForcePremiumCommand(plugin);
        this.forceCrackedCommand = new VelocityForceCrackedCommand(plugin);
        this.forceStartSessionCommand = new VelocityForceStartSessionCommand(plugin);
        this.forceDestroySessionCommand = new VelocityForceDestroySessionCommand(plugin);
        this.forceChangeEmailAddressCommand = new VelocityForceChangeEmailAddressCommand(plugin);
        this.forcePurgeProfileCommand = new VelocityForcePurgeUserProfileCommand(plugin);
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

