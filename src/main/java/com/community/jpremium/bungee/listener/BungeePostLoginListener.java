package com.community.jpremium.bungee.listener;

import com.community.jpremium.bungee.service.BungeeMessageService;
import com.community.jpremium.storage.UserProfileRepository;
import com.community.jpremium.common.util.ProfileDataUtils;
import com.community.jpremium.common.config.BungeeConfigService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeePostLoginListener
implements Listener {
    private final JPremium plugin;
    private final BungeeConfigService config;
    private final UserProfileRepository userRepository;
    private final BungeeMessageService messageService;

    public BungeePostLoginListener(JPremium plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.userRepository = plugin.getUserRepository();
        this.messageService = plugin.getMessageService();
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent postLoginEvent) {
        ProxiedPlayer proxiedPlayer = postLoginEvent.getPlayer();
        UserProfileData userProfile = this.plugin.getOnlineUserRegistry().findByUniqueId(proxiedPlayer.getUniqueId()).orElseThrow();
        Instant now = Instant.now();
        Instant sessionExpiresAt = userProfile.getSessionExpires();
        String lastKnownAddress = userProfile.getLastAddress();
        String currentAddress = proxiedPlayer.getAddress().getAddress().getHostAddress();
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
                    if (userProfile.wasServerRedirected()) {
                        userProfile.setServerRedirected(false);
                        this.messageService.sendMessageToUser(userProfile, "lastServerRedirection");
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
                if (userProfile.wasServerRedirected()) {
                    userProfile.setServerRedirected(false);
                    this.messageService.sendMessageToUser(userProfile, "lastServerRedirection");
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
