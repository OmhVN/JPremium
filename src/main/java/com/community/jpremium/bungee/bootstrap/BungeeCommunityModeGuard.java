package com.community.jpremium.bungee.bootstrap;

import com.community.jpremium.bungee.JPremium;
import java.util.logging.Logger;
import net.md_5.bungee.api.ProxyServer;

public class BungeeCommunityModeGuard {
    private final Logger logger;
    private final ProxyServer proxyServer;

    public BungeeCommunityModeGuard(JPremium jPremium) {
        this.logger = jPremium.getLogger();
        this.proxyServer = jPremium.getProxy();
    }

    public boolean checkCommunityMode() {
        this.logger.info("Community edition mode enabled: remote license checks are disabled.");
        return this.proxyServer != null;
    }
}
