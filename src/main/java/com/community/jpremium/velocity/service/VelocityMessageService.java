package com.community.jpremium.velocity.service;

import com.community.jpremium.common.config.VelocityConfigService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import jakarta.activation.CommandMap;
import jakarta.activation.MailcapCommandMap;
import jakarta.mail.Message;
import jakarta.mail.Provider;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;

public class VelocityMessageService {
    private final JPremiumVelocity plugin;
    private final VelocityConfigService config;
    private final VelocityConfigService messages;
    private final Map<UUID, BossBar> bossBarsByUserId = new ConcurrentHashMap<>();
    private static final Provider SMTP_PROVIDER = new Provider(Provider.Type.TRANSPORT, "smtp", "com.community.jpremium.library.com.sun.mail.smtp.SMTPTransport", "Oracle", "2.0.1");

    public VelocityMessageService(JPremiumVelocity jPremiumVelocity) {
        this.plugin = jPremiumVelocity;
        this.config = jPremiumVelocity.getConfig();
        this.messages = jPremiumVelocity.getMessagesConfig();
        jPremiumVelocity.scheduleRepeatingTask(this::tickAuthorizationTimers, 1L, TimeUnit.SECONDS);
    }

    public Component buildComponentMessage(String messageKey, String nickname, String ... arguments) {
        String messageTemplate = this.messages.getString(messageKey);
        if (messageTemplate == null) {
            return Component.text("<undefined path: " + messageKey + ">");
        }
        if (messageTemplate.isEmpty()) {
            return null;
        }
        for (int i = 0; i < arguments.length; i += 2) {
            messageTemplate = messageTemplate.replace(arguments[i], arguments[i + 1]);
        }
        if (nickname != null) {
            messageTemplate = messageTemplate.replace("%nickname%", nickname);
        }
        if (messageTemplate.startsWith("<")) {
            return MiniMessage.miniMessage().deserialize(messageTemplate);
        }
        return LegacyComponentSerializer.legacyAmpersand().deserialize(messageTemplate);
    }

    public void sendMessage(CommandSource commandSource, String messageKey) {
        Component component;
        if (commandSource != null && (component = this.buildComponentMessage(messageKey, "")) != null) {
            commandSource.sendMessage(component);
        }
    }

    public void sendMessageWithNickname(CommandSource commandSource, String nickname, String messageKey, String ... arguments) {
        Component component;
        if (commandSource != null && (component = this.buildComponentMessage(messageKey, nickname, arguments)) != null) {
            commandSource.sendMessage(component);
        }
    }

    public void sendMessageForUser(CommandSource commandSource, UserProfileData userProfile, String messageKey, String ... arguments) {
        String nickname;
        Component component;
        if (commandSource != null && (component = this.buildComponentMessage(messageKey, nickname = userProfile.getLastNickname(), arguments)) != null) {
            commandSource.sendMessage(component);
        }
    }

    public void sendMessageToUser(UserProfileData userProfile, String messageKey, String ... arguments) {
        String nickname;
        Component component;
        Player player = this.plugin.findPlayer(userProfile);
        if (player != null && (component = this.buildComponentMessage(messageKey, nickname = player.getUsername(), arguments)) != null) {
            player.sendMessage(component);
        }
    }

    public void disconnectUserWithMessage(UserProfileData userProfile, String messageKey, String ... arguments) {
        Player player = this.plugin.findPlayer(userProfile);
        if (player != null) {
            String nickname = player.getUsername();
            Component component = this.buildComponentMessage(messageKey, nickname, arguments);
            player.disconnect(component);
        }
    }

    public void sendActionBarToUser(UserProfileData userProfile, String messageKey, String ... arguments) {
        Player player = this.plugin.findPlayer(userProfile);
        if (player != null) {
            String nickname = player.getUsername();
            ProtocolVersion protocolVersion = player.getProtocolVersion();
            Component component = this.buildComponentMessage(messageKey, nickname, arguments);
            if (component != null && protocolVersion.getProtocol() >= 47) {
                player.sendActionBar(component);
            }
        }
    }

    public void clearActionBar(UserProfileData userProfile) {
        ProtocolVersion protocolVersion;
        Player player = this.plugin.findPlayer(userProfile);
        if (player != null && (protocolVersion = player.getProtocolVersion()).getProtocol() >= 47) {
            player.sendActionBar(Component.empty());
        }
    }

    public void sendTitleBundleToUser(UserProfileData userProfile, String messageKeyPrefix, String ... arguments) {
        Player player = this.plugin.findPlayer(userProfile);
        if (player == null) {
            return;
        }
        ProtocolVersion protocolVersion = player.getProtocolVersion();
        String nickname = player.getUsername();
        Component titleComponent = this.buildComponentMessage(messageKeyPrefix.concat("Title"), nickname, arguments);
        Component subtitleComponent = this.buildComponentMessage(messageKeyPrefix.concat("SubTitle"), nickname, arguments);
        Component chatComponent = this.buildComponentMessage(messageKeyPrefix.concat("Chat"), nickname, arguments);
        int fadeInTicks = this.messages.getInt(messageKeyPrefix.concat("FadeIn"));
        int stayTicks = this.messages.getInt(messageKeyPrefix.concat("Stay"));
        int fadeOutTicks = this.messages.getInt(messageKeyPrefix.concat("FadeOut"));
        int protocolVersionId = protocolVersion.getProtocol();
        if (titleComponent != null && subtitleComponent != null && protocolVersionId >= 47) {
            player.showTitle(Title.title(titleComponent, subtitleComponent, Title.Times.times(Ticks.duration((long)fadeInTicks), Ticks.duration((long)stayTicks), Ticks.duration((long)fadeOutTicks))));
        }
        if (chatComponent != null) {
            player.sendMessage(chatComponent);
        }
    }

    public void sendBossBarTimerToUser(UserProfileData userProfile, String messageKeyPrefix, float progress, String ... arguments) {
        Player player = this.plugin.findPlayer(userProfile);
        if (player == null) {
            return;
        }
        String nickname = player.getUsername();
        ProtocolVersion protocolVersion = player.getProtocolVersion();
        Component titleComponent = this.buildComponentMessage(messageKeyPrefix.concat("Title"), nickname, arguments);
        String colorName = this.messages.getString(messageKeyPrefix.concat("Color")).toLowerCase(Locale.ROOT);
        String overlayName = this.messages.getString(messageKeyPrefix.concat("Division")).toLowerCase(Locale.ROOT);
        int protocolVersionId = protocolVersion.getProtocol();
        if (titleComponent != null && protocolVersionId >= 107) {
            if (this.bossBarsByUserId.containsKey(userProfile.getUniqueId())) {
                BossBar bossBar = this.bossBarsByUserId.get(userProfile.getUniqueId());
                bossBar.progress(progress);
                bossBar.name(titleComponent);
            } else {
                BossBar.Color color;
                BossBar.Overlay overlay;
                try {
                    color = BossBar.Color.valueOf(colorName.toUpperCase(Locale.ROOT));
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    color = BossBar.Color.YELLOW;
                }
                try {
                    overlay = BossBar.Overlay.valueOf(overlayName.toUpperCase(Locale.ROOT));
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    overlay = BossBar.Overlay.NOTCHED_6;
                }
                BossBar bossBar = BossBar.bossBar(titleComponent, progress, color, overlay);
                player.showBossBar(bossBar);
                this.bossBarsByUserId.put(userProfile.getUniqueId(), bossBar);
            }
        }
    }

    public void clearBossBar(UserProfileData userProfile) {
        Player player = this.plugin.findPlayer(userProfile);
        BossBar bossBar = this.bossBarsByUserId.remove(userProfile.getUniqueId());
        if (player == null) {
            return;
        }
        ProtocolVersion protocolVersion = player.getProtocolVersion();
        int protocolVersionId = protocolVersion.getProtocol();
        if (protocolVersionId >= 107 && bossBar != null) {
            player.hideBossBar(bossBar);
        }
    }

    public void sendRecoveryEmail(UserProfileData userProfile, String subject, String htmlTemplate, Map<String, String> replacements) {
        if (!userProfile.hasEmailAddress()) {
            return;
        }
        String senderEmail = this.config.getString("mailUser");
        String senderPassword = this.config.getString("mailPassword");
        String smtpHost = this.config.getString("mailHost");
        String senderName = this.config.getString("mailName");
        int smtpPort = this.config.getInt("mailPort");
        boolean useTls = this.config.getBoolean("mailUseTLS");
        for (Map.Entry<String, String> replacement : replacements.entrySet()) {
            htmlTemplate = htmlTemplate.replace(replacement.getKey(), replacement.getValue());
        }
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", smtpHost);
        properties.setProperty("mail.smtp.port", String.valueOf(smtpPort));
        if (!senderPassword.isEmpty()) {
            properties.setProperty("mail.smtp.auth", "true");
        }
        if (smtpPort == 465) {
            properties.setProperty("mail.smtp.ssl.enable", "true");
        } else {
            properties.setProperty("mail.smtp.starttls.enable", String.valueOf(useTls));
        }
        try {
            Session mailSession = Session.getInstance(properties, null);
            mailSession.setProvider(SMTP_PROVIDER);
            MimeMessage mimeMessage = new MimeMessage(mailSession);
            InternetAddress senderAddress = new InternetAddress(senderEmail, senderName);
            InternetAddress recipientAddress = new InternetAddress(userProfile.getEmailAddress());
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            ((MailcapCommandMap)CommandMap.getDefaultCommandMap()).addMailcap("text/html;; x-java-content-handler=com.community.jpremium.library.com.sun.mail.handlers.text_html");
            mimeMessage.setFrom(senderAddress);
            mimeMessage.addRecipient(Message.RecipientType.TO, recipientAddress);
            mimeMessage.setSubject(subject, "UTF-8");
            mimeMessage.setContent(htmlTemplate, "text/html; charset=UTF-8");
            if (senderPassword.isEmpty()) {
                Transport.send(mimeMessage);
            } else {
                Transport.send(mimeMessage, senderEmail, senderPassword);
            }
        }
        catch (Exception mailException) {
            mailException.printStackTrace();
        }
    }

    private void tickAuthorizationTimers() {
        float maxAuthorizationMillis = this.config.getInt("maximumAuthorisationTime") * 1000.0f;
        for (UserProfileData userProfile : this.plugin.getOnlineUserRegistry().getOnlineProfiles()) {
            long loginDeadlineMillis;
            if (userProfile.isLogged() || (loginDeadlineMillis = userProfile.getLoginDeadlineMillis()) <= 0L) continue;
            long remainingMillis = loginDeadlineMillis - System.currentTimeMillis();
            float progress = remainingMillis / maxAuthorizationMillis;
            long remainingSeconds = remainingMillis / 1000L;
            String timeoutMessageKey = userProfile.isRegistered() ? "loginErrorTimeElapsed" : "registerErrorTimeElapsed";
            String bossBarMessageKey = userProfile.isRegistered() ? "loginBossBarTimer" : "registerBossBarTimer";
            String actionBarMessageKey = userProfile.isRegistered() ? "loginActionBarTimer" : "registerActionBarTimer";
            String[] placeholders = new String[]{"%time%", String.valueOf(remainingSeconds), "%captcha_code%", userProfile.getCaptchaCode()};
            if (remainingMillis <= 0L) {
                this.plugin.fireEventAsync(new UserEvent.FailedLogin(userProfile, null, UserEvent.FailedLogin.Reason.TIMEOUT));
                this.disconnectUserWithMessage(userProfile, timeoutMessageKey);
                continue;
            }
            this.sendBossBarTimerToUser(userProfile, bossBarMessageKey, progress, placeholders);
            this.sendActionBarToUser(userProfile, actionBarMessageKey, placeholders);
        }
    }
}
