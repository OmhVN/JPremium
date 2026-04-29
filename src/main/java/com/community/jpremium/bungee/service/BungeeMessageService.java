package com.community.jpremium.bungee.service;

import com.community.jpremium.common.config.BungeeConfigService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import jakarta.activation.CommandMap;
import jakarta.activation.MailcapCommandMap;
import jakarta.mail.Message;
import jakarta.mail.Provider;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.BossBar;

public class BungeeMessageService {
    private final JPremium plugin;
    private final ProxyServer proxyServer;
    private final BungeeConfigService config;
    private final BungeeConfigService messages;
    private static final Provider SMTP_PROVIDER = new Provider(Provider.Type.TRANSPORT, "smtp", "com.community.jpremium.library.com.sun.mail.smtp.SMTPTransport", "Oracle", "2.0.1");

    public BungeeMessageService(JPremium jPremium) {
        this.plugin = jPremium;
        this.proxyServer = jPremium.getProxy();
        this.config = jPremium.getConfig();
        this.messages = jPremium.getMessagesConfig();
        jPremium.scheduleRepeatingTask(this::tickAuthorizationTimers, 1L, TimeUnit.SECONDS);
    }

    public BaseComponent buildComponentMessage(String messageKey, String nickname, String ... arguments) {
        String messageTemplate = this.messages.getString(messageKey);
        if (messageTemplate == null) {
            return TextComponent.fromLegacy("<undefined path: " + messageKey + ">");
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
        messageTemplate = ChatColor.translateAlternateColorCodes('&', messageTemplate);
        return TextComponent.fromLegacy(messageTemplate);
    }

    public void sendMessage(CommandSender commandSender, String messageKey) {
        BaseComponent messageComponent;
        if (commandSender != null && (messageComponent = this.buildComponentMessage(messageKey, "")) != null) {
            commandSender.sendMessage(messageComponent);
        }
    }

    public void sendMessageWithNickname(CommandSender commandSender, String nickname, String messageKey, String ... arguments) {
        BaseComponent messageComponent;
        if (commandSender != null && (messageComponent = this.buildComponentMessage(messageKey, nickname, arguments)) != null) {
            commandSender.sendMessage(messageComponent);
        }
    }

    public void sendMessageForUser(CommandSender commandSender, UserProfileData userProfile, String messageKey, String ... arguments) {
        String nickname;
        BaseComponent messageComponent;
        if (commandSender != null && (messageComponent = this.buildComponentMessage(messageKey, nickname = userProfile.getLastNickname(), arguments)) != null) {
            commandSender.sendMessage(messageComponent);
        }
    }

    public void sendMessageToUser(UserProfileData userProfile, String messageKey, String ... arguments) {
        String nickname;
        BaseComponent messageComponent;
        ProxiedPlayer player = this.plugin.findPlayer(userProfile);
        if (player != null && (messageComponent = this.buildComponentMessage(messageKey, nickname = player.getName(), arguments)) != null) {
            player.sendMessage(messageComponent);
        }
    }

    public void disconnectUserWithMessage(UserProfileData userProfile, String messageKey, String ... arguments) {
        ProxiedPlayer player = this.plugin.findPlayer(userProfile);
        if (player != null) {
            String nickname = player.getName();
            BaseComponent disconnectMessage = this.buildComponentMessage(messageKey, nickname, arguments);
            player.disconnect(disconnectMessage);
        }
    }

    public void sendActionBarToUser(UserProfileData userProfile, String messageKey, String ... arguments) {
        ProxiedPlayer player = this.plugin.findPlayer(userProfile);
        if (player != null) {
            String nickname = player.getName();
            PendingConnection pendingConnection = player.getPendingConnection();
            BaseComponent actionBarMessage = this.buildComponentMessage(messageKey, nickname, arguments);
            if (actionBarMessage != null && pendingConnection.getVersion() >= 47) {
                player.sendMessage(ChatMessageType.ACTION_BAR, actionBarMessage);
            }
        }
    }

    public void clearActionBar(UserProfileData userProfile) {
        PendingConnection pendingConnection;
        ProxiedPlayer player = this.plugin.findPlayer(userProfile);
        if (player != null && (pendingConnection = player.getPendingConnection()).getVersion() >= 47) {
            player.sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
        }
    }

    public void sendTitleBundleToUser(UserProfileData userProfile, String messageKeyPrefix, String ... arguments) {
        ProxiedPlayer player = this.plugin.findPlayer(userProfile);
        if (player == null) {
            return;
        }
        PendingConnection pendingConnection = player.getPendingConnection();
        String nickname = player.getName();
        BaseComponent titleComponent = this.buildComponentMessage(messageKeyPrefix.concat("Title"), nickname, arguments);
        BaseComponent subtitleComponent = this.buildComponentMessage(messageKeyPrefix.concat("SubTitle"), nickname, arguments);
        BaseComponent chatComponent = this.buildComponentMessage(messageKeyPrefix.concat("Chat"), nickname, arguments);
        int fadeInTicks = this.messages.getInt(messageKeyPrefix.concat("FadeIn"));
        int stayTicks = this.messages.getInt(messageKeyPrefix.concat("Stay"));
        int fadeOutTicks = this.messages.getInt(messageKeyPrefix.concat("FadeOut"));
        int protocolVersion = pendingConnection.getVersion();
        if (titleComponent != null && subtitleComponent != null && protocolVersion >= 47) {
            this.proxyServer.createTitle().title(titleComponent).subTitle(subtitleComponent).fadeIn(fadeInTicks).stay(stayTicks).fadeOut(fadeOutTicks).send(player);
        }
        if (chatComponent != null) {
            player.sendMessage(chatComponent);
        }
    }

    public void sendBossBarTimerToUser(UserProfileData userProfile, String messageKeyPrefix, float progress, String ... arguments) {
        ProxiedPlayer player = this.plugin.findPlayer(userProfile);
        if (player == null) {
            return;
        }
        String nickname = player.getName();
        PendingConnection pendingConnection = player.getPendingConnection();
        BaseComponent titleComponent = this.buildComponentMessage(messageKeyPrefix.concat("Title"), nickname, arguments);
        BossBar bossBar = new BossBar(userProfile.getUniqueId(), 0);
        int colorId = this.resolveBossBarColorId(this.messages.getString(messageKeyPrefix.concat("Color")));
        int divisionId = this.resolveBossBarDivisionId(this.messages.getString(messageKeyPrefix.concat("Division")));
        int protocolVersion = pendingConnection.getVersion();
        if (titleComponent != null && protocolVersion >= 107) {
            bossBar.setColor(colorId);
            bossBar.setDivision(divisionId);
            bossBar.setHealth(progress);
            bossBar.setFlags((byte)2);
            bossBar.setTitle(titleComponent);
            player.unsafe().sendPacket(bossBar);
        }
    }

    private int resolveBossBarColorId(String colorName) {
        if (colorName == null) {
            return 4;
        }
        return switch (colorName.toUpperCase(Locale.ROOT)) {
            case "PINK" -> 0;
            case "BLUE" -> 1;
            case "RED" -> 2;
            case "GREEN" -> 3;
            case "YELLOW" -> 4;
            case "PURPLE" -> 5;
            case "WHITE" -> 6;
            default -> 4;
        };
    }

    private int resolveBossBarDivisionId(String divisionName) {
        if (divisionName == null) {
            return 1;
        }
        return switch (divisionName.toUpperCase(Locale.ROOT)) {
            case "PROGRESS" -> 0;
            case "NOTCHED_6" -> 1;
            case "NOTCHED_10" -> 2;
            case "NOTCHED_12" -> 3;
            case "NOTCHED_20" -> 4;
            default -> 1;
        };
    }

    public void clearBossBar(UserProfileData userProfile) {
        ProxiedPlayer player = this.plugin.findPlayer(userProfile);
        if (player == null) {
            return;
        }
        PendingConnection pendingConnection = player.getPendingConnection();
        BossBar bossBar = new BossBar(userProfile.getUniqueId(), 1);
        int protocolVersion = pendingConnection.getVersion();
        if (protocolVersion >= 107) {
            player.unsafe().sendPacket(bossBar);
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
            ProxiedPlayer player = this.plugin.findPlayer(userProfile);
            if (player == null || !player.isConnected() || player.getServer() == null) {
                continue;
            }
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
