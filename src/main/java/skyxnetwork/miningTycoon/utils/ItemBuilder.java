package skyxnetwork.miningTycoon.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
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
     * Set skull texture from base64 string
     * Only works if the item is a PLAYER_HEAD
     */
    public ItemBuilder setSkullTexture(String base64Texture) {
        if (meta instanceof SkullMeta skullMeta) {
            try {
                // Create a unique profile for this skull
                UUID uuid = UUID.nameUUIDFromBytes(base64Texture.getBytes());
                PlayerProfile profile = Bukkit.createProfile(uuid, "CustomHead");

                // Set the texture property
                ProfileProperty property = new ProfileProperty("textures", base64Texture);
                profile.setProperty(property);

                // Apply the profile to the skull
                skullMeta.setPlayerProfile(profile);

            } catch (Exception e) {
                Bukkit.getLogger().warning("Failed to set skull texture: " + e.getMessage());
            }
        }
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}