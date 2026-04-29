package com.community.jpremium.proxy.api;

public class JPremiumApi {
    private static App app;

    public static App getApp() {
        if (app == null) {
            throw new IllegalStateException("App is not initialized yet!");
        }
        return app;
    }

    public static synchronized void setApp(App appInstance) {
        if (JPremiumApi.app != null) {
            throw new IllegalStateException("App is already initialized!");
        }
        JPremiumApi.app = appInstance;
    }
}
