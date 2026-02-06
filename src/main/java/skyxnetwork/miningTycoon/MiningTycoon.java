package skyxnetwork.miningTycoon;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import skyxnetwork.miningTycoon.commands.*;
import skyxnetwork.miningTycoon.data.DataStorage;
import skyxnetwork.miningTycoon.gui.AdminGUINew;
import skyxnetwork.miningTycoon.listeners.*;
import skyxnetwork.miningTycoon.managers.*;
import skyxnetwork.miningTycoon.placeholders.MiningTycoonPlaceholders;
import skyxnetwork.miningTycoon.tasks.AFKRewardTask;
import skyxnetwork.miningTycoon.tasks.LevelCheckTask;
import skyxnetwork.miningTycoon.tasks.NightVisionTask;
import skyxnetwork.miningTycoon.utils.ConfigUtil;

public final class MiningTycoon extends JavaPlugin {

    private static MiningTycoon instance;

    // Managers
    private PlayerDataManager playerDataManager;
    private BoostManager boostManager;
    private PrestigeManager prestigeManager;
    private ZoneManager zoneManager;
    private DataStorage dataStorage;
    private ItemManager itemManager;
    private EconomyManager economyManager;
    private PermissionCommand permissionCommand;

    // Tab Completer
    private MiningTycoonTabCompleter tabCompleter;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("====================================");
        getLogger().info("  MiningTycoon v" + getDescription().getVersion());
        getLogger().info("  By XPaladiumyX");
        getLogger().info("  Starting plugin...");
        getLogger().info("====================================");

        // Load configurations
        saveDefaultConfig();
        ConfigUtil.loadConfigurations(this);

        // Create items directory structure
        createItemsDirectory();

        // Initialize managers
        dataStorage = new DataStorage(this);
        playerDataManager = new PlayerDataManager(this);
        economyManager = new EconomyManager(this); // Initialize economy first
        boostManager = new BoostManager(this);
        prestigeManager = new PrestigeManager(this);
        zoneManager = new ZoneManager(this);
        itemManager = new ItemManager(this);

        // Register listeners
        registerListeners();

        // Register commands
        registerCommands();

        // Start tasks
        startTasks();

        // Register PlaceholderAPI expansion
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MiningTycoonPlaceholders(this).register();
            getLogger().info("PlaceholderAPI expansion registered!");
        } else {
            getLogger().warning("PlaceholderAPI not found! Placeholders will not work.");
        }

        getLogger().info("MiningTycoon enabled successfully!");
        getLogger().info("Loaded " + itemManager.getAllPickaxeIds().size() + " pickaxes, " +
                itemManager.getAllArmorIds().size() + " armor pieces, and " +
                itemManager.getAllPetIds().size() + " pets");
        getLogger().info("Economy system: " + economyManager.getEconomyType());
    }

    @Override
    public void onDisable() {
        // Save all data
        if (dataStorage != null) {
            dataStorage.saveAllData();
        }

        // Stop all tasks
        Bukkit.getScheduler().cancelTasks(this);

        getLogger().info("MiningTycoon disabled successfully!");
    }

    private void createItemsDirectory() {
        java.io.File itemsDir = new java.io.File(getDataFolder(), "items");
        if (!itemsDir.exists()) {
            itemsDir.mkdirs();
            getLogger().info("Created items directory");
        }
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new BlockBreakListener(this), this);
        pm.registerEvents(new PlayerJoinListener(this), this);
        pm.registerEvents(new PlayerMoveListener(this), this);
        pm.registerEvents(new InventoryClickListener(this), this);
        pm.registerEvents(new DropListener(this), this);
        pm.registerEvents(new AFKListener(this), this);
        pm.registerEvents(new PortalListener(this), this);
        pm.registerEvents(new BlockPlaceListener(this), this);
        pm.registerEvents(new AdminGUINew(this), this);

        getLogger().info("Registered all event listeners");
    }

    private void registerCommands() {
        // Initialize tab completer
        tabCompleter = new MiningTycoonTabCompleter(this);

        // Player commands
        getCommand("level").setExecutor(new LevelCommand(this));
        getCommand("prestige").setExecutor(new PrestigeCommand(this));
        getCommand("afk").setExecutor(new AFKCommand(this));
        getCommand("fasttp").setExecutor(new FastTeleportCommand(this));
        getCommand("fasttp").setTabCompleter(tabCompleter);
        getCommand("droptoggle").setExecutor(new DropToggleCommand(this));
        getCommand("booststatus").setExecutor(new BoostStatusCommand(this));
        getCommand("mode").setExecutor(new ModeCommand(this));
        getCommand("index").setExecutor(new IndexCommand(this));
        getCommand("lobby").setExecutor(new LobbyCommand(this));

        // Admin commands
        getCommand("admin").setExecutor(new AdminCommand(this));
        getCommand("giveitem").setExecutor(new GiveItemCommand(this));
        getCommand("giveitem").setTabCompleter(tabCompleter);
        getCommand("givearmor").setExecutor(new GiveArmorCommand(this));
        getCommand("givearmor").setTabCompleter(tabCompleter);
        getCommand("givepet").setExecutor(new GivePetCommand(this));
        getCommand("givepet").setTabCompleter(tabCompleter);
        getCommand("leveladmin").setExecutor(new LevelAdminCommand(this));
        getCommand("leveladmin").setTabCompleter(tabCompleter);
        getCommand("prestigeadmin").setExecutor(new PrestigeAdminCommand(this));
        getCommand("prestigeadmin").setTabCompleter(tabCompleter);

        // Permission management
        permissionCommand = new PermissionCommand(this);
        getCommand("permconfig").setExecutor(permissionCommand);
        getCommand("permconfig").setTabCompleter(tabCompleter);

        // Main plugin command
        getCommand("miningtycoon").setExecutor(new MiningTycoonCommand(this));
        getCommand("miningtycoon").setTabCompleter(tabCompleter);

        getLogger().info("Registered all commands with tab completion");
    }

    private void startTasks() {
        // Level check task - every second
        new LevelCheckTask(this).runTaskTimer(this, 20L, 20L);

        // Night vision task - every 5 seconds
        new NightVisionTask(this).runTaskTimer(this, 100L, 100L);

        // AFK reward task - every tick
        new AFKRewardTask(this).runTaskTimer(this, 1L, 1L);

        getLogger().info("Started all scheduled tasks");
    }

    // Getters for managers
    public static MiningTycoon getInstance() {
        return instance;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public BoostManager getBoostManager() {
        return boostManager;
    }

    public PrestigeManager getPrestigeManager() {
        return prestigeManager;
    }

    public ZoneManager getZoneManager() {
        return zoneManager;
    }

    public DataStorage getDataStorage() {
        return dataStorage;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public PermissionCommand getPermissionCommand() {
        return permissionCommand;
    }
}