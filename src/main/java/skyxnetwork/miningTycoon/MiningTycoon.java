package skyxnetwork.miningTycoon;

import com.comphenix.protocol.ProtocolManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import skyxnetwork.miningTycoon.commands.*;
import skyxnetwork.miningTycoon.data.DataStorage;
import skyxnetwork.miningTycoon.gui.AdminGUINew;
import skyxnetwork.miningTycoon.gui.PrestigePortalGUI;
import skyxnetwork.miningTycoon.listeners.*;
import skyxnetwork.miningTycoon.managers.*;
import skyxnetwork.miningTycoon.placeholders.MiningTycoonPlaceholders;
import skyxnetwork.miningTycoon.tasks.AFKRewardTask;
import skyxnetwork.miningTycoon.tasks.AFKCheckTask;
import skyxnetwork.miningTycoon.tasks.LevelCheckTask;
import skyxnetwork.miningTycoon.tasks.RegionTimeTask;
import skyxnetwork.miningTycoon.config.CommunityGeneratorConfig;
import skyxnetwork.miningTycoon.utils.ConfigUtil;

public final class MiningTycoon extends JavaPlugin {

    private static MiningTycoon instance;

    // Managers
    private PlayerDataManager playerDataManager;
    private BoostManager boostManager;
    private AFKManager afkManager;
    private PrestigeManager prestigeManager;
    private PrestigePortalManager prestigePortalManager;
    private ZoneManager zoneManager;
    private MineManager mineManager;
    private WorldGuardManager worldGuardManager;
    private DataStorage dataStorage;
    private ItemManager itemManager;
    private EconomyManager economyManager;
    private PermissionCommand permissionCommand;
    private AreaGateManager areaGateManager;
    private CommunityGeneratorConfig communityGeneratorConfig;

    // GUI
    private PrestigePortalGUI prestigePortalGUI;

    // Tab Completer
    private MiningTycoonTabCompleter tabCompleter;

    // ProtocolLib
    private ProtocolManager protocolManager;

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }

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

        // ✅ Fix /lobby : enregistrement du canal plugin messaging pour Velocity/BungeeCord
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // Initialize managers
        dataStorage = new DataStorage(this);
        playerDataManager = new PlayerDataManager(this);
        economyManager = new EconomyManager(this);
        boostManager = new BoostManager(this);
        afkManager = new AFKManager(this);
        worldGuardManager = new WorldGuardManager(this);
        prestigeManager = new PrestigeManager(this);
        prestigePortalManager = new PrestigePortalManager(this);
        zoneManager = new ZoneManager(this);
        mineManager = new MineManager(this);
        itemManager = new ItemManager(this);
        areaGateManager = new AreaGateManager(this);
        communityGeneratorConfig = new CommunityGeneratorConfig(this);

        // Initialize ProtocolLib
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            protocolManager = com.comphenix.protocol.ProtocolLibrary.getProtocolManager();
            getLogger().info("ProtocolLib detected! VeinMiner visual effects enabled.");
        } else {
            getLogger().warning("ProtocolLib not found! VeinMiner will work without visual effects.");
        }

        // Initialize GUI
        prestigePortalGUI = new PrestigePortalGUI(this);

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

        // ✅ Fix /lobby : désenregistrement propre du canal au shutdown
        getServer().getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");

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
        pm.registerEvents(new AFKActivityListener(this), this);
        pm.registerEvents(new PrestigePortalListener(this), this);
        pm.registerEvents(prestigePortalGUI, this);
        pm.registerEvents(new BlockPlaceListener(this), this);
        pm.registerEvents(new AdminGUINew(this), this);
        pm.registerEvents(new PortalListener(this), this); // END PORTAL LISTENER DONT REMOVE

        // Community Generator
        pm.registerEvents(new CommunityGeneratorListener(this, communityGeneratorConfig), this);

        // Boost Items
        pm.registerEvents(new BoostItemListener(this), this);

        getLogger().info("Registered all event listeners");
    }

    private void registerCommands() {
        // Initialize tab completer
        tabCompleter = new MiningTycoonTabCompleter(this);

        // Player commands
        getCommand("level").setExecutor(new LevelCommand(this));
        getCommand("prestige").setExecutor(new PrestigeCommand(this));
        getCommand("afk").setExecutor(new AFKCommand(this));
        getCommand("afk").setTabCompleter(tabCompleter);
        getCommand("fasttp").setExecutor(new FastTeleportCommand(this));
        getCommand("fasttp").setTabCompleter(tabCompleter);
        getCommand("droptoggle").setExecutor(new DropToggleCommand(this));
        getCommand("levelsound").setExecutor(new LevelSoundCommand(this));
        getCommand("booststatus").setExecutor(new BoostStatusCommand(this));
        getCommand("mode").setExecutor(new ModeCommand(this));
        getCommand("index").setExecutor(new IndexCommand(this));
        getCommand("lobby").setExecutor(new LobbyCommand(this));
        getCommand("multiplier").setExecutor(new MultiplierCommand(this));

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
        getCommand("multiplieradmin").setExecutor(new MultiplierAdminCommand(this));
        getCommand("multiplieradmin").setTabCompleter(tabCompleter);
        getCommand("prestigeadmin").setExecutor(new PrestigeAdminCommand(this));
        getCommand("prestigeadmin").setTabCompleter(tabCompleter);
        getCommand("givemenu").setExecutor(new GiveMenuCommand(this));
        getCommand("menu").setExecutor(new MenuCommand(this));

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
        new LevelCheckTask(this).runTaskTimer(this, 20L, 20L);
        new RegionTimeTask(this).runTaskTimer(this, 100L, 100L);
        new AFKRewardTask(this).runTaskTimer(this, 1L, 1L);
        new AFKCheckTask(this).runTaskTimer(this, 20L, 20L);

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

    public AFKManager getAfkManager() {
        return afkManager;
    }

    public PrestigeManager getPrestigeManager() {
        return prestigeManager;
    }

    public PrestigePortalManager getPrestigePortalManager() {
        return prestigePortalManager;
    }

    public PrestigePortalGUI getPrestigePortalGUI() {
        return prestigePortalGUI;
    }

    public ZoneManager getZoneManager() {
        return zoneManager;
    }

    public MineManager getMineManager() {
        return mineManager;
    }

    public WorldGuardManager getWorldGuardManager() {
        return worldGuardManager;
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

    public AreaGateManager getAreaGateManager() {
        return areaGateManager;
    }

    public CommunityGeneratorConfig getCommunityGeneratorConfig() {
        return communityGeneratorConfig;
    }
}