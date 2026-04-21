package skyxnetwork.miningTycoon.managers;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.utils.ColorUtil;
import skyxnetwork.miningTycoon.utils.ItemBuilder;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ItemManager {

    private final MiningTycoon plugin;
    // Changed to LinkedHashMap to preserve insertion order
    private final Map<String, ItemStack> pickaxes = new LinkedHashMap<>();
    private final Map<String, ItemStack> armors = new LinkedHashMap<>();
    private final Map<String, ItemStack> pets = new LinkedHashMap<>();

    private FileConfiguration pickaxesConfig;
    private FileConfiguration armorConfig;
    private FileConfiguration petsConfig;

    public ItemManager(MiningTycoon plugin) {
        this.plugin = plugin;
        loadAllItems();
    }

    public void loadAllItems() {
        pickaxes.clear();
        armors.clear();
        pets.clear();

        loadPickaxes();
        loadArmor();
        loadPets();

        plugin.getLogger().info("Loaded " + pickaxes.size() + " pickaxes, " +
                armors.size() + " armor pieces, and " +
                pets.size() + " pets");
    }

    private void loadPickaxes() {
        File pickaxesFile = new File(plugin.getDataFolder(), "items/pickaxes.yml");
        if (!pickaxesFile.exists()) {
            plugin.saveResource("items/pickaxes.yml", false);
        }

        pickaxesConfig = YamlConfiguration.loadConfiguration(pickaxesFile);

        for (String key : pickaxesConfig.getKeys(false)) {
            ConfigurationSection section = pickaxesConfig.getConfigurationSection(key);
            if (section == null) continue;

            try {
                Material material = Material.valueOf(section.getString("material", "DIAMOND_PICKAXE"));
                int customModelData = section.getInt("customModelData", 0);
                String name = ColorUtil.translate(section.getString("name", "&7Pickaxe"));
                List<String> lore = ColorUtil.translate(section.getStringList("lore"));
                int efficiency = section.getInt("efficiency", 0);

                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();

                meta.setDisplayName(name);
                meta.setLore(lore);
                meta.setCustomModelData(customModelData);
                meta.setUnbreakable(true);

                if (efficiency > 0) {
                    meta.addEnchant(Enchantment.EFFICIENCY, efficiency, true);
                }
                meta.addEnchant(Enchantment.UNBREAKING, 255, true);

                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);

                item.setItemMeta(meta);
                pickaxes.put(key, item);

            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load pickaxe: " + key + " - " + e.getMessage());
            }
        }
    }

    private void loadArmor() {
        File armorFile = new File(plugin.getDataFolder(), "items/armor.yml");
        if (!armorFile.exists()) {
            plugin.saveResource("items/armor.yml", false);
        }

        armorConfig = YamlConfiguration.loadConfiguration(armorFile);

        for (String key : armorConfig.getKeys(false)) {
            ConfigurationSection section = armorConfig.getConfigurationSection(key);
            if (section == null) continue;

            try {
                Material material = Material.valueOf(section.getString("material", "LEATHER_CHESTPLATE"));
                int customModelData = section.getInt("customModelData", 0);
                String name = ColorUtil.translate(section.getString("name", "&7Armor"));
                List<String> lore = ColorUtil.translate(section.getStringList("lore"));
                String colorString = section.getString("color", "255,255,255");

                ItemStack item = new ItemStack(material);
                LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();

                meta.setDisplayName(name);
                meta.setLore(lore);
                meta.setCustomModelData(customModelData);
                meta.setUnbreakable(true);

                // Parse color
                String[] rgb = colorString.split(",");
                if (rgb.length == 3) {
                    Color color = Color.fromRGB(
                            Integer.parseInt(rgb[0].trim()),
                            Integer.parseInt(rgb[1].trim()),
                            Integer.parseInt(rgb[2].trim())
                    );
                    meta.setColor(color);
                }

                meta.addEnchant(Enchantment.UNBREAKING, 255, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_DYE);

                item.setItemMeta(meta);
                armors.put(key, item);

            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load armor: " + key + " - " + e.getMessage());
            }
        }
    }

    private void loadPets() {
        File petsFile = new File(plugin.getDataFolder(), "items/pets.yml");
        if (!petsFile.exists()) {
            plugin.saveResource("items/pets.yml", false);
        }

        petsConfig = YamlConfiguration.loadConfiguration(petsFile);

        for (String key : petsConfig.getKeys(false)) {
            ConfigurationSection section = petsConfig.getConfigurationSection(key);
            if (section == null) continue;

            try {
                String name = ColorUtil.translate(section.getString("name", "&7Pet"));
                List<String> lore = ColorUtil.translate(section.getStringList("lore"));
                String texture = section.getString("texture", "");

                ItemBuilder builder = new ItemBuilder(Material.PLAYER_HEAD)
                        .setName(name)
                        .setLore(lore);

                if (!texture.isEmpty()) {
                    builder.setSkullTexture(texture);
                }

                ItemStack item = builder.build();
                pets.put(key, item);

            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load pet: " + key);
                e.printStackTrace();
            }
        }
    }

    // Getters
    public ItemStack getPickaxe(String id) {
        ItemStack item = pickaxes.get(id);
        return item != null ? item.clone() : null;
    }

    public ItemStack getArmor(String id) {
        ItemStack item = armors.get(id);
        return item != null ? item.clone() : null;
    }

    public ItemStack getPet(String id) {
        ItemStack item = pets.get(id);
        return item != null ? item.clone() : null;
    }

    public Set<String> getAllPickaxeIds() {
        // LinkedHashSet preserves order
        return new LinkedHashSet<>(pickaxes.keySet());
    }

    public Set<String> getAllArmorIds() {
        // LinkedHashSet preserves order
        return new LinkedHashSet<>(armors.keySet());
    }

    public Set<String> getAllPetIds() {
        // LinkedHashSet preserves order
        return new LinkedHashSet<>(pets.keySet());
    }

    // Get bonus values from config
    public double getPickaxeExpBonus(String id) {
        return pickaxesConfig.getDouble(id + ".expBonus", 0);
    }

    public double getPickaxeMoneyBonus(String id) {
        return pickaxesConfig.getDouble(id + ".moneyBonus", 0);
    }

    public int getPickaxeLuckyMinerLevel(String id) {
        return pickaxesConfig.getInt(id + ".luckyMiner", 0);
    }

    public int getPickaxeHasteLevel(String id) {
        return pickaxesConfig.getInt(id + ".haste", 0);
    }

    public boolean canHaveCooldownReduction(String id) {
        return pickaxesConfig.getBoolean(id + ".tempoEnabled", false);
    }

    public boolean canHaveTempo(String id) {
        return pickaxesConfig.getBoolean(id + ".tempoEnabled", false);
    }

    public int getPickaxeTempoLevelFromConfig(String id) {
        return pickaxesConfig.getInt(id + ".enchants.tempo", 0);
    }

    public boolean canApplyTempoEnchant(String pickaxeId) {
        return pickaxesConfig.getBoolean(pickaxeId + ".tempoEnabled", false);
    }

    public boolean applyTempoEnchant(ItemStack item, int level) {
        if (item == null || level < 1 || level > 5) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        String enchantLore = getTempoLore(level);
        lore.add(enchantLore);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return true;
    }

    public static String getTempoLore(int level) {
        StringBuilder sb = new StringBuilder();
        sb.append("§5Tempo ");
        for (int i = 0; i < level; i++) {
            sb.append("I");
            if (i < level - 1) sb.append(" ");
        }
        sb.append("\n§7Reduces §6Community Generator §7cooldown by §a");
        sb.append((int)(getCooldownReductionPercentStatic(level) * 100));
        sb.append("%%");
        return sb.toString();
    }

    private static double getCooldownReductionPercentStatic(int level) {
        switch (level) {
            case 1: return 0.15;
            case 2: return 0.30;
            case 3: return 0.50;
            case 4: return 0.75;
            case 5: return 1.00;
            default: return 0.0;
        }
    }

    public static int parseTempoLevelFromLore(String lore) {
        if (lore == null || !lore.contains("§5Tempo")) {
            return 0;
        }
        if (lore.contains("V")) return 5;
        if (lore.contains("IV")) return 4;
        if (lore.contains("III")) return 3;
        if (lore.contains("II")) return 2;
        if (lore.contains(" I")) return 1;
        return 0;
    }

    public int getPickaxeCooldownReductionLevel(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return 0;
        }
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) {
            return 0;
        }
        for (String line : lore) {
            if (line.contains("§5Tempo")) {
                if (line.contains("V")) return 5;
                if (line.contains("IV")) return 4;
                if (line.contains("III")) return 3;
                if (line.contains("II")) return 2;
                if (line.contains(" I")) return 1;
            }
        }
return 0;
    }

    public double getCooldownReductionFromPickaxe(ItemStack pickaxe) {
        int level = getPickaxeCooldownReductionLevel(pickaxe);
        return getCooldownReductionPercent(level);
    }

    public double getCooldownReductionPercent(int level) {
        switch (level) {
            case 1: return 0.15;
            case 2: return 0.30;
            case 3: return 0.50;
            case 4: return 0.75;
            case 5: return 1.00;
            default: return 0.0;
        }
    }

    public double getArmorExpBonus(String id) {
        return armorConfig.getDouble(id + ".expBonus", 0);
    }

    public double getArmorMoneyBonus(String id) {
        return armorConfig.getDouble(id + ".moneyBonus", 0);
    }

    public double getPetExpBonus(String id) {
        return petsConfig.getDouble(id + ".expBonus", 0);
    }

    public double getPetMoneyBonus(String id) {
        return petsConfig.getDouble(id + ".moneyBonus", 0);
    }

    // Find item ID from ItemStack
    public String getPickaxeId(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasCustomModelData()) {
            return null;
        }

        int cmd = item.getItemMeta().getCustomModelData();
        for (String id : pickaxes.keySet()) {
            if (pickaxesConfig.getInt(id + ".customModelData") == cmd) {
                return id;
            }
        }
        return null;
    }

    public String getArmorId(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasCustomModelData()) {
            return null;
        }

        int cmd = item.getItemMeta().getCustomModelData();
        for (String id : armors.keySet()) {
            if (armorConfig.getInt(id + ".customModelData") == cmd) {
                return id;
            }
        }
        return null;
    }

    public String getPetId(ItemStack item) {
        if (item == null || item.getType() != Material.PLAYER_HEAD || !item.hasItemMeta()) {
            return null;
        }

        String name = item.getItemMeta().getDisplayName();
        for (String id : pets.keySet()) {
            String configName = ColorUtil.translate(petsConfig.getString(id + ".name", ""));
            if (configName.equals(name)) {
                return id;
            }
        }
        return null;
    }

    // Save methods for editing items via GUI
    public void savePickaxe(String id, ConfigurationSection data) throws IOException {
        pickaxesConfig.set(id, data);
        File pickaxesFile = new File(plugin.getDataFolder(), "items/pickaxes.yml");
        pickaxesConfig.save(pickaxesFile);
        loadPickaxes();
    }

    public void saveArmor(String id, ConfigurationSection data) throws IOException {
        armorConfig.set(id, data);
        File armorFile = new File(plugin.getDataFolder(), "items/armor.yml");
        armorConfig.save(armorFile);
        loadArmor();
    }

    public void savePet(String id, ConfigurationSection data) throws IOException {
        petsConfig.set(id, data);
        File petsFile = new File(plugin.getDataFolder(), "items/pets.yml");
        petsConfig.save(petsFile);
        loadPets();
    }

    public void deletePickaxe(String id) throws IOException {
        pickaxesConfig.set(id, null);
        File pickaxesFile = new File(plugin.getDataFolder(), "items/pickaxes.yml");
        pickaxesConfig.save(pickaxesFile);
        loadPickaxes();
    }

    public void deleteArmor(String id) throws IOException {
        armorConfig.set(id, null);
        File armorFile = new File(plugin.getDataFolder(), "items/armor.yml");
        armorConfig.save(armorFile);
        loadArmor();
    }

    public void deletePet(String id) throws IOException {
        petsConfig.set(id, null);
        File petsFile = new File(plugin.getDataFolder(), "items/pets.yml");
        petsConfig.save(petsFile);
        loadPets();
    }

    public FileConfiguration getPickaxesConfig() {
        return pickaxesConfig;
    }

    public FileConfiguration getArmorConfig() {
        return armorConfig;
    }

    public FileConfiguration getPetsConfig() {
        return petsConfig;
    }
}