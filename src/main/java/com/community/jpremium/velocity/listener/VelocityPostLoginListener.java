package com.community.jpremium.velocity.listener;

import com.community.jpremium.velocity.service.VelocityMessageService;
import com.community.jpremium.storage.UserProfileRepository;
import com.community.jpremium.common.util.ProfileDataUtils;
import com.community.jpremium.common.config.VelocityConfigService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class VelocityPostLoginListener {
    private final JPremiumVelocity plugin;
    private final VelocityConfigService config;
    private final UserProfileRepository userRepository;
    private final VelocityMessageService messageService;

    public VelocityPostLoginListener(JPremiumVelocity plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.userRepository = plugin.getUserRepository();
        this.messageService = plugin.getMessageService();
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent postLoginEvent) {
        Player player = postLoginEvent.getPlayer();
        UserProfileData userProfile = this.plugin.getOnlineUserRegistry().findByUniqueId(player.getUniqueId()).orElse(null);
        if (userProfile == null) {
            return;
        }
        Instant now = Instant.now();
        Instant sessionExpiresAt = userProfile.getSessionExpires();
        String lastKnownAddress = userProfile.getLastAddress();
        String currentAddress = player.getRemoteAddress().getAddress().getHostAddress();
        userProfile.setLoggedIn(userProfile.isPremium() || userProfile.isBedrock());
        if (userProfile.hasSession()) {
            if (now.isBefore(sessionExpiresAt) && currentAddress.equals(lastKnownAddress)) {
                userProfile.setLoggedIn(true);
            } else {
                userProfile.setSessionExpires(null);
                this.plugin.runAsync(() -> this.userRepository.update(userProfile));
            }
        }
        if (userProfile.isLogged()) {
            String joinMessagePath = userProfile.isBedrock() ? "joinSessionBedrock" : (userProfile.isPremium() ? "joinSessionPremium" : "joinSessionCracked");
            userProfile.setLastAddress(currentAddress);
            userProfile.setLastSeen(now);
            if (!userProfile.hasFirstAddress()) {
                userProfile.setFirstAddress(currentAddress);
            }
            if (!userProfile.hasFirstSeen()) {
                userProfile.setFirstSeen(now);
            }
            int titleDelayMillis = this.config.getInt("delayTitlesAfterJoinTime");
            this.plugin.runAsync(() -> this.userRepository.update(userProfile));
            this.plugin.fireEventAsync(new UserEvent.Login(userProfile, null));
            if (titleDelayMillis > 0) {
                this.plugin.scheduleDelayedTask(() -> {
                    this.messageService.sendTitleBundleToUser(userProfile, joinMessagePath);
                    if (!userProfile.isPremium() && !userProfile.isBedrock()) {
                        if (!userProfile.hasEmailAddress()) {
                            this.messageService.sendMessageToUser(userProfile, "loginReminderEmail");
                        }
                        if (!userProfile.hasVerificationToken()) {
                            this.messageService.sendMessageToUser(userProfile, "loginReminderSecondFactor");
                        }
                    }
                }, titleDelayMillis, TimeUnit.MILLISECONDS);
            } else {
                this.messageService.sendTitleBundleToUser(userProfile, joinMessagePath);
                if (!userProfile.isPremium() && !userProfile.isBedrock()) {
                    if (!userProfile.hasEmailAddress()) {
                        this.messageService.sendMessageToUser(userProfile, "loginReminderEmail");
                    }
                    if (!userProfile.hasVerificationToken()) {
                        this.messageService.sendMessageToUser(userProfile, "loginReminderSecondFactor");
                    }
                }
            }
        } else {
            int maximumAuthorizationTimeSeconds = this.config.getInt("maximumAuthorisationTime");
            int titleDelayMillis = this.config.getInt("delayTitlesAfterJoinTime");
            long loginDeadlineMillis = System.currentTimeMillis() + (long)maximumAuthorizationTimeSeconds * 1000L;
            boolean registered = userProfile.isRegistered();
            String captchaCode = ProfileDataUtils.generateNumericCode();
            String joinMessagePath;
            if (registered) {
                joinMessagePath = userProfile.hasVerificationToken() ? "joinRequireSecurityLogin" : "joinRequireLogin";
            } else {
                joinMessagePath = "joinRequireRegister";
            }
            userProfile.setCaptchaCode(captchaCode);
            userProfile.setLoginDeadlineMillis(loginDeadlineMillis);
            Runnable sendJoinTitles;
            if (registered) {
                sendJoinTitles = () -> this.messageService.sendTitleBundleToUser(userProfile, joinMessagePath);
            } else {
                sendJoinTitles = () -> this.messageService.sendTitleBundleToUser(userProfile, joinMessagePath, "%captcha_code%", captchaCode);
            }
            if (titleDelayMillis > 0) {
                this.plugin.scheduleDelayedTask(sendJoinTitles, titleDelayMillis, TimeUnit.MILLISECONDS);
            } else {
                sendJoinTitles.run();
            }
        }
    }
}
