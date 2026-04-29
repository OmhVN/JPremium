package com.community.jpremium.backend.service;

import com.community.jpremium.backend.service.BackendStateRegistry;
import com.community.jpremium.backend.config.BackendConfigService;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapFont;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MinecraftFont;

public class CaptchaMapRenderer
extends MapRenderer {
    private static final short DEFAULT_MAP_ID = 0;
    private final BackendStateRegistry stateRegistry;
    private final BackendConfigService backendConfig;
    private final Material mapMaterial;
    private final ItemStack captchaItem;
    private final boolean legacyMapApi;

    public ItemStack getCaptchaItem() {
        return this.captchaItem;
    }

    public CaptchaMapRenderer(BackendStateRegistry stateRegistry, BackendConfigService backendConfig) {
        super(true);
        this.stateRegistry = stateRegistry;
        this.backendConfig = backendConfig;
        this.legacyMapApi = this.isLegacyMapApi();
        this.mapMaterial = this.resolveMapMaterial();
        this.captchaItem = this.createCaptchaItem();
    }

    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
        BackendStateRegistry.AuthState state = this.stateRegistry.find(player.getUniqueId()).orElseThrow(() -> new NullPointerException("A user object is not exist!"));
        if (!state.hasCaptchaCode() || state.isCaptchaRendered()) {
            return;
        }
        BufferedImage bufferedImage = this.backendConfig.getCaptchaImage();
        String captchaMessage = this.backendConfig.getCaptchaMessage().replace("%captcha_code%", state.getCaptchaCode());
        int imageX = (128 - bufferedImage.getWidth()) / 2;
        int imageY = (100 - bufferedImage.getHeight()) / 2 + 23;
        int textX = (128 - MinecraftFont.Font.getWidth(captchaMessage)) / 2;
        for (int x = 0; x < 128; ++x) {
            for (int y = 0; y < 128; ++y) {
                mapCanvas.setPixelColor(x, y, null);
            }
        }
        mapCanvas.drawText(textX, 8, MinecraftFont.Font, captchaMessage);
        mapCanvas.drawImage(imageX, imageY, bufferedImage);
        state.setCaptchaRendered(true);
    }

    public void registerMapRenderer() {
        MapView mapView;
        try {
            if (this.legacyMapApi) {
                mapView = (MapView)Bukkit.getServer().getClass().getMethod("getMap", Short.TYPE).invoke(Bukkit.getServer(), (short)0);
            } else {
                mapView = (MapView)Bukkit.getServer().getClass().getMethod("getMap", Integer.TYPE).invoke(Bukkit.getServer(), 0);
            }
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            throw new RuntimeException("Could not resolve server map by id", reflectiveOperationException);
        }
        if (mapView == null) {
            mapView = Bukkit.createMap(Bukkit.getWorlds().get(0));
        }
        for (MapRenderer mapRenderer : new ArrayList<>(mapView.getRenderers())) {
            mapView.removeRenderer(mapRenderer);
        }
        mapView.addRenderer(this);
    }

    private ItemStack createCaptchaItem() {
        ItemStack itemStack = new ItemStack(this.mapMaterial);
        MapMeta mapMeta = (MapMeta)itemStack.getItemMeta();
        try {
            if (this.legacyMapApi) {
                ItemStack.class.getMethod("setDurability", Short.TYPE).invoke(itemStack, (short)0);
            } else {
                MapMeta.class.getMethod("setMapId", Integer.TYPE).invoke(mapMeta, 0);
            }
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            throw new RuntimeException("Could not initialize map metadata", reflectiveOperationException);
        }
        itemStack.setItemMeta((ItemMeta)mapMeta);
        return itemStack;
    }

    private Material resolveMapMaterial() {
        return this.legacyMapApi ? Material.MAP : Material.FILLED_MAP;
    }

    private boolean isLegacyMapApi() {
        return Integer.parseInt(Bukkit.getServer().getBukkitVersion().split("\\.")[1].split("\\.")[0].split("-")[0]) < 13;
    }
}
