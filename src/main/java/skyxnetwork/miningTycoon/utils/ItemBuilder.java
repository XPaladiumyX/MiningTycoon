package skyxnetwork.miningTycoon.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder setName(String name) {
        meta.setDisplayName(name);
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        meta.setLore(Arrays.asList(lore));
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        meta.setLore(lore);
        return this;
    }

    public ItemBuilder setCustomModelData(int data) {
        meta.setCustomModelData(data);
        return this;
    }

    public ItemBuilder addEnchant(Enchantment enchant, int level) {
        meta.addEnchant(enchant, level, true);
        return this;
    }

    public ItemBuilder setUnbreakable(boolean unbreakable) {
        meta.setUnbreakable(unbreakable);
        return this;
    }

    public ItemBuilder addItemFlags(ItemFlag... flags) {
        meta.addItemFlags(flags);
        return this;
    }

    /**
     * Set skull texture from base64 string or HeadDatabase ID
     * Only works if the item is a PLAYER_HEAD
     * <p>
     * Formats supported:
     * - Base64 texture: "eyJ0ZXh0dXJlcyI6..."
     * - HeadDatabase ID: "hdb-12345"
     */
    public ItemBuilder setSkullTexture(String textureOrId) {
        if (meta instanceof SkullMeta skullMeta) {
            try {
                // Check if it's a HeadDatabase ID
                if (textureOrId.toLowerCase().startsWith("hdb-")) {
                    String hdbId = textureOrId.substring(4); // Remove "hdb-" prefix

                    // Try to get skull from HeadDatabase
                    if (Bukkit.getPluginManager().getPlugin("HeadDatabase") != null) {
                        try {
                            HeadDatabaseAPI api = new HeadDatabaseAPI();
                            ItemStack hdbHead = api.getItemHead(hdbId);

                            if (hdbHead != null && hdbHead.getItemMeta() instanceof SkullMeta hdbMeta) {
                                // Copy the skull profile from HDB head to our item
                                skullMeta.setOwnerProfile(hdbMeta.getOwnerProfile());
                                Bukkit.getLogger().info("Successfully loaded HeadDatabase skull: " + hdbId);
                            } else {
                                Bukkit.getLogger().warning("HeadDatabase ID not found: " + hdbId);
                                setDefaultTexture(skullMeta);
                            }
                        } catch (Exception e) {
                            Bukkit.getLogger().severe("Failed to load HeadDatabase skull " + hdbId + ": " + e.getMessage());
                            setDefaultTexture(skullMeta);
                        }
                    } else {
                        Bukkit.getLogger().warning("HeadDatabase plugin not found! Cannot load skull: " + hdbId);
                        setDefaultTexture(skullMeta);
                    }
                } else {
                    // It's a base64 texture
                    PlayerProfile profile = Bukkit.createProfileExact(UUID.randomUUID(), null);
                    ProfileProperty property = new ProfileProperty("textures", textureOrId);
                    profile.getProperties().add(property);
                    skullMeta.setOwnerProfile(profile);
                }
            } catch (Exception e) {
                Bukkit.getLogger().severe("Failed to set skull texture: " + e.getMessage());
                e.printStackTrace();
                setDefaultTexture(skullMeta);
            }
        }
        return this;
    }

    /**
     * Set a default texture for skulls that fail to load
     */
    private void setDefaultTexture(SkullMeta skullMeta) {
        try {
            // Default to a question mark texture
            String defaultTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmY3ZTUyMDk4ZjAyN2Q2Y2VkOWNiMmNiMTljOGIzMGY3OTg3MzEzNmI1MmRkYWVlMWZlMjRjNGQzZTI2YzYzYyJ9fX0=";
            PlayerProfile profile = Bukkit.createProfileExact(UUID.randomUUID(), null);
            ProfileProperty property = new ProfileProperty("textures", defaultTexture);
            profile.getProperties().add(property);
            skullMeta.setOwnerProfile(profile);
        } catch (Exception ignored) {
        }
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}