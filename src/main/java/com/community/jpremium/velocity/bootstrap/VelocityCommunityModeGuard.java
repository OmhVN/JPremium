package com.community.jpremium.velocity.bootstrap;

import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.logging.Logger;

public class VelocityCommunityModeGuard {
    private final Logger logger;
    private final ProxyServer proxyServer;

    public VelocityCommunityModeGuard(JPremiumVelocity jPremiumVelocity) {
        this.logger = jPremiumVelocity.getLogger();
        this.proxyServer = jPremiumVelocity.getProxyServer();
    }

    public boolean checkCommunityMode() {
        this.logger.info("Community edition mode enabled: remote license checks are disabled.");
        return this.proxyServer != null;
    }
}
