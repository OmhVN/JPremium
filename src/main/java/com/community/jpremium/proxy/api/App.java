package com.community.jpremium.proxy.api;

import com.community.jpremium.proxy.api.user.User;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface App {
    public Set<? extends User> getUserProfiles();

    public Optional<? extends User> getUserProfileByUniqueId(UUID uniqueId);

    public Optional<? extends User> getUserProfileByPremiumId(UUID premiumId);

    public Optional<? extends User> getUserProfileByNickname(String nickname);

    public Optional<? extends User> fetchUserProfileByUniqueId(UUID uniqueId);

    public Optional<? extends User> fetchUserProfileByPremiumId(UUID premiumId);

    public Optional<? extends User> fetchUserProfileByNickname(String nickname);

    public void forceLogin(User user);

    public void forceRegister(User user, String plainPassword);

    public void forceUnregister(User user);

    public void forceChangePassword(User user, String plainPassword);

    public void forceCreatePassword(User user, String plainPassword);

    public void forcePremium(User user);

    public void forceCracked(User user);

    public void forceStartSession(User user);

    public void forceDestroySession(User user);

    public void forceChangeEmailAddress(User user, String emailAddress);

    public void forcePurgeUserProfile(User user);

    @Deprecated
    public void forceRecoveryPassword(User user);

    public boolean validatePassword(User user, String plainPassword);

    public boolean validateSecondFactorCode(User user, int verificationCode);

    public static App getApp() {
        return JPremiumApi.getApp();
    }
}
