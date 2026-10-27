package io.github.lino9999.pluginshider.config;

import io.github.lino9999.pluginshider.util.ColorUtil;
import org.bukkit.configuration.ConfigurationSection;

public final class BrandSettings {
    private final boolean enabled;
    private final String brand;
    private final String rawBrand;

    public BrandSettings(ConfigurationSection section) {
        if (section == null) {
            this.enabled = false;
            this.brand = "";
            this.rawBrand = "";
            return;
        }
        this.enabled = section.getBoolean("enabled", true);
        this.rawBrand = section.getString("server-brand", "&8[&bPlugins&3Hider&8]");
        this.brand = ColorUtil.colorize(this.rawBrand);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getBrand() {
        return brand;
    }

    public String getRawBrand() {
        return rawBrand;
    }
}
