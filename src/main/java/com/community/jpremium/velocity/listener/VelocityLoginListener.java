package com.community.jpremium.velocity.listener;

import com.community.jpremium.velocity.service.VelocityMessageService;
import com.community.jpremium.storage.UserProfileRepository;
import com.community.jpremium.common.util.ProfileDataUtils;
import com.community.jpremium.common.config.VelocityConfigService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import java.time.Instant;
import java.util.UUID;
import net.kyori.adventure.text.Component;

public class VelocityLoginListener {
    private final JPremiumVelocity plugin;
    private final VelocityConfigService config;
    private final UserProfileRepository userRepository;
    private final VelocityMessageService messageService;

    public VelocityLoginListener(JPremiumVelocity plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.userRepository = plugin.getUserRepository();
        this.messageService = plugin.getMessageService();
    }

    @Subscribe(order=PostOrder.FIRST)
    public EventTask onLogin(LoginEvent loginEvent) {
        String address = loginEvent.getPlayer().getRemoteAddress().getAddress().getHostAddress();
        String username = loginEvent.getPlayer().getUsername();
        UUID uniqueId = loginEvent.getPlayer().getUniqueId();
        Component userNotLoadedMessage = this.messageService.buildComponentMessage("preLoginErrorUserNotLoaded", username);
        UserProfileData userProfile = this.plugin.getOnlineUserRegistry().findByUniqueId(uniqueId).orElse(null);
        if (userProfile == null) {
            boolean floodgateSupportEnabled = this.config.getBoolean("floodgateSupport");
            if (floodgateSupportEnabled && uniqueId.getMostSignificantBits() == 0L) {
                return EventTask.async(() -> {
                    UserProfileData existingUserProfile = this.userRepository.findByUniqueId(uniqueId).orElse(null);
                    if (existingUserProfile != null) {
                        this.plugin.getOnlineUserRegistry().add(existingUserProfile);
                        if (!username.equals(existingUserProfile.getLastNickname())) {
                            existingUserProfile.setLastNickname(username);
                            this.userRepository.update(existingUserProfile);
                        }
                    } else {
                        UserProfileData premiumUserProfile = new UserProfileData(uniqueId);
                        premiumUserProfile.setLastNickname(username);
                        premiumUserProfile.setFirstAddress(address);
                        premiumUserProfile.setFirstSeen(Instant.now());
                        this.userRepository.insert(premiumUserProfile);
                        this.plugin.getOnlineUserRegistry().add(premiumUserProfile);
                        this.plugin.fireEventAsync(new UserEvent.Register(premiumUserProfile, null));
                    }
                });
            }
            if (floodgateSupportEnabled && !ProfileDataUtils.compileUsernamePatternOrDefault(this.config.getString("allowedUsernamePattern")).matcher(username).matches()) {
                loginEvent.setResult(ResultedEvent.ComponentResult.denied(this.messageService.buildComponentMessage("preLoginErrorInvalidNickname", username)));
                return null;
            }
            loginEvent.setResult(ResultedEvent.ComponentResult.denied(userNotLoadedMessage));
        }
        return null;
    }
}
